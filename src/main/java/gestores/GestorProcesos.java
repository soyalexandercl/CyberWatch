package gestores;

import entidad.ProcesoSimulado;
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
import util.Log;

public class GestorProcesos extends Thread {

    private final List<ProcesoSimulado> procesosActivos;
    private final List<String> listaNegraProcesos;
    private final Log registroLog;
    private final Random aleatorio;
    private final String rutaRegistroAlertas = "logs/log_sdas.txt";
    private final String rutaListaNegra = "logs/procesosProhibidos.txt";

    public GestorProcesos(Log registroLog) {
        this.registroLog = registroLog;
        this.procesosActivos = new ArrayList<>();
        this.listaNegraProcesos = new ArrayList<>();
        this.aleatorio = new Random();
    }

    private void cargarListaNegra() {
        registroLog.registrarLinea(rutaRegistroAlertas, "[INFO] Cargando procesos prohibidos");
        try {
            Path archivoPath = Paths.get(rutaListaNegra);
            if (!Files.exists(archivoPath)) {
                Files.createFile(archivoPath);
            }
            try (BufferedReader lector = Files.newBufferedReader(archivoPath)) {
                String lineaLeida;
                while ((lineaLeida = lector.readLine()) != null) {
                    if (!lineaLeida.trim().isEmpty()) {
                        listaNegraProcesos.add(lineaLeida.trim());
                    }
                }
            }
            registroLog.registrarLinea(rutaRegistroAlertas, "[INFO] Procesos prohibidos cargados correctamente");
        } catch (IOException e) {
        }
    }

    private void sancionarProceso(String nombreProceso) {
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(rutaListaNegra, true))) {
            escritor.write(nombreProceso);
            escritor.newLine();
            listaNegraProcesos.add(nombreProceso);
            registroLog.registrarLinea(rutaRegistroAlertas, "[INFO] Proceso " + nombreProceso + " añadido a lista negra");
        } catch (IOException e) {
        }
    }

    @Override
    public void run() {
        registroLog.registrarLinea(rutaRegistroAlertas, "[INFO] Monitor de procesos iniciado");
        cargarListaNegra();

        // Creación inicial de procesos simulados
        for (int i = 0; i < 6; i++) {
            String nombre = (aleatorio.nextInt(10) > 7) ? "keylogger.exe" : "proceso" + i + ".exe";
            ProcesoSimulado nuevoProceso = new ProcesoSimulado(registroLog, nombre, aleatorio.nextInt(100));
            nuevoProceso.start();
            procesosActivos.add(nuevoProceso);
        }

        while (procesosActivos.size() > 0) {
            long tiempoActualMillis = System.currentTimeMillis();
            int indice = 0;

            while (indice < procesosActivos.size()) {
                ProcesoSimulado proceso = procesosActivos.get(indice);
                boolean esSospechoso = false;

                // 1. Verificación en lista negra
                for (int j = 0; j < listaNegraProcesos.size(); j++) {
                    if (listaNegraProcesos.get(j).equalsIgnoreCase(proceso.getNombre())) {
                        registroLog.registrarLinea(rutaRegistroAlertas, "[PROCESO] El proceso se encuentra en la lista negra: " + proceso.getNombre());
                        esSospechoso = true;
                    }
                }

                // 2. Verificación por Consumo de CPU
                if (!esSospechoso && proceso.getUsoCpu() > 80) {
                    registroLog.registrarLinea(rutaRegistroAlertas, "[PROCESO] Uso excesivo de CPU: " + proceso.getNombre() + " al " + proceso.getUsoCpu() + "%");
                    esSospechoso = true;
                }

                // 3. Verificación por Tiempo de Ejecución (Máximo 10 segundos)
                long segundosActivo = (tiempoActualMillis - proceso.getTiempoInicio()) / 1000;
                if (!esSospechoso && segundosActivo > 10) {
                    registroLog.registrarLinea(rutaRegistroAlertas, "[PROCESO] Tiempo límite excedido: " + proceso.getNombre() + " (" + segundosActivo + " segundos)");
                    esSospechoso = true;
                }

                // Si la variable esSospechoso es true inrgesa
                if (esSospechoso) {
                    // Verificamos si ya está sancionado para no duplicar en archivo
                    boolean yaSancionado = false;
                    for (int k = 0; k < listaNegraProcesos.size(); k++) {
                        if (listaNegraProcesos.get(k).equalsIgnoreCase(proceso.getNombre())) {
                            yaSancionado = true;
                        }
                    }

                    if (!yaSancionado) {
                        sancionarProceso(proceso.getNombre());
                    }

                    registroLog.registrarLinea(rutaRegistroAlertas, "[PROCESO] Interrumpiendo proceso malicioso: " + proceso.getNombre());
                    proceso.interrupt();
                    procesosActivos.remove(indice);
                } else if (!proceso.isAlive()) {
                    // El proceso terminó por su cuenta
                    procesosActivos.remove(indice);
                } else {
                    // El proceso es seguro, pasamos al siguiente
                    indice++;
                }
            }
        }
        registroLog.registrarLinea(rutaRegistroAlertas, "[INFO] Monitor de procesos finalizado");
    }
}
