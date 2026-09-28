package ai.proofstate.client.deserialization;

import static org.assertj.core.api.Assertions.assertThat;

import ai.proofstate.client.core.ObjectMappers;
import ai.proofstate.client.resources.commons.types.Model;
import org.junit.jupiter.api.Test;

class ModelDeserializationTest {

    @Test
    void readsAndWritesProofStateManagedField() throws Exception {
        String json = "{\"id\":\"test-model\",\"modelName\":\"test-model\","
                + "\"matchPattern\":\".*\",\"tokenizerConfig\":{},"
                + "\"isProofStateManaged\":true,\"createdAt\":\"2026-09-28T00:00:00Z\","
                + "\"prices\":{},\"pricingTiers\":[]}";

        Model model = ObjectMappers.JSON_MAPPER.readValue(json, Model.class);

        assertThat(model.getIsProofStateManaged()).isTrue();
        assertThat(ObjectMappers.JSON_MAPPER.writeValueAsString(model))
                .contains("\"isProofStateManaged\":true");
    }
}
