package com.unir.jade.chat;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatResponderAgent extends Agent {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

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

