package co.edu.uniquindio.empresacomercial.empresacomercial.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static final String SERVIDOR = "localhost";
    private static final String PUERTO = "1433";
    private static final String BASE_DATOS = "ProveedoresBD";
    private static final String USUARIO = "app_proveedores";
    private static final String PASSWORD = "123";

    private static final String URL = "jdbc:sqlserver://" + SERVIDOR + ":" + PUERTO
            + ";databaseName=" + BASE_DATOS
            + ";encrypt=true;trustServerCertificate=true";

    // Clase de utilidad: no se crean objetos de ella
    private ConexionBD() {
    }

    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, PASSWORD);
    }
}
