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

    private static void startAgent(ContainerController container, String name, String className, Object[] arguments) {
        try {
            AgentController controller = container.createNewAgent(name, className, arguments);
            controller.start();
        } catch (StaleProxyException exception) {
            throw new IllegalStateException("No se pudo iniciar el agente " + name, exception);
        }
    }
}
