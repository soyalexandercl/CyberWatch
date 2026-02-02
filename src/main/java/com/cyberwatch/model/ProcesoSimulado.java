package com.cyberwatch.model;

import com.cyberwatch.util.Log;
import java.util.Random;

public class ProcesoSimulado extends Thread {

    private final Random random;
    private final Log log;
    private final String nombre;
    private int usoCpu;
    private boolean estado;
    private final long tiempoInicio;

    public ProcesoSimulado(Log log, String nombre, int usoCpu) {
        this.random = new Random();
        this.log = log;
        this.nombre = nombre;
        this.usoCpu = usoCpu;
        this.estado = true;
        this.tiempoInicio = System.currentTimeMillis() - random.nextInt(2000);
    }

    public String getNombre() {
        return nombre;
    }

    public int getUsoCpu() {
        return usoCpu;
    }

    public boolean getEstado() {
        return estado;
    }

    public long getTiempoInicio() {
        return tiempoInicio;
    }

    @Override
    public void run() {
        // Lógica de ejecución del proceso simulado
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (estado) {
                    log.registrarLinea("logs/log_sdas.txt", "[PROCESO] El proceso "
                            + this.nombre + " ha iniciado.");
                    
                    this.usoCpu = random.nextInt(100);

                    Thread.sleep(100);

                    this.estado = random.nextBoolean();
                } else {
                    // Detener el hilo si el proceso termina
                    Thread.currentThread().interrupt();

                    log.registrarLinea("logs/log_sdas.txt", "[PROCESO] El proceso "
                            + this.nombre + " ha finalizado.");
                }
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    }
}