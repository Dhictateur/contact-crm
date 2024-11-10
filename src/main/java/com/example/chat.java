package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class chat {

    // Método que se encargará de abrir la ventana del chat
    public static void mostrarChat(JFrame parentFrame) {
        // Crear el JDialog
        JDialog chatDialog = new JDialog(parentFrame, "Chat", true);
        chatDialog.setLayout(new BorderLayout());
        chatDialog.setSize(500, 300);
        chatDialog.setLocationRelativeTo(parentFrame);

        // Crear el área de mensajes (solo lectura)
        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        chatDialog.add(scrollPane, BorderLayout.CENTER);

        // Crear el panel inferior con el campo de texto y el botón
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        // Crear el campo de texto para ingresar el mensaje
        JTextField messageField = new JTextField();
        bottomPanel.add(messageField, BorderLayout.CENTER); // El campo de texto ocupa el centro

        // Crear el botón de enviar (alineado a la derecha)
        JButton sendButton = new JButton("Enviar");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = messageField.getText();
                if (!message.isEmpty()) {
                    chatArea.append("Tú: " + message + "\n");
                    messageField.setText("");
                }
            }
        });
        bottomPanel.add(sendButton, BorderLayout.EAST); // Colocar el botón a la derecha

        chatDialog.add(bottomPanel, BorderLayout.SOUTH);

        // Panel lateral izquierdo
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS)); // Layout vertical para colocar varios elementos
        leftPanel.setBackground(Color.LIGHT_GRAY); // Opcional: para darle color al panel
        leftPanel.setPreferredSize(new Dimension(100, 0)); // Establecer un tamaño fijo de ancho para el panel

        chatDialog.add(leftPanel, BorderLayout.WEST);

        // Mostrar la ventana de chat
        chatDialog.setVisible(true);
    }
}
