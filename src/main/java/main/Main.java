package main;

import javax.swing.SwingUtilities;
import ui.VentanaPrincipal;

public class Main {

    public static void main(String[] args) {
        /* Se utiliza invokeLater para asegurar que la interfaz se inicie en el hilo */
        SwingUtilities.invokeLater(() -> {

            /* Creación del objeto de la ventana principal del sistema */
            VentanaPrincipal ventanaDeSeguridad = new VentanaPrincipal();

            /* Centra la ventana en la pantalla del usuario */
            ventanaDeSeguridad.setLocationRelativeTo(null);

            /* Hace que la ventana sea visible para comenzar la ejecución */
            ventanaDeSeguridad.setVisible(true);

        });
    }
}
