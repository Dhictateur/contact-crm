package com.example;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;

public class AppTest {

    private registre registre;
    private Connection mockConnection;
    
    @BeforeEach
    public void setUp() {
        registre = new registre();
        mockConnection = mock(Connection.class);
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
        String nombre = "1234@gmail.com";
        String contrasenya = "1234";

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

    // Test para inicializar la lista de contactos
    @Test
    public void testInicializarListaContactos() {
        List<contact> contactosOriginales = new ArrayList<>();
        contactosOriginales.add(new contact("Juan Pérez", "123456789"));
        contactosOriginales.add(new contact("Ana López", "987654321"));
        
        assertEquals(2, contactosOriginales.size());
    }
}
