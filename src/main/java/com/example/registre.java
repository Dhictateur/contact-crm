package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class registre {
    // Método para mostrar la interfaz de registro/inicio de sesión
    public static void mostrarRegistre() {
        JFrame frame = new JFrame("Registre/Iniciar Sessió");
        frame.setLayout(new GridLayout(4, 2));

        // Etiquetas y campos de texto
        frame.add(new JLabel("Nom:"));
        JTextField nomField = new JTextField();
        frame.add(nomField);

        frame.add(new JLabel("Contrasenya:"));
        JPasswordField contrasenyaField = new JPasswordField();
        frame.add(contrasenyaField);

        // Botón para iniciar sesión
        JButton btIniciarSessio = new JButton("Iniciar Sessió");
        btIniciarSessio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nom = nomField.getText();
                String contrasenya = new String(contrasenyaField.getPassword());

                // Verificar si el usuario existe en la base de datos
                if (verificarUsuarioEnBaseDeDatos(nom, contrasenya)) {
                    JOptionPane.showMessageDialog(frame, "Iniciando sesión como: " + nom);
                    frame.dispose(); // Cerrar la ventana de registro
                    contact.mostrarAgenda(); // Llamar a la interfaz de contacto
                } else {
                    JOptionPane.showMessageDialog(frame, "Credenciales incorrectas.");
                }

                // Limpia los campos
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

    // Método para verificar el usuario en la base de datos
    public static boolean verificarUsuarioEnBaseDeDatos(String nombre, String contrasena) {
        String url = "jdbc:postgresql://localhost:5432/test"; // URL de la base de datos
        String user = "odoo"; // Usuario de la base de datos
        String password = "1234"; // Contraseña del usuario de la base de datos

        String query = "SELECT * FROM test_users WHERE nombre = ? AND pass = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, nombre);
            pstmt.setString(2, contrasena);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return true; // Usuario encontrado
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false; // Usuario no encontrado o error
    }
}
