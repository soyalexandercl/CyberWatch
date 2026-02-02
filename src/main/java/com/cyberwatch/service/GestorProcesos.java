package com.cyberwatch.service;

import com.cyberwatch.model.ProcesoSimulado;
import com.cyberwatch.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GestorProcesos extends Thread {

    private final Random random;
    private final Log log;
    private final String rutaLog;
    private final String rutaProcesosProhibidos;
    private final List<ProcesoSimulado> listaProcesosActivos;
    private final List<String> listaProcesosProhibidos;

    public GestorProcesos(Log log) {
        this.random = new Random();
        this.log = log;
        this.rutaLog = "logs/log_sdas.txt";
        this.rutaProcesosProhibidos = "logs/procesosProhibidos.txt";
        this.listaProcesosActivos = new ArrayList<>();
        this.listaProcesosProhibidos = new ArrayList<>();
    }

    // Leer nombres de procesos bloqueados desde el archivo
    public void cargarProcesosProhibidos() throws IOException {
        log.registrarLinea(rutaLog, "[INFO] Cargando procesos prohibidos");

        Path ruta = Paths.get(this.rutaProcesosProhibidos);
        if (Files.notExists(ruta)) {
            Files.createDirectories(ruta.getParent());
            Files.createFile(ruta);
        }

        try (BufferedReader br = Files.newBufferedReader(ruta)) {
            String lineaActual;
            while ((lineaActual = br.readLine()) != null) {
                if (!lineaActual.trim().isEmpty()) {
                    listaProcesosProhibidos.add(lineaActual.trim());
                }
            }
        } catch (IOException e) {
            log.registrarLinea(this.rutaLog, "[ERROR] No se pudo leer "
                    + this.rutaProcesosProhibidos);
        }

        log.registrarLinea(rutaLog, "[INFO] Los procesos prohibidos existentes fueron cargados");
    }

    // Guardar un proceso en la lista negra
    public void agregarProcesoProhibido(String nombre) {
        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(this.rutaProcesosProhibidos, true))) {
            bw.write(nombre);
            bw.newLine();
            listaProcesosProhibidos.add(nombre);

            log.registrarLinea(rutaLog, "[INFO] El proceso " + nombre + " fue agregado a la lista de procesos prohibidos");
        } catch (IOException e) {
            log.registrarLinea(this.rutaLog, "[ERROR] No se pudo escribir en "
                    + this.rutaProcesosProhibidos);
        }
    }

    // Verificar si el proceso está en la lista negra
    public boolean analizarProcesoProhibido(ProcesoSimulado procesoActual) {
        for (String procesoProhibido : listaProcesosProhibidos) {
            if (procesoActual.getNombre().equalsIgnoreCase(procesoProhibido)) {
                log.registrarLinea(this.rutaLog, "[PROCESO] El proceso "
                        + procesoActual.getNombre() + " se encuentra en la lista negra");

                return true;
            }
        }

        return false;
    }

    // Revisar si el consumo de recursos es muy alto
    public boolean analizarUsoCpuProceso(ProcesoSimulado procesoActual) {
        int usoCpuProcesoActual = procesoActual.getUsoCpu();

        if (usoCpuProcesoActual > 80) {
            log.registrarLinea(this.rutaLog, "[PROCESO] El proceso "
                    + procesoActual.getNombre() + " excede el uso de CPU "
                    + usoCpuProcesoActual + "%");

            return true;
        }

        return false;
    }

    // Controlar cuánto tiempo lleva encendido el proceso
    public boolean analizarTiempoInicioProceso(long tiempoActual, ProcesoSimulado procesoActual) {
        long tiempo = (tiempoActual - procesoActual.getTiempoInicio()) / 1000;

        if (tiempo > 10) {
            log.registrarLinea(this.rutaLog, "[PROCESO] El proceso "
                    + procesoActual.getNombre() + " lleva activo " + tiempo
                    + " segundos");

            return true;
        }

        return false;
    }

    // Evaluar y detener procesos sospechosos
    public void analizarProcesos() {
        long tiempoActual = System.currentTimeMillis();

        for (int i = 0; i < listaProcesosActivos.size(); i++) {
            ProcesoSimulado procesoActual = listaProcesosActivos.get(i);

            // Detener si es peligroso, consume mucho o lleva mucho tiempo
            if (this.analizarProcesoProhibido(procesoActual)
                    || this.analizarUsoCpuProceso(procesoActual)
                    || this.analizarTiempoInicioProceso(tiempoActual, procesoActual)) {

                if (!this.analizarProcesoProhibido(procesoActual)) {
                    this.agregarProcesoProhibido(procesoActual.getNombre());
                }

                log.registrarLinea(this.rutaLog, "[PROCESO] El proceso "
                        + procesoActual.getNombre() + " se interrumpió");

                procesoActual.interrupt();
                listaProcesosActivos.remove(i);
                i--;
            } else if (!procesoActual.getEstado()) {
                listaProcesosActivos.remove(i);
                i--;
            }
        }
    }

    @Override
    public void run() {
        // Ejecutar la simulación de procesos y el análisis
        try {
            log.registrarLinea(rutaLog, "[INFO] Gestor de procesos simulados iniciado");

            this.cargarProcesosProhibidos();

            int cantidadProcesos = 5;

            // Generar procesos normales o maliciosos al azar
            for (int i = 0; i <= cantidadProcesos; i++) {
                String[] procesos = {"keylogger.exe", "troyano.exe", "virus.exe"};

                if (random.nextInt(20) + 1 >= 15) {
                    ProcesoSimulado proceso = new ProcesoSimulado(log,
                            procesos[random.nextInt(procesos.length)],
                            (random.nextInt(3) + 8) * 10
                    );
                    proceso.start();
                    listaProcesosActivos.add(proceso);
                } else {
                    ProcesoSimulado proceso = new ProcesoSimulado(log,
                            "proceso" + i + ".exe",
                            random.nextInt(100) + 1);
                    proceso.start();
                    listaProcesosActivos.add(proceso);
                }
            }

            // Mantener el análisis activo mientras haya procesos (Análisis continuo sin sleep)
            while (listaProcesosActivos.size() > 0) {
                analizarProcesos();
            }

            log.registrarLinea(rutaLog, "[INFO] Gestor de procesos simulados detenido");
        } catch (IOException e) {
            log.registrarLinea(this.rutaLog, "[ERROR] hubo un error con el gestor de procesos");
        }
    }
}