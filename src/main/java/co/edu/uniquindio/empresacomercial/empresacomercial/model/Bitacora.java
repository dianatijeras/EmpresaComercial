package co.edu.uniquindio.empresacomercial.empresacomercial.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Bitacora {

    private int id;
    private int idUsuario;
    private LocalDateTime fechaHoraEntrada;
    private LocalDateTime fechaHoraSalida;

    public Bitacora() {
    }

    public Bitacora(int id, int idUsuario, LocalDateTime fechaHoraEntrada, LocalDateTime fechaHoraSalida) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.fechaHoraEntrada = fechaHoraEntrada;
        this.fechaHoraSalida = fechaHoraSalida;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public LocalDateTime getFechaHoraEntrada() {
        return fechaHoraEntrada;
    }

    public void setFechaHoraEntrada(LocalDateTime fechaHoraEntrada) {
        this.fechaHoraEntrada = fechaHoraEntrada;
    }

    public LocalDateTime getFechaHoraSalida() {
        return fechaHoraSalida;
    }

    public void setFechaHoraSalida(LocalDateTime fechaHoraSalida) {
        this.fechaHoraSalida = fechaHoraSalida;
    }

    /**
     * Atributo derivado: solo tiene getter, no tiene setter
     * @return
     */
    public String getRegistro(){
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/YYYY HH:mm:ss");
        String entrada = (fechaHoraEntrada != null) ? fechaHoraEntrada.format(formato) : "-";
        String salida = (fechaHoraSalida != null) ? fechaHoraSalida.format(formato) : "Sesion abierta";
        return "Entrada: " + entrada + " | Salida: " + salida;
    }

    /**
     * Registra la salida del usuario con la fecha y hora actual
     */
    public void cerrarSesion() {
        this.fechaHoraSalida = LocalDateTime.now();
    }

    /**
     * La sesion sigue abierta mientras no se haya registrado la salida
     * @return
     */
    public boolean estaAbierta(){
        return fechaHoraSalida == null;
    }
}
