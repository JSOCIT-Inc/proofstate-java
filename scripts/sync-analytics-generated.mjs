import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

// Sync the isolated analytics client after `fern generate --local --api analytics`.
// Keep the original ProofStateClient tree untouched for source compatibility.
const repository = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const source = path.join(repository, "generated/analytics/src/main/java");
const destination = path.join(repository, "src/main/java/ai/proofstate/client/analytics");
const version = fs.readFileSync(path.join(repository, "pom.xml"), "utf8")
  .match(/<artifactId>proofstate-java<\/artifactId>\s*<version>([^<]+)<\/version>/)?.[1];

if (!version) throw new Error("Missing ProofState artifact version");
if (!fs.existsSync(source)) throw new Error(`Generate the analytics client first: ${source}`);
if (!destination.startsWith(path.join(repository, "src/main/java") + path.sep)) {
  throw new Error("Destination must stay inside this SDK source tree");
}

function filesUnder(directory) {
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap((entry) => {
    const entryPath = path.join(directory, entry.name);
    return entry.isDirectory() ? filesUnder(entryPath) : entry.isFile() ? [entryPath] : [];
  });
}

const generated = filesUnder(source).filter((file) => file.endsWith(".java"));
if (generated.length < 100) throw new Error("The analytics generation is unexpectedly small");
const relativeTo = (base, file) => path.relative(base, file).split(path.sep).join("/");
const expected = new Set(generated.map((file) => relativeTo(source, file)));
if (fs.existsSync(destination)) {
  const stale = filesUnder(destination)
    .filter((file) => file.endsWith(".java"))
    .map((file) => relativeTo(destination, file))
    .filter((file) => !expected.has(file));
  if (stale.length) {
    throw new Error(`Generation would remove existing analytics classes: ${stale.join(", ")}`);
  }
}

for (const file of generated) {
  const relative = relativeTo(source, file);
  let content = fs.readFileSync(file, "utf8");
  if (relative === "core/ClientOptions.java") {
    const fernHeader = 'this.headers.putAll(new HashMap<String,String>() {{put("X-Fern-Language", "JAVA");}});';
    if (!content.includes(fernHeader)) throw new Error("Generated identity header changed unexpectedly");
    content = content.replace(
      fernHeader,
      'this.headers.putIfAbsent("X-ProofState-Sdk-Name", "proofstate-java");\n'
      + `    this.headers.putIfAbsent("X-ProofState-Sdk-Version", "${version}");`,
    );
  }
  if (relative.endsWith("ProofStateAnalyticsClientBuilder.java")) {
    const unsetEnvironment = "private Environment environment;";
    if (!content.includes(unsetEnvironment)) throw new Error("Generated environment builder changed unexpectedly");
    content = content.replace(
      unsetEnvironment,
      'private Environment environment = Environment.custom("https://proofstate.ai");',
    );
  }
  // Fern currently emits trailing spaces in a few generated Javadocs.
  content = content.replace(/[ \t]+(?=\r?\n|$)/g, "");
  const target = path.join(destination, relative);
  fs.mkdirSync(path.dirname(target), { recursive: true });
  fs.writeFileSync(target, content);
}

console.log(`Synced ${generated.length} analytics Java files`);
