package com.example;

import javax.swing.*;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import com.toedter.calendar.JCalendar;
import com.toedter.calendar.JDateChooser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class calendari {

    public static void mostrarCalendari(JFrame parentFrame, String nombreUsuario) throws MalformedURLException {

        Connection conexion = null;
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(odoo.ODOO_URL + "xmlrpc/2/object"));
        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);
   
        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        Calendar calendar = Calendar.getInstance(timeZone); // Usar UTC+1

        // Crear una nueva ventana (JFrame)
        JFrame calendarioFrame = new JFrame("Calendario");

        // Configurar las propiedades de la ventana
        calendarioFrame.setSize(400, 500); // Establece el tamaño de la ventana (aumento de altura)
        calendarioFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cierra solo esta ventana

        // Crear un panel con un layout que permita expandir el JCalendar
        JPanel panel = new JPanel(new BorderLayout()); // Usar BorderLayout
        JCalendar jCalendar = new JCalendar(); // Componente de calendario

        // Establecer la fecha actual en UTC+1 al calendario
        jCalendar.setCalendar(calendar); // Asignar el calendario con la zona UTC+1

        List<Map<String, Object>> eventos; //Declaramos la lista de eventos

        // Lista de eventos desde Odoo
        if (registre.verificarTipoUsuario(client, odoo.db, odoo.PASSWORD, registre.nombreUsuario) == "Admin") {
            eventos = odoo.obtenerEventos();
        } else {
            eventos = odoo.obtenerEventosDeUser();
        }

        // Formato de fecha esperado (el mismo que Odoo devuelve)
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        // Crear un conjunto para almacenar las fechas con eventos
        Set<Date> fechasConEventos = new HashSet<>();

        // Crear un mapa de eventos por fecha
        Map<Date, List<Map<String, Object>>> eventosPorFecha = new HashMap<>();

        for (Map<String, Object> evento : eventos) {
            String startString = (String) evento.get("start"); // Obtener la fecha como cadena
            
            if (startString != null) {
                try {
                    // Convertir la cadena de la fecha a un objeto Date
                    Date startDate = formatter.parse(startString);

                    // Normalizar la fecha para que tenga la hora a las 00:00:00
                    startDate = normalizeDate(startDate);

                    // Añadir al conjunto de fechas con eventos
                    fechasConEventos.add(startDate);

                    // Agrupar eventos por fecha
                    eventosPorFecha.putIfAbsent(startDate, new ArrayList<>());
                    eventosPorFecha.get(startDate).add(evento);

                } catch (ParseException e) {
                    System.err.println("Error al convertir la fecha: " + e.getMessage());
                }
            }
        }

        // Añadir el JCalendar al centro del panel (esto hará que ocupe todo el espacio)
        panel.add(jCalendar, BorderLayout.CENTER);

        // Crear el panel inferior para mostrar los eventos
        JPanel panelInferior = new JPanel(new BorderLayout());
        JTextArea textAreaEventos = new JTextArea();
        textAreaEventos.setEditable(false); // No debe ser editable
        textAreaEventos.setText("Selecciona una fecha para ver los eventos.");
        textAreaEventos.setPreferredSize(new Dimension(400, 150)); // Ajusta el tamaño preferido (más grande verticalmente)
        JScrollPane scrollPane = new JScrollPane(textAreaEventos);
        panelInferior.add(scrollPane, BorderLayout.CENTER);

        // Botón para crear evento
        JButton btnCrearEvento = new JButton("Crear Evento");
        btnCrearEvento.addActionListener(e -> {
            // Crear un nuevo JFrame para ingresar el nombre y las fechas del evento
            JFrame ventanaCrearEvento = new JFrame("Crear Evento");
            ventanaCrearEvento.setSize(400, 350); // Ajuste el tamaño para agregar más componentes
            ventanaCrearEvento.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            // Panel para la ventana
            JPanel panelEvento = new JPanel();
            panelEvento.setLayout(new GridLayout(6, 2)); // Añadimos más filas para los campos de hora

            // Campo para ingresar el nombre del evento
            JLabel lblNombreEvento = new JLabel("Nombre del Evento:");
            JTextField txtNombreEvento = new JTextField(20);
            panelEvento.add(lblNombreEvento);
            panelEvento.add(txtNombreEvento);

            // JDateChooser para la fecha de inicio
            JLabel lblFechaInicio = new JLabel("Fecha de Inicio:");
            JDateChooser jDateChooserInicio = new JDateChooser();
            jDateChooserInicio.setDateFormatString("yyyy-MM-dd");
            panelEvento.add(lblFechaInicio);
            panelEvento.add(jDateChooserInicio);

            // Campo para seleccionar la hora de inicio
            JLabel lblHoraInicio = new JLabel("Hora de Inicio:");
            SpinnerDateModel modelHoraInicio = new SpinnerDateModel();
            JSpinner spHoraInicio = new JSpinner(modelHoraInicio);
            spHoraInicio.setEditor(new JSpinner.DateEditor(spHoraInicio, "HH:mm"));
            panelEvento.add(lblHoraInicio);
            panelEvento.add(spHoraInicio);

            // JDateChooser para la fecha de fin
            JLabel lblFechaFin = new JLabel("Fecha de Fin:");
            JDateChooser jDateChooserFin = new JDateChooser();
            jDateChooserFin.setDateFormatString("yyyy-MM-dd");
            panelEvento.add(lblFechaFin);
            panelEvento.add(jDateChooserFin);

            // Campo para seleccionar la hora de fin
            JLabel lblHoraFin = new JLabel("Hora de Fin:");
            SpinnerDateModel modelHoraFin = new SpinnerDateModel();
            JSpinner spHoraFin = new JSpinner(modelHoraFin);
            spHoraFin.setEditor(new JSpinner.DateEditor(spHoraFin, "HH:mm"));
            panelEvento.add(lblHoraFin);
            panelEvento.add(spHoraFin);

            // Botón para guardar el evento
            JButton btnGuardarEvento = new JButton("Guardar Evento");
            btnGuardarEvento.addActionListener(e1 -> {
                String nombreEvento = txtNombreEvento.getText();
                Date fechaInicio = jDateChooserInicio.getDate();
                Date fechaFin = jDateChooserFin.getDate();
                
                // Combinamos la fecha con la hora seleccionada
                if (fechaInicio != null && fechaFin != null) {
                    // Establecer la hora de inicio y fin
                    Calendar calInicio = Calendar.getInstance();
                    calInicio.setTime(fechaInicio);
                    calInicio.set(Calendar.HOUR_OF_DAY, ((Date) spHoraInicio.getValue()).getHours());
                    calInicio.set(Calendar.MINUTE, ((Date) spHoraInicio.getValue()).getMinutes());

                    Calendar calFin = Calendar.getInstance();
                    calFin.setTime(fechaFin);
                    calFin.set(Calendar.HOUR_OF_DAY, ((Date) spHoraFin.getValue()).getHours());
                    calFin.set(Calendar.MINUTE, ((Date) spHoraFin.getValue()).getMinutes());

                    // Crear los objetos de fecha con la hora incluida
                    Date fechaInicioConHora = calInicio.getTime();
                    Date fechaFinConHora = calFin.getTime();

                    // Validar si el nombre y las fechas son válidas
                    if (nombreEvento != null && !nombreEvento.isEmpty() && fechaInicioConHora != null && fechaFinConHora != null) {
                        // Obtener el userId (deberías pasarlo desde la clase `registre`)
                        int userId = registre.id_odoo;

                        // Crear el evento en Odoo
                        odoo.crearEvento(nombreEvento, fechaInicioConHora, fechaFinConHora, userId);

                        // Cerrar la ventana después de guardar el evento
                        ventanaCrearEvento.dispose();
                    } else {
                        JOptionPane.showMessageDialog(ventanaCrearEvento, "Por favor, complete todos los campos.");
                    }
                }
            });

            // Agregar el botón al panel
            panelEvento.add(btnGuardarEvento);

            // Agregar el panel a la ventana
            ventanaCrearEvento.add(panelEvento);

            // Hacer visible la ventana
            ventanaCrearEvento.setVisible(true);
        });

        // Añadir el botón "Mostrar eventos" en la parte inferior del panel
        JButton btnMostrarEventos = new JButton("Mostrar eventos");
        btnMostrarEventos.addActionListener(e -> {
            // Crear un panel para mostrar los eventos
            JPanel panelEventos = new JPanel();
            panelEventos.setLayout(new BoxLayout(panelEventos, BoxLayout.Y_AXIS)); // Diseño vertical para los eventos
            
            // Obtener la lista de eventos
            for (Map<String, Object> evento : eventos) {
                String nombreEvento = (String) evento.get("name");
                String fechaEvento = (String) evento.get("start"); // Usamos la fecha de inicio
                Integer idEvento = (Integer) evento.get("id"); // Obtener el ID del evento

                // Crear un JPanel para cada evento
                JPanel eventoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Panel para cada evento
                eventoPanel.add(new JLabel(nombreEvento + " el " + fechaEvento)); // Mostrar nombre y fecha del evento

                // Crear un botón "Editar" para editar el evento
                JButton btnEditarEvento = new JButton("Editar");
                btnEditarEvento.addActionListener(e2 -> {
                    // Crear un nuevo JFrame para la edición
                    JFrame ventanaEdicion = new JFrame("Editar Evento");
                    ventanaEdicion.setSize(400, 350); 
                    ventanaEdicion.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                    // Crear un panel para la edición
                    JPanel panelEdicion = new JPanel();
                    panelEdicion.setLayout(new GridLayout(6, 2));

                    // Campos de edición con los valores actuales del evento
                    JLabel lblNombreEvento = new JLabel("Nombre del Evento:");
                    JTextField txtNombreEvento = new JTextField((String) evento.get("name"), 20);
                    panelEdicion.add(lblNombreEvento);
                    panelEdicion.add(txtNombreEvento);

                    JLabel lblFechaInicio = new JLabel("Fecha de Inicio:");
                    JDateChooser jDateChooserInicio = new JDateChooser();
                    try {
                        jDateChooserInicio.setDate(formatter.parse((String) evento.get("start")));
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                    panelEdicion.add(lblFechaInicio);
                    panelEdicion.add(jDateChooserInicio);

                    JLabel lblHoraInicio = new JLabel("Hora de Inicio:");
                    SpinnerDateModel modelHoraInicio = new SpinnerDateModel();
                    JSpinner spHoraInicio = new JSpinner(modelHoraInicio);
                    spHoraInicio.setEditor(new JSpinner.DateEditor(spHoraInicio, "HH:mm"));
                    Calendar calInicio = Calendar.getInstance();
                    try {
                        Date startDate = formatter.parse((String) evento.get("start"));
                        calInicio.setTime(startDate);
                        spHoraInicio.setValue(startDate);
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                    panelEdicion.add(lblHoraInicio);
                    panelEdicion.add(spHoraInicio);

                    JLabel lblFechaFin = new JLabel("Fecha de Fin:");
                    JDateChooser jDateChooserFin = new JDateChooser();
                    try {
                        jDateChooserFin.setDate(formatter.parse((String) evento.get("stop")));
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                    panelEdicion.add(lblFechaFin);
                    panelEdicion.add(jDateChooserFin);

                    JLabel lblHoraFin = new JLabel("Hora de Fin:");
                    SpinnerDateModel modelHoraFin = new SpinnerDateModel();
                    JSpinner spHoraFin = new JSpinner(modelHoraFin);
                    spHoraFin.setEditor(new JSpinner.DateEditor(spHoraFin, "HH:mm"));
                    Calendar calFin = Calendar.getInstance();
                    try {
                        Date endDate = formatter.parse((String) evento.get("stop"));
                        calFin.setTime(endDate);
                        spHoraFin.setValue(endDate);
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                    panelEdicion.add(lblHoraFin);
                    panelEdicion.add(spHoraFin);

                    // Botón para guardar cambios
                    JButton btnGuardarCambios = new JButton("Guardar Cambios");
                    btnGuardarCambios.addActionListener(e3 -> {
                        String nuevoNombre = txtNombreEvento.getText();
                        Date nuevaFechaInicio = jDateChooserInicio.getDate();
                        Date nuevaFechaFin = jDateChooserFin.getDate();

                        // Combinar las fechas con las horas seleccionadas
                        calInicio.setTime(nuevaFechaInicio);
                        calInicio.set(Calendar.HOUR_OF_DAY, ((Date) spHoraInicio.getValue()).getHours());
                        calInicio.set(Calendar.MINUTE, ((Date) spHoraInicio.getValue()).getMinutes());

                        calFin.setTime(nuevaFechaFin);
                        calFin.set(Calendar.HOUR_OF_DAY, ((Date) spHoraFin.getValue()).getHours());
                        calFin.set(Calendar.MINUTE, ((Date) spHoraFin.getValue()).getMinutes());

                        Date nuevaFechaHorasInicio = calInicio.getTime();
                        Date nuevaFechaHorasFin = calFin.getTime();

                        // Validar datos
                        if (nuevoNombre.isEmpty() || nuevaFechaHorasInicio == null || nuevaFechaHorasFin == null) {
                            JOptionPane.showMessageDialog(ventanaEdicion, "Por favor, complete todos los campos.");
                            return;
                        }

                        try {
                            // Convertir las fechas al formato adecuado
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String fechaInicioStr = sdf.format(nuevaFechaHorasInicio); // Fecha/hora de inicio como String
                            String fechaFinStr = sdf.format(nuevaFechaHorasFin); // Fecha/hora de fin como String
                        
                            // Crear parámetros para la llamada XML-RPC
                            Map<String, Object> values = new HashMap<>();
                            values.put("name", nuevoNombre); // Nombre del evento
                            values.put("start", fechaInicioStr); // Fecha/hora de inicio
                            values.put("stop", fechaFinStr); // Fecha/hora de fin
                        
                            // Ejecutar la llamada XML-RPC para actualizar el evento
                            List<Object> writeParams = Arrays.asList(
                                odoo.db, registre.id_odoo, registre.pass,
                                "calendar.event", "write",
                                Arrays.asList(
                                    Arrays.asList(idEvento), // ID del evento a actualizar
                                    values // Los nuevos valores a escribir
                                )
                            );
                        
                            boolean success = (boolean) client.execute("execute_kw", writeParams);
                            if (success) {
                                JOptionPane.showMessageDialog(ventanaEdicion, "Evento actualizado exitosamente.");
                                ventanaEdicion.dispose(); // Cerrar la ventana
                            } else {
                                JOptionPane.showMessageDialog(ventanaEdicion, "Error al actualizar el evento.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(ventanaEdicion, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    });

                    // Agregar el botón al panel
                    panelEdicion.add(btnGuardarCambios);

                    // Añadir el panel al JFrame
                    ventanaEdicion.add(panelEdicion);

                    // Mostrar la ventana de edición
                    ventanaEdicion.setVisible(true);
                });
                eventoPanel.add(btnEditarEvento); // Añadir el botón "Editar" al panel del evento

                // Crear un botón "x" para cerrar el evento
                JButton btnCerrarEvento = new JButton("x");

                // Asociar el ID del evento al ActionListener del botón
                btnCerrarEvento.addActionListener(e2 -> {
                    System.out.println("Evento a eliminar con ID: " + idEvento);
                    // Llamar a la función para eliminar el evento en Odoo
                    odoo.eliminarEventoOdoo(idEvento);
                });
                eventoPanel.add(btnCerrarEvento); // Añadir el botón "x" al panel del evento

                // Añadir el panel del evento al panel principal
                panelEventos.add(eventoPanel);
            }

            // Crear un JScrollPane para permitir el desplazamiento si hay muchos eventos
            JScrollPane scrollPaneEventos = new JScrollPane(panelEventos);
            scrollPaneEventos.setPreferredSize(new Dimension(400, 300)); // Tamaño del JScrollPane
            
            // Mostrar los eventos en el JFrame principal o en un panel específico
            JFrame eventosFrame = new JFrame("Eventos");
            eventosFrame.setSize(400, 300);
            eventosFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            eventosFrame.add(scrollPaneEventos); // Añadir el JScrollPane con los eventos al JFrame
            eventosFrame.setLocationRelativeTo(calendarioFrame); // Centrado respecto al JFrame principal
            eventosFrame.setVisible(true);
        });
        // Añadir el botón al panel inferior
        panelInferior.add(btnMostrarEventos, BorderLayout.NORTH); // Cambié a NORTH para que esté arriba del JTextArea


        // Añadir el botón al panel inferior
        panelInferior.add(btnCrearEvento, BorderLayout.SOUTH);

        // Añadir el panel inferior a la parte inferior del panel principal
        panel.add(panelInferior, BorderLayout.SOUTH);

        // Añadir el panel a la ventana
        calendarioFrame.add(panel);

        // Añadir un PropertyChangeListener para detectar la selección de fecha
        jCalendar.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("calendar".equals(evt.getPropertyName())) {
                    // Se ha seleccionado una nueva fecha
                    Date fechaSeleccionada = jCalendar.getDate();
                    System.out.println("Clic en la fecha: " + fechaSeleccionada);

                    // Normalizar la fecha seleccionada
                    fechaSeleccionada = normalizeDate(fechaSeleccionada);

                    // Comprobar si hay eventos para la fecha seleccionada
                    if (eventosPorFecha.containsKey(fechaSeleccionada)) {
                        List<Map<String, Object>> eventosDelDia = eventosPorFecha.get(fechaSeleccionada);
                        StringBuilder eventosTexto = new StringBuilder("Eventos para esta fecha:\n");
                        for (Map<String, Object> evento : eventosDelDia) {
                            // Añadir detalles del evento al StringBuilder
                            eventosTexto.append("Evento: ").append(evento.get("name"))
                                    .append(" a las ").append(evento.get("start")).append("\n");
                        }
                        // Actualizar el área de texto con los eventos
                        textAreaEventos.setText(eventosTexto.toString());
                    } else {
                        textAreaEventos.setText("No hay eventos para esta fecha.");
                    }
                }
            }
        });

        // Centrar la ventana en relación a la ventana principal
        calendarioFrame.setLocationRelativeTo(parentFrame);

        // Hacer visible la ventana
        calendarioFrame.setVisible(true);
    }

    // Método para normalizar la fecha y establecer la hora a las 00:00:00
    private static Date normalizeDate(Date date) {
        // Crear un objeto Calendar con la zona horaria UTC+1
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"));
        calendar.setTime(date);
        
        // Ajustar la hora a las 00:00:00 (medianoche)
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        // Devolver la fecha normalizada
        return calendar.getTime();
    }
}
