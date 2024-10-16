package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import java.net.URL;

public class registre {

    // Método para mostrar la interfaz de registro/inicio de sesión
    public static void mostrarRegistre() {
        JFrame frame = new JFrame("Registre/Iniciar Sessió");
        frame.setLayout(new GridLayout(5, 2));

        // Etiquetas y campos de texto
        frame.add(new JLabel("Nom:"));
        JTextField nomField = new JTextField();
        frame.add(nomField);

        frame.add(new JLabel("Contrasenya:"));
        JPasswordField contrasenyaField = new JPasswordField();
        frame.add(contrasenyaField);

        // Botón para registrar usuario (pendiente de implementar con XML-RPC si fuera necesario)
        JButton btRegistrarUsuari = new JButton("Registrar Usuari");
        frame.add(btRegistrarUsuari);

        // Botón para iniciar sesión
        JButton btIniciarSessio = new JButton("Iniciar Sessió");
        btIniciarSessio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nom = nomField.getText();
                String contrasenya = new String(contrasenyaField.getPassword());

                // Verificar si el usuario existe en Odoo usando XML-RPC
                if (verificarUsuarioEnOdoo(nom, contrasenya)) {
                    JOptionPane.showMessageDialog(frame, "Iniciando sesión como: " + nom);
                    frame.dispose(); // Cerrar la ventana de registro
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
        frame.setSize(300, 200);
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
                    "test", // Nombre de la base de datos de Odoo
                    nombre,         // Nombre de usuario
                    contrasenya,    // Contraseña
                    new HashMap<>() // Sin contexto adicional
            };

            // Autenticar usuario
            Object uid = client.execute("authenticate", params);

            // Si el UID no es null, la autenticación fue exitosa
            return uid != null;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
