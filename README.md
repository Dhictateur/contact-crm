# Contact CRM - Manual de Usuario

## Descripción
**Contact CRM** es una aplicación de gestión de contactos desarrollada en Java. Permite a los usuarios y administradores conectarse a un servidor Odoo, donde pueden iniciar sesión con sus credenciales y gestionar su lista de contactos. La aplicación está diseñada para facilitar la gestión de contactos y su integración con un sistema ERP como Odoo.

## Requisitos

Antes de ejecutar el programa, asegúrate de cumplir con los siguientes requisitos:

- **Java 8 o superior** instalado en tu máquina.
- **Servidor Odoo** en funcionamiento, configurado para aceptar conexiones XML-RPC.
- **Usuario** creado en el servidor Odoo para poder iniciar sesión.
- **JDBC Driver para PostgreSQL** si estás utilizando una base de datos PostgreSQL como backend de Odoo.

## Instalación y Configuración

1. **Clonar el repositorio**:  
   Clona el proyecto desde el repositorio:
   ```bash
   git clone https://github.com/tu-usuario/contact-crm.git
   
2. **Configurar el servidor Odoo**:  
   Asegúrate de que el servidor Odoo esté configurado correctamente y accesible para conexiones externas a través de XML-RPC.
   
3. **Editar la configuración del servidor en el código**:
   Si es necesario, actualiza la URL de conexión a Odoo en el archivo registre.java:
   ```bash
   config.setServerURL(new URL("http://localhost:8069/xmlrpc/2/common"));  // Cambia localhost por la IP del servidor

4. **Compilar y ejecutar**:
   Compila y ejecuta el programa con el siguiente comando:
   ```bash
   javac -cp .:path/to/jar/xmlrpc-client.jar com/example/*.java
   java -cp .:path/to/jar/xmlrpc-client.jar com.example.Main

## Contribuir
   Si deseas contribuir a este proyecto, por favor envía un pull request o contacta al desarrollador a través de devforcrm@gmail.com.






   
