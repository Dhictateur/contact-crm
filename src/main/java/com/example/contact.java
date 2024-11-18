package com.example;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.*;

import org.apache.xmlrpc.XmlRpcException;

import java.awt.*;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class contact {
    private String nombre;
    private String telefono;
    public static String userType; // Variable para almacenar el tipo de usuario
    public static int LoginSuccess;

    private static List<contact> contactesOriginals = new ArrayList<>();
    private static List<contact> contactesFiltrats = new ArrayList<>();

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

    public static List<contact> getContactesOriginals() {
        return contactesOriginals;
    }

    public static void main(String[] args) throws MalformedURLException, XmlRpcException {
        odoo.inicializarXmlRpc();
        // Mostrar la ventana de registro/inicio de sesión
        registre.mostrarRegistre();
        LoginSuccess = 0;
    }

    public static void mostrarAgenda() {
        try {
            // Obtener contactos desde Odoo
            List<Map<String, Object>> contactosOdoo = odoo.obtenerContactos();
            
            // Convertir la lista de Map a una lista de objetos `contact`
            contactesOriginals = new ArrayList<>();
            for (Map<String, Object> datos : contactosOdoo) {
                String name = (String) datos.get("name");
                String phone = (String) datos.get("phone");
                contact contacto = new contact(name, phone);
                contactesOriginals.add(contacto);
            }
            
            // Clonar la lista original a la lista filtrada
            contactesFiltrats = new ArrayList<>(contactesOriginals);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al obtener contactos de Odoo");
            return;
        }

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
                    JOptionPane.showMessageDialog(frame, "Trucant a " + contacte.getNom());
                    log.registrarLlamada(contacte.getNom());
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

        // Crear un panel inferior
        JPanel panelInferior = new JPanel(new BorderLayout());
        JPanel panelDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JPanel panelIzquierda = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Botón "Modificar usuarios" (nuevo)
        JButton btnModificarUsuarios = new JButton("Modificar usuaris");
        btnModificarUsuarios.addActionListener(e -> {
            try {
                modificar.mostrarUsuarios(frame);
            } catch (MalformedURLException | SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });
        
        // Boton Logout
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
        panelInferior.add(btnLogout);

        // Botón "Chat"
        JButton btnChat = new JButton("Chat");
        btnChat.addActionListener(e -> {
            chat.mostrarChat(frame);  // 'frame' es el JFrame actual
        });

        panelInferior.add(btnChat);

        // Botón "Historial"
        JButton btnHistorial = new JButton("Historial");
        btnHistorial.addActionListener(e -> {
            JDialog historialDialog = new JDialog(frame, "Log", true);
            historialDialog.setSize(500, 300);
            historialDialog.setLocationRelativeTo(frame);
            historialDialog.setLayout(new BorderLayout());

            JTextArea historialArea = new JTextArea();
            historialArea.setEditable(false);
            
            // Obtener y mostrar el historial
            List<String> logEntries = log.obtenerHistorial();
            for (String entry : logEntries) {
                historialArea.append(entry + "\n");
            }
            
            historialDialog.add(new JScrollPane(historialArea), BorderLayout.CENTER);
            historialDialog.setVisible(true);
        });

        // Agregar el panel de logout al final del frame
        frame.add(panelInferior, BorderLayout.SOUTH);

        // Mostrar tipo de usuario en la parte inferior
        JLabel userTypeLabel = new JLabel(userType);
        panelIzquierda.add(userTypeLabel);
        panelIzquierda.add(btnModificarUsuarios);
        panelDerecha.add(btnChat);
        panelDerecha.add(btnHistorial);
        panelDerecha.add(btnLogout);

        // Agregar los subpaneles al panel inferior principal
        panelInferior.add(panelIzquierda, BorderLayout.WEST);
        panelInferior.add(panelDerecha, BorderLayout.EAST);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setVisible(true);

        // Actualizar la lista inicialmente
        actualizarLlista.run();
    }
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
