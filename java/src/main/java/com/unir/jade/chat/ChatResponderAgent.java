package com.unir.jade.chat;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * JADE agent that answers incoming chat messages.
 *
 * <p>The agent is intentionally simple: it listens for ACL messages, applies a
 * small decision table, and sends a reply back to the original sender. The
 * behavior is useful for academic demonstration because it shows a complete
 * message round trip without introducing unnecessary domain complexity.
 */
public class ChatResponderAgent extends Agent {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Registers the cyclic behavior that consumes incoming messages and sends
     * responses.
     *
     * <p>The agent prints a startup message to standard output so the demo
     * trace clearly shows when the responder becomes available.
     */
    @Override
    protected void setup() {
        System.out.println("Agente respondedor listo: " + getLocalName());

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage message = receive();
                if (message == null) {
                    block();
                    return;
                }

                String content = message.getContent() == null ? "" : message.getContent().trim();
                String replyText = buildReply(content);

                ACLMessage reply = message.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent(replyText);
                send(reply);

                System.out.println("Recibido de " + message.getSender().getLocalName() + ": " + content);
                System.out.println("Respuesta enviada: " + replyText);
            }
        });
    }

    /**
     * Builds the reply text for a received message.
     *
     * <p>The responder supports a few simple commands:
     * <ul>
     *   <li>an empty message produces a diagnostic response;</li>
     *   <li>{@code hora} or {@code /hora} returns the current time; and</li>
     *   <li>{@code ayuda} or {@code /ayuda} returns a short usage hint.</li>
     * </ul>
     * Any other message is echoed back to the sender.
     *
     * @param content the normalized message content
     * @return the reply text that will be sent to the original sender
     */
    private String buildReply(String content) {
        if (content.isEmpty()) {
            return "No he recibido texto para responder.";
        }

        if ("hora".equalsIgnoreCase(content) || "/hora".equalsIgnoreCase(content)) {
            return "Hora actual: " + LocalDateTime.now().format(TIME_FORMAT);
        }

        if ("ayuda".equalsIgnoreCase(content) || "/ayuda".equalsIgnoreCase(content)) {
            return "Prueba con texto libre, 'hora' o '/ayuda'.";
        }

        return "Eco desde " + getLocalName() + ": " + content;
    }
}
