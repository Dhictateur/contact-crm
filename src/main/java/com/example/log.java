package com.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class log {
    private static final List<String> historial = new ArrayList<>();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Método para registrar un inicio de sesión
    public static void registrarInicioSesion(String usuario) {
        String timestamp = LocalDateTime.now().format(formatter);
        String mensaje = "Inicio de sesión: " + usuario + " | " + timestamp;
        historial.add(mensaje);
    }

    // Método para registrar un cierre de sesión
    public static void registrarCierreSesion(String usuario) {
        String timestamp = LocalDateTime.now().format(formatter);
        String mensaje = "Cierre de sesión: " + usuario + " | " + timestamp;
        historial.add(mensaje);
    }

    // Método para registrar una llamada
    public static void registrarLlamada(String contacto) {
        String timestamp = LocalDateTime.now().format(formatter);
        String mensaje = "Llamada a " + contacto + " | " + timestamp;
        historial.add(mensaje);
    }

    // Método para obtener el historial completo
    public static List<String> obtenerHistorial() {
        return new ArrayList<>(historial);
    }
}
