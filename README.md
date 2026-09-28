# proofstate-java

Java API client for [ProofState](https://proofstate.ai). This is a source
prerelease snapshot of the ProofState API; it has not been published to Maven
Central. The original MIT license and attribution are retained in [LICENSE](LICENSE).

It has not been verified against the live ProofState deployment. Use it with a matching server revision after authenticated end-to-end checks.

The client covers API operations for prompts, datasets, scores, models,
comments, and observations. Use the OpenTelemetry Java SDK separately to send
traces. This API client does not instrument an application or export spans.

## Build and install locally

The build was verified with Java 21 and the included Maven wrapper:

```bash
./mvnw clean install
```

On Windows PowerShell, run `.\mvnw.cmd clean install`.

```xml
<dependency>
    <groupId>ai.proofstate</groupId>
    <artifactId>proofstate-java</artifactId>
    <version>0.1.0-SNAPSHOT</version>
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
project, also reads the project prompt, dataset, and model lists, and skips
when credentials are absent.

## API coverage and regeneration

This snapshot omits deprecated v3 endpoints and does not yet generate all
newer endpoints, including `GET /api/public/v3/scores`,
`GET /api/public/experiments`, and `GET /api/public/experiment-items`. Those
reads require a fresh generation from the current ProofState API definition.

The API definition is maintained in the ProofState server repository under
`fern/apis/server`. Regenerate with a `ProofStateClient` class name and the
`ai.proofstate.client` Java package. Run
`node scripts/rebrand-generated.mjs --refresh` to normalize generated defaults,
artifact identity, and tests after generation. During migration from a
differently named generated package, use `--source-brand NAME` to transform its
symbols and wire names; review the diff and run `./mvnw verify` afterward.

The matching ProofState server release must accept the `x-proofstate-*` headers,
the `proofstate.*` span attributes, and the `isProofStateManaged` model JSON
property before this client is used against production. The Java client and
server must be deployed together for the renamed contract.

The source repository is [JSOCIT-Inc/proofstate-java](https://github.com/JSOCIT-Inc/proofstate-java). Before publishing, establish Maven Central ownership, a signing process, and authenticated server-backed checks.

## Publishing a prerelease

The release job is opt-in and does not run on normal pushes. A Maven Central
publisher must first verify the `ai.proofstate` namespace by proving ownership
of `proofstate.ai` in the [Central Portal](https://central.sonatype.com/). Set up
the `maven-central` GitHub environment with required reviewer protection and
these environment secrets:

- `MAVEN_CENTRAL_USERNAME` and `MAVEN_CENTRAL_PASSWORD`: the Central Portal user
  token pair, not the account login.
- `MAVEN_GPG_PRIVATE_KEY` and `MAVEN_GPG_PASSPHRASE`: the armored release-signing
  private key and its passphrase. Publish the corresponding public key.

After the matching server release and authenticated SDK smoke test pass, replace
the snapshot POM version with a prerelease version, such as `0.1.0-rc.1`. Make
the generated `X-ProofState-Sdk-Version` value match the POM with
`node scripts/rebrand-generated.mjs --refresh`, then run `./mvnw clean verify`.
Commit the version change, create a tag `v0.1.0-rc.1`
on that commit, and start the **Publish Maven Central** workflow with that tag.
The workflow verifies the tag and POM version, reruns tests, signs the JAR,
sources, Javadoc and POM, and publishes through the Central Portal. The
release profile in `pom.xml` is inactive during ordinary builds. Published
Maven Central versions are immutable.

Do not run the release workflow until the namespace is verified and the
server-backed smoke test succeeds. This snapshot's missing newer API endpoints
must also be resolved or explicitly documented for the first public version.
