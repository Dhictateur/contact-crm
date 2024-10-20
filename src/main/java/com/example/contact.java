package com.example;

import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class contact {
    private String nombre;
    private String telefono;

    public contact(String nombre, String telefono) {
        this.nombre = nombre;
        this.telefono = telefono;
    }

    public String getNom() {
        return nombre;
    }

    public String getTelefon() {
        return telefono;
    }

    public static void main(String[] args) {
        // Mostrar la ventana de registro/inicio de sesión
        registre.mostrarRegistre();

        // Intentar conectar a la base de datos de Odoo
        Connection conexion = null;
        try {
            // Aquí probamos la conexión
            conexion = odoo.conectar();
        } catch (SQLException e) {
            System.out.println("Error al conectar a la base de datos: " + e.getMessage());
            e.printStackTrace();
            return;  // Termina el programa si no se puede conectar
        }
    }

    public static void mostrarAgenda() {
        // Lista original de contactos
        List<contact> contactesOriginals = new ArrayList<>();
        contactesOriginals.add(new contact("Juan Pérez", "123456789"));
        contactesOriginals.add(new contact("Ana López", "987654321"));
        contactesOriginals.add(new contact("Carlos Ramírez", "555555555"));
        contactesOriginals.add(new contact("María García", "444444444"));
        contactesOriginals.add(new contact("José Fernández", "123123123"));
        contactesOriginals.add(new contact("Laura Martínez", "654654654"));
        contactesOriginals.add(new contact("Pedro Sánchez", "111111111"));
        contactesOriginals.add(new contact("Lucía Torres", "222222222"));
        contactesOriginals.add(new contact("Andrés González", "333333333"));
        contactesOriginals.add(new contact("Marta Díaz", "444555666"));
        contactesOriginals.add(new contact("Rosa Jiménez", "777888999"));

        List<contact> contactesFiltrats = new ArrayList<>(contactesOriginals);
        List<Grup> grups = new ArrayList<>();

        JFrame frame = new JFrame("Agenda de Contactes");
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        Runnable actualizarLlista = () -> {
            panel.removeAll();
            for (contact contacte : contactesFiltrats) {
                JPanel contactePanel = new JPanel();
                contactePanel.setLayout(new FlowLayout());

                JLabel nomLabel = new JLabel(contacte.getNom() + " (" + contacte.getTelefon() + ")");
                contactePanel.add(nomLabel);

                JButton trucarButton = new JButton("Trucar");
                trucarButton.addActionListener(e -> {
                    JOptionPane.showMessageDialog(frame, "Tracant a " + contacte.getNom());
                });
                contactePanel.add(trucarButton);

                panel.add(contactePanel);
            }
            panel.revalidate();
            panel.repaint();
        };

        JTextField buscador = new JTextField();
        buscador.setText("Buscador");
        buscador.setPreferredSize(new Dimension(250, 30));
        buscador.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (buscador.getText().equals("Buscador")) {
                    buscador.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (buscador.getText().isEmpty()) {
                    buscador.setText("Buscador");
                }
            }
        });
        buscador.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String filtre = buscador.getText().toLowerCase();
                contactesFiltrats.clear();

                if (filtre.isEmpty()) {
                    contactesFiltrats.addAll(contactesOriginals);
                } else {
                    for (contact contacte : contactesOriginals) {
                        if (contacte.getNom().toLowerCase().contains(filtre) ||
                                contacte.getTelefon().contains(filtre)) {
                            contactesFiltrats.add(contacte);
                        }
                    }
                }
                actualizarLlista.run();
            }
        });

        // Boto "Afegir Grupo"
        JButton btAfegirGrup = new JButton("Afegir Grup");
        btAfegirGrup.addActionListener(e -> {
            JDialog dialog = new JDialog(frame, "Crear Grup", true);
            dialog.setLayout(new BorderLayout());

            JTextField nomGrup = new JTextField("Nom del Grup");
            dialog.add(nomGrup, BorderLayout.NORTH);

            JPanel llistaContactes = new JPanel();
            llistaContactes.setLayout(new BoxLayout(llistaContactes, BoxLayout.Y_AXIS));

            JCheckBox[] checkBoxes = new JCheckBox[contactesOriginals.size()];
            for (int i = 0; i < contactesOriginals.size(); i++) {
                checkBoxes[i] = new JCheckBox(contactesOriginals.get(i).getNom());
                llistaContactes.add(checkBoxes[i]);
            }

            dialog.add(new JScrollPane(llistaContactes), BorderLayout.CENTER);

            JButton btCrear = new JButton("Crear Grup");
            btCrear.addActionListener(e1 -> {
                List<contact> grup = new ArrayList<>();
                for (int i = 0; i < checkBoxes.length; i++) {
                    if (checkBoxes[i].isSelected()) {
                        grup.add(contactesOriginals.get(i));
                    }
                }
                if (!grup.isEmpty()) {
                    grups.add(new Grup(nomGrup.getText(), grup));
                    JOptionPane.showMessageDialog(dialog, "Grupo creado: " + nomGrup.getText());
                } else {
                    JOptionPane.showMessageDialog(dialog, "No se han seleccionado contactos.");
                }
                dialog.dispose();
            });

            dialog.add(btCrear, BorderLayout.SOUTH);
            dialog.setSize(400, 400);
            dialog.setVisible(true);
        });

        // Botón "Grupos"
        JButton btnGrupos = new JButton("Grups");
        btnGrupos.addActionListener(e -> {
            JDialog dialog = new JDialog(frame, "Grups", true);
            dialog.setLayout(new BorderLayout());

            JPanel llistaGrups = new JPanel();
            llistaGrups.setLayout(new BoxLayout(llistaGrups, BoxLayout.Y_AXIS));

            if (grups.isEmpty()) {
                llistaGrups.add(new JLabel("No hi han grups afegits."));
            } else {
                for (Grup grup : grups) {
                    JLabel nomGrup = new JLabel("Grup: " + grup.getNom());
                    llistaGrups.add(nomGrup);

                    for (contact contacte : grup.getContactes()) {
                        JLabel contacteLabel = new JLabel(" - " + contacte.getNom() + " (" + contacte.getTelefon() + ")");
                        llistaGrups.add(contacteLabel);
                    }
                }
            }

            dialog.add(new JScrollPane(llistaGrups), BorderLayout.CENTER);
            dialog.setSize(400, 300);
            dialog.setVisible(true);
        });

        // Panel superior
        JPanel panellSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panellSuperior.add(buscador);
        panellSuperior.add(btAfegirGrup);
        panellSuperior.add(btnGrupos);

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        frame.add(panellSuperior, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Crear un panel para el botón "Logout"
        JPanel panelLogout = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> {
            // Cerrar sesión en la API de Odoo (si fuese necesario)
            try {
                registre.cerrarSesion();  // Método que puedes implementar en odoo.java
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // Cerrar la ventana actual de la agenda
            frame.dispose();

            // Volver a la pantalla de registro/inicio de sesión
            registre.mostrarRegistre();
        });
        panelLogout.add(btnLogout);

        // Agregar el panel de logout al final del frame
        frame.add(panelLogout, BorderLayout.SOUTH);

        // Asegúrate de que el tamaño del frame es adecuado
        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Llamar al método para actualizar la lista
        actualizarLlista.run();

        // Revalidar y repintar el frame
        frame.revalidate();
        frame.repaint();
    }

    // Clase para representar los grupos
    static class Grup {
        private String nom;
        private List<contact> contactes;

        public Grup(String nom, List<contact> contactes) {
            this.nom = nom;
            this.contactes = contactes;
        }

        public String getNom() {
            return nom;
        }

        public List<contact> getContactes() {
            return contactes;
        }
    }
}