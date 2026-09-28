package ai.proofstate.client;

import static org.assertj.core.api.Assertions.assertThat;

import ai.proofstate.client.resources.health.types.HealthResponse;
import ai.proofstate.client.core.RequestOptions;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;

class ProofStateClientConfigurationTest {

    @Test
    void usesProofStateHostBasicAuthAndSdkHeaders() {
        AtomicReference<Request> captured = new AtomicReference<>();
        ProofStateClient client = ProofStateClient.builder()
                .credentials("pk-ps-example", "sk-ps-example")
                .xProofStatePublicKey("pk-ps-example")
                .httpClient(recordingClient(captured))
                .build();

        HealthResponse health = client.health().health();

        assertThat(health.getStatus()).isEqualTo("OK");
        Request request = captured.get();
        assertThat(request.url().toString()).isEqualTo("https://proofstate.ai/api/public/health");
        String token = Base64.getEncoder().encodeToString(
                "pk-ps-example:sk-ps-example".getBytes(StandardCharsets.UTF_8));
        assertThat(request.header("Authorization")).isEqualTo("Basic " + token);
        assertThat(request.header("X-ProofState-Sdk-Name")).isEqualTo("proofstate-java");
        assertThat(request.header("X-ProofState-Sdk-Version")).isEqualTo("0.1.0-rc.1");
        assertThat(request.header("X-ProofState-Public-Key")).isEqualTo("pk-ps-example");
        assertThat(request.header("X-Fern-SDK-Name")).isNull();
    }

    @Test
    void requestOptionsUseProofStateHeaders() {
        RequestOptions options = RequestOptions.builder()
                .xProofStateSdkName("proofstate-java")
                .xProofStateSdkVersion("0.1.0-rc.1")
                .xProofStatePublicKey("pk-ps-example")
                .build();

        assertThat(options.getHeaders()).containsEntry("X-ProofState-Sdk-Name", "proofstate-java")
                .containsEntry("X-ProofState-Sdk-Version", "0.1.0-rc.1")
                .containsEntry("X-ProofState-Public-Key", "pk-ps-example");
    }

    @Test
    void allowsCustomHost() {
        AtomicReference<Request> captured = new AtomicReference<>();
        ProofStateClient client = ProofStateClient.builder()
                .url("https://self-hosted.example")
                .httpClient(recordingClient(captured))
                .build();

        client.health().health();

        assertThat(captured.get().url().toString())
                .isEqualTo("https://self-hosted.example/api/public/health");
    }

    private static OkHttpClient recordingClient(AtomicReference<Request> captured) {
        return new OkHttpClient.Builder().addInterceptor(chain -> {
            Request request = chain.request();
            captured.set(request);
            return new Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(ResponseBody.create(
                            MediaType.get("application/json"),
                            "{\"status\":\"OK\",\"version\":\"4.36.0\"}"))
                    .build();
        }).build();
    }
}
