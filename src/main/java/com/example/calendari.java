package com.example;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import com.toedter.calendar.JCalendar;

public class calendari {
    public static void mostrarCalendari(JFrame parentFrame) {
        // Crear una nueva ventana (JFrame)
        JFrame calendarioFrame = new JFrame("Calendario");
        
        // Configurar las propiedades de la ventana
        calendarioFrame.setSize(400, 300); // Establece el tamaño de la ventana
        calendarioFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cierra solo esta ventana
        
        // Crear un panel con un layout que permita expandir el JCalendar
        JPanel panel = new JPanel(new BorderLayout()); // Usar BorderLayout
        JCalendar jCalendar = new JCalendar(); // Componente de calendario
        
        // Añadir el JCalendar al centro del panel (esto hará que ocupe todo el espacio)
        panel.add(jCalendar, BorderLayout.CENTER);
        
        // Añadir el panel a la ventana
        calendarioFrame.add(panel);
        
        // Centrar la ventana en relación a la ventana principal
        calendarioFrame.setLocationRelativeTo(parentFrame);
        
        // Hacer visible la ventana
        calendarioFrame.setVisible(true);
    }
}
