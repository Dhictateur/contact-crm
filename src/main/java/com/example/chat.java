package com.example;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class chat {

    private static List<Map<String, Object>> usuarios; 
    public static String mailUser;

    // Método que se encargará de abrir la ventana del chat
    public static void mostrarChat(JFrame parentFrame) {
        usuarios = odoo.obtenerUsuariosConLogin();
        JFrame chatFrame = new JFrame("Chat");
        chatFrame.setSize(600, 400);
        chatFrame.setLocationRelativeTo(parentFrame);
        chatFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel panelIzquierdo = new JPanel();
        panelIzquierdo.setLayout(new BoxLayout(panelIzquierdo, BoxLayout.Y_AXIS));
        panelIzquierdo.setPreferredSize(new Dimension(170, 400));

        JPanel panelDerecho = new JPanel(new BorderLayout());
        JTextArea areaEscritura = new JTextArea();
        areaEscritura.setLineWrap(true);
        areaEscritura.setWrapStyleWord(true);
        areaEscritura.setEditable(false);

        JButton botonEnviar = new JButton("Enviar");
        botonEnviar.setEnabled(false);

        JLabel lblMailPara = new JLabel("Selecciona un usuario para iniciar el chat", SwingConstants.CENTER);
        lblMailPara.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblMailPara.setPreferredSize(new Dimension(0, 30));

        botonEnviar.addActionListener(e -> {
            String mensaje = areaEscritura.getText().trim();
        if (!mensaje.isEmpty() && mailUser != null) {
            // Buscar el 'name' correspondiente al 'login' del usuario activo
            String remitenteNombre = "";
            for (Map<String, Object> usuario : usuarios) {
                String login = (String) usuario.get("login");
                if (registre.nombreUsuario.equals(login)) {
                    remitenteNombre = (String) usuario.get("name");
                    break;
                }
            }

            // Verificar si se encontró el 'name' del remitente
            if (remitenteNombre.isEmpty()) {
                JOptionPane.showMessageDialog(chatFrame, "No se pudo identificar el remitente.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Personalizar el asunto con el 'name' del remitente
            String asunto = "Correo de: " + remitenteNombre; 
            try {
                enviarCorreo(mailUser, asunto, mensaje);
                JOptionPane.showMessageDialog(chatFrame, "Correo enviado a: " + mailUser, "Envío exitoso", JOptionPane.INFORMATION_MESSAGE);
                areaEscritura.setText(""); // Limpiar el área de texto
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(chatFrame, "Error al enviar el correo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(chatFrame, "Selecciona un usuario y escribe un mensaje", "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
        });

        panelDerecho.add(lblMailPara, BorderLayout.NORTH);
        panelDerecho.add(new JScrollPane(areaEscritura), BorderLayout.CENTER);
        panelDerecho.add(botonEnviar, BorderLayout.SOUTH);

        mainPanel.add(panelDerecho, BorderLayout.CENTER);
        mainPanel.add(panelIzquierdo, BorderLayout.WEST);
        chatFrame.add(mainPanel);

        List<Map<String, Object>> usuarios = odoo.obtenerUsuariosConLogin();
        String usuarioActivo = registre.nombreUsuario;

        if (usuarios != null && !usuarios.isEmpty()) {
            for (Map<String, Object> usuario : usuarios) {
                String name = (String) usuario.get("name");
                String login = (String) usuario.get("login");

                if (usuarioActivo.equals(login)) continue;

                JButton btnUsuario = new JButton(name);
                btnUsuario.addActionListener(e -> {
                    mailUser = login;
                    areaEscritura.setEditable(true);
                    botonEnviar.setEnabled(true);
                    lblMailPara.setText("Mail para: " + name + " / " + login);
                });

                panelIzquierdo.add(btnUsuario);
            }
        } else {
            JLabel lblNoUsuarios = new JLabel("No hay usuarios disponibles.");
            lblNoUsuarios.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelIzquierdo.add(lblNoUsuarios);
        }

        chatFrame.setVisible(true);
    }

    // Método para enviar correos electrónicos
    public static void enviarCorreo(String destinatario, String asunto, String mensaje) throws MessagingException {
        String remitente = "mypersonalcrm.odoo@gmail.com"; // Tu correo
        String contraseña = "trkr lurg vmvv ogyw";     // Tu contraseña

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(remitente, contraseña);
            }
        });

        Message correo = new MimeMessage(session);
        correo.setFrom(new InternetAddress(remitente));
        correo.setRecipient(Message.RecipientType.TO, new InternetAddress(destinatario));
        correo.setSubject(asunto);
        correo.setText(mensaje);

        Transport.send(correo);
    }
}
