package ai.proofstate.client.integration;

import static org.assertj.core.api.Assertions.assertThat;

import ai.proofstate.client.ProofStateClient;
import ai.proofstate.client.TestClientFactory;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Read-only checks against a running ProofState server with project credentials. */
@Tag("integration")
class ApiReadIntegrationTest {

    private static ProofStateClient client;

    @BeforeAll
    static void setUp() {
        Assumptions.assumeTrue(TestClientFactory.hasCredentials(),
                "Set PROOFSTATE_PUBLIC_KEY, PROOFSTATE_SECRET_KEY and PROOFSTATE_BASE_URL");
        client = TestClientFactory.createClient();
    }

    @Test
    void listPrompts() {
        assertThat(client.prompts().list().getData()).isNotNull();
    }

    @Test
    void listDatasets() {
        assertThat(client.datasets().list().getData()).isNotNull();
    }

    @Test
    void listModels() {
        assertThat(client.models().list().getData()).isNotNull();
    }
}
