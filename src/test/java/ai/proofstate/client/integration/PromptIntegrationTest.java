package ai.proofstate.client.integration;

import static org.assertj.core.api.Assertions.assertThat;

import ai.proofstate.client.ProofStateClient;
import ai.proofstate.client.TestClientFactory;
import ai.proofstate.client.resources.prompts.types.ChatMessage;
import ai.proofstate.client.resources.prompts.types.ChatMessageWithPlaceholders;
import ai.proofstate.client.resources.prompts.types.ChatPrompt;
import ai.proofstate.client.resources.prompts.types.Prompt;
import ai.proofstate.client.resources.prompts.types.TextPrompt;
import java.util.List;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration tests that connect to a real ProofState project.
 *
 * <p><b>Prerequisites:</b> The target ProofState project must contain:
 * <ul>
 *   <li>{@code test-chat-prompt} — a chat-type prompt with at least one message
 *       that has both {@code role} and {@code content} set.</li>
 *   <li>{@code test-text-prompt} — a text-type prompt with non-empty text.</li>
 * </ul>
 *
 * <p>Credentials are loaded from a {@code .env} file (or system env vars).
 * See {@code .env.example} for the required variables. When credentials are
 * absent the tests are skipped rather than failed.
 */
@Tag("integration")
class PromptIntegrationTest {

    private static ProofStateClient client;

    @BeforeAll
    static void setUp() {
        Assumptions.assumeTrue(
                TestClientFactory.hasCredentials(),
                "Skipping integration tests — PROOFSTATE_PUBLIC_KEY / PROOFSTATE_SECRET_KEY / PROOFSTATE_BASE_URL not set");
        client = TestClientFactory.createClient();
    }

    @Test
    void fetchChatPrompt_messagesHaveRoleAndContent() {
        Prompt prompt = client.prompts().get("test-chat-prompt");

        assertThat(prompt.isChat())
                .as("Expected a chat prompt")
                .isTrue();

        ChatPrompt chatPrompt = prompt.getChat().orElseThrow();
        List<ChatMessageWithPlaceholders> messages = chatPrompt.getPrompt();

        assertThat(messages).isNotEmpty();

        for (ChatMessageWithPlaceholders msg : messages) {
            if (msg.get() instanceof ChatMessage) {
                ChatMessage chatMessage = (ChatMessage) msg.get();
                assertThat(chatMessage.getRole())
                        .as("role must not be null")
                        .isNotNull()
                        .isNotEmpty();
                assertThat(chatMessage.getContent())
                        .as("content must not be null")
                        .isNotNull()
                        .isNotEmpty();
            }
        }
    }

    @Test
    void fetchTextPrompt_promptIsNonEmpty() {
        Prompt prompt = client.prompts().get("test-text-prompt");

        assertThat(prompt.isText())
                .as("Expected a text prompt")
                .isTrue();

        TextPrompt textPrompt = prompt.getText().orElseThrow();

        assertThat(textPrompt.getPrompt())
                .as("prompt text must not be null or empty")
                .isNotNull()
                .isNotEmpty();
    }
}
