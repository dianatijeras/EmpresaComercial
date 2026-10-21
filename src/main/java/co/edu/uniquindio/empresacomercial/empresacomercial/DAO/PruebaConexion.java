package co.edu.uniquindio.empresacomercial.empresacomercial.DAO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PruebaConexion {

    public static void main(String[] args) {
        String consulta = "SELECT DB_NAME(), SUSER_SNAME(), (SELECT COUNT(*) FROM Ciudad)";

        try (Connection con = ConexionBD.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(consulta)) {

            System.out.println("Conexión exitosa");
            if (rs.next()) {
                System.out.println("Base de datos: " + rs.getString(1));
                System.out.println("Usuario: " + rs.getString(2));
                System.out.println("Ciudades en la tabla: " + rs.getInt(3));
            }

        } catch (SQLException e) {
            System.out.println("No se pudo conectar: " + e.getMessage());

            String mensaje = (e.getMessage() == null) ? "" : e.getMessage();
            if (e.getErrorCode() == 18456) {
                System.out.println("Pista: usuario o contraseña incorrectos (revisa ConexionBD).");
            } else if (e.getErrorCode() == 4060) {
                System.out.println("Pista: la base ProveedoresDB no existe o el usuario no tiene acceso a ella.");
            } else if (mensaje.contains("TCP/IP")) {
                System.out.println("Pista: revisa que TCP/IP esté habilitado, el puerto sea 1433 y el servicio reiniciado.");
            }
        }
    }
}
