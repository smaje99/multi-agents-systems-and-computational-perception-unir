package com.unir.jade.chat;

public final class ChatTranscriptFormatter {

    private ChatTranscriptFormatter() {
    }

    public static String formatOutgoing(String sender, String message) {
        return "[" + sender + " -> responder] " + normalize(message);
    }

    public static String formatIncoming(String sender, String message) {
        return "[" + sender + "] " + normalize(message);
    }

    public static String formatSystem(String message) {
        return "[Sistema] " + normalize(message);
    }

    private static String normalize(String message) {
        return message == null ? "" : message.trim();
    }
}

