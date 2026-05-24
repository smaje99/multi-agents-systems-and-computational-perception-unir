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

/**
 * JADE agent that provides the user-facing chat client.
 *
 * <p>The agent has two responsibilities:
 * <ol>
 *   <li>manage the Swing-based interface when the application runs in
 *   interactive mode; and</li>
 *   <li>coordinate outgoing and incoming ACL messages with the responder
 *   agent.</li>
 * </ol>
 *
 * <p>The implementation deliberately separates the GUI event handlers from the
 * agent behavior queue so the UI thread remains responsive while JADE handles
 * message exchange on its own scheduler. In demo mode, the agent also emits a
 * predefined sequence of messages to make the project easy to verify in
 * headless or restricted environments.
 */
public class ChatClientAgent extends Agent {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String[] DEMO_MESSAGES = {"hola", "hora", "/ayuda"};

    private final ConcurrentLinkedQueue<String> pendingMessages = new ConcurrentLinkedQueue<String>();
    private volatile ChatWindow chatWindow;
    private volatile String responderLocalName = "responder";
    private volatile boolean demoMode;
    private volatile int demoRepliesReceived;

    /**
     * Initializes the agent, reads its arguments, configures the optional GUI,
     * and installs the behaviors that drive message exchange.
     *
     * <p>Expected arguments:
     * <ol>
     *   <li>the local name of the responder agent; and</li>
     *   <li>a boolean flag that enables demo mode.</li>
     * </ol>
     */
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

    /**
     * Releases the Swing window when the agent is stopped.
     *
     * <p>The method keeps the shutdown path defensive: if the GUI was never
     * created, the agent still terminates cleanly without dereferencing a null
     * reference.
     */
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

    /**
     * Enqueues a message typed by the user or generated by the demo mode.
     *
     * <p>Blank messages are ignored to avoid sending empty ACL payloads.
     *
     * @param text the raw text to queue for delivery to the responder agent
     */
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

    /**
     * Sends a request message to the responder agent.
     *
     * @param text the message body to transmit
     */
    private void sendMessageToResponder(String text) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.addReceiver(new AID(responderLocalName, AID.ISLOCALNAME));
        message.setContent(text);
        message.setConversationId("jade-chat");
        send(message);
    }

    /**
     * Appends an incoming ACL message to the console trace and the optional
     * Swing transcript.
     *
     * <p>In demo mode, this method also tracks how many replies were received
     * so the agent can terminate the platform once the scripted conversation is
     * complete.
     *
     * @param message the received ACL message
     */
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

    /**
     * Appends an outgoing message to the console trace and the optional Swing
     * transcript.
     *
     * @param text the outgoing message content
     */
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

    /**
     * Appends a system-level status line to the console trace and the optional
     * Swing transcript.
     *
     * @param text the status message to display
     */
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

    /**
     * Writes a single line to standard output.
     *
     * @param text the text to log
     */
    private void logLine(String text) {
        System.out.println(text);
    }

    /**
     * Returns the current local time in {@code HH:mm:ss} format.
     *
     * @return the formatted current time
     */
    private static String now() {
        return LocalDateTime.now().format(TIME_FORMAT);
    }
}
