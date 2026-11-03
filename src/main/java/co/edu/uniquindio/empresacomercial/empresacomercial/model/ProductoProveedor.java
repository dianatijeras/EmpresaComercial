package co.edu.uniquindio.empresacomercial.empresacomercial.model;

/**
 * Un producto tal como lo ofrece un proveedor: es el producto más el precio
 * al que ese proveedor lo vende (una fila de Proveedor_Producto).
 */
public class ProductoProveedor extends Producto {

    private double precio;

    public ProductoProveedor() {
    }

    public ProductoProveedor(int id, String nombre, int idCategoria, double precio) {
        super(id, nombre, idCategoria);
        this.precio = precio;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }
}