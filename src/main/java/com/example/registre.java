package com.example;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import java.net.URL;

public class registre {

    private static Object uid;  // Almacena el UID del usuario autenticado
    private static JFrame loginFrame;  // Ventana de login para reutilizar

    // Método para mostrar la interfaz de inicio de sesión
    public static void mostrarRegistre() {
        // Si ya existe la ventana, simplemente la hacemos visible
        if (loginFrame != null) {
            loginFrame.setVisible(true);
            return;
        }

        // Crear la ventana solo si no existe
        loginFrame = new JFrame("Iniciar Sesión");
        loginFrame.setLayout(new GridLayout(3, 2));

        // Etiquetas y campos de texto
        JLabel nomLabel = new JLabel("Nombre:");
        JTextField nomField = new JTextField();
        JLabel contrasenyaLabel = new JLabel("Contraseña:");
        JPasswordField contrasenyaField = new JPasswordField();

        loginFrame.add(nomLabel);
        loginFrame.add(nomField);
        loginFrame.add(contrasenyaLabel);
        loginFrame.add(contrasenyaField);

        // Botón para iniciar sesión
        JButton btIniciarSessio = new JButton("Iniciar Sesión");
        btIniciarSessio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nom = nomField.getText();
                String contrasenya = new String(contrasenyaField.getPassword());

                // Validar campos vacíos
                if (nom.isEmpty() || contrasenya.isEmpty()) {
                    JOptionPane.showMessageDialog(loginFrame, "Por favor, complete todos los campos.");
                    return;
                }

                // Verificar si el usuario existe en Odoo usando XML-RPC
                if (verificarUsuarioEnOdoo(nom, contrasenya)) {
                    JOptionPane.showMessageDialog(loginFrame, "Iniciando sesión como: " + nom);
                    loginFrame.setVisible(false); // Ocultar la ventana de login en lugar de eliminarla
                    contact.LoginSuccess = 1;
                    if (contact.LoginSuccess == 1) { 
                        Connection conexion = null;
                        try {
                            // Aquí probamos la conexión
                            conexion = odoo.conectar();
                            // Verificar el tipo de usuario después de iniciar sesión
                            contact.userType = verificarTipoUsuario(conexion, nom); // Cambia "nombre_user" por el nombre del usuario actual
                        } catch (SQLException m) {
                            System.out.println("Error al conectar a la base de datos: " + m.getMessage());
                            m.printStackTrace();
                            return;  // Termina el programa si no se puede conectar
                        }
                    }
                    contact.mostrarAgenda(); // Llamar a la interfaz de contacto
                } else {
                    JOptionPane.showMessageDialog(loginFrame, "Credenciales incorrectas.");
                }

                // Limpiar campos
                nomField.setText("");
                contrasenyaField.setText("");
            }
        });

        loginFrame.add(btIniciarSessio);

        // Configuración del marco
        loginFrame.setSize(300, 150);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setVisible(true); // Mostrar la ventana por primera vez
    }

// Método para verificar si el usuario tiene permisos de administrador
    private static String verificarTipoUsuario(Connection conexion, String userName) {
        String tipoUsuario = "User"; // Por defecto, es un usuario normal
        String sql = "SELECT u.login AS user_name, g.name AS group_name " +
                    "FROM res_users u " +
                    "JOIN res_groups_users_rel gu ON u.id = gu.uid " +
                    "JOIN res_groups g ON gu.gid = g.id " +
                    "WHERE g.name = '{\"en_US\": \"Settings\"}' AND u.login = '" + userName + "'";

        try (Statement stmt = conexion.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                tipoUsuario = "Admin"; // Si se encuentra un resultado, el usuario es administrador
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(tipoUsuario);
        return tipoUsuario;
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

    // Método para cerrar sesión en Odoo
    public static void cerrarSesion() {
        try {
            // Invalidar el UID (usuario autenticado) para simular cierre de sesión
            uid = null;

            // Imprimir que se ha cerrado la sesión
            System.out.println("Sesión cerrada.");

            // Ocultar la ventana actual (en este caso, la ventana de agenda)
            JFrame frameActual = (JFrame) javax.swing.FocusManager.getCurrentManager().getActiveWindow();
            if (frameActual != null) {
                frameActual.setVisible(false); // Ocultar la ventana actual
            }

            // Volver a mostrar la ventana de inicio de sesión
            mostrarRegistre();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}