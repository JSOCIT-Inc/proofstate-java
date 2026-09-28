package ai.proofstate.client.integration;

import static org.assertj.core.api.Assertions.assertThat;

import ai.proofstate.client.TestClientFactory;
import ai.proofstate.client.analytics.ProofStateAnalyticsClient;
import ai.proofstate.client.analytics.resources.experiments.requests.ListExperimentItemsRequest;
import ai.proofstate.client.analytics.resources.experiments.requests.ListExperimentsRequest;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Read-only analytics checks against a running ProofState server. */
@Tag("integration")
class AnalyticsIntegrationTest {

    private static ProofStateAnalyticsClient client;

    @BeforeAll
    static void setUp() {
        Assumptions.assumeTrue(TestClientFactory.hasCredentials(),
                "Set PROOFSTATE_PUBLIC_KEY, PROOFSTATE_SECRET_KEY and PROOFSTATE_BASE_URL");
        client = ProofStateAnalyticsClient.builder()
                .credentials(TestClientFactory.getEnv("PROOFSTATE_PUBLIC_KEY"),
                        TestClientFactory.getEnv("PROOFSTATE_SECRET_KEY"))
                .url(TestClientFactory.getEnv("PROOFSTATE_BASE_URL"))
                .build();
    }

    @Test
    void listScoresV3() {
        assertThat(client.scoresV3().getManyV3().getData()).isNotNull();
    }

    @Test
    void listExperiments() {
        OffsetDateTime from = OffsetDateTime.now(ZoneOffset.UTC).minusDays(30);
        assertThat(client.experiments().list(
                ListExperimentsRequest.builder().fromStartTime(from).build()).getData()).isNotNull();
    }

    @Test
    void listExperimentItems() {
        OffsetDateTime from = OffsetDateTime.now(ZoneOffset.UTC).minusDays(30);
        assertThat(client.experiments().listItems(
                ListExperimentItemsRequest.builder().fromStartTime(from).build()).getData()).isNotNull();
    }
}
