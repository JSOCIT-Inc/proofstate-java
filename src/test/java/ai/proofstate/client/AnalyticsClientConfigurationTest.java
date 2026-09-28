package ai.proofstate.client;

import static org.assertj.core.api.Assertions.assertThat;

import ai.proofstate.client.analytics.ProofStateAnalyticsClient;
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

class AnalyticsClientConfigurationTest {

    @Test
    void usesProofStateHostAuthAndSdkHeaders() {
        AtomicReference<Request> captured = new AtomicReference<>();
        OkHttpClient httpClient = new OkHttpClient.Builder().addInterceptor(chain -> {
            Request request = chain.request();
            captured.set(request);
            return new Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(ResponseBody.create(MediaType.get("application/json"),
                            "{\"data\":[],\"meta\":{\"limit\":50}}"))
                    .build();
        }).build();
        ProofStateAnalyticsClient client = ProofStateAnalyticsClient.builder()
                .credentials("pk-ps-example", "sk-ps-example")
                .httpClient(httpClient)
                .build();

        assertThat(client.scoresV3().getManyV3().getData()).isEmpty();
        Request request = captured.get();
        assertThat(request.url().toString()).isEqualTo("https://proofstate.ai/api/public/v3/scores");
        String token = Base64.getEncoder().encodeToString(
                "pk-ps-example:sk-ps-example".getBytes(StandardCharsets.UTF_8));
        assertThat(request.header("Authorization")).isEqualTo("Basic " + token);
        assertThat(request.header("X-ProofState-Sdk-Name")).isEqualTo("proofstate-java");
        assertThat(request.header("X-ProofState-Sdk-Version")).isEqualTo("0.1.0-SNAPSHOT");
        assertThat(request.header("X-Fern-Language")).isNull();
    }
}
