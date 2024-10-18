package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import java.net.URL;

public class registre {

    private static Object uid;  // Almacena el UID del usuario autenticado

    // Método para mostrar la interfaz de inicio de sesión
    public static void mostrarRegistre() {
        JFrame frame = new JFrame("Iniciar Sesión");
        frame.setLayout(new GridLayout(3, 2));

        // Etiquetas y campos de texto
        JLabel nomLabel = new JLabel("Nombre:");
        JTextField nomField = new JTextField();
        JLabel contrasenyaLabel = new JLabel("Contraseña:");
        JPasswordField contrasenyaField = new JPasswordField();

        frame.add(nomLabel);
        frame.add(nomField);
        frame.add(contrasenyaLabel);
        frame.add(contrasenyaField);

        // Botón para iniciar sesión
        JButton btIniciarSessio = new JButton("Iniciar Sesión");
        btIniciarSessio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nom = nomField.getText();
                String contrasenya = new String(contrasenyaField.getPassword());

                // Validar campos vacíos
                if (nom.isEmpty() || contrasenya.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Por favor, complete todos los campos.");
                    return;
                }

                // Verificar si el usuario existe en Odoo usando XML-RPC
                if (verificarUsuarioEnOdoo(nom, contrasenya)) {
                    JOptionPane.showMessageDialog(frame, "Iniciando sesión como: " + nom);
                    frame.dispose(); // Cerrar la ventana de inicio de sesión
                    contact.mostrarAgenda(); // Llamar a la interfaz de contacto
                } else {
                    JOptionPane.showMessageDialog(frame, "Credenciales incorrectas.");
                }

                // Limpiar campos
                nomField.setText("");
                contrasenyaField.setText("");
            }
        });

        frame.add(btIniciarSessio);

        // Configuración del marco
        frame.setSize(300, 150);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    // Método para verificar el usuario en Odoo usando XML-RPC
    public static boolean verificarUsuarioEnOdoo(String nombre, String contrasenya) {
        try {
            // Configuración del cliente XML-RPC
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL("http://localhost:8069/xmlrpc/2/common"));  // URL del servidor Odoo
            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);

            // Llamada a la función 'authenticate' de Odoo
            Object[] params = new Object[]{
                    "test",         // Nombre de la base de datos de Odoo
                    nombre,         // Nombre de usuario
                    contrasenya,    // Contraseña
                    new HashMap<>() // Sin contexto adicional
            };

            // Autenticar usuario
            uid = client.execute("authenticate", params);

            // Si el UID no es null, la autenticación fue exitosa
            return uid != null;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Método para cerrar sesión en Odoo (puede que necesites implementar más lógica dependiendo de tu caso)
    public static void cerrarSesion() {
        try {
            // Invalidar el UID (usuario autenticado) para simular cierre de sesión
            uid = null;

            // Imprimir que se ha cerrado la sesión
            System.out.println("Sesión cerrada.");

            // Si tienes un JFrame o ventana abierta, aquí podrías cerrarla y volver a mostrar la ventana de inicio de sesión
            javax.swing.SwingUtilities.invokeLater(() -> {
                // Cerrar ventana actual
                JFrame frameActual = (JFrame) javax.swing.FocusManager.getCurrentManager().getActiveWindow();
                if (frameActual != null) {
                    frameActual.dispose(); // Cierra la ventana actual
                }

                // Mostrar la ventana de inicio de sesión
                registre.mostrarRegistre();
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
