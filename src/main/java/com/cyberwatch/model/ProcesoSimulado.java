package com.cyberwatch.model;

import com.cyberwatch.util.Log;
import java.util.Random;

public class ProcesoSimulado extends Thread {
    private String nombre;
    private int usoCpu;
    private boolean activo;
    private long tiempoInicio;
    private Log logger;
    private Random random;

    public ProcesoSimulado(String nombre, int usoCpu, Log logger) {
        this.nombre = nombre;
        this.usoCpu = usoCpu;
        this.logger = logger;
        this.activo = true;
        this.random = new Random();
        // Guardamos el momento de inicio para calcular la persistencia
        this.tiempoInicio = System.currentTimeMillis();
    }

    public String getNombre() { return nombre; }
    public int getUsoCpu() { return usoCpu; }
    public long getTiempoInicio() { return tiempoInicio; }
    public boolean isActivo() { return activo; }

    @Override
    public void run() {
        // El proceso se ejecuta mientras esté activo y no sea interrumpido
        while (activo && !Thread.currentThread().isInterrupted()) {
            // Actualizamos el uso de CPU aleatoriamente
            this.usoCpu = random.nextInt(100);
            // Pequeña probabilidad de que el proceso termine por su cuenta
            if (random.nextInt(100) < 5) {
                activo = false;
            }
        }
    }
}