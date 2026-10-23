package co.edu.uniquindio.empresacomercial.empresacomercial.model;

import java.util.ArrayList;
import java.util.List;

public class Proveedor {

    private String nit;
    private String nombre;
    private String telefono;
    private String direccion;
    private int idCiudad;
    private List<ProveedorProducto> productos = new ArrayList<>();

    public Proveedor() {
    }

    public Proveedor(String nit, String nombre, String telefono, String direccion, int idCiudad) {
        this.nit = nit;
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
        this.idCiudad = idCiudad;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public int getIdCiudad() {
        return idCiudad;
    }

    public void setIdCiudad(int idCiudad) {
        this.idCiudad = idCiudad;
    }

    public List<ProveedorProducto> getProductos() {
        return productos;
    }

    public void setProductos(List<ProveedorProducto> productos) {
        this.productos = productos;
    }


    /**
     *  Devuelve el producto con su precio si el proveedor lo ofrece
     *  (se compara por id), o null.
     * @param producto
     * @return
     */
    public ProveedorProducto buscarProducto(Producto producto) {
        for (ProveedorProducto pp : productos) {
            if (pp.getProducto().getId() == producto.getId()) {
                return pp;
            }
        }
        return null;
    }

    /**
     * Devuelve true si el proveedor ofrece el producto (se compara por id).
     * @param producto
     * @return
     */
    public boolean tieneProducto(Producto producto) {
        return buscarProducto(producto) != null;
    }

    /**
     * Agrega el producto con su precio si no estaba.
     * Devuelve true si lo agregó.
     * @param producto
     * @param precio
     * @return
     */
    public boolean agregarProducto(Producto producto, double precio) {
        if (tieneProducto(producto)) {
            return false;
        }
        productos.add(new ProveedorProducto(producto, precio));
        return true;
    }

    /**
     * Quita el producto (se compara por id). Devuelve true si lo quitó.
     * @param producto
     * @return
     */
    public boolean quitarProducto(Producto producto) {
        return productos.removeIf(pp -> pp.getProducto().getId() == producto.getId());
    }
}
