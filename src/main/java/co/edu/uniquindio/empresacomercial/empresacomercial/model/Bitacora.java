package co.edu.uniquindio.empresacomercial.empresacomercial.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Bitacora {

    private int id;
    private long idUsuario;
    private LocalDate fechaEntrada;
    private LocalTime horaEntrada;
    private LocalDate fechaSalida;
    private LocalTime horaSalida;

    public Bitacora() {
    }

    public Bitacora(int id, long idUsuario, LocalDate fechaEntrada, LocalTime horaEntrada, LocalDate fechaSalida, LocalTime horaSalida) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.fechaEntrada = fechaEntrada;
        this.horaEntrada = horaEntrada;
        this.fechaSalida = fechaSalida;
        this.horaSalida = horaSalida;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public LocalDate getFechaEntrada() {
        return fechaEntrada;
    }

    public LocalDateTime getFechaHoraEntrada() {
        if (fechaEntrada == null || horaEntrada == null) {
            return null;
        }
        return LocalDateTime.of(fechaEntrada, horaEntrada);
    }

    public void setFechaEntrada(LocalDate fechaEntrada) {
        this.fechaEntrada = fechaEntrada;
    }

    public LocalTime getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(LocalTime horaEntrada) {
        this.horaEntrada = horaEntrada;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public LocalDateTime getFechaHoraSalida() {
        if (fechaSalida == null || horaSalida == null) {
            return null;
        }
        return LocalDateTime.of(fechaSalida, horaSalida);
    }

    public void setFechaSalida(LocalDate fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public LocalTime getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(LocalTime horaSalida) {
        this.horaSalida = horaSalida;
    }

    /**
     * Registra la entrada con la fecha y la hora actuales
     */
    public void iniciarSesion() {
        this.fechaEntrada = LocalDate.now();
        this.horaEntrada = LocalTime.now().withNano(0);
    }

    /**
     * Registra la salida con la fecha y la hora actuales
     */
    public void cerrarSesion() {
        this.fechaSalida = LocalDate.now();
        this.horaSalida = LocalTime.now().withNano(0);
    }

    /**
     * La sesión sigue abierta mientras no se haya registrado la salida.
     * @return
     */
    public boolean estaAbierta() {
        return fechaSalida == null;
    }
}
