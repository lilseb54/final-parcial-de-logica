package src.vista;

import src.controlador.PredioController;
import src.modelo.Predio;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

public class MainView extends JFrame {
    private PredioController controller;
    
    // Componentes Gráficos
    private JComboBox<String> comboCriterio;
    private JTextField txtBuscar;
    private JButton btnBuscar;
    private JTable tablaResultados;
    private DefaultTableModel modeloTabla;
    private JLabel lblStatus;
    private JLabel lblTiempo;

    public MainView() {
        controller = new PredioController();
        configurarVentana();
        inicializarComponentes();
        cargarDatosDeFondo();
    }

    private void configurarVentana() {
        setTitle("Sistema de Gestión Catastral de Antioquia");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
    }

    private void inicializarComponentes() {
        // --- Panel Superior: Filtros de Búsqueda ---
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelSuperior.setBorder(BorderFactory.createTitledBorder("Panel de Búsqueda y Filtrado"));

        panelSuperior.add(new JLabel("Buscar por:"));
        comboCriterio = new JComboBox<>(new String[]{"NPN", "Municipio", "Dirección", "Número Ficha"});
        panelSuperior.add(comboCriterio);

        panelSuperior.add(new JLabel("Término:"));
        txtBuscar = new JTextField(25);
        panelSuperior.add(txtBuscar);

        btnBuscar = new JButton("Buscar con Quicksort & Binaria");
        btnBuscar.setBackground(new Color(33, 150, 243));
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.setFocusPainted(false);
        panelSuperior.add(btnBuscar);

        add(panelSuperior, BorderLayout.NORTH);

        // --- Panel Central: Tabla de Resultados ---
        String[] columnas = {"NPN", "Municipio", "Dirección", "Número Ficha"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaResultados = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaResultados);
        scrollTabla.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        add(scrollTabla, BorderLayout.CENTER);

        // --- Panel Inferior: Barra de Estado e Indicadores de Tiempo ---
        JPanel panelInferior = new JPanel(new GridLayout(1, 2, 10, 0));
        panelInferior.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        lblStatus = new JLabel("Estado: Esperando base de datos...");
        lblStatus.setFont(new Font("SansSerif", Font.BOLD, 12));
        
        lblTiempo = new JLabel("Tiempo de ejecución: 0.000000 segundos", SwingConstants.RIGHT);
        lblTiempo.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblTiempo.setForeground(new Color(76, 175, 80));

        panelInferior.add(lblStatus);
        panelInferior.add(lblTiempo);
        add(panelInferior, BorderLayout.SOUTH);

        // Acción del Botón Buscar
        btnBuscar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ejecutarBusquedaOptimizada();
            }
        });
    }

    private void cargarDatosDeFondo() {
        String rutaArchivo = "predios.csv";
        try {
            long inicio = System.nanoTime();
            controller.cargarDatosDesdeCSV(rutaArchivo);
            long fin = System.nanoTime();
            double tiempoCarga = (fin - inicio) / 1_000_000_000.0;
            
            lblStatus.setText("Estado: Base de datos cargada (" + controller.getListaPredios().size() + " predios).");
            JOptionPane.showMessageDialog(this, 
                    "Archivo 'predios.csv' cargado con éxito en " + String.format("%.4f", tiempoCarga) + " segundos.\n" +
                    "Total de registros: " + controller.getListaPredios().size(), 
                    "Carga Completa", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            lblStatus.setText("Estado: [Error] No se encontró predios.csv");
            JOptionPane.showMessageDialog(this, 
                    "No se pudo leer el archivo 'predios.csv' en la raíz del proyecto.\nDetalle: " + e.getMessage(), 
                    "Error de Archivo", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ejecutarBusquedaOptimizada() {
        String criterioTexto = txtBuscar.getText().trim();
        int columnaSeleccionada = comboCriterio.getSelectedIndex() + 1; // Mapea a 1, 2, 3 o 4

        if (criterioTexto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese un término para buscar.", "Campo Vacío", JOptionPane.WARNING_MESSAGE);
            return;
        }

        lblStatus.setText("Procesando ordenamiento Quicksort y Búsqueda Binaria...");
        
        // --- INICIO DE MEDICIÓN CRÍTICA ---
        long tiempoInicio = System.nanoTime();

        // 1. Requisito del Examen: Ordenar primero con Quicksort
        controller.ordenarPorQuicksort(columnaSeleccionada);

        // 2. Requisito del Examen: Búsqueda Binaria
        List<Predio> resultados = controller.buscarPorAtributo(criterioTexto, columnaSeleccionada);

        long tiempoFin = System.nanoTime();
        // --- FIN DE MEDICIÓN ---

        double tiempoTotalSegundos = (tiempoFin - tiempoInicio) / 1_000_000_000.0;
        lblTiempo.setText(String.format("Tiempo de ejecución: %.6f segundos", tiempoTotalSegundos));

        // Actualizar la cuadrícula de datos en la aplicación
        modeloTabla.setRowCount(0); // Limpiar filas anteriores
        
        if (resultados.isEmpty()) {
            lblStatus.setText("Búsqueda finalizada. 0 coincidencias encontradas.");
            JOptionPane.showMessageDialog(this, "No se encontraron registros con el término: " + criterioTexto, "Sin Resultados", JOptionPane.INFORMATION_MESSAGE);
        } else {
            lblStatus.setText("Búsqueda finalizada. " + resultados.size() + " coincidencias agregadas a la tabla.");
            for (Predio p : resultados) {
                modeloTabla.addRow(new Object[]{p.getNpn(), p.getMunicipio(), p.getDireccion(), p.getFicha()});
            }
        }
    }

    public static void main(String[] args) {
        // Asegura que la interfaz gráfica corra de manera segura en el hilo de Swing
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainView().setVisible(true);
            }
        });
    }
}