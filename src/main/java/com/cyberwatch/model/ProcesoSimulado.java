package com.cyberwatch.model;

public class ProcesoSimulado {

    private String nombre;
    private int usoCpu;
    private int tiempoActivo;

    public ProcesoSimulado(String nombre, int usoCpu) {
        this.nombre = nombre;
        this.usoCpu = usoCpu;
        this.tiempoActivo = 0;
    }

    public String getNombre() {
        return nombre;
    }

    public int getUsoCpu() {
        return usoCpu;
    }

    public int getTiempoActivo() {
        return tiempoActivo;
    }

    public void incrementarTiempo() {
        this.tiempoActivo++;
    }
}
