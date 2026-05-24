package com.unir.jade.chat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link ChatTranscriptFormatter}.
 *
 * <p>These tests focus on string normalization and transcript conventions so
 * the formatter can be trusted as a stable utility for both the console trace
 * and the Swing transcript.
 */
class ChatTranscriptFormatterTest {

    /**
     * Verifies that outgoing messages are trimmed and annotated with the local
     * sender label.
     */
    @Test
    void formatsOutgoingMessage() {
        assertEquals("[cliente -> responder] hola", ChatTranscriptFormatter.formatOutgoing("cliente", "  hola  "));
    }

    /**
     * Verifies that incoming messages preserve the responder label and the
     * expected transcript structure.
     */
    @Test
    void formatsIncomingMessage() {
        assertEquals("[responder] Eco desde responder: hola", ChatTranscriptFormatter.formatIncoming("responder", "Eco desde responder: hola"));
    }

    /**
     * Verifies that system messages are rendered with the expected prefix and
     * without leading or trailing whitespace.
     */
    @Test
    void formatsSystemMessage() {
        assertEquals("[Sistema] Agente listo", ChatTranscriptFormatter.formatSystem(" Agente listo "));
    }
}
