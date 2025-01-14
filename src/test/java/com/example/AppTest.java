package com.example;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;



public class AppTest {
    
    private odoo odooMock;
    private XmlRpcClient clientCommonMock;
    private XmlRpcClient clientObjectMock;

    private registre registre;
    private Connection connectionMock;

    private chat chatMock;
    
    @BeforeEach
    public void setUp() {
        chatMock = new chat();
        odooMock = new odoo();
        clientCommonMock = mock(XmlRpcClient.class);
        clientObjectMock = mock(XmlRpcClient.class);
        connectionMock = mock(Connection.class);
        
        odoo.setClientCommon(clientCommonMock);
        odoo.setClientObject(clientObjectMock);
    }

    // Test para verificar la funcionalidad de filtrado de contactos
    @Test
    public void testFiltrarContactos() {
        List<contact> contactosOriginales = new ArrayList<>();
        contactosOriginales.add(new contact("Juan Pérez", "123456789"));
        contactosOriginales.add(new contact("Ana López", "987654321"));
        
        List<contact> contactosFiltrados = new ArrayList<>(contactosOriginales);
        
        String filtro = "Ana";
        contactosFiltrados.removeIf(contacto -> 
            !contacto.getNom().toLowerCase().contains(filtro.toLowerCase()));
        
        assertEquals(1, contactosFiltrados.size());
        assertEquals("Ana López", contactosFiltrados.get(0).getNom());
    }

    @Test
    public void testVerificarLogin() {
        // Simular el comportamiento de verificarUsuarioEnOdoo
        String nombre = "admin";
        String contrasenya = "admin";

        // Aquí simula que el usuario se autentica correctamente
        boolean autenticado = registre.verificarUsuarioEnOdoo(nombre, contrasenya);

        // Verificar que la autenticación fue exitosa
        assertTrue(autenticado);

        // Verificar que el UID no es null
        assertNotNull(registre.getUid());
    }
    

    @Test
    public void testLogout() {
        // Simular un login exitoso
        contact.LoginSuccess = 1;

        // Llamar al método de cerrar sesión
        registre.cerrarSesion();

        // Verificar que el UID se ha restablecido a null usando el getter
        assertNull(registre.getUid());

        // Verificar que la variable LoginSuccess se restableció a 0
        assertEquals(0, contact.LoginSuccess);

        // Verificar que la ventana de login se ha mostrado de nuevo
        assertTrue(registre.loginFrame.isVisible());
    }

    @Test
    public void testInicializarListaContactos() throws Exception {
        // Simular respuesta de Odoo con contactos válidos y un campo owner_id
        List<Map<String, Object>> contactosSimulados = new ArrayList<>();
        Map<String, Object> contacto1 = new HashMap<>();
        contacto1.put("name", "Juan Pérez");
        contacto1.put("phone", "123456789");
        contacto1.put("owner_id", new Object[]{1, "Administrador"});

        Map<String, Object> contacto2 = new HashMap<>();
        contacto2.put("name", "Ana López");
        contacto2.put("phone", "987654321");
        contacto2.put("owner_id", new Object[]{2, "Usuario"});

        contactosSimulados.add(contacto1);
        contactosSimulados.add(contacto2);

        // Simular el cliente Odoo
        when(clientObjectMock.execute(eq("execute_kw"), anyList())).thenReturn(contactosSimulados.toArray());

        // Llamar al método
        List<Map<String, Object>> contactos = odooMock.obtenerContactos();

        // Verificar los resultados
        assertNotNull(contactos);
        assertEquals(2, contactos.size());

        // Verificar primer contacto
        assertEquals("Juan Pérez", contactos.get(0).get("name"));
        assertEquals("123456789", contactos.get(0).get("phone"));
        assertEquals(1, contactos.get(0).get("owner_id"));

        // Verificar segundo contacto
        assertEquals("Ana López", contactos.get(1).get("name"));
        assertEquals("987654321", contactos.get(1).get("phone"));
        assertEquals(2, contactos.get(1).get("owner_id"));
    }

    @Test
    public void testObtenerUsuariosConLogin() throws Exception {
        // Simular respuesta de Odoo
        Object[] respuestaSimulada = {
            new HashMap<String, Object>() {{
                put("name", "Juan Pérez");
                put("phone", "123456789");
                put("login", "juan.perez@example.com");
            }},
            new HashMap<String, Object>() {{
                put("name", "Ana López");
                put("login", "ana.lopez@example.com");
            }}
        };

        when(clientObjectMock.execute(eq("execute_kw"), anyList())).thenReturn(respuestaSimulada);

        // Llamar al método
        List<Map<String, Object>> usuarios = odooMock.obtenerUsuariosConLogin();

        // Verificar resultados
        assertNotNull(usuarios);
        assertEquals(2, usuarios.size());
        assertEquals("Juan Pérez", usuarios.get(0).get("name"));
        assertEquals("ana.lopez@example.com", usuarios.get(1).get("login"));
    }

    @Test
    public void testVerificarTipoUsuario_Admin() throws Exception {
        Connection conexion = null;
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(odoo.ODOO_URL + "xmlrpc/2/object"));
        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);

        // Llamar al método verificarTipoUsuario
        String result = registre.verificarTipoUsuario(client, odoo.db, odoo.PASSWORD, "admin");

        // Verificar que el tipo de usuario sea "Admin"
        assertEquals("Admin", result);
    }

    @Test
    public void testVerificarTipoUsuario_User() throws Exception {
        Connection conexion = null;
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(odoo.ODOO_URL + "xmlrpc/2/object"));
        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);

        // Llamar al método verificarTipoUsuario
        String result = registre.verificarTipoUsuario(client, odoo.db, odoo.PASSWORD, "test@gmail.com");

        // Verificar que el tipo de usuario sea "User"
        assertEquals("User", result);
    }

    @Test
    public void testUsuarioNoEncontrado() throws Exception {
        Connection conexion = null;
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL("http://localhost:8069/xmlrpc/2/object"));
        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);

        // Llamar al método verificarTipoUsuario
        String result = registre.verificarTipoUsuario(client, odoo.db, odoo.PASSWORD, "test_no@gmail.com");

        // Verificar que el tipo de usuario sea "User" si no se encuentra el usuario
        assertEquals("User", result);
    }
}
