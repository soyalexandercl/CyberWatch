package com.cyberwatch.entidad;

import com.cyberwatch.util.Log;
import java.util.Random;

public class ProcesoSimulado extends Thread {

    private final String nombre;
    private final Log log;
    private int usoCpu;
    private boolean estado;
    private final long tiempoInicio;
    private final Random random;

    public ProcesoSimulado(Log log, String nombre, int usoCpu) {
        this.log = log;
        this.nombre = nombre;
        this.usoCpu = usoCpu;
        this.estado = true;
        this.random = new Random();
        this.tiempoInicio = System.currentTimeMillis();
    }

    public String getNombre() {
        return nombre;
    }

    public int getUsoCpu() {
        return usoCpu;
    }

    public long getTiempoInicio() {
        return tiempoInicio;
    }

    public boolean getEstado() {
        return estado;
    }

    @Override
    public void run() {
        log.registrarLinea("logs/log_sdas.txt", "[PROCESO] El proceso " + nombre + " ha iniciado.");
        try {
            while (estado && !Thread.currentThread().isInterrupted()) {
                this.usoCpu = random.nextInt(100);
                Thread.sleep(500);
                if (random.nextInt(100) < 5) {
                    estado = false;
                }
            }
        } catch (InterruptedException e) {
            estado = false;
        }
        log.registrarLinea("logs/log_sdas.txt", "[PROCESO] El proceso " + nombre + " ha finalizado.");
    }
}
