package com.cyberwatch.gestores;

import com.cyberwatch.entidad.ArchivoIntegridad;
import com.cyberwatch.util.Log;
import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class GestorIntegridad extends Thread {

    private final String rutaDirectorio;
    private final List<ArchivoIntegridad> registros;
    private final Log log;
    private boolean activo;

    public GestorIntegridad(String rutaDirectorio, Log log) {
        this.rutaDirectorio = rutaDirectorio;
        this.log = log;
        this.registros = new ArrayList<>();
        this.activo = true;
    }

    private String calcularHash(File archivo) {
        try {
            byte[] datos = Files.readAllBytes(archivo.toPath());
            byte[] hashBytes = MessageDigest.getInstance("SHA-256").digest(datos);
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void run() {
        File folder = new File(rutaDirectorio);
        if (!folder.isDirectory()) {
            return;
        }

        log.registrarLinea("logs/log_sdas.txt", "[INFO] Gestor de archivos iniciado");
        File[] iniciales = folder.listFiles();
        if (iniciales != null) {
            for (File f : iniciales) {
                if (f.isFile()) {
                    registros.add(new ArchivoIntegridad(f.getName(), calcularHash(f)));
                }
            }
        }
        log.registrarLinea("logs/log_sdas.txt", "[INFO] Los archivos existentes fueron cargados");

        while (activo) {
            File[] actuales = folder.listFiles();
            if (actuales != null) {
                // Detectar nuevos y modificados
                for (File f : actuales) {
                    if (!f.isFile()) {
                        continue;
                    }
                    boolean encontrado = false;
                    String hashActual = calcularHash(f);
                    int i = 0;
                    while (i < registros.size()) {
                        ArchivoIntegridad r = registros.get(i);
                        if (r.getNombre().equals(f.getName())) {
                            encontrado = true;
                            if (!r.getHash().equals(hashActual)) {
                                log.registrarLinea("logs/log_sdas.txt", "[INTEGRIDAD] archivo " + f.getName() + " modificado");
                                r.setHash(hashActual);
                            }
                        }
                        i++;
                    }
                    if (!encontrado) {
                        registros.add(new ArchivoIntegridad(f.getName(), hashActual));
                        log.registrarLinea("logs/log_sdas.txt", "[INTEGRIDAD] archivo " + f.getName() + " creado");
                    }
                }
                // Detectar eliminados
                int j = 0;
                while (j < registros.size()) {
                    boolean existe = false;
                    for (File f : actuales) {
                        if (f.getName().equals(registros.get(j).getNombre())) {
                            existe = true;
                        }
                    }
                    if (!existe) {
                        log.registrarLinea("logs/log_sdas.txt", "[INTEGRIDAD] archivo " + registros.get(j).getNombre() + " eliminado");
                        registros.remove(j);
                    } else {
                        j++;
                    }
                }
            }
        }
    }
}
