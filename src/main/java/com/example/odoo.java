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
import java.util.List;
import java.util.Map;

public class odoo {

    // Parámetros de conexión
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/test";
    public static final String USER = "1234@gmail.com";
    public static final String PASSWORD = "1234";
    public static final String USERDB = "odoo";

    // Variables para XML-RPC
    private static XmlRpcClient clientCommon;
    private static XmlRpcClient clientObject;
    private static String db = "test";
    private static int userId;

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
                    put("fields", Arrays.asList("name", "phone")); // Campos que queremos obtener
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
                    if (phoneValue instanceof String && phoneValue != null) {
                        // Crear un nuevo mapa para almacenar el contacto
                        Map<String, Object> contactoMap = new HashMap<>();
                        contactoMap.put("name", contacto.get("name"));
                        contactoMap.put("phone", phoneValue);
                        
                        // Agregar solo si el teléfono no es nulo
                        if (phoneValue != null) {
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

}
