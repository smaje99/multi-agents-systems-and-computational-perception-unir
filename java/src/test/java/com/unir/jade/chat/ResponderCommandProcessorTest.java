package com.unir.jade.chat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ResponderCommandProcessor}.
 */
class ResponderCommandProcessorTest {

    @Test
    void echoesRegularMessages() {
        ResponderCommandProcessor processor = new ResponderCommandProcessor("responder");
        assertEquals("Eco desde responder: hola", processor.buildReply("hola"));
    }

    @Test
    void returnsUppercaseText() {
        ResponderCommandProcessor processor = new ResponderCommandProcessor("responder");
        assertEquals("HOLA MUNDO", processor.buildReply("upper Hola mundo"));
    }

    @Test
    void countsWordsAndCharacters() {
        ResponderCommandProcessor processor = new ResponderCommandProcessor("responder");
        assertEquals("Count -> palabras: 3, caracteres: 15.", processor.buildReply("count hola mundo jade"));
    }

    @Test
    void evaluatesSimpleExpressions() {
        ResponderCommandProcessor processor = new ResponderCommandProcessor("responder");
        assertEquals("Resultado: 14", processor.buildReply("calc 2 + 3 * 4"));
    }

    @Test
    void reportsInvalidExpressions() {
        ResponderCommandProcessor processor = new ResponderCommandProcessor("responder");
        assertTrue(processor.buildReply("calc 2 +").startsWith("Expresion no valida:"));
    }

    @Test
    void returnsRecentHistory() {
        ResponderCommandProcessor processor = new ResponderCommandProcessor("responder");
        processor.buildReply("hola");
        processor.buildReply("upper prueba");
        String history = processor.buildReply("history");

        assertTrue(history.contains("1. history"));
        assertTrue(history.contains("2. upper prueba"));
        assertTrue(history.contains("3. hola"));
    }
}
