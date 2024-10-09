package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class odoo {

    // Parámetros de conexión
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/test";
    private static final String USER = "odoo";
    private static final String PASSWORD = "1234";

    // Método para obtener una conexión
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

    // Método de ejemplo para cerrar la conexión
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
}

