package co.edu.uniquindio.empresacomercial.empresacomercial.controller;

import co.edu.uniquindio.empresacomercial.empresacomercial.model.Categoria;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Producto;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.TipoUsuario;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Usuario;

import java.util.List;

public class PruebaControllers {

    private interface Accion {
        void ejecutar() throws NegocioException;
    }

    // Ejecuta algo que DEBE fallar y muestra el mensaje de error.
    private static void esperarError(String caso, Accion accion) {
        try {
            accion.ejecutar();
            System.out.println("ATENCIÓN: \"" + caso + "\" debía fallar");
        } catch (NegocioException e) {
            System.out.println(caso + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        CategoriaController categorias = new CategoriaController();
        ProductoController productos = new ProductoController();
        ProveedorController proveedores = new ProveedorController();
        UsuarioController usuarios = new UsuarioController();
        BitacoraController bitacora = new BitacoraController();

        Usuario tradicional = new Usuario(2, "Usuario", "Tradicional", "x", TipoUsuario.TRADICIONAL);
        Usuario admin = new Usuario(1, "Administrador", "Sistema", "x", TipoUsuario.ADMINISTRADOR);

        try {
            Sesion.iniciar(tradicional);

            //CATEGORÍA
            System.out.println("Categoria");
            esperarError("Crear con nombre vacío", () -> categorias.crear("  ", "x"));
            Categoria cat = categorias.crear("  Categoría de prueba ", "Solo para probar");
            System.out.println("Categoría creada: " + cat.getNombre() + " (id " + cat.getId() + ")");
            categorias.actualizar(cat.getId(), cat.getNombre(), "Descripción editada");
            System.out.println("Categoría actualizada: " + categorias.buscarPorId(cat.getId()).getDescripcion());

            //PRODUCTO
            System.out.println("Producto");
            esperarError("Precio negativo", () -> productos.crear("Producto de prueba", -5, cat.getId()));
            esperarError("Categoría inexistente", () -> productos.crear("Producto de prueba", 1500, 9999));
            Producto prod = productos.crear("Producto de prueba", 1500, cat.getId());
            System.out.println("Producto creado: " + prod.getNombre() + " (id " + prod.getId() + ")");
            productos.actualizar(prod.getId(), prod.getNombre(), 2000, cat.getId());
            System.out.println("Producto actualizado, precio: " + productos.buscarPorId(prod.getId()).getPrecio());

            //PROVEEDOR
            System.out.println("Proveedor");
            String nit = "999999999-9";
            proveedores.crear(nit, "Proveedor de prueba", "3000000000", "Calle 1 # 2-3", 1, List.of(prod));
            System.out.println("Proveedor creado con "
                    + proveedores.buscarPorNit(nit).getProductos().size() + " producto(s)");
            esperarError("NIT repetido", () -> proveedores.crear(nit, "Otro", null, null, 1, null));
            esperarError("Ciudad inexistente", () -> proveedores.crear("888888888-8", "Otro", null, null, 9999, null));

            proveedores.agregarProducto(nit, 1);
            System.out.println("Tras agregar otro producto: "
                    + proveedores.buscarPorNit(nit).getProductos().size());
            esperarError("Producto repetido", () -> proveedores.agregarProducto(nit, 1));
            proveedores.quitarProducto(nit, 1);
            System.out.println("Tras quitar uno: " + proveedores.buscarPorNit(nit).getProductos().size());

            esperarError("Eliminar categoría con productos", () -> categorias.eliminar(cat.getId()));

            // Limpieza
            proveedores.eliminar(nit);
            productos.eliminar(prod.getId());
            categorias.eliminar(cat.getId());
            System.out.println("Datos de prueba eliminados");

            //USUARIO
            System.out.println("Usuario");
            esperarError("Tradicional intenta crear usuario",
                    () -> usuarios.crear("12345", "Prueba", "Usuario", "clave", TipoUsuario.TRADICIONAL));

            Sesion.iniciar(admin);
            esperarError("Cédula con letras",
                    () -> usuarios.crear("abc", "Prueba", "Usuario", "clave", TipoUsuario.TRADICIONAL));
            usuarios.crear("12345", "Prueba", "Usuario", "clave", TipoUsuario.TRADICIONAL);
            System.out.println("Usuario creado: 12345 (" + usuarios.buscarPorId(12345).getTipoUsuario() + ")");
            esperarError("Cédula repetida",
                    () -> usuarios.crear("12345", "Prueba", "Usuario", "clave", TipoUsuario.TRADICIONAL));
            esperarError("Segundo administrador",
                    () -> usuarios.crear("54321", "Otro", "Admin", "x", TipoUsuario.ADMINISTRADOR));

            usuarios.actualizar(12345, "Prueba", "Usuario", "", TipoUsuario.ESPORADICO);
            Usuario actualizado = usuarios.buscarPorId(12345);
            System.out.println("Usuario actualizado, tipo: " + actualizado.getTipoUsuario()
                    + ", contraseña conservada: " + actualizado.getPassword().equals("clave"));

            esperarError("Eliminar al administrador", () -> usuarios.eliminar(1));
            esperarError("Cambiar el tipo del administrador",
                    () -> usuarios.actualizar(1, "Administrador", "Sistema", "", TipoUsuario.TRADICIONAL));

            usuarios.eliminar(12345);
            System.out.println("Usuarios en la base: " + usuarios.listar().size());

            //BITÁCORA
            System.out.println("Bitácora");
            Sesion.iniciar(tradicional);
            esperarError("Tradicional consulta la bitácora", () -> bitacora.listar());
            Sesion.iniciar(admin);
            System.out.println("Administrador consulta la bitácora: " + bitacora.listar().size() + " registro(s)");

            Sesion.cerrar();

        } catch (NegocioException e) {
            System.out.println("Error inesperado: " + e.getMessage());
        }
    }
}
