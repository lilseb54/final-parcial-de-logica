package src.controlador;

import src.modelo.Predio;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PredioController {
    private List<Predio> listaPredios;

    public PredioController() {
        this.listaPredios = new ArrayList<>();
    }

    public List<Predio> getListaPredios() {
        return listaPredios;
    }

    /**
     * Carga de forma segura los datos desde el archivo plano predios.csv
     */
    public void cargarDatosDesdeCSV(String rutaArchivo) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            boolean esPrimeraLinea = true;
            
            while ((linea = br.readLine()) != null) {
                if (esPrimeraLinea) {
                    esPrimeraLinea = false;
                    continue; // Omite la fila de los encabezados (npn, municipio...)
                }
                if (linea.trim().isEmpty()) continue;

                String[] datos = linea.split(",", -1);
                
                // Procesamiento de índices seguros (0 a 3)
                if (datos.length >= 4) {
                    Predio predio = new Predio(datos[0], datos[1], datos[2], datos[3]);
                    listaPredios.add(predio);
                }
            }
        }
    }

    /**
     * Algoritmo de Ordenamiento Rápido: Quicksort
     */
    public void ordenarPorQuicksort(int columna) {
        if (!listaPredios.isEmpty()) {
            quicksort(0, listaPredios.size() - 1, columna);
        }
    }

    private void quicksort(int bajo, int alto, int columna) {
        if (bajo < alto) {
            int indicePivote = particion(bajo, alto, columna);
            quicksort(bajo, indicePivote - 1, columna);
            quicksort(indicePivote + 1, alto, columna);
        }
    }

    private int particion(int bajo, int alto, int columna) {
        String valPivote = listaPredios.get(alto).getValorPorColumna(columna);
        String pivote = (valPivote != null) ? valPivote.toLowerCase() : "";
        
        int i = bajo - 1;

        for (int j = bajo; j < alto; j++) {
            String valAct = listaPredios.get(j).getValorPorColumna(columna);
            String valorActual = (valAct != null) ? valAct.toLowerCase() : "";
            
            // OPTIMIZACIÓN CONTRA CONGELAMIENTOS:
            // Reparte los elementos idénticos de manera balanceada usando j % 2 == 0
            if (valorActual.compareTo(pivote) < 0 || (valorActual.equals(pivote) && j % 2 == 0)) {
                i++;
                Predio temp = listaPredios.get(i);
                listaPredios.set(i, listaPredios.get(j));
                listaPredios.set(j, temp);
            }
        }
        
        Predio temp = listaPredios.get(i + 1);
        listaPredios.set(i + 1, listaPredios.get(alto));
        listaPredios.set(alto, temp);

        return i + 1;
    }

    /**
     * Algoritmo de Búsqueda Binaria con Expansión Lateral Corregida
     */
    public List<Predio> buscarPorAtributo(String criterio, int columna) {
        List<Predio> resultados = new ArrayList<>();
        String criterioBuscado = criterio.toLowerCase().trim();
        
        int bajo = 0;
        int alto = listaPredios.size() - 1;
        int indiceEncontrado = -1;

        // 1. Fase Binaria: Localizar un punto inicial que contenga el término parcial
        while (bajo <= alto) {
            int medio = bajo + (alto - bajo) / 2;
            
            String valMedio = listaPredios.get(medio).getValorPorColumna(columna);
            String valorMedio = (valMedio != null) ? valMedio.toLowerCase() : "";

            if (valorMedio.contains(criterioBuscado)) {
                indiceEncontrado = medio;
                break; 
            }

            if (valorMedio.compareTo(criterioBuscado) < 0) {
                bajo = medio + 1;
            } else {
                alto = medio - 1;
            }
        }

        // 2. Fase de Expansión Lateral: Extraer de forma segura los registros similares
        if (indiceEncontrado != -1) {
            // Recorrer hacia la izquierda
            int izq = indiceEncontrado;
            while (izq >= 0) {
                String valIzq = listaPredios.get(izq).getValorPorColumna(columna);
                String valorIzq = (valIzq != null) ? valIzq.toLowerCase() : "";
                
                if (!valorIzq.contains(criterioBuscado)) break;
                
                // CORRECCIÓN: Agregar al principio de manera limpia usando índice 0
                resultados.add(0, listaPredios.get(izq));
                izq--;
            }

            // Recorrer hacia la derecha (empezando desde el siguiente elemento)
            int der = indiceEncontrado + 1;
            while (der < listaPredios.size()) {
                String valDer = listaPredios.get(der).getValorPorColumna(columna);
                String valorDer = (valDer != null) ? valDer.toLowerCase() : "";
                
                if (!valorDer.contains(criterioBuscado)) break;
                
                resultados.add(listaPredios.get(der));
                der++;
            }
        }
        
        return resultados;
    }
}