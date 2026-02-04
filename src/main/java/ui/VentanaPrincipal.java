package ui;

import gestores.GestorIntegridad;
import gestores.GestorProcesos;
import gestores.GestorRed;
import util.Log;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class VentanaPrincipal extends JFrame {

    // Componentes de la Interfaz Gráfica
    private JTextArea areaTextoRegistros;
    private JTextField campoRutaDirectorio;
    private Log registroLog;

    // Paleta de colores decorativa
    private final Color COLOR_FONDO = new Color(255, 204, 255);
    private final Color COLOR_TITULO = new Color(153, 51, 255);
    private final Color COLOR_BORDE_ROSA = new Color(255, 0, 255);
    private final Color COLOR_BOTON_LILA = new Color(204, 153, 255);

    public VentanaPrincipal() {
        configurarVentanaPrincipal();
        inicializarComponentesVisuales();
        // Se inicializa el log vinculándolo al área de texto para mostrar eventos
        this.registroLog = new Log(areaTextoRegistros);
    }

    private void configurarVentanaPrincipal() {
        setTitle("CyberWatch");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 600);
        setLocationRelativeTo(null);
    }

    private void inicializarComponentesVisuales() {
        // Contenedor principal con diseño de bordes
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBackground(COLOR_FONDO);
        panelPrincipal.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDE_ROSA, 4),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // 1. Título superior
        JLabel etiquetaTitulo = new JLabel("CIBERSEGURIDAD", SwingConstants.CENTER);
        etiquetaTitulo.setFont(new Font("Georgia", Font.BOLD | Font.ITALIC, 26));
        etiquetaTitulo.setForeground(COLOR_TITULO);
        panelPrincipal.add(etiquetaTitulo, BorderLayout.NORTH);

        // 2. Área del log
        areaTextoRegistros = new JTextArea();
        areaTextoRegistros.setEditable(false);
        areaTextoRegistros.setFont(new Font("Segoe UI", Font.BOLD, 14));
        areaTextoRegistros.setForeground(new Color(120, 0, 200)); 
        areaTextoRegistros.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, COLOR_BOTON_LILA, COLOR_BORDE_ROSA));
        
        JScrollPane panelDesplazamiento = new JScrollPane(areaTextoRegistros);
        panelPrincipal.add(panelDesplazamiento, BorderLayout.CENTER);

        // 3. Panel inferior
        JPanel panelInferior = new JPanel(new GridBagLayout());
        panelInferior.setBackground(COLOR_FONDO);
        GridBagConstraints restricciones = new GridBagConstraints();
        restricciones.fill = GridBagConstraints.BOTH;
        restricciones.insets = new Insets(5, 5, 5, 5);

        // Selección de carpeta
        campoRutaDirectorio = new JTextField();
        campoRutaDirectorio.setEditable(false);
        campoRutaDirectorio.setFont(new Font("Georgia", Font.ITALIC, 12));
        campoRutaDirectorio.setBorder(new LineBorder(COLOR_BOTON_LILA, 2));
        restricciones.gridx = 0; restricciones.gridy = 0; restricciones.weightx = 0.7; restricciones.ipady = 10;
        panelInferior.add(campoRutaDirectorio, restricciones);

        JButton botonSeleccionar = crearBotonPersonalizado("Seleccionar");
        botonSeleccionar.addActionListener(this::ejecutarSeleccionCarpeta);
        restricciones.gridx = 1; restricciones.weightx = 0.3;
        panelInferior.add(botonSeleccionar, restricciones);

        // Botón para monitor de archivos
        JButton botonMonitorArchivos = crearBotonPersonalizado("Iniciar Monitor de Archivos");
        botonMonitorArchivos.addActionListener(this::ejecutarMonitoreoIntegridad);
        restricciones.gridx = 0; restricciones.gridy = 1; restricciones.gridwidth = 2;
        panelInferior.add(botonMonitorArchivos, restricciones);

        // Botones de tráfico de red
        JButton botonRed = crearBotonPersonalizado("Iniciar Tráfico");
        botonRed.addActionListener(this::ejecutarAnalisisRed);
        restricciones.gridx = 0; restricciones.gridy = 2; restricciones.gridwidth = 1; restricciones.weightx = 0.5; restricciones.ipady = 40;
        panelInferior.add(botonRed, restricciones);

        JButton botonProcesos = crearBotonPersonalizado("Simulación Procesos");
        botonProcesos.addActionListener(this::ejecutarSimulacionProcesos);
        restricciones.gridx = 1;
        panelInferior.add(botonProcesos, restricciones);

        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);
        add(panelPrincipal);
    }

    private void ejecutarSeleccionCarpeta(ActionEvent evento) {
        JFileChooser selectorDirectorio = new JFileChooser();
        selectorDirectorio.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (selectorDirectorio.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            campoRutaDirectorio.setText(selectorDirectorio.getSelectedFile().getAbsolutePath());
        }
    }

    private void ejecutarMonitoreoIntegridad(ActionEvent evento) {
        String rutaSeleccionada = campoRutaDirectorio.getText();
        if (rutaSeleccionada.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar una carpeta para iniciar el monitoreo.");
        } else {
            // Se inicia el hilo de integridad
            new GestorIntegridad(rutaSeleccionada, registroLog).start();
        }
    }

    private void ejecutarAnalisisRed(ActionEvent evento) {
        GestorRed administradorRed = new GestorRed(registroLog);
        administradorRed.crearTrafico();
        administradorRed.analizar();
    }

    private void ejecutarSimulacionProcesos(ActionEvent evento) {
        // Se inicia el hilo del gestor de procesos
        new GestorProcesos(registroLog).start();
    }

    private JButton crearBotonPersonalizado(String textoBoton) {
        JButton botonNuevo = new JButton(textoBoton);
        botonNuevo.setFont(new Font("Georgia", Font.BOLD | Font.ITALIC, 14));
        botonNuevo.setForeground(COLOR_BOTON_LILA);
        botonNuevo.setBackground(Color.WHITE);
        botonNuevo.setBorder(new LineBorder(COLOR_BOTON_LILA, 2, true));
        botonNuevo.setFocusPainted(false);
        return botonNuevo;
    }
}