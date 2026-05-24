package com.unir.jade.chat;

/**
 * Utility methods that format chat transcript lines in a consistent way.
 *
 * <p>The project uses this helper to keep console output and Swing transcript
 * messages aligned. Centralizing the formatting rules also keeps the tests
 * focused on a single responsibility instead of duplicating string literals
 * across the code base.
 */
public final class ChatTranscriptFormatter {

    /**
     * Prevents instantiation.
     */
    private ChatTranscriptFormatter() {
    }

    /**
     * Formats a line representing an outgoing message.
     *
     * @param sender the local sender name
     * @param message the message content
     * @return a normalized transcript line for an outgoing message
     */
    public static String formatOutgoing(String sender, String message) {
        return "[" + sender + " -> responder] " + normalize(message);
    }

    /**
     * Formats a line representing an incoming message.
     *
     * @param sender the remote sender name
     * @param message the message content
     * @return a normalized transcript line for an incoming message
     */
    public static String formatIncoming(String sender, String message) {
        return "[" + sender + "] " + normalize(message);
    }

    /**
     * Formats a system status line.
     *
     * @param message the system message content
     * @return a normalized transcript line for a system status message
     */
    public static String formatSystem(String message) {
        return "[Sistema] " + normalize(message);
    }

    /**
     * Trims the provided message and converts {@code null} values to an empty
     * string.
     *
     * @param message the raw message text
     * @return a trimmed, non-null message string
     */
    private static String normalize(String message) {
        return message == null ? "" : message.trim();
    }
}
