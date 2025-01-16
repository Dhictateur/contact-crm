package com.example;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.XmlRpcException;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

// Clase Odoo local funcional
public class odoo {

    // Parámetros de conexión
    static final String ODOO_URL = "https://devsforcrm.mywire.org/";
    public static final String USER = "admin";
    public static final String PASSWORD = "admin";
    public static final String USERDB = "admin";

    // Variables para XML-RPC
    private static XmlRpcClient clientCommon;
    private static XmlRpcClient clientObject;
    public static String db = "prova3";
    private static int userId;
    
    public static void setClientCommon(XmlRpcClient client) {
        clientCommon = client;
    }
    
    public static void setClientObject(XmlRpcClient client) {
        clientObject = client;
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
        configCommon.setServerURL(new URL(ODOO_URL + "xmlrpc/2/common"));
        clientCommon = new XmlRpcClient();
        clientCommon.setConfig(configCommon);

        userId = (int) clientCommon.execute("authenticate", Arrays.asList(db, USER, PASSWORD, new HashMap<>()));

        if (userId == 0) {
            System.err.println("Error: Autenticación fallida. Verifica tus credenciales.");
            return;
        }
        
        XmlRpcClientConfigImpl configObject = new XmlRpcClientConfigImpl();
        configObject.setServerURL(new URL(ODOO_URL + "xmlrpc/2/object"));
        clientObject = new XmlRpcClient();
        clientObject.setConfig(configObject);

        System.out.println("Cliente XML-RPC inicializado y autenticado. User ID: " + userId);
    }

