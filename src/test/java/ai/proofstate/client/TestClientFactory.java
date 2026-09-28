package ai.proofstate.client;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Utility for integration tests: loads ProofState credentials from a {@code .env}
 * file (or system/environment variables) and builds a {@link ProofStateClient}.
 */
public final class TestClientFactory {

    private static final Dotenv DOTENV = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    private TestClientFactory() {}

    public static String getEnv(String key) {
        // dotenv-java checks .env first, then falls back to system env
        return DOTENV.get(key);
    }

    public static boolean hasCredentials() {
        String publicKey = getEnv("PROOFSTATE_PUBLIC_KEY");
        String secretKey = getEnv("PROOFSTATE_SECRET_KEY");
        String host = getEnv("PROOFSTATE_BASE_URL");
        return publicKey != null && !publicKey.isEmpty()
                && secretKey != null && !secretKey.isEmpty()
                && host != null && !host.isEmpty();
    }

    public static ProofStateClient createClient() {
        return ProofStateClient.builder()
                .credentials(getEnv("PROOFSTATE_PUBLIC_KEY"), getEnv("PROOFSTATE_SECRET_KEY"))
                .url(getEnv("PROOFSTATE_BASE_URL"))
                .build();
    }
}
