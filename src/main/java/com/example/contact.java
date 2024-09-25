package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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

        // Lista de contactos que se muestra (inicialmente la misma que contactosOriginales)
        List<contact> contactosFiltrados = new ArrayList<>(contactosOriginales);

        // Crear la ventana principal
        JFrame frame = new JFrame("Agenda de Contactos");
        frame.setLayout(new BorderLayout()); // Usar BorderLayout para posicionar los elementos

        // Crear un panel que contendrá la lista de contactos
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Organiza los contactos en columnas verticales

        // Función para actualizar la lista de contactos mostrada
        Runnable actualizarLista = new Runnable() {
            public void run() {
                panel.removeAll(); // Limpiar el panel de contactos
                for (contact contacto : contactosFiltrados) {
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
                panel.revalidate();
                panel.repaint();
            }
        };

        // Crear el campo de búsqueda
        JTextField buscador = new JTextField();
        buscador.setText("Buscador");
        buscador.setPreferredSize(new Dimension(400, 30)); // Tamaño del campo de búsqueda
        buscador.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String filtro = buscador.getText().toLowerCase();
                contactosFiltrados.clear(); // Limpiar la lista filtrada

                if (filtro.isEmpty()) {
                    // Si no hay filtro, mostrar todos los contactos
                    contactosFiltrados.addAll(contactosOriginales);
                } else {
                    // Filtrar contactos por nombre o teléfono
                    for (contact contacto : contactosOriginales) {
                        if (contacto.getNombre().toLowerCase().contains(filtro) ||
                            contacto.getTelefono().contains(filtro)) {
                            contactosFiltrados.add(contacto);
                        }
                    }
                }
                // Actualizar la lista de contactos con los contactos filtrados
                actualizarLista.run();
            }
        });

        // Crear un JScrollPane y añadir el panel de contactos dentro de él
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); // Siempre mostrar el scroll vertical

        // Añadir los componentes a la ventana
        frame.add(buscador, BorderLayout.NORTH); // Campo de búsqueda en la parte superior
        frame.add(scrollPane, BorderLayout.CENTER); // Lista de contactos con scroll en el centro

        frame.setSize(600, 600); // Tamaño de la ventana
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Cerrar la aplicación al salir
        frame.setVisible(true); // Hacer visible la ventana

        // Mostrar la lista inicial de contactos
        actualizarLista.run();
    }
}
