package com.unir.jade.chat;

import jade.core.AID;
import jade.core.Agent;
import jade.core.Runtime;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

import javax.swing.SwingUtilities;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChatClientAgent extends Agent {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String[] DEMO_MESSAGES = {"hola", "hora", "/ayuda"};

    private final ConcurrentLinkedQueue<String> pendingMessages = new ConcurrentLinkedQueue<String>();
    private volatile ChatWindow chatWindow;
    private volatile String responderLocalName = "responder";
    private volatile boolean demoMode;
    private volatile int demoRepliesReceived;

    @Override
    protected void setup() {
        Object[] arguments = getArguments();
        if (arguments != null && arguments.length > 0 && arguments[0] != null) {
            responderLocalName = String.valueOf(arguments[0]);
        }
        if (arguments != null && arguments.length > 1 && arguments[1] != null) {
            demoMode = Boolean.parseBoolean(String.valueOf(arguments[1]));
        }

        if (demoMode) {
            demoRepliesReceived = 0;
            appendSystemMessage("Modo demostracion activo.");
            appendSystemMessage("Mensajes preparados: " + Arrays.toString(DEMO_MESSAGES));
            addBehaviour(new WakerBehaviour(this, 500) {
                @Override
                protected void onWake() {
                    for (String message : DEMO_MESSAGES) {
                        queueOutgoingMessage(message);
                    }
                }
            });
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    chatWindow = new ChatWindow(
                            getLocalName(),
                            responderLocalName,
                            new ChatWindow.MessageSender() {
                                @Override
                                public void send(String text) {
                                    queueOutgoingMessage(text);
                                }
                            },
                            new Runnable() {
                                @Override
                                public void run() {
                                    doDelete();
                                }
                            });
                    chatWindow.appendSystemMessage("Agente cliente listo a " + now());
                }
            });
        }

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                boolean processed = false;

                String pending;
                while ((pending = pendingMessages.poll()) != null) {
                    sendMessageToResponder(pending);
                    processed = true;
                }

                ACLMessage incoming = receive();
                if (incoming != null) {
                    processed = true;
                    appendIncomingMessage(incoming);
                }

                if (!processed) {
                    block(100);
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        appendSystemMessage("Agente cliente detenido.");
        final ChatWindow window = chatWindow;
        if (window != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    window.dispose();
                }
            });
        }
    }

    private void queueOutgoingMessage(String text) {
        if (text == null) {
            return;
        }

        String trimmed = text.trim();
        if (!trimmed.isEmpty()) {
            pendingMessages.offer(trimmed);
            appendOutgoingMessage(trimmed);
        }
    }

    private void sendMessageToResponder(String text) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.addReceiver(new AID(responderLocalName, AID.ISLOCALNAME));
        message.setContent(text);
        message.setConversationId("jade-chat");
        send(message);
    }

    private void appendIncomingMessage(final ACLMessage message) {
        final String sender = message.getSender() != null ? message.getSender().getLocalName() : "desconocido";
        final String content = message.getContent() == null ? "" : message.getContent();
        logLine(ChatTranscriptFormatter.formatIncoming(sender, content));

        final ChatWindow window = chatWindow;
        if (window != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    window.appendIncomingMessage(sender, content);
                }
            });
        }

        if (demoMode) {
            demoRepliesReceived++;
            if (demoRepliesReceived >= DEMO_MESSAGES.length && pendingMessages.isEmpty()) {
                doDelete();
                Runtime.instance().shutDown();
                System.exit(0);
            }
        }
    }

    private void appendOutgoingMessage(final String text) {
        logLine(ChatTranscriptFormatter.formatOutgoing(getLocalName(), text));

        final ChatWindow window = chatWindow;
        if (window != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    window.appendOutgoingMessage(getLocalName(), text);
                }
            });
        }
    }

    private void appendSystemMessage(final String text) {
        logLine(ChatTranscriptFormatter.formatSystem(text));

        final ChatWindow window = chatWindow;
        if (window != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    window.appendSystemMessage(text);
                }
            });
        }
    }

    private void logLine(String text) {
        System.out.println(text);
    }

    private static String now() {
        return LocalDateTime.now().format(TIME_FORMAT);
    }
}
