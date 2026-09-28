# proofstate-java

Java API client for [ProofState](https://proofstate.ai). The original MIT
license and attribution are retained in [LICENSE](LICENSE).

The `0.1.0-rc.1` candidate was checked against the live ProofState deployment
on 2026-09-28 for project authentication, prompts, datasets, models, score v3,
and nonempty experiment and experiment-item lists. Use a test project when
adopting a prerelease.

The client covers API operations for prompts, datasets, scores, models,
comments, and observations. Use the OpenTelemetry Java SDK separately to send
traces. This API client does not instrument an application or export spans.

The additive `ProofStateAnalyticsClient` covers score v3 reads, experiment
lists, and experiment-item lists. It lives in a separate Java package so every
method on the original `ProofStateClient` stays available.

## Build and install

The build was verified with Java 21 and the included Maven wrapper:

```bash
./mvnw clean install
```

On Windows PowerShell, run `.\mvnw.cmd clean install`.

```xml
<dependency>
    <groupId>ai.proofstate</groupId>
    <artifactId>proofstate-java</artifactId>
    <version>0.1.0-rc.1</version>
</dependency>
```

## Use the API client

```java
import ai.proofstate.client.ProofStateClient;
import ai.proofstate.client.resources.prompts.types.PromptMetaListResponse;

ProofStateClient client = ProofStateClient.builder()
    .credentials("pk-ps-...", "sk-ps-...")
    .build();

PromptMetaListResponse prompts = client.prompts().list();
```

The default host is `https://proofstate.ai`. Set `.url("https://your-host")`
for another ProofState deployment. `AsyncProofStateClient.builder()` provides
the asynchronous API. Credentials are sent with HTTP Basic authentication.
The client sends `X-ProofState-Sdk-Name: proofstate-java` and its version by
default. `X-ProofState-Public-Key` is available when explicitly configured.

## Read scores and experiments

```java
import ai.proofstate.client.analytics.ProofStateAnalyticsClient;
import ai.proofstate.client.analytics.resources.experiments.requests.ListExperimentItemsRequest;
import ai.proofstate.client.analytics.resources.experiments.requests.ListExperimentsRequest;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

ProofStateAnalyticsClient analytics = ProofStateAnalyticsClient.builder()
    .credentials("pk-ps-...", "sk-ps-...")
    .build();

analytics.scoresV3().getManyV3().getData();
OffsetDateTime from = OffsetDateTime.now(ZoneOffset.UTC).minusDays(30);
analytics.experiments().list(
    ListExperimentsRequest.builder().fromStartTime(from).build()).getData();
analytics.experiments().listItems(
    ListExperimentItemsRequest.builder().fromStartTime(from).build()).getData();
```

Use `.url("https://your-host")` for another ProofState deployment. The
`fromStartTime` query parameter is required for both experiment list methods.

## Send traces with OpenTelemetry

Configure the OpenTelemetry Java SDK's OTLP/HTTP exporter for
`https://proofstate.ai/api/public/otel`. Use the Basic authorization value
formed from `pk-ps-...:sk-ps-...`. For the v4 ingestion path, send
`x-proofstate-ingestion-version: 4` and v4-compatible spans. ProofState-specific
span attributes use the `proofstate.*` namespace. An OTLP exporter appends
`/v1/traces` to the endpoint.

The generated `opentelemetry()` resource exposes a low-level HTTP endpoint,
but does not instrument an application or manage span export.

## Tests

`./mvnw test` runs credential-free unit tests. `./mvnw verify` also runs
integration tests when `PROOFSTATE_PUBLIC_KEY`, `PROOFSTATE_SECRET_KEY`, and
`PROOFSTATE_BASE_URL` are set. Copy `.env.example` to `.env` to provide them.
The integration suite expects `test-chat-prompt` and `test-text-prompt` in that
project, also reads the project prompt, dataset, model, score v3, experiment,
and experiment-item lists, and skips when credentials are absent.

## API coverage and regeneration

This snapshot omits some deprecated and newer API endpoints. Score v3,
experiment, and experiment-item reads are generated in the additive analytics
client described above. The original API client remains available for its
existing methods.

The API definition is maintained in the ProofState server repository under
`fern/apis/server`. Regenerate with a `ProofStateClient` class name and the
`ai.proofstate.client` Java package. Run
`node scripts/rebrand-generated.mjs --refresh` to normalize generated defaults,
artifact identity, and tests after generation. During migration from a
differently named generated package, use `--source-brand NAME` to transform its
symbols and wire names; review the diff and run `./mvnw verify` afterward.

The analytics client is generated from the pinned ProofState Fern definitions
in `fern/apis/analytics/definition`. Update those definitions from the matching
server revision, then on a Linux host with Docker run:

```bash
npx --yes fern-api@3.88.0 generate --local --api analytics --group java
node scripts/sync-analytics-generated.mjs
./mvnw verify
```

The sync script refuses to remove existing analytics classes, so schema changes
that would shrink the Java API require review. Generated output under
`generated/analytics` is excluded from Git; the compiled source lives under
`src/main/java/ai/proofstate/client/analytics`.

The ProofState server at `https://proofstate.ai` accepts the matching
`x-proofstate-*` headers, `proofstate.*` span attributes, and
`isProofStateManaged` model JSON property. Verify compatibility with any other
ProofState deployment before using this client there.

The source repository is [JSOCIT-Inc/proofstate-java](https://github.com/JSOCIT-Inc/proofstate-java). The `ai.proofstate` Maven Central namespace is verified, and the protected release environment holds a Central publisher token and signing credentials. The first release requires protected environment approval after its build checks pass.

## Publishing a prerelease

The release job is opt-in and does not run on normal pushes. The verified
`ai.proofstate` namespace belongs to `proofstate.ai` in the
[Central Portal](https://central.sonatype.com/). The `maven-central` GitHub
environment requires reviewer approval and contains these environment secrets:

- `MAVEN_CENTRAL_USERNAME` and `MAVEN_CENTRAL_PASSWORD`: the Central Portal user
  token pair, not the account login.
- `MAVEN_GPG_PRIVATE_KEY` and `MAVEN_GPG_PASSPHRASE`: the armored release-signing
  private key and its passphrase. The public signing key is available from
  `keyserver.ubuntu.com` with fingerprint
  `CDFE04E6EAE97E1B30F1DB49C61542C4BD952F8E`.

For each release, verify the matching server and SDK, set the POM to a new
prerelease version, and make the generated `X-ProofState-Sdk-Version` value
match it with `node scripts/rebrand-generated.mjs --refresh`. Run
`./mvnw clean verify`, commit the version change, create a `v<version>` tag on
that commit, and start the **Publish Maven Central** workflow with that tag.
The workflow verifies the tag and POM version, reruns tests, signs the JAR,
sources, Javadoc and POM, and publishes through the Central Portal. The
release profile in `pom.xml` is inactive during ordinary builds. Published
Maven Central versions are immutable.

Before the first public version, authenticated checks confirmed nonempty
experiment and item responses with the additive analytics client. Repeat
server-backed compatibility checks for every later protocol release.