    public static List<Map<String, Object>> obtenerContactos() {
        try {
            // Parámetros para la llamada `search_read` sin filtros
            List<Object> params = Arrays.asList(
                db, 2, PASSWORD,
                "res.partner", "search_read",
                Arrays.asList(Arrays.asList()), // Sin filtro, obtendremos todos los contactos
                new HashMap<String, Object>() {{
                    put("fields", Arrays.asList("name", "phone", "owner_id", "id")); // Campos que queremos obtener
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
                    Integer id = (Integer) contacto.get("id");
                    
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
                            contactoMap.put("id", id);
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
                db, 2, PASSWORD,
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

    public static List<Map<String, Object>> obtenerEventosDeUser() {
        try {
            // Parámetros para la llamada `search_read` al modelo `calendar.event`
            List<Object> params = Arrays.asList(
                db, 2, PASSWORD,
                "calendar.event", "search_read",
                Arrays.asList(Arrays.asList()), // Sin filtros, obtener todos los eventos
                new HashMap<String, Object>() {{
                    put("fields", Arrays.asList("id", "name", "start", "stop", "user_id")); // Campos requeridos
                }}
            );

            // Ejecutar la llamada XML-RPC
            Object[] result = (Object[]) clientObject.execute("execute_kw", params);

            if (result == null || result.length == 0) {
                System.err.println("No se obtuvieron datos de eventos de Odoo.");
                return new ArrayList<>();
            }

            // Crear lista para almacenar los eventos procesados
            List<Map<String, Object>> eventos = new ArrayList<>();

            for (Object item : result) {
                if (item instanceof Map) {
                    Map<String, Object> evento = (Map<String, Object>) item;


                    // Extraer y procesar los datos necesarios
                    Integer id = (Integer) evento.get("id");
                    String name = (String) evento.get("name");
                    String start = (String) evento.get("start");
                    String stop = (String) evento.get("stop");
                    Object userIdObj = evento.get("user_id");

                    // Validar y extraer el `user_id`
                    Integer userId = null;
                    if (userIdObj instanceof Object[] && ((Object[]) userIdObj).length > 0) {
                        Object[] userArray = (Object[]) userIdObj;
                        if (userArray[0] instanceof Integer) {
                            userId = (Integer) userArray[0];
                        }
                    }

                    // Agregar evento a la lista si los datos son válidos
                    if (name != null && start != null && stop != null && userId != null && userId == registre.id_odoo) {
                        Map<String, Object> eventoMap = new HashMap<>();
                        eventoMap.put("name", name);
                        eventoMap.put("start", start);
                        eventoMap.put("stop", stop);
                        eventoMap.put("user_id", userId);
                        eventoMap.put("id", id);
                        eventos.add(eventoMap);
                    }
                }
            }

            System.out.println("Eventos obtenidos de Odoo: " + eventos);
            return eventos;

        } catch (XmlRpcException e) {
            System.err.println("Error al obtener eventos: " + e.getMessage());
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public static List<Map<String, Object>> obtenerEventos() {
        try {
            // Parámetros para la llamada `search_read` al modelo `calendar.event`
            List<Object> params = Arrays.asList(
                db, userId, PASSWORD,
                "calendar.event", "search_read",
                Arrays.asList(Arrays.asList()), // Sin filtros, obtener todos los eventos
                new HashMap<String, Object>() {{
                    put("fields", Arrays.asList("id", "name", "start", "stop", "user_id")); // Campos requeridos
                }}
            );

            // Ejecutar la llamada XML-RPC
            Object[] result = (Object[]) clientObject.execute("execute_kw", params);

            if (result == null || result.length == 0) {
                System.err.println("No se obtuvieron datos de eventos de Odoo.");
                return new ArrayList<>();
            }

            // Crear lista para almacenar los eventos procesados
            List<Map<String, Object>> eventos = new ArrayList<>();

            for (Object item : result) {
                if (item instanceof Map) {
                    Map<String, Object> evento = (Map<String, Object>) item;


                    // Extraer y procesar los datos necesarios
                    Integer id = (Integer) evento.get("id");
                    String name = (String) evento.get("name");
                    String start = (String) evento.get("start");
                    String stop = (String) evento.get("stop");
                    Object userIdObj = evento.get("user_id");

                    // Validar y extraer el `user_id`
                    Integer userId = null;
                    if (userIdObj instanceof Object[] && ((Object[]) userIdObj).length > 0) {
                        Object[] userArray = (Object[]) userIdObj;
                        if (userArray[0] instanceof Integer) {
                            userId = (Integer) userArray[0];
                        }
                    }

                    // Agregar evento a la lista si los datos son válidos
                    if (name != null && start != null && stop != null && userId != null) {
                        Map<String, Object> eventoMap = new HashMap<>();
                        eventoMap.put("name", name);
                        eventoMap.put("start", start);
                        eventoMap.put("stop", stop);
                        eventoMap.put("user_id", userId);
                        eventoMap.put("id", id);
                        eventos.add(eventoMap);
                    }
                }
            }

            System.out.println("Eventos obtenidos de Odoo: " + eventos);
            return eventos;

        } catch (XmlRpcException e) {
            System.err.println("Error al obtener eventos: " + e.getMessage());
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public static int obtenerUserIdPorLogin(String login) {
        // Obtener la lista de usuarios con login desde Odoo
        List<Map<String, Object>> usuarios = obtenerUsuariosConLogin();
    
        // Recorrer la lista de usuarios y buscar por login
        for (Map<String, Object> usuario : usuarios) {
            String usuarioLogin = (String) usuario.get("login");
            
            // Si encontramos el login, devolver el id del usuario
            if (usuarioLogin != null && usuarioLogin.equals(login)) {
                Integer userId = (Integer) usuario.get("id");
                if (userId != null) {
                    return userId;  // Retornar el id del usuario encontrado
                }
            }
        }
    
        // Si no se encontró el usuario, retornar un valor predeterminado o -1 para indicar no encontrado
        System.err.println("Usuario con login " + login + " no encontrado.");
        return -1;
    }

    public static void crearEvento(String nombreEvento, Date fechaInicio, Date fechaFin, int userId) {
        try {
            // Convertir las fechas a formato Odoo (usualmente es el formato datetime de Odoo)
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String startDate = formatter.format(fechaInicio); // Fecha de inicio en el formato de Odoo
            String endDate = formatter.format(fechaFin); // Fecha de fin en el formato de Odoo

            // Crear un mapa con los datos del nuevo evento
            Map<String, Object> eventoData = new HashMap<>();
            eventoData.put("name", nombreEvento);
            eventoData.put("start", startDate);
            eventoData.put("stop", endDate);
            eventoData.put("user_id", userId); // Se asume que user_id es un campo de relación con `res.users`
            eventoData.put("create_uid", userId);
            eventoData.put("write_uid", userId);
            System.out.println(eventoData);

            // Parámetros para la llamada XML-RPC a `create` en el modelo `calendar.event`
            List<Object> params = Arrays.asList(
                db, 2, PASSWORD,  // Conexión a Odoo
                "calendar.event", "create", // Llamada a la función create en el modelo calendar.event
                new Object[]{eventoData}  // Los datos del nuevo evento
            );

            // Ejecutar la llamada XML-RPC para crear el evento
            Object result = clientObject.execute("execute_kw", params);

            if (result != null) {
                System.out.println("Evento creado con éxito. ID: " + result);
            } else {
                System.err.println("No se pudo crear el evento.");
            }

        } catch (XmlRpcException e) {
            System.err.println("Error al crear el evento: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void eliminarEventoOdoo(Integer idEvento) {
        try {
            // Parámetros para la llamada `unlink` al modelo `calendar.event`
            List<Object> params = Arrays.asList(
                db, 2, PASSWORD, // Conexión y credenciales
                "calendar.event", "unlink", // Modelo y método
                Arrays.asList(Arrays.asList(idEvento)) // Lista con el ID del evento que se desea eliminar
            );
    
            System.out.println("Evento eliminado con ID = " + idEvento);
            // Ejecutar la llamada XML-RPC
            Object result = clientObject.execute("execute_kw", params);
    
        } catch (XmlRpcException e) {
            System.err.println("Error al eliminar evento en Odoo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void crearContacto(int userId, String nombre, String telefono, String email) throws XmlRpcException {
        try {
            Map<String, Object> contactoData = new HashMap<>();
            contactoData.put("name", nombre);
            contactoData.put("phone", telefono);
            contactoData.put("email", email);

            List<Object> params = Arrays.asList(
                db, userId, registre.pass,  // Conexión a Odoo
                "res.partner", "create",
                new Object[]{contactoData}  // Los datos del nuevo contacto
            );

            // Ejecutar la llamada XML-RPC para crear el evento
            Object result = clientObject.execute("execute_kw", params);

            if (result != null) {
                System.out.println("Contacto creado con éxito. ID: " + result);
            } 
            System.out.println(contactoData);

        } catch (XmlRpcException e) {
            System.err.println("Error al crear el contacto: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void crearUsuario(String login, String pass) {
        try {
            Map<String, Object> usuarioData = new HashMap<>();
            usuarioData.put("login", login);
            usuarioData.put("name", login);
            usuarioData.put("password", pass);

            List<Object> params = Arrays.asList(
                db, 2, PASSWORD,  // Conexión a Odoo
                "res.users", "create",
                new Object[]{usuarioData}  // Los datos del nuevo contacto
            );

            // Ejecutar la llamada XML-RPC para crear el evento
            Object result = clientObject.execute("execute_kw", params);

            if (result != null) {
                System.out.println("Usuario creado con éxito. ID: " + result);
            } 
            System.out.println(usuarioData);

        } catch (XmlRpcException e) {
            System.err.println("Error al crear el usuario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void modificarContacto(int contactId, String nuevoNombre, String nuevoTelefono, int userId) {
        try {
            // Crear un mapa con los valores a actualizar
            Map<String, Object> valoresActualizados = new HashMap<>();
            valoresActualizados.put("name", nuevoNombre);
            valoresActualizados.put("phone", nuevoTelefono);
    
            // Validar que se incluyan campos para modificar
            if (valoresActualizados.isEmpty()) {
                System.out.println("No hay valores para actualizar.");
                return;
            }
    
            // Parámetros para la llamada XML-RPC
            Object[] params = new Object[]{
                db,                    // Base de datos
                userId,                // UID del usuario autenticado
                registre.pass,         // Contraseña del usuario
                "res.partner",         // Nombre del modelo
                "write",               // Método para modificar
                Arrays.asList(contactId),   // ID del contacto (en una lista)
                valoresActualizados    // Nuevos valores
            };
    
            // Ejecutar la llamada XML-RPC
            Boolean resultado = (Boolean) clientObject.execute("execute_kw", params);
    
            if (resultado) {
                System.out.println("Contacto modificado con éxito.");
            } else {
                System.out.println("Error al modificar el contacto.");
            }
        } catch (XmlRpcException e) {
            System.err.println("Error al modificar el contacto: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void eliminarContactOdoo(Integer idContact) {
        try {
            // Parámetros para la llamada `unlink` al modelo `calendar.event`
            List<Object> params = Arrays.asList(
                db, registre.id_odoo, registre.pass, // Conexión y credenciales
                "res.partner", "unlink", // Modelo y método
                Arrays.asList(Arrays.asList(idContact)) // Lista con el ID del evento que se desea eliminar
            );
    
            System.out.println("Evento eliminado con ID = " + idContact);
            // Ejecutar la llamada XML-RPC
            Object result = clientObject.execute("execute_kw", params);
    
        } catch (XmlRpcException e) {
            System.err.println("Error al eliminar evento en Odoo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
