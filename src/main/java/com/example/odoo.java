package com.example;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;

public class odoo {

    // Parámetros de conexión
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/test";
    public static final String USER = "odoo";
    public static final String PASSWORD = "1234";

    // Variables para XML-RPC
    private static XmlRpcClient clientCommon;
    private static XmlRpcClient clientObject;
    private static String db = "test"; // Cambia esto por tu base de datos
    private static int userId;

    // Método para establecer la conexión a la base de datos
    public static Connection conectar() throws SQLException {
        try {
            // Registrar el controlador
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("No se encontró el controlador de PostgreSQL");
            e.printStackTrace();
        }

        // Establecer la conexión
        Connection conexion = DriverManager.getConnection(DB_URL, USER, PASSWORD);
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
        // Configuración del cliente para `common`
        XmlRpcClientConfigImpl configCommon = new XmlRpcClientConfigImpl();
        configCommon.setServerURL(new URL("http://localhost:8069/xmlrpc/2/common"));
        clientCommon = new XmlRpcClient();
        clientCommon.setConfig(configCommon);

        // Autenticación
        userId = (int) clientCommon.execute("authenticate", Arrays.asList(db, USER, PASSWORD));
        
        // Configuración del cliente para `object`
        XmlRpcClientConfigImpl configObject = new XmlRpcClientConfigImpl();
        configObject.setServerURL(new URL("http://localhost:8069/xmlrpc/2/object"));
        clientObject = new XmlRpcClient();
        clientObject.setConfig(configObject);

        System.out.println("Cliente XML-RPC inicializado y autenticado. User ID: " + userId);
    }

    // Métodos de acceso a los clientes XML-RPC
    public static XmlRpcClient getClientObject() {
        return clientObject;
    }

    public static int getUserId() {
        return userId;
    }
}
