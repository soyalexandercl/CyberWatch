package com.cyberwatch.service;

import com.cyberwatch.model.ProcesoSimulado;
import com.cyberwatch.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GestorProcesos extends Thread {
    private List<ProcesoSimulado> procesosActivos;
    private List<String> listaNegra;
    private Log logger;
    private boolean activo;
    private Random random;
    private String rutaListaNegra = "procesosProhibidos.txt"; // Archivo de persistencia

    public GestorProcesos(Log logger) {
        this.logger = logger;
        this.procesosActivos = new ArrayList<>();
        this.listaNegra = new ArrayList<>();
        this.activo = true;
        this.random = new Random();
        cargarListaNegra();
    }

    // Carga inicial desde el archivo
    private void cargarListaNegra() {
        if (!Files.exists(Paths.get(rutaListaNegra))) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(rutaListaNegra))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (!linea.trim().isEmpty()) {
                    listaNegra.add(linea.trim().toLowerCase());
                }
            }
        } catch (IOException e) {
            logger.registrar("PROCESOS", "Error al cargar la lista negra física.");
        }
    }

    // Método para agregar y PERSISTIR el sospechoso
    private void agregarAListaNegra(String nombre) {
        String nombreLimpio = nombre.toLowerCase();
        // Solo agregamos si no estaba ya
        if (!estaEnListaNegra(nombreLimpio)) {
            listaNegra.add(nombreLimpio);
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaListaNegra, true))) {
                bw.write(nombreLimpio);
                bw.newLine();
                logger.registrar("PROCESOS", "Proceso " + nombre + " agregado permanentemente a la lista negra.");
            } catch (IOException e) {
                logger.registrar("PROCESOS", "Error al escribir en el archivo de lista negra.");
            }
        }
    }

    private boolean estaEnListaNegra(String nombre) {
        String nombreBusqueda = nombre.toLowerCase();
        int i = 0;
        while (i < listaNegra.size()) {
            if (listaNegra.get(i).equals(nombreBusqueda)) {
                return true;
            }
            i++;
        }
        return false;
    }

    @Override
    public void run() {
        logger.registrar("PROCESOS", "Iniciando simulación y monitoreo...");

        // Generación de tanda inicial de procesos (Mismo sistema de probabilidad)
        int cantidadMax = 6;
        int contador = 0;
        while (contador < cantidadMax) {
            String nombre;
            if (random.nextInt(20) + 1 >= 15) {
                String[] maliciosos = {"keylogger.exe", "troyano.exe", "virus.exe"};
                nombre = maliciosos[random.nextInt(maliciosos.length)];
            } else {
                nombre = "proceso_" + contador + ".exe";
            }

            ProcesoSimulado p = new ProcesoSimulado(nombre, random.nextInt(100), logger);
            p.start();
            procesosActivos.add(p);
            logger.registrar("PROCESOS", "Proceso " + nombre + " iniciado.");
            contador++;
        }

        // Bucle de monitoreo activo
        while (activo && procesosActivos.size() > 0) {
            analizarProcesos();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                activo = false;
            }
        }
        logger.registrar("PROCESOS", "Simulación finalizada.");
    }

    private void analizarProcesos() {
        long tiempoActual = System.currentTimeMillis();
        int i = 0;

        while (i < procesosActivos.size()) {
            ProcesoSimulado p = procesosActivos.get(i);
            boolean sospechoso = false;

            // 1. Verificación contra lista negra cargada
            if (estaEnListaNegra(p.getNombre())) {
                logger.registrar("PROCESOS", "ALERTA: Detectado proceso en lista negra: " + p.getNombre());
                sospechoso = true;
            }

            // 2. Verificación de comportamiento anómalo (CPU)
            if (!sospechoso && p.getUsoCpu() > 80) {
                logger.registrar("PROCESOS", "ALERTA: Uso excesivo de CPU en " + p.getNombre() + " (" + p.getUsoCpu() + "%)");
                agregarAListaNegra(p.getNombre()); // Lo agregamos para el futuro
                sospechoso = true;
            }

            // 3. Verificación de persistencia (>10 segundos)
            long segundosActivo = (tiempoActual - p.getTiempoInicio()) / 1000;
            if (!sospechoso && segundosActivo > 10) {
                logger.registrar("PROCESOS", "ALERTA: Proceso persistente sospechoso: " + p.getNombre());
                agregarAListaNegra(p.getNombre()); // Lo agregamos para el futuro
                sospechoso = true;
            }

            if (sospechoso) {
                p.interrupt(); // Detenemos el hilo del proceso
                procesosActivos.remove(i);
                logger.registrar("PROCESOS", "Acción: Proceso " + p.getNombre() + " bloqueado y eliminado.");
            } else if (!p.isAlive()) {
                procesosActivos.remove(i); // El proceso terminó solo
            } else {
                i++;
            }
        }
    }

    public void detener() {
        this.activo = false;
    }
}