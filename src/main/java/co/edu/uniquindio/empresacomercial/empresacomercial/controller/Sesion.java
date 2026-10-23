package co.edu.uniquindio.empresacomercial.empresacomercial.controller;

import co.edu.uniquindio.empresacomercial.empresacomercial.model.Bitacora;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Usuario;

public class Sesion {

    private static Usuario usuarioActual;
    private static Bitacora bitacoraActual;

    // Clase de utilidad: no se crean objetos de ella.
    private Sesion() {
    }

    /**
     * Inicia la sesión con el usuario proporcionado y establece la bitácora actual como nula.
     * @param usuario
     */
    public static void iniciar(Usuario usuario) {
        usuarioActual = usuario;
        bitacoraActual = null;
    }

    /**
     * Inicia la sesión con el usuario y la bitácora proporcionados.
     * @param usuario
     * @param bitacora
     */
    public static void iniciar(Usuario usuario, Bitacora bitacora) {
        usuarioActual = usuario;
        bitacoraActual = bitacora;
    }

    /**
     * Cierra la sesión actual, estableciendo el usuario y la bitácora como nulos.
     */
    public static void cerrar() {
        usuarioActual = null;
        bitacoraActual = null;
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static Bitacora getBitacoraActual() {
        return bitacoraActual;
    }
}
