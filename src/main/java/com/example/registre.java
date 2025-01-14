package com.example;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.MalformedURLException;
import java.net.URL;

public class registre {

    public static String nombreUsuario;
    public static int id_odoo;

    private static Object uid;
    static JFrame loginFrame;  

    public static Object getUid() {
        return uid;
    }

    // Método para mostrar la interfaz de inicio de sesión
    public static void mostrarRegistre() {
        // Si ya existe la ventana la hacemos visible
        if (loginFrame != null) {
            loginFrame.setVisible(true);
            return;
        }

        // Crear la ventana solo si no existe
        loginFrame = new JFrame("Inici de Sessió");
        loginFrame.setLayout(new GridLayout(3, 2));

        // Etiquetas y campos de texto
        JLabel nomLabel = new JLabel("Nom:");
        JTextField nomField = new JTextField();
        JLabel contrasenyaLabel = new JLabel("Contrasenya:");
        JPasswordField contrasenyaField = new JPasswordField();

        loginFrame.add(nomLabel);
        loginFrame.add(nomField);
        loginFrame.add(contrasenyaLabel);
        loginFrame.add(contrasenyaField);

        // Boton inicio
        JButton btIniciarSessio = new JButton("Inici");
        btIniciarSessio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nom = nomField.getText();
                String contrasenya = new String(contrasenyaField.getPassword());

                // Validar campos vacios
                if (nom.isEmpty() || contrasenya.isEmpty()) {
                    JOptionPane.showMessageDialog(loginFrame, "Por favor, complete todos los campos.");
                    return;
                }

                // Verificar si el usuario existe en Odoo usando XML-RPC
                if (verificarUsuarioEnOdoo(nom, contrasenya)) {
                    JOptionPane.showMessageDialog(loginFrame, "Iniciando sesión como: " + nom);
                    nombreUsuario = nom;
                    id_odoo = odoo.obtenerUserIdPorLogin(nombreUsuario);
                    log.registrarInicioSesion(nom);
                    loginFrame.setVisible(false); // Ocultar la ventana de login en lugar de eliminarla
                        Connection conexion = null;
                        try {
                            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
                            config.setServerURL(new URL(odoo.ODOO_URL + "xmlrpc/2/object"));
                            XmlRpcClient client = new XmlRpcClient();
                            client.setConfig(config);

                            // Verificar el tipo de usuario
                            String tipoUsuario = verificarTipoUsuario(client, odoo.db, odoo.PASSWORD, nom);
                            contact.userType = tipoUsuario;
                        } catch (MalformedURLException m) {
                            System.out.println("URL malformada: " + m.getMessage());
                            m.printStackTrace();
                            return; // Termina el programa si la URL es inválida
                        } catch (Exception y) {
                            System.out.println("Error inesperado: " + y.getMessage());
                            y.printStackTrace();
                            return; // Manejo general para otras excepciones no anticipadas
                        }
                    contact.mostrarAgenda(); // Llamar a la interfaz de contacto
                } else {
                    JOptionPane.showMessageDialog(loginFrame, "Credenciales incorrectas.");
                }

                // Limpiar campos
                nomField.setText("");
                contrasenyaField.setText("");
            }
        });

        loginFrame.add(btIniciarSessio);

        // Macro
        loginFrame.setSize(300, 150);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setVisible(true); // Mostrar la ventana por primera vez
    }

    public static String verificarTipoUsuario(XmlRpcClient client, String db, String password, String userNameToCheck) {
        try {
            // ID fijo del grupo Settings
            int settingsGroupId = 4;

            // Buscar al usuario
            Object[] userSearchParams = new Object[]{
                new Object[]{new Object[]{"login", "=", userNameToCheck}}
            };
            Object[] userResult = (Object[]) client.execute("execute_kw", new Object[]{
                db, uid, password, "res.users", "search_read",
                userSearchParams,
                new HashMap<String, Object>() {{
                    put("fields", new String[]{"groups_id"}); // Leer los grupos asociados al usuario
                }}
            });

            if (userResult.length > 0) {
                // Extraer los grupos del usuario
                HashMap<String, Object> user = (HashMap<String, Object>) userResult[0];
                Object[] groups = (Object[]) user.get("groups_id");

                System.out.println("Grupos del usuario: " + Arrays.toString(groups)); // Debug

                // Verificar si el usuario pertenece al grupo Settings
                for (Object groupId : groups) {
                    int id = Integer.parseInt(groupId.toString());
                    if (id == settingsGroupId) {
                        return "Admin"; // Es administrador
                    }
                }
            } else {
                System.out.println("Usuario no encontrado.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "User"; // Es un usuario normal
    }

    

    // Metodo para conexion Odoo > XML-RPC
    public static boolean verificarUsuarioEnOdoo(String nombre, String contrasenya) {
        try {
            // Configuración del cliente XML-RPC
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL(odoo.ODOO_URL + "xmlrpc/2/common"));
            XmlRpcClient client = new XmlRpcClient(); 
            client.setConfig(config);
    
            // Llamada a la función 'authenticate' de Odoo
            Object[] params = new Object[]{
                "test",         // Nombre de la base de datos de Odoo
                nombre,         // Nombre de usuario
                contrasenya,    // Contraseña
                new HashMap<>() // Sin contexto adicional
            };
    
            // Autenticar usuario
            Object result = client.execute("authenticate", params);
    
            // Comprobar si el resultado es un número (UID válido) y no es 0
            if (result instanceof Integer) {
                uid = result;  // Almacena el UID del usuario autenticado
                return (Integer) uid != 0; // La autenticación es exitosa solo si el UID no es 0
            }
    
            return false; // Si el resultado no es un número, la autenticación ha fallado
    
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    

    // Método para cerrar sesión en Odoo
    public static void cerrarSesion() {
        try {
            if (uid != null) {
                log.registrarCierreSesion(nombreUsuario); // Log para cierre de sesión
            }
            // Invalidar el UID (usuario autenticado) para simular cierre de sesión
            uid = null;

            // Imprimir que se ha cerrado la sesión
            System.out.println("Sesión cerrada.");

            // Ocultar la ventana actual (en este caso, la ventana de agenda)
            JFrame frameActual = (JFrame) javax.swing.FocusManager.getCurrentManager().getActiveWindow();
            if (frameActual != null) {
                frameActual.setVisible(false); // Ocultar la ventana actual
            }
            contact.LoginSuccess = 0;

            // Volver a mostrar la ventana de inicio de sesión
            mostrarRegistre();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}