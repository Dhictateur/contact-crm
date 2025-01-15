package com.example;

import javax.swing.*;

import org.apache.xmlrpc.XmlRpcException;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class crearContacte {
    
    public static void creacioContacte(JFrame parentFrame) {
        // Crear el panel donde se ingresarán los datos del contacto
        JDialog dialog = new JDialog(parentFrame, "Crear Contacto", true);
        dialog.setSize(300, 250);
        dialog.setLayout(new GridLayout(5, 1));
        dialog.setLocationRelativeTo(parentFrame);

        // Campos de entrada
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();

        // Etiquetas y campos
        dialog.add(new JLabel("Nombre:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Email:"));
        dialog.add(emailField);
        dialog.add(new JLabel("Teléfono:"));
        dialog.add(phoneField);

        // Botón Crear
        JButton createButton = new JButton("Crear");
        dialog.add(createButton);

        // Acción del botón Crear
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nombre = nameField.getText();
                String email = emailField.getText();
                String telefono = phoneField.getText();

                try {
                    odoo.crearContacto(registre.id_odoo, nombre, telefono, email);
                } catch (XmlRpcException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                // Mostrar mensaje de confirmación
                JOptionPane.showMessageDialog(dialog, "Contacto creado con éxito.");
                log.registrarContacto(nombre, telefono);
                dialog.dispose();
            }
        });

        // Mostrar el diálogo
        dialog.setVisible(true);
    }
}
