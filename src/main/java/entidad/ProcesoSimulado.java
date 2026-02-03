package entidad;

import java.util.Random;
import util.Log;

public class ProcesoSimulado extends Thread {

    private final String nombreProceso;
    private final Log registroLog;
    private int consumoCpu;
    private boolean estaActivo;
    private final long marcaTiempoInicio;
    private final Random generadorAleatorio;

    public ProcesoSimulado(Log registroLog, String nombreProceso, int consumoCpu) {
        this.registroLog = registroLog;
        this.nombreProceso = nombreProceso;
        this.consumoCpu = consumoCpu;
        this.estaActivo = true;
        this.generadorAleatorio = new Random();
        this.marcaTiempoInicio = System.currentTimeMillis();
    }

    public String getNombre() {
        return nombreProceso;
    }

    public int getUsoCpu() {
        return consumoCpu;
    }

    public long getTiempoInicio() {
        return marcaTiempoInicio;
    }

    public boolean getEstado() {
        return estaActivo;
    }

    @Override
    public void run() {
        registroLog.registrarLinea("logs/log_sdas.txt", "[PROCESO] El proceso " + nombreProceso + " ha iniciado.");

        /* El bucle se mantiene mientras el proceso sea válido y no haya sido interrumpido */
        while (estaActivo && !Thread.currentThread().isInterrupted()) {

            /* Simulamos el cambio constante en el consumo de recursos */
            this.consumoCpu = generadorAleatorio.nextInt(100);

            /* Probabilidad del 5% de que el proceso termine por sí solo en cada ciclo */
            if (generadorAleatorio.nextInt(100) < 5) {
                estaActivo = false;
            }
        }

        registroLog.registrarLinea("logs/log_sdas.txt", "[PROCESO] El proceso " + nombreProceso + " ha finalizado.");
    }
}
