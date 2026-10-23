package co.edu.uniquindio.empresacomercial.empresacomercial.model;

/**
 * Las consultas que exige el enunciado: 3 con JOIN de al menos 3 tablas y
 * 2 con subconsultas. El SQL de cada una está en ConsultaDAO.
 */
public enum Consulta {

    CATALOGO(
            "Catálogo de productos con categoría, proveedor y precio",
            "JOIN de 4 tablas: Producto, Categoria, Proveedor_Producto y Proveedor.",
            Tipo.JOIN, Parametro.NINGUNO),

    PROVEEDORES_POR_CIUDAD(
            "Proveedores por ciudad con cantidad de productos y precio promedio",
            "JOIN de 3 tablas: Proveedor, Ciudad y Proveedor_Producto (incluye proveedores sin productos).",
            Tipo.JOIN, Parametro.NINGUNO),

    PRODUCTOS_EN_CIUDAD(
            "Productos que se pueden pedir en una ciudad",
            "JOIN de 5 tablas: Proveedor, Ciudad, Proveedor_Producto, Producto y Categoria. Elija la ciudad.",
            Tipo.JOIN, Parametro.CIUDAD),

    PRODUCTOS_CON_VARIOS_PROVEEDORES(
            "Productos ofrecidos por más de un proveedor",
            "Subconsulta correlacionada sobre Proveedor_Producto: cuenta, precio mínimo y máximo por producto.",
            Tipo.SUBCONSULTA, Parametro.NINGUNO),

    OFERTAS_SOBRE_EL_PROMEDIO(
            "Ofertas con precio por encima del promedio de su producto",
            "Subconsulta que calcula el precio promedio de cada producto y lo compara con cada oferta.",
            Tipo.SUBCONSULTA, Parametro.NINGUNO);

    public enum Tipo {JOIN, SUBCONSULTA}

    /** Dato que el usuario debe elegir antes de ejecutar la consulta. */
    public enum Parametro {NINGUNO, CIUDAD}

    private final String titulo;
    private final String descripcion;
    private final Tipo tipo;
    private final Parametro parametro;

    Consulta(String titulo, String descripcion, Tipo tipo, Parametro parametro) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.parametro = parametro;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public Parametro getParametro() {
        return parametro;
    }

    public boolean necesitaParametro() {
        return parametro != Parametro.NINGUNO;
    }

    // Así se ve en el combo de la pantalla
    @Override
    public String toString() {
        return tipo + " - " + titulo;
    }
}