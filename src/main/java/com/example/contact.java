package com.example;

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

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public static void main(String[] args) {
        List<contact> contactosOriginales = new ArrayList<>(); // Lista original completa
        contactosOriginales.add(new contact("Juan Pérez", "123456789"));
        contactosOriginales.add(new contact("Ana López", "987654321"));
        contactosOriginales.add(new contact("Carlos Ramírez", "555555555"));
        contactosOriginales.add(new contact("María García", "444444444"));
        contactosOriginales.add(new contact("José Fernández", "123123123"));
        contactosOriginales.add(new contact("Laura Martínez", "654654654"));
        contactosOriginales.add(new contact("Pedro Sánchez", "111111111"));
        contactosOriginales.add(new contact("Lucía Torres", "222222222"));
        contactosOriginales.add(new contact("Andrés González", "333333333"));
        contactosOriginales.add(new contact("Marta Díaz", "444555666"));
        contactosOriginales.add(new contact("Rosa Jiménez", "777888999"));

        List<contact> contactosFiltrados = new ArrayList<>(contactosOriginales);

        List<Grupo> grupos = new ArrayList<>(); // Lista de grupos

        JFrame frame = new JFrame("Agenda de Contactes");
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        Runnable actualizarLista = new Runnable() {
            public void run() {
                panel.removeAll();
                for (contact contacto : contactosFiltrados) {
                    JPanel contactoPanel = new JPanel();
                    contactoPanel.setLayout(new FlowLayout());

                    JLabel nombreLabel = new JLabel(contacto.getNombre() + " (" + contacto.getTelefono() + ")");
                    contactoPanel.add(nombreLabel);

                    JButton llamarButton = new JButton("Trucar");
                    llamarButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            JOptionPane.showMessageDialog(frame, "Tracant a " + contacto.getNombre());
                        }
                    });
                    contactoPanel.add(llamarButton);

                    panel.add(contactoPanel);
                }
                panel.revalidate();
                panel.repaint();
            }
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
                String filtro = buscador.getText().toLowerCase();
                contactosFiltrados.clear();

                if (filtro.isEmpty()) {
                    contactosFiltrados.addAll(contactosOriginales);
                } else {
                    for (contact contacto : contactosOriginales) {
                        if (contacto.getNombre().toLowerCase().contains(filtro) ||
                            contacto.getTelefono().contains(filtro)) {
                            contactosFiltrados.add(contacto);
                        }
                    }
                }
                actualizarLista.run();
            }
        });

        // Botón "Añadir Grupo"
        JButton btnAñadirGrupo = new JButton("Afegir Grup");
        btnAñadirGrupo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JDialog dialog = new JDialog(frame, "Crear Grup", true);
                dialog.setLayout(new BorderLayout());

                JTextField nombreGrupo = new JTextField("Nom del Grup");
                dialog.add(nombreGrupo, BorderLayout.NORTH);

                JPanel listaContactos = new JPanel();
                listaContactos.setLayout(new BoxLayout(listaContactos, BoxLayout.Y_AXIS));

                JCheckBox[] checkBoxes = new JCheckBox[contactosOriginales.size()];
                for (int i = 0; i < contactosOriginales.size(); i++) {
                    checkBoxes[i] = new JCheckBox(contactosOriginales.get(i).getNombre());
                    listaContactos.add(checkBoxes[i]);
                }

                dialog.add(new JScrollPane(listaContactos), BorderLayout.CENTER);

                JButton btnCrear = new JButton("Crear Grup");
                btnCrear.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        List<contact> grupo = new ArrayList<>();
                        for (int i = 0; i < checkBoxes.length; i++) {
                            if (checkBoxes[i].isSelected()) {
                                grupo.add(contactosOriginales.get(i));
                            }
                        }
                        if (!grupo.isEmpty()) {
                            grupos.add(new Grupo(nombreGrupo.getText(), grupo));
                            JOptionPane.showMessageDialog(dialog, "Grupo creado: " + nombreGrupo.getText());
                        } else {
                            JOptionPane.showMessageDialog(dialog, "No se han seleccionado contactos.");
                        }
                        dialog.dispose();
                    }
                });

                dialog.add(btnCrear, BorderLayout.SOUTH);
                dialog.setSize(400, 400);
                dialog.setVisible(true);
            }
        });

        // Botón "Grupos" para mostrar los grupos creados
        JButton btnGrupos = new JButton("Grups");
        btnGrupos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JDialog dialog = new JDialog(frame, "Grups", true);
                dialog.setLayout(new BorderLayout());

                JPanel listaGrupos = new JPanel();
                listaGrupos.setLayout(new BoxLayout(listaGrupos, BoxLayout.Y_AXIS));

                if (grupos.isEmpty()) {
                    listaGrupos.add(new JLabel("No hi han grups afegits."));
                } else {
                    for (Grupo grupo : grupos) {
                        JLabel nombreGrupo = new JLabel("Grup: " + grupo.getNombre());
                        listaGrupos.add(nombreGrupo);

                        for (contact contacto : grupo.getContactos()) {
                            JLabel contactoLabel = new JLabel(" - " + contacto.getNombre() + " (" + contacto.getTelefono() + ")");
                            listaGrupos.add(contactoLabel);
                        }
                    }
                }

                dialog.add(new JScrollPane(listaGrupos), BorderLayout.CENTER);
                dialog.setSize(400, 300);
                dialog.setVisible(true);
            }
        });

        // Panel superior con los botones y el buscador
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelSuperior.add(buscador);
        panelSuperior.add(btnAñadirGrupo);
        panelSuperior.add(btnGrupos);

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        frame.add(panelSuperior, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        actualizarLista.run();
    }
}

// Clase para representar los grupos
class Grupo {
    private String nombre;
    private List<contact> contactos;

    public Grupo(String nombre, List<contact> contactos) {
        this.nombre = nombre;
        this.contactos = contactos;
    }

    public String getNombre() {
        return nombre;
    }

    public List<contact> getContactos() {
        return contactos;
    }
}
