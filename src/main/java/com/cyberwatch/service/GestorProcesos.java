package com.cyberwatch.service;

import com.cyberwatch.model.ProcesoSimulado;
import com.cyberwatch.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GestorProcesos extends Thread {

    private List<ProcesoSimulado> procesosActivos;
    private List<String> listaNegra;
    private Log log;
    private boolean activo;

    public GestorProcesos(Log log) {
        this.log = log;
        this.procesosActivos = new ArrayList<>();
        this.listaNegra = new ArrayList<>();
        this.activo = true;
        listaNegra.add("keylogger.exe");
        listaNegra.add("miner.exe");
    }

    public void detener() {
        this.activo = false;
    }

    @Override
    public void run() {
        Random ran = new Random();
        log.registrar("PROCESOS", "Simulador de procesos activo.");

        while (activo) {
            // Generar proceso aleatorio
            if (ran.nextInt(10) > 7) {
                String nombre = (ran.nextBoolean()) ? "chrome.exe" : "keylogger.exe";
                int cpu = ran.nextInt(100);
                procesosActivos.add(new ProcesoSimulado(nombre, cpu));
            }

            // Analizar
            int i = 0;
            while (i < procesosActivos.size()) {
                ProcesoSimulado p = procesosActivos.get(i);
                p.incrementarTiempo();
                boolean eliminar = false;

                // Lista negra
                int j = 0;
                while (j < listaNegra.size()) {
                    if (listaNegra.get(j).equals(p.getNombre())) {
                        log.registrar("PROCESOS", "Proceso en lista negra detectado: " + p.getNombre());
                        eliminar = true;
                    }
                    j++;
                }

                // CPU Alta
                if (!eliminar && p.getUsoCpu() > 80) {
                    log.registrar("PROCESOS", "Uso excesivo CPU: " + p.getNombre() + " (" + p.getUsoCpu() + "%)");
                    eliminar = true;
                }

                if (eliminar) {
                    procesosActivos.remove(i);
                } else {
                    if (p.getTiempoActivo() > 10) {
                        procesosActivos.remove(i);
                    } else {
                        i++;
                    }
                }
            }
        }
    }
}
