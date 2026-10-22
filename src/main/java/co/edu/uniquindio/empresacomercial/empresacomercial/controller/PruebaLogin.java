package co.edu.uniquindio.empresacomercial.empresacomercial.controller;

import co.edu.uniquindio.empresacomercial.empresacomercial.model.Bitacora;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Usuario;

public class PruebaLogin {

    public static void main(String[] args) {
        LoginController login = new LoginController();

        // 1. Casos que deben fallar
        intentar(login, "", "", "Campos vacíos");
        intentar(login, "abc", "admin123", "Cédula con letras");
        intentar(login, "1", "otra", "Contraseña incorrecta");
        intentar(login, "999", "admin123", "Cédula que no existe");

        try {
            // 2. Ingreso del administrador
            Usuario admin = login.iniciarSesion("1", "admin123");
            System.out.println("Ingresó: " + admin.getNombre() + " " + admin.getApellido()
                    + " (" + admin.getTipoUsuario() + "), puede crear usuarios: " + admin.puedeCrearUsuario());

            Bitacora bitacora = Sesion.getBitacoraActual();
            System.out.println("Bitácora (id " + bitacora.getId() + "): " + bitacora.getRegistro());

            // 3. Salida
            login.cerrarSesion();
            System.out.println("Sesión cerrada: " + bitacora.getRegistro());
            System.out.println("Usuario en sesión después de salir: " + Sesion.getUsuarioActual());

            // 4. Ingreso de un usuario esporádico
            Usuario espo = login.iniciarSesion("3", "espo123");
            System.out.println("Ingresó: " + espo.getNombre() + " " + espo.getApellido()
                    + " (" + espo.getTipoUsuario() + "), puede modificar datos: " + espo.puedeModificarDatos());
            login.cerrarSesion();

        } catch (NegocioException e) {
            System.out.println("Error inesperado: " + e.getMessage());
        }
    }

    private static void intentar(LoginController login, String cedula, String password, String caso) {
        try {
            login.iniciarSesion(cedula, password);
            System.out.println("ATENCIÓN: " + caso + " no debía permitir el ingreso");
        } catch (NegocioException e) {
            System.out.println(caso + ": " + e.getMessage());
        }
    }
}
