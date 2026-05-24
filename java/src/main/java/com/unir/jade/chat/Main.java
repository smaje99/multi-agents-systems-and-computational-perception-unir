package com.unir.jade.chat;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public final class Main {

    private static final String RESPONDER_AGENT_NAME = "responder";
    private static final String CLIENT_AGENT_NAME = "client";

    private Main() {
    }

    /**
     * Starts the JADE runtime, creates the main container, and launches the
     * two agents used by the chat demo.
     *
     * <p>The application supports two execution modes:
     * <ul>
     *   <li>the default Swing mode, where the user interacts with a graphical
     *   window; and</li>
     *   <li>a deterministic demo mode activated with {@code --demo}, which
     *   sends a predefined conversation automatically so the behavior can be
     *   verified in environments without a GUI.</li>
     * </ul>
     *
     * <p>The method also configures the local host and a fixed local port so
     * the JADE platform starts predictably in restricted or sandboxed
     * environments.
     *
     * @param args optional command-line arguments; {@code --demo} enables the
     *             non-interactive execution mode
     */
    public static void main(String[] args) {
        boolean demoMode = false;
        if (args != null) {
            for (String arg : args) {
                if ("--demo".equalsIgnoreCase(arg)) {
                    demoMode = true;
                }
            }
        }

        Runtime.instance().setCloseVM(true);

        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.GUI, Boolean.toString(!demoMode));
        profile.setParameter(Profile.LOCAL_HOST, "127.0.0.1");
        profile.setParameter(Profile.LOCAL_PORT, System.getProperty("jade.local.port", "12000"));

        ContainerController container = Runtime.instance().createMainContainer(profile);
        startAgent(container, RESPONDER_AGENT_NAME, ChatResponderAgent.class.getName(), null);
        startAgent(
                container,
                CLIENT_AGENT_NAME,
                ChatClientAgent.class.getName(),
                new Object[] {RESPONDER_AGENT_NAME, Boolean.toString(demoMode)});

        if (demoMode) {
            System.out.println("JADE arrancado en modo demostracion.");
        } else {
            System.out.println("JADE arrancado. Abre la ventana Swing del cliente para hablar con el agente respondedor.");
        }
    }

    /**
     * Creates and starts a new JADE agent inside the provided container.
     *
     * @param container the container that will own the new agent
     * @param name the local agent name to register in the platform
     * @param className the fully qualified class name of the agent
     * @param arguments optional agent arguments passed to {@code setup()}
     * @throws IllegalStateException if JADE cannot create or start the agent
     */
    private static void startAgent(ContainerController container, String name, String className, Object[] arguments) {
        try {
            AgentController controller = container.createNewAgent(name, className, arguments);
            controller.start();
        } catch (StaleProxyException exception) {
            throw new IllegalStateException("No se pudo iniciar el agente " + name, exception);
        }
    }
}
