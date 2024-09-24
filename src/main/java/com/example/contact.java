package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class contact {
    private String nombre;
    private String telefono;

    // Constructor de la clase Contact
    public contact(String nombre, String telefono) {
        this.nombre = nombre;
        this.telefono = telefono;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public static void main(String[] args) {
        // Crear la lista de contactos simulada con más contactos para verificar el scroll
        List<contact> contactos = new ArrayList<>();
        contactos.add(new contact("Juan Pérez", "123456789"));
        contactos.add(new contact("Ana López", "987654321"));
        contactos.add(new contact("Carlos Ramírez", "555555555"));
        contactos.add(new contact("María García", "444444444"));
        contactos.add(new contact("José Fernández", "123123123"));
        contactos.add(new contact("Laura Martínez", "654654654"));
        contactos.add(new contact("Pedro Sánchez", "111111111"));
        contactos.add(new contact("Lucía Torres", "222222222"));
        contactos.add(new contact("Andrés González", "333333333"));
        contactos.add(new contact("Marta Díaz", "444555666"));
        contactos.add(new contact("Rosa Jiménez", "777888999"));

        // Crear la ventana principal
        JFrame frame = new JFrame("Agenda de Contactos");

        // Crear un panel que contendrá la lista de contactos
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Organiza los contactos en columnas verticales

        // Crear la lista de contactos con un botón "Llamar" al lado de cada uno
        for (contact contacto : contactos) {
            JPanel contactoPanel = new JPanel();
            contactoPanel.setLayout(new FlowLayout());

            // Etiqueta con el nombre y teléfono del contacto
            JLabel nombreLabel = new JLabel(contacto.getNombre() + " (" + contacto.getTelefono() + ")");
            contactoPanel.add(nombreLabel);

            // Botón "Llamar" (no funcional por ahora)
            JButton llamarButton = new JButton("Llamar");
            llamarButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Muestra un mensaje simulado cuando se pulsa el botón "Llamar"
                    JOptionPane.showMessageDialog(frame, "Llamando a " + contacto.getNombre());
                }
            });
            contactoPanel.add(llamarButton);

            // Agregar el panel del contacto al panel principal
            panel.add(contactoPanel);
        }

        // Crear un JScrollPane y añadir el panel de contactos dentro de él
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); // Siempre mostrar el scroll vertical

        // Añadir el JScrollPane a la ventana
        frame.add(scrollPane);

        frame.setSize(400, 400); // Tamaño de la ventana
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Cerrar la aplicación al salir
        frame.setVisible(true); // Hacer visible la ventana
    }
}
