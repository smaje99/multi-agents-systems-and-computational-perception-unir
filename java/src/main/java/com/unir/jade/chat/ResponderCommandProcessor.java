package com.unir.jade.chat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Locale;

/**
 * Stateful command processor used by {@link ChatResponderAgent}.
 *
 * <p>The processor keeps a short in-memory history of the last user messages
 * and resolves the small command set supported by the academic demo. The class
 * is intentionally independent from JADE so its behavior can be tested with
 * ordinary unit tests.
 */
public final class ResponderCommandProcessor {

    private static final int MAX_HISTORY_SIZE = 10;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final String responderName;
    private final Deque<String> recentMessages = new ArrayDeque<String>();

    /**
     * Creates a processor for the given responder name.
     *
     * @param responderName the logical name used in echo responses
     */
    public ResponderCommandProcessor(String responderName) {
        this.responderName = responderName;
    }

    /**
     * Resolves the reply for a received user message.
     *
     * <p>Supported commands:
     * <ul>
     *   <li>{@code hora} or {@code /hora}</li>
     *   <li>{@code ayuda} or {@code /ayuda}</li>
     *   <li>{@code upper <text>}</li>
     *   <li>{@code count <text>}</li>
     *   <li>{@code calc <expression>}</li>
     *   <li>{@code history}</li>
     * </ul>
     *
     * @param content the normalized message content
     * @return the reply to send back to the client
     */
    public String buildReply(String content) {
        if (content.isEmpty()) {
            return "No he recibido texto para responder.";
        }

        recordMessage(content);

        if ("hora".equalsIgnoreCase(content) || "/hora".equalsIgnoreCase(content)) {
            return "Hora actual: " + LocalDateTime.now().format(TIME_FORMAT);
        }

        if ("ayuda".equalsIgnoreCase(content) || "/ayuda".equalsIgnoreCase(content)) {
            return "Comandos: hora, /ayuda, upper <texto>, count <texto>, calc <expresion>, history.";
        }

        if ("history".equalsIgnoreCase(content)) {
            return formatHistory();
        }

        if (startsWithCommand(content, "upper")) {
            return handleUpper(extractArgument(content, "upper"));
        }

        if (startsWithCommand(content, "count")) {
            return handleCount(extractArgument(content, "count"));
        }

        if (startsWithCommand(content, "calc")) {
            return handleCalc(extractArgument(content, "calc"));
        }

        return "Eco desde " + responderName + ": " + content;
    }

    private void recordMessage(String content) {
        recentMessages.addLast(content);
        while (recentMessages.size() > MAX_HISTORY_SIZE) {
            recentMessages.removeFirst();
        }
    }

    private String formatHistory() {
        if (recentMessages.isEmpty()) {
            return "No hay mensajes en el historial.";
        }

        StringBuilder builder = new StringBuilder("Historial reciente:");
        int index = 1;
        for (Iterator<String> iterator = recentMessages.descendingIterator(); iterator.hasNext(); ) {
            builder.append(System.lineSeparator())
                    .append(index++)
                    .append(". ")
                    .append(iterator.next());
        }
        return builder.toString();
    }

    private String handleUpper(String argument) {
        if (argument.isEmpty()) {
            return "Uso: upper <texto>";
        }

        return argument.toUpperCase(Locale.ROOT);
    }

    private String handleCount(String argument) {
        if (argument.isEmpty()) {
            return "Uso: count <texto>";
        }

        String trimmed = argument.trim();
        int characters = trimmed.length();
        int words = trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;
        return "Count -> palabras: " + words + ", caracteres: " + characters + ".";
    }

    private String handleCalc(String argument) {
        if (argument.isEmpty()) {
            return "Uso: calc <expresion>";
        }

        try {
            double result = new SimpleExpressionEvaluator(argument).evaluate();
            return "Resultado: " + formatNumber(result);
        } catch (IllegalArgumentException exception) {
            return "Expresion no valida: " + exception.getMessage();
        }
    }

    private static boolean startsWithCommand(String content, String command) {
        return content.regionMatches(true, 0, command, 0, command.length())
                && (content.length() == command.length() || Character.isWhitespace(content.charAt(command.length())));
    }

    private static String extractArgument(String content, String command) {
        return content.length() <= command.length() ? "" : content.substring(command.length()).trim();
    }

    private static String formatNumber(double value) {
        if (value == Math.rint(value)) {
            return Long.toString((long) value);
        }
        return Double.toString(value);
    }

    /**
     * Minimal arithmetic evaluator that supports {@code +}, {@code -},
     * {@code *}, {@code /}, and parentheses.
     */
    private static final class SimpleExpressionEvaluator {

        private final String expression;
        private int index;

        private SimpleExpressionEvaluator(String expression) {
            this.expression = expression;
        }

        private double evaluate() {
            double value = parseExpression();
            skipWhitespace();
            if (index != expression.length()) {
                throw new IllegalArgumentException("sobran terminos al final.");
            }
            return value;
        }

        private double parseExpression() {
            double value = parseTerm();
            while (true) {
                skipWhitespace();
                if (match('+')) {
                    value += parseTerm();
                } else if (match('-')) {
                    value -= parseTerm();
                } else {
                    return value;
                }
            }
        }

        private double parseTerm() {
            double value = parseFactor();
            while (true) {
                skipWhitespace();
                if (match('*')) {
                    value *= parseFactor();
                } else if (match('/')) {
                    double divisor = parseFactor();
                    if (divisor == 0.0d) {
                        throw new IllegalArgumentException("division por cero.");
                    }
                    value /= divisor;
                } else {
                    return value;
                }
            }
        }

        private double parseFactor() {
            skipWhitespace();

            if (match('+')) {
                return parseFactor();
            }

            if (match('-')) {
                return -parseFactor();
            }

            if (match('(')) {
                double value = parseExpression();
                skipWhitespace();
                if (!match(')')) {
                    throw new IllegalArgumentException("falta un parentesis de cierre.");
                }
                return value;
            }

            return parseNumber();
        }

        private double parseNumber() {
            skipWhitespace();
            int start = index;
            boolean seenDot = false;

            while (index < expression.length()) {
                char current = expression.charAt(index);
                if (Character.isDigit(current)) {
                    index++;
                } else if (current == '.' && !seenDot) {
                    seenDot = true;
                    index++;
                } else {
                    break;
                }
            }

            if (start == index) {
                throw new IllegalArgumentException("se esperaba un numero.");
            }

            return Double.parseDouble(expression.substring(start, index));
        }

        private boolean match(char expected) {
            if (index < expression.length() && expression.charAt(index) == expected) {
                index++;
                return true;
            }
            return false;
        }

        private void skipWhitespace() {
            while (index < expression.length() && Character.isWhitespace(expression.charAt(index))) {
                index++;
            }
        }
    }
}
