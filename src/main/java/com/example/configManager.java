package com.example;

import java.io.*;
import java.util.Properties;

public class configManager {
    private static final String CONFIG_FILE = "config.properties";

    // Guardar el nombre de usuario en el archivo
    public static void guardarNombreUsuario(String nombreUsuario) {
        Properties props = new Properties();
        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.setProperty("nombreUsuario", nombreUsuario);
            props.store(out, "Configuración del usuario");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Leer el nombre de usuario del archivo
    public static String leerNombreUsuario() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
            props.load(in);
            return props.getProperty("nombreUsuario", "");
        } catch (IOException e) {
            // Si no se encuentra el archivo, devolvemos un valor vacío
            return "";
        }
    }
}
