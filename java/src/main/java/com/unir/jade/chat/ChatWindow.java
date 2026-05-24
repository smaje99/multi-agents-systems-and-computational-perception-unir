package com.unir.jade.chat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class ChatWindow extends JFrame {

    public interface MessageSender {
        void send(String text);
    }

    private final JTextArea transcript;
    private final JTextField inputField;
    private final MessageSender messageSender;

    public ChatWindow(String clientName, String responderName, MessageSender messageSender, final Runnable onClose) {
        super("Chat JADE - " + clientName);
        this.messageSender = messageSender;

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setPreferredSize(new Dimension(760, 480));

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Cliente JADE conectado con " + responderName, SwingConstants.LEFT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        root.add(title, BorderLayout.NORTH);

        transcript = new JTextArea();
        transcript.setEditable(false);
        transcript.setLineWrap(true);
        transcript.setWrapStyleWord(true);
        transcript.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        root.add(new JScrollPane(transcript), BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

        JPanel inputPanel = new JPanel(new BorderLayout(8, 8));
        inputField = new JTextField();
        inputPanel.add(inputField, BorderLayout.CENTER);

        JButton sendButton = new JButton("Enviar");
        inputPanel.add(sendButton, BorderLayout.EAST);
        bottom.add(inputPanel);
        bottom.add(Box.createVerticalStrut(8));

        JPanel helpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JLabel help = new JLabel("Comandos: /ayuda, hora");
        helpPanel.add(help);
        bottom.add(helpPanel);

        root.add(bottom, BorderLayout.SOUTH);
        setContentPane(root);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                sendCurrentText();
            }
        });

        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                sendCurrentText();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                onClose.run();
            }
        });

        pack();
        setLocationRelativeTo(null);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setVisible(true);
                inputField.requestFocusInWindow();
            }
        });
    }

    public void appendSystemMessage(String text) {
        appendLine("[Sistema] " + text);
    }

    public void appendOutgoingMessage(String sender, String text) {
        appendLine(ChatTranscriptFormatter.formatOutgoing(sender, text));
    }

    public void appendIncomingMessage(String sender, String text) {
        appendLine(ChatTranscriptFormatter.formatIncoming(sender, text));
    }

    private void sendCurrentText() {
        String text = inputField.getText();
        if (text != null) {
            text = text.trim();
        }

        if (text == null || text.isEmpty()) {
            return;
        }

        inputField.setText("");
        messageSender.send(text);
    }

    private void appendLine(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                transcript.append(text);
                transcript.append(System.lineSeparator());
                transcript.setCaretPosition(transcript.getDocument().getLength());
            }
        });
    }
}

