package com.unir.jade.chat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatTranscriptFormatterTest {

    @Test
    void formatsOutgoingMessage() {
        assertEquals("[cliente -> responder] hola", ChatTranscriptFormatter.formatOutgoing("cliente", "  hola  "));
    }

    @Test
    void formatsIncomingMessage() {
        assertEquals("[responder] Eco desde responder: hola", ChatTranscriptFormatter.formatIncoming("responder", "Eco desde responder: hola"));
    }

    @Test
    void formatsSystemMessage() {
        assertEquals("[Sistema] Agente listo", ChatTranscriptFormatter.formatSystem(" Agente listo "));
    }
}

