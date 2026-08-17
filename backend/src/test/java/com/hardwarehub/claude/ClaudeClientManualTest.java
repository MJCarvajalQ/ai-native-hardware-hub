package com.hardwarehub.claude;

/**
 * Throwaway manual check for ClaudeClient, per the plan's K1 verification step.
 * Not a JUnit test (no assertions, not run by mvn test) — run its main() by hand
 * with ANTHROPIC_API_KEY set to confirm a real call works before wiring anything else.
 */
public class ClaudeClientManualTest {

    public static void main(String[] args) throws Exception {
        String apiKey = System.getenv("ANTHROPIC_API_KEY");
        ClaudeClient client = new ClaudeClient(apiKey);
        String reply = client.sendMessage("reply OK");
        System.out.println("Claude replied: " + reply);
    }
}
