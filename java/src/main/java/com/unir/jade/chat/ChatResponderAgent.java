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

    private ResponderCommandProcessor commandProcessor;

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
        commandProcessor = new ResponderCommandProcessor(getLocalName());

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage message = receive();
                if (message == null) {
                    block();
                    return;
                }

                String content = message.getContent() == null ? "" : message.getContent().trim();
                String replyText = commandProcessor.buildReply(content);

                ACLMessage reply = message.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent(replyText);
                send(reply);

                System.out.println("Recibido de " + message.getSender().getLocalName() + ": " + content);
                System.out.println("Respuesta enviada: " + replyText);
            }
        });
    }
}
