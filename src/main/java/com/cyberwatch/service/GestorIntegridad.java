package com.cyberwatch.service;

import com.cyberwatch.model.ArchivoIntegridad;
import com.cyberwatch.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class GestorIntegridad extends Thread {

    private String rutaCarpeta;
    private List<ArchivoIntegridad> archivosRegistrados;
    private Log log;
    private boolean activo;

    public GestorIntegridad(String rutaCarpeta, Log log) {
        this.rutaCarpeta = rutaCarpeta;
        this.log = log;
        this.archivosRegistrados = new ArrayList<>();
        this.activo = true;
    }

    private String obtenerHash(File archivo) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            FileInputStream fis = new FileInputStream(archivo);
            byte[] buffer = new byte[8192];
            int leido;
            while ((leido = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, leido);
            }
            fis.close();
            byte[] bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public void detener() {
        this.activo = false;
    }

    @Override
    public void run() {
        File directorio = new File(rutaCarpeta);
        File[] iniciales = directorio.listFiles();

        if (iniciales != null) {
            for (File f : iniciales) {
                if (f.isFile()) {
                    archivosRegistrados.add(new ArchivoIntegridad(f.getName(), obtenerHash(f)));
                }
            }
        }
        log.registrar("INTEGRIDAD", "Monitoreo iniciado en: " + rutaCarpeta);

        while (activo) {
            File[] actuales = directorio.listFiles();
            if (actuales == null) {
                continue;
            }

            List<String> nombresActuales = new ArrayList<>();
            for (File f : actuales) {
                if (f.isFile()) {
                    String nombre = f.getName();
                    String hashActual = obtenerHash(f);
                    nombresActuales.add(nombre);

                    boolean encontrado = false;
                    int i = 0;
                    while (i < archivosRegistrados.size()) {
                        ArchivoIntegridad reg = archivosRegistrados.get(i);
                        if (reg.getNombre().equals(nombre)) {
                            encontrado = true;
                            if (!reg.getHash().equals(hashActual)) {
                                log.registrar("INTEGRIDAD", "Archivo modificado: " + nombre);
                                reg.setHash(hashActual);
                            }
                        }
                        i++;
                    }

                    if (!encontrado) {
                        archivosRegistrados.add(new ArchivoIntegridad(nombre, hashActual));
                        log.registrar("INTEGRIDAD", "Archivo creado: " + nombre);
                    }
                }
            }

            int j = 0;
            while (j < archivosRegistrados.size()) {
                ArchivoIntegridad reg = archivosRegistrados.get(j);
                boolean todaviaExiste = false;
                int k = 0;
                while (k < nombresActuales.size()) {
                    if (nombresActuales.get(k).equals(reg.getNombre())) {
                        todaviaExiste = true;
                    }
                    k++;
                }
                if (!todaviaExiste) {
                    log.registrar("INTEGRIDAD", "Archivo eliminado: " + reg.getNombre());
                    archivosRegistrados.remove(j);
                } else {
                    j++;
                }
            }
        }
    }
}
