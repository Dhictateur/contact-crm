package com.example;

import java.awt.BorderLayout;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.*;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class modificar {

    

    // Método para mostrar usuarios como botones en una lista vertical
    public static void mostrarUsuarios(JFrame frame) throws SQLException, MalformedURLException {

        Connection conexion = null;
        conexion = odoo.conectar();
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL("http://localhost:8069/xmlrpc/2/object"));
        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);

        if (registre.verificarTipoUsuario(client, odoo.db, odoo.PASSWORD, registre.nombreUsuario) == "Admin") {
            // Crear un JDialog modal
            JDialog modificarDialog = new JDialog(frame, "Modificació", true);
            modificarDialog.setSize(250, 300);
            modificarDialog.setLocationRelativeTo(frame); // Centrado respecto al frame principal
            modificarDialog.setLayout(new BorderLayout());

            // Crear un panel con BoxLayout para mostrar botones verticalmente
            JPanel panelUsuarios = new JPanel();
            panelUsuarios.setLayout(new BoxLayout(panelUsuarios, BoxLayout.Y_AXIS));

            // Crear un JScrollPane para permitir desplazamiento si hay muchos usuarios
            JScrollPane scrollPane = new JScrollPane(panelUsuarios);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

            // Obtener la lista de usuarios con sus contactos desde Odoo
            List<Map<String, Object>> usuariosConContactos = odoo.obtenerUsuariosConLogin(); // Método ajustado para contactos

            // Verificar si la lista de usuarios está vacía
            if (usuariosConContactos != null && !usuariosConContactos.isEmpty()) {
                for (Map<String, Object> usuario : usuariosConContactos) {
                    String name = (String) usuario.get("name"); // Nombre del usuario
                    String phone = usuario.get("phone") != null ? (String) usuario.get("phone") : "Sin teléfono";
                    String email = usuario.get("login") !=null ? (String) usuario.get("login") : "Sin email";

                    // Crear un botón para cada usuario
                    JButton btnUsuario = new JButton(name); // Nombre como texto del botón
                    btnUsuario.addActionListener(e -> {
                        try {
                            // Llamar al método para mostrar los datos del usuario y permitir edición
                            mostrarVentanaEdicionPorNombre(client, frame, name);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(frame, "Error al cargar datos del usuario: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    });

                    // Añadir el botón al panel
                    panelUsuarios.add(btnUsuario);
                }
            } else {
                System.out.println("No se encontraron usuarios.");
            }

            // Agregar el JScrollPane al JDialog
            modificarDialog.add(scrollPane, BorderLayout.CENTER);

            // Mostrar el JDialog
            modificarDialog.setVisible(true);
        } else {
            System.out.println(registre.nombreUsuario);
            // Mostrar ventana para editar los datos del usuario actual
            mostrarVentanaEdicionUsuario(client, frame, registre.nombreUsuario);
        }
    }

    public static void mostrarVentanaEdicionUsuario(XmlRpcClient client, JFrame frame, String login) {
        try {
            // Crear un JDialog modal para la edición del usuario
            JDialog edicionDialog = new JDialog(frame, "Editar Usuari", true);
            edicionDialog.setSize(300, 250);
            edicionDialog.setLocationRelativeTo(frame); // Centrado respecto al frame principal
            edicionDialog.setLayout(new BorderLayout());

            // Crear un panel para los campos de edición
            JPanel panelEdicion = new JPanel();
            panelEdicion.setLayout(new BoxLayout(panelEdicion, BoxLayout.Y_AXIS));

            // Obtener los datos actuales del usuario con el login
            List<Object> params = Arrays.asList(
                odoo.db, odoo.getUserId(), odoo.PASSWORD,
                "res.users", "search_read",
                Arrays.asList(Arrays.asList(
                    Arrays.asList("login", "=", login) // Filtrar por login
                )),
                new HashMap<String, Object>() {{
                    put("fields", Arrays.asList("name", "phone", "login")); // Campos a recuperar
                }}
            );

            Object[] result = (Object[]) client.execute("execute_kw", params);

            if (result == null || result.length == 0) {
                JOptionPane.showMessageDialog(frame, "Usuari no trobat.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Map<String, Object> usuario = (Map<String, Object>) result[0];
            String nombreActual = (String) usuario.get("name");
            String telefonoActual = usuario.get("phone") instanceof String ? (String) usuario.get("phone") : "";
            String emailActual = usuario.get("login") instanceof String ? (String) usuario.get("login") : "";

            // Campos de texto para edición
            JTextField txtNombre = new JTextField(nombreActual);
            JTextField txtTelefono = new JTextField(telefonoActual);
            JTextField txtEmail = new JTextField(emailActual);

            // Añadir los campos al panel
            panelEdicion.add(new JLabel("Nom:"));
            panelEdicion.add(txtNombre);
            panelEdicion.add(new JLabel("Telèfon:"));
            panelEdicion.add(txtTelefono);
            panelEdicion.add(new JLabel("Email:"));
            panelEdicion.add(txtEmail);

            // Botón para guardar los cambios
            JButton btnGuardar = new JButton("Guardar");
            btnGuardar.addActionListener(e -> {
                try {
                    // Actualizar los datos en Odoo
                    List<Object> writeParams = Arrays.asList(
                        odoo.db, odoo.getUserId(), odoo.PASSWORD,
                        "res.users", "write",
                        Arrays.asList(
                            Arrays.asList((int) usuario.get("id")), // ID del usuario
                            new HashMap<String, Object>() {{
                                put("name", txtNombre.getText());
                                put("phone", txtTelefono.getText());
                                put("login", txtEmail.getText());
                            }}
                        )
                    );

                    boolean success = (boolean) client.execute("execute_kw", writeParams);
                    if (success) {
                        JOptionPane.showMessageDialog(edicionDialog, "Dades actualitzades correctament.");
                        edicionDialog.dispose(); // Cerrar la ventana
                    } else {
                        JOptionPane.showMessageDialog(edicionDialog, "Error a l'actualizar les dades.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(edicionDialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            // Botón para cancelar
            JButton btnCancelar = new JButton("Cancel·lar");
            btnCancelar.addActionListener(e -> edicionDialog.dispose());

            // Panel para los botones
            JPanel panelBotones = new JPanel();
            panelBotones.add(btnGuardar);
            panelBotones.add(btnCancelar);

            // Añadir paneles al JDialog
            edicionDialog.add(panelEdicion, BorderLayout.CENTER);
            edicionDialog.add(panelBotones, BorderLayout.SOUTH);

            // Mostrar el JDialog
            edicionDialog.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error en obtenir les dades de l'usuari: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void mostrarVentanaEdicionPorNombre(XmlRpcClient client, JFrame frame, String name) {
        try {
            // Buscar usuario por "name"
            List<Object> params = Arrays.asList(
                odoo.db, odoo.getUserId(), odoo.PASSWORD,
                "res.users", "search_read",
                Arrays.asList(Arrays.asList(
                    Arrays.asList("name", "=", name) // Filtro por nombre
                )),
                new HashMap<String, Object>() {{
                    put("fields", Arrays.asList("name", "phone", "login")); // Campos a recuperar
                }}
            );
    
            Object[] result = (Object[]) client.execute("execute_kw", params);
    
            if (result == null || result.length == 0) {
                JOptionPane.showMessageDialog(frame, "Usuari no trobat.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
    
            Map<String, Object> usuario = (Map<String, Object>) result[0];
            String nombreActual = (String) usuario.get("name");
            String telefonoActual = usuario.get("phone") instanceof String ? (String) usuario.get("phone") : "";
            String emailActual = usuario.get("login") instanceof String ? (String) usuario.get("login") : "";
    
            // Crear un JDialog modal para la edición del usuario
            JDialog edicionDialog = new JDialog(frame, "Editar Usuari: " + nombreActual, true);
            edicionDialog.setSize(300, 250);
            edicionDialog.setLocationRelativeTo(frame); // Centrado respecto al frame principal
            edicionDialog.setLayout(new BorderLayout());
    
            // Crear un panel para los campos de edición
            JPanel panelEdicion = new JPanel();
            panelEdicion.setLayout(new BoxLayout(panelEdicion, BoxLayout.Y_AXIS));
    
            // Campos de texto para edición
            JTextField txtNombre = new JTextField(nombreActual);
            JTextField txtTelefono = new JTextField(telefonoActual);
            JTextField txtLogin = new JTextField(emailActual);
    
            // Añadir los campos al panel
            panelEdicion.add(new JLabel("Nom:"));
            panelEdicion.add(txtNombre);
            panelEdicion.add(new JLabel("Telèfon:"));
            panelEdicion.add(txtTelefono);
            panelEdicion.add(new JLabel("Email:"));
            panelEdicion.add(txtLogin);
    
            // Botón para guardar los cambios
            JButton btnGuardar = new JButton("Guardar");
            btnGuardar.addActionListener(e -> {
                try {
                    // Actualizar los datos en Odoo
                    List<Object> writeParams = Arrays.asList(
                        odoo.db, odoo.getUserId(), odoo.PASSWORD,
                        "res.users", "write",
                        Arrays.asList(
                            Arrays.asList((int) usuario.get("id")), // ID del usuario
                            new HashMap<String, Object>() {{
                                put("name", txtNombre.getText());
                                put("phone", txtTelefono.getText());
                                put("login", txtLogin.getText());
                            }}
                        )
                    );
    
                    boolean success = (boolean) client.execute("execute_kw", writeParams);
                    if (success) {
                        JOptionPane.showMessageDialog(edicionDialog, "Datos actualizados correctamente.");
                        edicionDialog.dispose(); // Cerrar la ventana
                    } else {
                        JOptionPane.showMessageDialog(edicionDialog, "Error al actualizar los datos.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(edicionDialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
    
            // Botón para cancelar
            JButton btnCancelar = new JButton("Cancelar");
            btnCancelar.addActionListener(e -> edicionDialog.dispose());
    
            // Panel para los botones
            JPanel panelBotones = new JPanel();
            panelBotones.add(btnGuardar);
            panelBotones.add(btnCancelar);
    
            // Añadir paneles al JDialog
            edicionDialog.add(panelEdicion, BorderLayout.CENTER);
            edicionDialog.add(panelBotones, BorderLayout.SOUTH);
    
            // Mostrar el JDialog
            edicionDialog.setVisible(true);
    
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error al obtener los datos del usuario: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }    
}
