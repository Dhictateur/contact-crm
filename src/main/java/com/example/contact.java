package com.example;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.*;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class contact {
    private int id;
    private String nombre;
    private String telefono;
    public static String userType; // Variable para almacenar el tipo de usuario
    public static int LoginSuccess;

    private static List<contact> contactesOriginals = new ArrayList<>();
    private static List<contact> contactesFiltrats = new ArrayList<>();
    public static List<contact> contactesFiltratsBuscador = new ArrayList<>();

    public contact(Integer id, String nombre, String telefono) {
        this.id = id;
        this.nombre = nombre; 
        this.telefono = telefono;
    }

    public int getId() {
        return id;
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



    public static void mostrarAgenda() throws MalformedURLException {

        Connection conexion = null;
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(odoo.ODOO_URL + "xmlrpc/2/object"));
        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);
        
        try {
            // Paso 1: Obtener el ID del usuario actual usando `registre.nombreUsuario`
            String usuarioActual = registre.nombreUsuario;
            System.out.println(usuarioActual);
            Integer usuarioId = null;
            
            // Buscar el usuario actual en la lista de usuarios
            List<Map<String, Object>> usuarios = odoo.obtenerUsuariosConLogin();
            for (Map<String, Object> usuario : usuarios) {
                String login = (String) usuario.get("login");
                if (usuarioActual.equals(login)) {
                    usuarioId = (Integer) usuario.get("id"); // Asegúrate de que estás obteniendo el campo `id` correctamente
                    break;
                }
            }
    
            if (usuarioId == null) {
                JOptionPane.showMessageDialog(null, "No se encontró el ID del usuario actual.");
                return;
            }
    
            // Paso 2: Obtener los contactos desde Odoo
            List<Map<String, Object>> contactosOdoo = odoo.obtenerContactos();
    
            // Paso 3: Filtrar los contactos según el `owner_id`
            contactesOriginals = new ArrayList<>();
            for (Map<String, Object> datos : contactosOdoo) {
                Integer contactId = (Integer) datos.get("id");
                Integer ownerId = (Integer) datos.get("owner_id");
                if (ownerId != null && ownerId.equals(usuarioId)) {
                    String name = (String) datos.get("name");
                    String phone = (String) datos.get("phone");
                    contact contacto = new contact(contactId, name, phone);
                    contactesOriginals.add(contacto);
                }
            }
    
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al obtener contactos de Odoo");
        }

        contactesFiltrats = new ArrayList<>(contactesOriginals);
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

                // Boton de trucar
                JButton trucarButton = new JButton("Trucar");
                trucarButton.addActionListener(e -> {
                    JOptionPane.showMessageDialog(frame, "Trucant a " + contacte.getNom());
                    log.registrarLlamada(contacte.getNom());
                });
                contactePanel.add(trucarButton);

                // Botón "Editar"
                JButton editarButton = new JButton("Editar");
                editarButton.addActionListener(e -> {
                    // Obtener los datos del contacto
                    int contactId = contacte.getId();  // Asegúrate de que 'contacte' tiene los datos correctos
                    String nombre = contacte.getNom();
                    String telefono = contacte.getTelefon();

                    // Crear un cuadro de diálogo para editar el contacto
                    JDialog editarDialog = new JDialog(frame, "Editar Contacto", true);
                    editarDialog.setLayout(new GridLayout(3, 2));

                    // Campos para editar nombre y teléfono
                    JTextField nombreField = new JTextField(nombre);
                    JTextField telefonoField = new JTextField(telefono);

                    editarDialog.add(new JLabel("Nombre:"));
                    editarDialog.add(nombreField);

                    editarDialog.add(new JLabel("Teléfono:"));
                    editarDialog.add(telefonoField);

                    // Botón para guardar los cambios
                    JButton guardarButton = new JButton("Guardar");
                    guardarButton.addActionListener(e1 -> {
                        String nuevoNombre = nombreField.getText();
                        String nuevoTelefono = telefonoField.getText();

                        // Validar datos
                        if (nuevoNombre.isEmpty() || nuevoTelefono.isEmpty()) {
                            JOptionPane.showMessageDialog(editarDialog, "Los campos no pueden estar vacíos.");
                            return;
                        }

                        try {
                            System.out.println(contactId);
                            // Crear parámetros para la llamada XML-RPC
                            Map<String, Object> values = new HashMap<>();
                            values.put("name", nuevoNombre);
                            values.put("phone", nuevoTelefono);

                            // Ejecutar la llamada XML-RPC para actualizar el contacto
                            List<Object> writeParams = Arrays.asList(
                                odoo.db, registre.id_odoo, registre.pass,
                                "res.partner", "write",
                                Arrays.asList(
                                    Arrays.asList(contactId), // ID del contacto a actualizar
                                    values // Los nuevos valores a escribir
                                )
                            );

                            boolean success = (boolean) client.execute("execute_kw", writeParams);
                            if (success) {
                                JOptionPane.showMessageDialog(editarDialog, "Datos del contacto actualizados correctamente.");
                                editarDialog.dispose(); // Cerrar la ventana
                            } else {
                                JOptionPane.showMessageDialog(editarDialog, "Error al actualizar los datos del contacto.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(editarDialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    });

                    editarDialog.add(new JLabel());  // Filler to align the button properly
                    editarDialog.add(guardarButton);

                    editarDialog.pack();
                    editarDialog.setLocationRelativeTo(frame);
                    editarDialog.setVisible(true);
                });
                contactePanel.add(editarButton);

                // Botón "X" para eliminar
                int contactId = contacte.getId();
                JButton eliminarButton = new JButton("X");
                eliminarButton.addActionListener(e -> {
                    System.out.println("Contacto a eliminar: " + contactId);
                    // Muestra un mensaje de confirmación
                    JOptionPane.showMessageDialog(
                        null, // Ventana principal (null usa el centro de la pantalla)
                        "Contacto eliminado correctamente", // Mensaje
                        "Eliminación exitosa", // Título del cuadro de diálogo
                        JOptionPane.INFORMATION_MESSAGE // Tipo de mensaje
                    );
                    odoo.eliminarContactOdoo(contactId);
                });
                contactePanel.add(eliminarButton);

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

        //Boton Calendario
        JButton btnCalendario = new JButton("Calendari");
        btnCalendario.addActionListener(e -> {
            try {
                calendari.mostrarCalendari(frame, registre.nombreUsuario);
            } catch (MalformedURLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            System.out.println(registre.nombreUsuario);
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
        panellSuperior.add(btnCalendario);

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        frame.add(panellSuperior, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel panelSecundario = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JPanel panelSuperiorContenedor = new JPanel(new BorderLayout());
        panelSuperiorContenedor.add(panellSuperior, BorderLayout.NORTH);
        panelSuperiorContenedor.add(panelSecundario, BorderLayout.WEST);

        // Añadir los contenedores al frame
        frame.add(panelSuperiorContenedor, BorderLayout.NORTH);
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

        //Botón "Crear Contacto"
        JButton btnCrearContacto = new JButton("Crear Contacto");
        btnCrearContacto.addActionListener(e -> {
            crearContacte.creacioContacte(frame);
        });

        panelSecundario.add(btnCrearContacto);

        //Botón "Actualizar Lista"
        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> {
            try {
                actualizarContactes(); // Recarga la lista de contactos
                actualizarLlista.run(); // Refresca la interfaz con la nueva lista
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error al actualizar la lista de contactos.");
            }
        });

        panelSecundario.add(btnActualizar);

        // Botón "Historial"
        JButton btnHistorial = new JButton("Historial");
        btnHistorial.addActionListener(e -> {
            JDialog historialDialog = new JDialog(frame, "Log", true);
            historialDialog.setSize(500, 300);
            historialDialog.setLocationRelativeTo(frame);
            historialDialog.setLayout(new BorderLayout());

            // Crear el área de texto para el historial
            JTextArea historialArea = new JTextArea();
            historialArea.setEditable(false);

            // Obtener el historial inicial
            List<String> logEntries = new ArrayList<>(log.obtenerHistorial());
            for (String entry : logEntries) {
                historialArea.append(entry + "\n");
            }

            // Crear el botón en la parte superior izquierda
            JButton btnOrdenar = new JButton("Asc.");
            btnOrdenar.addActionListener(event -> {
                // Alternar el texto del botón entre "Asc." y "Desc."
                if (btnOrdenar.getText().equals("Asc.")) {
                    btnOrdenar.setText("Desc.");
                } else {
                    btnOrdenar.setText("Asc.");
                }

                // Invertir el orden de la lista
                Collections.reverse(logEntries);

                // Actualizar el contenido del área de texto
                historialArea.setText(""); // Limpiar el área de texto
                for (String entry : logEntries) {
                    historialArea.append(entry + "\n"); // Mostrar el contenido en el nuevo orden
                }
            });

            // Crear el botón "Descargar"
            JButton btnDescargar = new JButton("Descargar");
            btnDescargar.addActionListener(event -> {
                // Generar archivo .txt con el contenido de logEntries
                File archivo = new File("historial_log.txt");
                try (PrintWriter writer = new PrintWriter(archivo)) {
                    for (String entry : logEntries) {
                        writer.println(entry);
                    }
                    JOptionPane.showMessageDialog(historialDialog, 
                        "Archivo guardado como " + archivo.getAbsolutePath(),
                        "Descarga exitosa", 
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(historialDialog, 
                        "Error al guardar el archivo: " + ex.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            });

            // Crear el panel superior y agregar los botones
            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            topPanel.add(btnOrdenar);
            topPanel.add(btnDescargar);

            // Agregar el panel superior y el área de texto al diálogo
            historialDialog.add(topPanel, BorderLayout.NORTH);
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
        frame.setSize(650, 450);
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

    public static void actualizarContactes() throws MalformedURLException, XmlRpcException {
        // Limpia la lista de contactos filtrados
        contactesFiltrats.clear();
    
        // Vuelve a obtener los contactos de Odoo
        Connection conexion = null;
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(odoo.ODOO_URL + "xmlrpc/2/object"));
        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);
    
        try {
            // Obtener el ID del usuario actual usando `registre.nombreUsuario`
            String usuarioActual = registre.nombreUsuario;
            Integer usuarioId = null;
            
            // Buscar el usuario actual en la lista de usuarios
            List<Map<String, Object>> usuarios = odoo.obtenerUsuariosConLogin();
            for (Map<String, Object> usuario : usuarios) {
                String login = (String) usuario.get("login");
                if (usuarioActual.equals(login)) {
                    usuarioId = (Integer) usuario.get("id");
                    break;
                }
            }
    
            if (usuarioId == null) {
                JOptionPane.showMessageDialog(null, "No se encontró el ID del usuario actual.");
                return;
            }
    
            // Obtener los contactos desde Odoo
            List<Map<String, Object>> contactosOdoo = odoo.obtenerContactos();
    
            // Filtrar los contactos según el `owner_id`
            contactesOriginals = new ArrayList<>();
            for (Map<String, Object> datos : contactosOdoo) {
                Integer contactId = (Integer) datos.get("id");
                Integer ownerId = (Integer) datos.get("owner_id");
                if (ownerId != null && ownerId.equals(usuarioId)) {
                    String name = (String) datos.get("name");
                    String phone = (String) datos.get("phone");
                    contact contacto = new contact(contactId, name, phone);
                    contactesOriginals.add(contacto);
                }
            }
    
            // Actualizar la lista filtrada
            contactesFiltrats.clear();
            contactesFiltrats.addAll(contactesOriginals);
    
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al obtener contactos de Odoo");
        }
    }
}
