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
        String mensaje = "Inici de sessió: " + usuario + " | " + timestamp;
        historial.add(mensaje);
    }

    // Método para registrar un cierre de sesión
    public static void registrarCierreSesion(String usuario) {
        String timestamp = LocalDateTime.now().format(formatter);
        String mensaje = "Tancament de sessió: " + usuario + " | " + timestamp;
        historial.add(mensaje);
    }

    // Método para registrar una llamada
    public static void registrarLlamada(String contacto) {
        String timestamp = LocalDateTime.now().format(formatter);
        String mensaje = "Trucada a " + contacto + " | " + timestamp;
        historial.add(mensaje);
    }

    public static void registrarMensajeEnviado(String remitente, String destinatario) {
        String timestamp = LocalDateTime.now().format(formatter);
        String mensaje = remitente + " ha enviado un mensaje a " + destinatario + " | " + timestamp;
        historial.add(mensaje);
    }

    // Método para obtener el historial completo
    public static List<String> obtenerHistorial() {
        return new ArrayList<>(historial);
    }

    //Metodo para obtener creacion de contacto
    public static void registrarContacto(String contactName, String numContacto) {
        String timestamp = LocalDateTime.now().format(formatter);
        String mensaje = "Contacto creado: " + contactName + "(" +  numContacto + ") | " + timestamp; 
        historial.add(mensaje);
    }
}
