package Controlador;

import Modelo.DatabaseManager;
import Vista.TablaVista;
import java.util.List;

public class TablaController {

    public TablaController(TablaVista vista) {
        List<String[]> top = DatabaseManager.getTopRanking(15);
        
    // En TablaController, antes de vista.cargarDatos(top):
    for (String[] fila : top) {
    long ms = Long.parseLong(fila[1]);
    long min = ms / 60000;
    long seg = (ms % 60000) / 1000;
    long mil = ms % 1000;
    fila[1] = String.format("%02d:%02d.%03d", min, seg, mil);
    }
    
        vista.cargarDatos(top);
    }
}
