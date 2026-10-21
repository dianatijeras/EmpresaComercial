package co.edu.uniquindio.empresacomercial.empresacomercial.DAO;

import co.edu.uniquindio.empresacomercial.empresacomercial.model.Ciudad;

import java.sql.SQLException;

public class PruebaCiudadDAO {

    public static void main(String[] args) {
        CiudadDAO dao = new CiudadDAO();

        try {
            Ciudad nueva = new Ciudad();
            nueva.setNombre("Ciudad de prueba");
            dao.insertar(nueva);
            System.out.println("Insertada: " + nueva.getNombre() + " (id " + nueva.getId() + ")");

            System.out.println("Ciudades en la base: " + dao.listar().size());

            Ciudad buscada = dao.buscarPorId(nueva.getId());
            System.out.println("Buscada por id: " + buscada.getNombre());

            buscada.setNombre("Ciudad de prueba editada");
            dao.actualizar(buscada);
            System.out.println("Actualizada: " + dao.buscarPorId(buscada.getId()).getNombre());

            System.out.println("Eliminada: " + dao.eliminar(buscada.getId()));
            System.out.println("Ciudades al final: " + dao.listar().size());

            try {
                dao.eliminar(1);
                System.out.println("ATENCIÓN: se eliminó una ciudad con proveedores");
            } catch (SQLException e) {
                System.out.println("Eliminar Armenia (tiene proveedores): rechazado por la base (código "
                        + e.getErrorCode() + ")");
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
