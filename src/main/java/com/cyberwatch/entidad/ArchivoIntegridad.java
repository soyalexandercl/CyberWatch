package com.cyberwatch.entidad;

public class ArchivoIntegridad {

    private String nombre;
    private String hash;

    public ArchivoIntegridad(String nombre, String hash) {
        this.nombre = nombre;
        this.hash = hash;
    }

    public String getNombre() {
        return nombre;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
