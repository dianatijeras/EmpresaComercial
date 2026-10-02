package co.edu.uniquindio.empresacomercial.empresacomercial.model;

public class Usuario {

    private  int id;
    private String nombre;
    private String apellido;
    private String password;
    private TipoUsuario tipoUsuario;

    public Usuario() {
    }

    public Usuario(int id, String nombre, String apellido, String password, TipoUsuario tipoUsuario) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.password = password;
        this.tipoUsuario = tipoUsuario;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    /**
     * Los permisos los define el tipo de usuario, aqui solo se pregunta
     * @return
     */


    public boolean puedeCrearUsuario() {
        return tipoUsuario != null && tipoUsuario.puedeCrearUsuario();
    }

    public boolean puedeModificarDatos() {
        return tipoUsuario != null && tipoUsuario.puedeModificarDatos();
    }

    public boolean esAdministrador() {
        return tipoUsuario != null && tipoUsuario.esAdministrador();
    }
}
