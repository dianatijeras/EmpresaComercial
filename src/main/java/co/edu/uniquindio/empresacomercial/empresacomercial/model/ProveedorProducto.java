package co.edu.uniquindio.empresacomercial.empresacomercial.model;

public class ProveedorProducto {

    private Producto producto;
    private double precio;

    public ProveedorProducto() {
    }

    public ProveedorProducto(Producto producto, double precio) {
        this.producto = producto;
        this.precio = precio;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }
}
