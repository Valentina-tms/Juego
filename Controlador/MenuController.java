package Controlador;

import Modelo.DatabaseManager;
import Modelo.Jugador;
import Modelo.MusicManager;
import Modelo.PartidaModel;
import Vista.JuegoVista;
import Vista.LoginVista;
import Vista.MenuVista;
import Vista.TablaVista;

public class MenuController {

    private static final String MUSICA_MENU = "/musica/cuento-de-hadas-playlist.wav";

    private MenuVista vista;
    private String jugadorActual   = null;
    private int    idUsuarioActual = -1;

    public MenuController(MenuVista vista) {
        this.vista = vista;
        vista.getbtnPlay().addActionListener(e  -> abrirJuego());
        vista.getbtnLogin().addActionListener(e -> abrirLogin());
        vista.getbtnTabla().addActionListener(e -> abrirTabla());

        // Arrancar música del menú al crear el controlador
        MusicManager.reproducir(MUSICA_MENU);

        // Detener música si el usuario cierra la ventana del menú
        vista.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                MusicManager.detener();
            }
        });
    }

    private void abrirLogin() {
        LoginVista loginVista = new LoginVista(vista);
        new LoginController(loginVista);
        loginVista.setVisible(true);

        String logueado = loginVista.getUsuarioLogueado();
        if (logueado != null) {
            jugadorActual   = logueado;
            idUsuarioActual = loginVista.getIdUsuarioLogueado();
            vista.setJugadorActual(jugadorActual);
        }
    }

    private void abrirJuego() {
        // Detener música del menú antes de entrar al juego
        MusicManager.detener();

        String nombre   = (jugadorActual != null) ? jugadorActual : "Invitado";
        Jugador jugador = new Jugador(nombre, idUsuarioActual);
        
        
        jugador.setPersonaje(vista.getPersonajeSeleccionado()); 

        PartidaModel partida  = new PartidaModel();
        JuegoVista juegoVista = new JuegoVista();
        new JuegoController(jugador, partida, juegoVista);
        juegoVista.setVisible(true);
        vista.setVisible(false);

        juegoVista.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                vista.setVisible(true);
                // Retomar música del menú al volver
                MusicManager.reproducir(MUSICA_MENU);
            }
        });
    }

    private void abrirTabla() {
        TablaVista tablaVista = new TablaVista();
        new TablaController(tablaVista);
        tablaVista.setVisible(true);
    }
}
