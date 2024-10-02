package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class registre {
    private static List<Usuari> usuaris = new ArrayList<>(); // Lista de usuarios

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

        // Boto per crear usuari
        JButton btCrearUsuari = new JButton("Crear Usuari");
        btCrearUsuari.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nom = nomField.getText();
                String contrasenya = new String(contrasenyaField.getPassword());

                // Agregar el usuario a la lista
                usuaris.add(new Usuari(nom, contrasenya));
                JOptionPane.showMessageDialog(frame, "Usuari creat: " + nom);
                
                // Limpia los campos
                nomField.setText("");
                contrasenyaField.setText("");
            }
        });
        frame.add(btCrearUsuari);

        // Botón para iniciar sesión
        JButton btIniciarSessio = new JButton("Iniciar Sessió");
        btIniciarSessio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nom = nomField.getText();
                String contrasenya = new String(contrasenyaField.getPassword());

                // Verificar si el usuario existe
                boolean usuariValid = false;
                for (Usuari usuari : usuaris) {
                    if (usuari.getNom().equals(nom) && usuari.getContrasenya().equals(contrasenya)) {
                        usuariValid = true;
                        break;
                    }
                }

                if (usuariValid) {
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
}

// Clase para representar un usuario
class Usuari {
    private String nombre;
    private String contrasena;

    public Usuari(String nombre, String contrasena) {
        this.nombre = nombre;
        this.contrasena = contrasena;
    }

    public String getNom() {
        return nombre;
    }

    public String getContrasenya() {
        return contrasena;
    }
}
