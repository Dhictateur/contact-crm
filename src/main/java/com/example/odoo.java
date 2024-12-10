package com.example;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.XmlRpcException;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

// Clase Odoo local funcional
public class odoo {

    // Parámetros de conexión
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/test";
    public static final String USER = "1234@gmail.com";
    public static final String PASSWORD = "1234";
    public static final String USERDB = "odoo";

    // Variables para XML-RPC
    private static XmlRpcClient clientCommon;
    private static XmlRpcClient clientObject;
    public static String db = "test";
    private static int userId;
    
    public static void setClientCommon(XmlRpcClient client) {
        clientCommon = client;
    }
    
    public static void setClientObject(XmlRpcClient client) {
        clientObject = client;
    }

    // Método para establecer la conexión a la base de datos
    public static Connection conectar() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("No se encontró el controlador de PostgreSQL");
            e.printStackTrace();
        }

        Connection conexion = DriverManager.getConnection(DB_URL, USERDB, PASSWORD);
        System.out.println("Conexión establecida correctamente con la base de datos de Odoo.");
        return conexion;
    }

    // Método para cerrar la conexión
    public static void cerrarConexion(Connection conexion) {
        if (conexion != null) {
            try {
                conexion.close();
                System.out.println("Conexión cerrada.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para inicializar el cliente XML-RPC y autenticar al usuario
    public static void inicializarXmlRpc() throws MalformedURLException, XmlRpcException {
        XmlRpcClientConfigImpl configCommon = new XmlRpcClientConfigImpl();
        configCommon.setServerURL(new URL("http://localhost:8069/xmlrpc/2/common"));
        clientCommon = new XmlRpcClient();
        clientCommon.setConfig(configCommon);

        userId = (int) clientCommon.execute("authenticate", Arrays.asList(db, USER, PASSWORD, new HashMap<>()));

        if (userId == 0) {
            System.err.println("Error: Autenticación fallida. Verifica tus credenciales.");
            return;
        }
        
        XmlRpcClientConfigImpl configObject = new XmlRpcClientConfigImpl();
        configObject.setServerURL(new URL("http://localhost:8069/xmlrpc/2/object"));
        clientObject = new XmlRpcClient();
        clientObject.setConfig(configObject);

        System.out.println("Cliente XML-RPC inicializado y autenticado. User ID: " + userId);
    }

    public static List<Map<String, Object>> obtenerContactos() {
        try {
            // Parámetros para la llamada `search_read` sin filtros
            List<Object> params = Arrays.asList(
                db, userId, PASSWORD,
                "res.partner", "search_read",
                Arrays.asList(Arrays.asList()), // Sin filtro, obtendremos todos los contactos
                new HashMap<String, Object>() {{
                    put("fields", Arrays.asList("name", "phone", "owner_id")); // Campos que queremos obtener
                }}
            );
    
            // Ejecutar la llamada XML-RPC
            Object[] result = (Object[]) clientObject.execute("execute_kw", params);
    
            // Verificar el contenido de `result`
            if (result == null || result.length == 0) {
                System.err.println("No se obtuvieron datos de contactos de Odoo.");
                return null;
            }
            // Crear lista para almacenar contactos convertidos
            List<Map<String, Object>> contactos = new ArrayList<>();

            // Convertir y agregar cada elemento a la lista `contactos`
            for (Object item : result) {
                if (item instanceof Map) {
                    Map<String, Object> contacto = (Map<String, Object>) item;
    
                    // Comprobar si el campo 'phone' no es nulo y no es un valor booleano
                    Object phoneValue = contacto.get("phone");
                    Object ownerValue = contacto.get("owner_id");
                    
                    if (phoneValue instanceof String && ownerValue != null) {
                        Integer ownerId = null; // Inicializar el ID del owner
                        if (ownerValue instanceof Object[]) {
                            Object[] ownerArray = (Object[]) ownerValue;
                            if (ownerArray.length > 0 && ownerArray[0] instanceof Integer) {
                                ownerId = (Integer) ownerArray[0]; // Extraer el ID (primer elemento del arreglo)
                            }
                        }

                        if (ownerId != null) {
                            // Crear un nuevo mapa para almacenar el contacto
                            Map<String, Object> contactoMap = new HashMap<>();
                            contactoMap.put("name", contacto.get("name"));
                            contactoMap.put("phone", phoneValue);
                            contactoMap.put("owner_id", ownerId);
                            contactos.add(contactoMap);
                        }
                    }
                    
                } else {
                    System.err.println("Elemento no es un Map: " + item);
                }
            }
            System.out.println("Contactos obtenidos de Odoo: " + contactos);
            return contactos;
    
        } catch (XmlRpcException e) {
            System.err.println("Error al obtener contactos: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Métodos de acceso a los clientes XML-RPC
    public static XmlRpcClient getClientObject() {
        return clientObject;
    }

    public static int getUserId() {
        return userId;
    }   

    public static List<Map<String, Object>> obtenerUsuariosConLogin() {
        try {
            // Parámetros para la llamada `search_read`
            List<Object> params = Arrays.asList(
                db, userId, PASSWORD,
                "res.users", "search_read",
                Arrays.asList(Arrays.asList()), // Sin filtros, obtener todos los usuarios
                new HashMap<String, Object>() {{
                    put("fields", Arrays.asList("name", "phone", "login", "id")); // Campos que queremos obtener
                }}
            );
    
            // Ejecutar la llamada XML-RPC
            Object[] result = (Object[]) clientObject.execute("execute_kw", params);
    
            if (result == null || result.length == 0) {
                System.err.println("No se obtuvieron datos de usuarios de Odoo.");
                return new ArrayList<>();
            }
    
            // Crear lista para almacenar los usuarios válidos
            List<Map<String, Object>> usuarios = new ArrayList<>();
    
            for (Object item : result) {
                if (item instanceof Map) {
                    Map<String, Object> usuario = (Map<String, Object>) item;
    
                    // Extraer campos requeridos
                    String name = (String) usuario.get("name");
                    String phone = usuario.get("phone") instanceof String ? (String) usuario.get("phone") : null;
                    String login = usuario.get("login") instanceof String ? (String) usuario.get("login") : null;
                    Integer id = usuario.get("id") instanceof Integer ? (Integer) usuario.get("id") : null;
    
                    // Solo agregar a la lista si tiene login
                    if (login != null && !login.isEmpty()) {
                        // Crear un mapa para almacenar los datos del usuario
                        Map<String, Object> usuarioMap = new HashMap<>();
                        usuarioMap.put("name", name);
                        usuarioMap.put("phone", phone);
                        usuarioMap.put("login", login);
                        usuarioMap.put("id", id);
                        usuarios.add(usuarioMap);
                    }
                }
            }
    
            System.out.println("Usuarios obtenidos de Odoo con login: " + usuarios);
            return usuarios;
    
        } catch (XmlRpcException e) {
            System.err.println("Error al obtener usuarios: " + e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
