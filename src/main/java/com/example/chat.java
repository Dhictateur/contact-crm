package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

public class chat {

    public static String mailUser;

    // Método que se encargará de abrir la ventana del chat
    public static void mostrarChat(JFrame parentFrame) {
        // Crear una nueva ventana (JFrame) para el chat
        JFrame chatFrame = new JFrame("Chat");

        // Configurar el tamaño, posición y comportamiento de la ventana
        chatFrame.setSize(600, 400);
        chatFrame.setLocationRelativeTo(parentFrame); // Centrar respecto a la ventana principal
        chatFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cerrar solo esta ventana

        // Crear un contenedor principal con BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Crear el panel izquierdo para la lista de usuarios
        JPanel panelIzquierdo = new JPanel();
        panelIzquierdo.setLayout(new BoxLayout(panelIzquierdo, BoxLayout.Y_AXIS));
        panelIzquierdo.setPreferredSize(new Dimension(170, 400)); // Ancho fijo para el panel izquierdo

        // Obtener la lista de usuarios de Odoo
        List<Map<String, Object>> usuarios = odoo.obtenerUsuariosConLogin();

        // Obtener el login del usuario activo
        String usuarioActivo = registre.nombreUsuario;

        // Crear el panel derecho para el área de escritura
        JPanel panelDerecho = new JPanel(new BorderLayout());

        // Crear un área de texto grande para escribir
        JTextArea areaEscritura = new JTextArea();
        areaEscritura.setLineWrap(true);
        areaEscritura.setWrapStyleWord(true);
        areaEscritura.setEditable(false); // Inicialmente no editable

        // Crear un botón "Enviar" en la parte inferior
        JButton botonEnviar = new JButton("Enviar");
        botonEnviar.setEnabled(false); // Inicialmente deshabilitado

        // Crear un JLabel para mostrar el mensaje de "Mail para: (name user / login)"
        JLabel lblMailPara = new JLabel("Selecciona un usuario para iniciar el chat", SwingConstants.CENTER);
        lblMailPara.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblMailPara.setPreferredSize(new Dimension(0, 30)); // Altura fija para el JLabel

        // Acción del botón enviar
        botonEnviar.addActionListener(e -> {
            String mensaje = areaEscritura.getText().trim();
            if (!mensaje.isEmpty()) {
                // Lógica para manejar el mensaje enviado
                JOptionPane.showMessageDialog(chatFrame, "Mensaje enviado: " + mensaje, "Envío de Mensaje", JOptionPane.INFORMATION_MESSAGE);
                areaEscritura.setText(""); // Limpiar el área de texto
            }
        });

        // Agregar el JLabel de "Mail para: ..." y el área de escritura al panel derecho
        panelDerecho.add(lblMailPara, BorderLayout.NORTH);
        panelDerecho.add(new JScrollPane(areaEscritura), BorderLayout.CENTER);
        panelDerecho.add(botonEnviar, BorderLayout.SOUTH);

        // Agregar el panel derecho al contenedor principal
        mainPanel.add(panelDerecho, BorderLayout.CENTER);

        // Agregar el panel izquierdo al contenedor principal
        mainPanel.add(panelIzquierdo, BorderLayout.WEST);

        // Agregar el contenedor principal a la ventana
        chatFrame.add(mainPanel);

        // Obtener la lista de usuarios de Odoo
        if (usuarios != null && !usuarios.isEmpty()) {
            for (Map<String, Object> usuario : usuarios) {
                String name = (String) usuario.get("name");
                String phone = (String) usuario.get("phone");
                String login = (String) usuario.get("login");

                // Excluir al usuario que ha iniciado sesión
                if (usuarioActivo.equals(login)) {
                    continue; // Saltar este usuario
                }

                // Crear un botón para cada usuario
                JButton btnUsuario = new JButton(name);
                btnUsuario.setAlignmentX(Component.CENTER_ALIGNMENT); // Centrar el botón
                btnUsuario.addActionListener(e -> {
                    // Guardar el login del usuario clicado en la variable mailUser
                    mailUser = login;
                    // Habilitar el área de escritura y el botón de enviar
                    areaEscritura.setEditable(true);
                    botonEnviar.setEnabled(true);
                    // Actualizar el JLabel con el nombre y login del usuario seleccionado
                    lblMailPara.setText("Mail para: " + name + " / " + login);
                    // Imprimir en consola el valor de mailUser para verificar
                    System.out.println("Login seleccionado: " + mailUser);
                });

                // Añadir el botón al panel izquierdo
                panelIzquierdo.add(btnUsuario);
            }
        } else {
            // Mostrar un mensaje si no hay usuarios
            JLabel lblNoUsuarios = new JLabel("No hay usuarios disponibles.");
            lblNoUsuarios.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelIzquierdo.add(lblNoUsuarios);
        }

        // Hacer la ventana visible
        chatFrame.setVisible(true);
    }
}
