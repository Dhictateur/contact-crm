package com.example;

import java.awt.BorderLayout;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.*;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

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
            JDialog modificarDialog = new JDialog(frame, "Modificar Usuarios", true);
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

                    // Crear un botón para cada usuario
                    JButton btnUsuario = new JButton(name); // Nombre como texto del botón
                    btnUsuario.addActionListener(e -> {
                        // Acción cuando se hace clic en el botón
                        System.out.println("Modificar: " + name + " (Teléfono: " + phone + ")");
                        // Aquí puedes agregar la lógica para abrir una pantalla de edición/modificación
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
        }
    }
}
