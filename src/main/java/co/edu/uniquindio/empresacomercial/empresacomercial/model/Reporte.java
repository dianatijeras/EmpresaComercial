package co.edu.uniquindio.empresacomercial.empresacomercial.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Los reportes en PDF que exige el enunciado: 3 de productos y proveedores y
 * 2 que relacionan los productos con sus categorías. Cada uno dice qué filtros
 * puede usar el usuario. El SQL de cada uno está en ReporteDAO.
 */
public enum Reporte {

    PRODUCTOS_POR_PROVEEDOR(
            "Productos por proveedor",
            "Los productos que ofrece cada proveedor con su precio. Puede filtrar por proveedor y por rango de precio.",
            new String[]{"Proveedor", "Producto", "Precio"},
            false,
            EnumSet.of(Filtro.PROVEEDOR, Filtro.PRECIO_MIN, Filtro.PRECIO_MAX)),

    DIRECTORIO_DE_PROVEEDORES(
            "Directorio de proveedores",
            "Datos de contacto de los proveedores y cuántos productos ofrece cada uno. Puede filtrar por ciudad.",
            new String[]{"Proveedor", "NIT", "Teléfono", "Dirección", "Ciudad", "Productos"},
            true,
            EnumSet.of(Filtro.CIUDAD)),

    PROVEEDORES_DE_UN_PRODUCTO(
            "Proveedores de un producto",
            "Comparativo de precios entre los proveedores de cada producto. Puede filtrar por producto y por ciudad del proveedor.",
            new String[]{"Producto", "Proveedor", "Ciudad", "Precio"},
            false,
            EnumSet.of(Filtro.PRODUCTO, Filtro.CIUDAD)),

    PRODUCTOS_POR_CATEGORIA(
            "Productos por categoría",
            "Los productos de cada categoría, con cuántos proveedores los ofrecen y su precio mínimo y máximo. Puede filtrar por categoría.",
            new String[]{"Categoría", "Producto", "Proveedores", "Precio mínimo", "Precio máximo"},
            false,
            EnumSet.of(Filtro.CATEGORIA)),

    OFERTAS_POR_CATEGORIA(
            "Ofertas por categoría",
            "Cada producto de una categoría con sus proveedores y precios. Puede filtrar por categoría y por rango de precio.",
            new String[]{"Categoría", "Producto", "Proveedor", "Precio"},
            false,
            EnumSet.of(Filtro.CATEGORIA, Filtro.PRECIO_MIN, Filtro.PRECIO_MAX));

    /** Cada dato que el usuario puede elegir para filtrar un reporte. */
    public enum Filtro {
        PROVEEDOR, CIUDAD, PRODUCTO, CATEGORIA, PRECIO_MIN, PRECIO_MAX
    }

    private final String titulo;
    private final String descripcion;
    private final String[] encabezados;
    private final boolean horizontal;
    private final Set<Filtro> filtros;

    Reporte(String titulo, String descripcion, String[] encabezados, boolean horizontal, Set<Filtro> filtros) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.encabezados = encabezados;
        this.horizontal = horizontal;
        this.filtros = filtros;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /** Títulos de las columnas tal como salen en el PDF. */
    public String[] getEncabezados() {
        return encabezados.clone();
    }

    /** true si el PDF va en hoja horizontal (porque tiene muchas columnas). */
    public boolean esHorizontal() {
        return horizontal;
    }

    public Set<Filtro> getFiltros() {
        return filtros;
    }

    // Así se ve en el combo de la pantalla
    @Override
    public String toString() {
        return titulo;
    }
}