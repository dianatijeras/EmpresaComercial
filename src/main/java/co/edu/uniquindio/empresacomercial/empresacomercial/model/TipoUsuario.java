package co.edu.uniquindio.empresacomercial.empresacomercial.model;

public enum TipoUsuario {

    ADMINISTRADOR,
    TRADICIONAL,
    ESPORADICO;

    /**
     * Solo el administrador puede crear usuarios
     * @return
     */
    public boolean puedeCrearUsuario(){
        return this == ADMINISTRADOR;
    }

    /**
     * Pueden ejecutar los CRUD el tradicional y el administrador
     * @return
     */
    public boolean puedeModificarDatos(){
        return this == ADMINISTRADOR || this == TRADICIONAL;
    }

    public boolean esAdministrador() {
        return this == ADMINISTRADOR;
    }
}
