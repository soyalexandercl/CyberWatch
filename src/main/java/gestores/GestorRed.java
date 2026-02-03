package gestores;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import util.Log;

public class GestorRed {

    private final Log registroLog;
    private final String rutaTrafico = "logs/traffic.log";
    private final String rutaInformeAlertas = "logs/log_sdas.txt";

    public GestorRed(Log registroLog) {
        this.registroLog = registroLog;
    }

    public void crearTrafico() {
        Random aleatorio = new Random();
        registroLog.registrarLinea(rutaInformeAlertas, "[INFO] Iniciando simulación de tráfico");

        int[] puertosPosibles = {4444, 80, 31337, 8080};

        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(rutaTrafico, true))) {
            for (int i = 0; i < 10; i++) {
                String marcaTiempo = String.format("2024-01-01 12:00:%02d", i * 2);
                String ipGenerada = "192.168.1." + aleatorio.nextInt(5);
                int puertoElegido = puertosPosibles[aleatorio.nextInt(puertosPosibles.length)];

                escritor.write("[" + marcaTiempo + "] " + ipGenerada + ":" + puertoElegido);
                escritor.newLine();
            }
        } catch (IOException e) {
            // Error en escritura ignorado para mantener flujo
        }
    }

    public void analizar() {
        registroLog.registrarLinea(rutaInformeAlertas, "[INFO] Análisis de red iniciado");

        List<String> historialIps = new ArrayList<>();
        List<String> historialTiempos = new ArrayList<>();
        
        List<String> puertosSospechosos = new ArrayList<>();
        puertosSospechosos.add("4444");
        puertosSospechosos.add("31337");
        puertosSospechosos.add("8888");

        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (BufferedReader lector = new BufferedReader(new FileReader(rutaTrafico))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                int posInicio = linea.indexOf("[");
                int posFin = linea.indexOf("]");

                if (posInicio != -1 && posFin != -1) {
                    String fechaHoraTexto = linea.substring(posInicio + 1, posFin);
                    String[] datosRed = linea.substring(posFin + 2).split(":");

                    if (datosRed.length == 2) {
                        String ipActual = datosRed[0];
                        String puertoActual = datosRed[1];
                        LocalDateTime tiempoActual = LocalDateTime.parse(fechaHoraTexto, formatoFecha);

                        // 1. Verificación de Puertos Sospechosos
                        if (puertosSospechosos.contains(puertoActual)) {
                            String soloHora = fechaHoraTexto.substring(11);
                            registroLog.registrarLinea(rutaInformeAlertas, "[RED] Puerto sospechoso: " + puertoActual + " desde " + ipActual);
                        }

                        // 2. Verificación de IPs Repetidas e Intervalos Anómalos
                        int contadorRepeticiones = 0;
                        String tiempoUltimaConexion = "";

                        for (int i = 0; i < historialIps.size(); i++) {
                            if (historialIps.get(i).equals(ipActual)) {
                                contadorRepeticiones++;
                                tiempoUltimaConexion = historialTiempos.get(i);
                            }
                        }

                        // Alerta por repetición
                        if (contadorRepeticiones >= 3) {
                            registroLog.registrarLinea(rutaInformeAlertas, "[RED] IP frecuente detectada: " + ipActual + " (" + (contadorRepeticiones + 1) + " veces)");
                        }

                        // Alerta por tiempo (Intervalo)
                        if (!tiempoUltimaConexion.isEmpty()) {
                            LocalDateTime tiempoAnterior = LocalDateTime.parse(tiempoUltimaConexion, formatoFecha);
                            long segundosTranscurridos = ChronoUnit.SECONDS.between(tiempoAnterior, tiempoActual);
                            
                            if (segundosTranscurridos >= 0 && segundosTranscurridos < 5) {
                                registroLog.registrarLinea(rutaInformeAlertas, "[RED] Tráfico anómalo detectado desde " + ipActual + ": " + segundosTranscurridos + " segundos.");
                            }
                        }

                        // Registro en memoria para el siguiente ciclo
                        historialIps.add(ipActual);
                        historialTiempos.add(fechaHoraTexto);
                    }
                }
            }
        } catch (IOException e) {
            registroLog.registrarLinea(rutaInformeAlertas, "[ERROR] Fallo en la lectura del tráfico de red");
        }

        registroLog.registrarLinea(rutaInformeAlertas, "[INFO] Análisis de red finalizado");
    }
}