package com.cyberwatch.gestores;

import com.cyberwatch.entidad.ProcesoSimulado;
import com.cyberwatch.util.Log;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GestorProcesos extends Thread {

    private final List<ProcesoSimulado> activos;
    private final List<String> listaNegra;
    private final Log log;
    private final Random random;
    private final String rutaLog = "logs/log_sdas.txt";
    private final String rutaBlacklist = "logs/procesosProhibidos.txt";

    public GestorProcesos(Log log) {
        this.log = log;
        this.activos = new ArrayList<>();
        this.listaNegra = new ArrayList<>();
        this.random = new Random();
    }

    private void cargarListaNegra() {
        log.registrarLinea(rutaLog, "[INFO] Cargando procesos prohibidos");
        try {
            Path path = Paths.get(rutaBlacklist);
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
            try (BufferedReader br = Files.newBufferedReader(path)) {
                String l;
                while ((l = br.readLine()) != null) {
                    if (!l.trim().isEmpty()) {
                        listaNegra.add(l.trim());
                    }
                }
            }
            log.registrarLinea(rutaLog, "[INFO] Los procesos prohibidos existentes fueron cargados");
        } catch (IOException e) {
        }
    }

    private void sancionar(String nombre) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaBlacklist, true))) {
            bw.write(nombre);
            bw.newLine();
            listaNegra.add(nombre);
            log.registrarLinea(rutaLog, "[INFO] El proceso " + nombre + " fue agregado a la lista de procesos prohibidos");
        } catch (IOException e) {
        }
    }

    @Override
    public void run() {
        log.registrarLinea(rutaLog, "[INFO] Gestor de procesos simulados iniciado");
        cargarListaNegra();

        for (int i = 0; i < 6; i++) {
            String nom = (random.nextInt(10) > 7) ? "virus.exe" : "proceso" + i + ".exe";
            ProcesoSimulado p = new ProcesoSimulado(log, nom, random.nextInt(100));
            p.start();
            activos.add(p);
        }

        while (activos.size() > 0) {
            long ahora = System.currentTimeMillis();
            int i = 0;
            while (i < activos.size()) {
                ProcesoSimulado p = activos.get(i);
                boolean sos = false;

                // Reglas
                for (String b : listaNegra) {
                    if (b.equalsIgnoreCase(p.getNombre())) {
                        log.registrarLinea(rutaLog, "[PROCESO] El proceso " + p.getNombre() + " se encuentra en la lista negra");
                        sos = true;
                    }
                }
                if (!sos && p.getUsoCpu() > 80) {
                    log.registrarLinea(rutaLog, "[PROCESO] El proceso " + p.getNombre() + " excede el uso de CPU " + p.getUsoCpu() + "%");
                    sos = true;
                }
                if (!sos && (ahora - p.getTiempoInicio()) / 1000 > 10) {
                    log.registrarLinea(rutaLog, "[PROCESO] El proceso " + p.getNombre() + " lleva activo " + ((ahora - p.getTiempoInicio()) / 1000) + " segundos");
                    sos = true;
                }

                if (sos) {
                    boolean yaEsta = false;
                    for (String s : listaNegra) {
                        if (s.equalsIgnoreCase(p.getNombre())) {
                            yaEsta = true;
                        }
                    }
                    if (!yaEsta) {
                        sancionar(p.getNombre());
                    }
                    log.registrarLinea(rutaLog, "[PROCESO] El proceso " + p.getNombre() + " se interrumpió");
                    p.interrupt();
                    activos.remove(i);
                } else if (!p.isAlive()) {
                    activos.remove(i);
                } else {
                    i++;
                }
            }
        }
        log.registrarLinea(rutaLog, "[INFO] Gestor de procesos simulados detenido");
    }
}
