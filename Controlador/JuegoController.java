package Controlador;

import Modelo.*;
import Modelo.DatabaseManager;
import Vista.JuegoVista;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class JuegoController {

    private static final String MUSICA_JUEGO =
        "/musica/Slipknot-Psychosocial-_OFFICIAL-VIDEO_-_HD_-Slipknot-_192k_.wav";

    private final Jugador      jugador;
    private final PartidaModel partida;
    private final JuegoVista   vista;

    // ── Constantes jugador ────────────────────────────────────────────────
    private static final int SUELO      = 550;
    private static final int ALTO_JUG   = 60;
    private static final int ANCHO_JUG  = 45;
    private static final int JUG_X      = 100;
    private static final int Y_MIN      = 0;
    private static final int Y_MAX      = 700;

    private static final int  VELOCIDAD_Y_JUG    = 5;
    private static final int  VELOCIDAD_BALA_JUG = 10;
    private static final int  PENALIZACION        = 20;
    private static final long COOLDOWN_DISPARO_MS = 400;

    // ── Constantes obstáculos ─────────────────────────────────────────────
    private static final int OBS_SPAWN_MIN = 900;
    private static final int OBS_SPAWN_MAX = 2800;

    // ── Constantes enemigo ────────────────────────────────────────────────
    private static final int ENE_X           = 1080;
    private static final int ENE_Y           = 350;
    private static final int PUNTOS_POR_KILL = 100;

    // ── Estado jugador ────────────────────────────────────────────────────
    private volatile float   velY         = 0;
    private volatile boolean enAire       = false;
    private volatile boolean teclaArriba  = false;
    private volatile boolean teclaAbajo   = false;
    private volatile long    ultimoDisparo = 0;

    // ── Hilos ─────────────────────────────────────────────────────────────
    private Thread hiloJugador, hiloSpawnObstaculo;
    private Thread hiloLimpieza, hiloRender;
    private volatile boolean hilosActivos = false;

    // ── Sets de colisión ──────────────────────────────────────────────────
    private final Set<Enemigo>     enemigosTocados  = ConcurrentHashMap.newKeySet();
    private final Set<BalaEnemigo> balasYaGolpearon = ConcurrentHashMap.newKeySet();
    private final Set<Obstaculo>   obstaculosTocados = ConcurrentHashMap.newKeySet(); // ← faltaba

    public JuegoController(Jugador jugador, PartidaModel partida, JuegoVista vista) {
    this.jugador = jugador;
    this.partida = partida;
    this.vista   = vista;

    partida.iniciar();
    jugador.setX(JUG_X);
    jugador.setY(Y_MAX);

    configurarTeclado();
    spawnEnemigoPrincipal();

    MusicManager.reproducir(MUSICA_JUEGO);

    // Mostrar instrucciones; el cronómetro y los hilos solo arrancan al continuar
    vista.mostrarInstrucciones(() -> {
        jugador.iniciarCronometro();
        iniciarHilos();
    });
    }

    // ── Teclado ───────────────────────────────────────────────────────────

    private void configurarTeclado() {
        vista.getPanel().setFocusable(true);
        vista.getPanel().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                    case KeyEvent.VK_UP:    teclaArriba = true;  break;
                    case KeyEvent.VK_S:
                    case KeyEvent.VK_DOWN:  teclaAbajo  = true;  break;
                    case KeyEvent.VK_SPACE: saltarJugador();      break;
                    case KeyEvent.VK_F:     dispararJugador();    break;
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                    case KeyEvent.VK_UP:   teclaArriba = false; break;
                    case KeyEvent.VK_S:
                    case KeyEvent.VK_DOWN: teclaAbajo  = false; break;
                }
            }
        });
    }

    private void saltarJugador() {
        if (!enAire) { velY = -14; enAire = true; }
    }

    private void dispararJugador() {
        long ahora = System.currentTimeMillis();
        if (ahora - ultimoDisparo < COOLDOWN_DISPARO_MS) return;
        ultimoDisparo = ahora;
        int bx = jugador.getX() + ANCHO_JUG;
        int by = jugador.getY() + ALTO_JUG / 2 - BalaJugador.ALTO / 2;
        BalaJugador b = new BalaJugador(bx, by, VELOCIDAD_BALA_JUG);
        partida.agregarBalaJugador(b);
        b.iniciarHilo();
    }

    // ── Spawn enemigo ─────────────────────────────────────────────────────

    private void spawnEnemigoPrincipal() {
        Enemigo e = new Enemigo(ENE_X, ENE_Y);
        e.setListaBalaEnemigo(partida.getBalasEnemigo());
        e.setJugador(jugador);
        partida.agregarEnemigo(e);
        e.iniciarHilo();
    }

    // ── Hilos ─────────────────────────────────────────────────────────────

    private void iniciarHilos() {
        hilosActivos = true;
        hiloJugador        = new Thread(this::loopJugador,        "Hilo-Jugador");
        hiloSpawnObstaculo = new Thread(this::loopSpawnObstaculo, "Hilo-SpawnObstaculo");
        hiloLimpieza       = new Thread(this::loopLimpieza,       "Hilo-Limpieza");
        hiloRender         = new Thread(this::loopRender,         "Hilo-Render");
        for (Thread t : new Thread[]{hiloJugador,
                hiloSpawnObstaculo, hiloLimpieza, hiloRender}) {
            t.setDaemon(true);
            t.start();
        }
    }

    private void detenerHilos() {
        hilosActivos = false;
        for (Thread t : new Thread[]{hiloJugador,
                hiloSpawnObstaculo, hiloLimpieza, hiloRender}) {
            if (t != null) t.interrupt();
        }
        partida.getObstaculos().forEach(Obstaculo::detenerHilo);
        partida.getEnemigos().forEach(Enemigo::detenerHilo);
        partida.getBalasEnemigo().forEach(BalaEnemigo::detenerHilo);
        partida.getBalasJugador().forEach(BalaJugador::detenerHilo);
    }

    // ── Loop jugador ──────────────────────────────────────────────────────

    private void loopJugador() {
        while (hilosActivos && partida.isActiva()) {
            moverJugador();
            try { Thread.sleep(16); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
        }
    }

    private void moverJugador() {
        int y = jugador.getY();
        if (teclaArriba) y = Math.max(Y_MIN, y - VELOCIDAD_Y_JUG);
        if (teclaAbajo)  y = Math.min(Y_MAX, y + VELOCIDAD_Y_JUG);
        jugador.setY(y);

        if (enAire) {
            velY += 0.8f;
            int nuevoY = (int)(jugador.getY() + velY);
            if (nuevoY >= Y_MAX) { nuevoY = Y_MAX; velY = 0; enAire = false; }
            if (nuevoY <= Y_MIN) { nuevoY = Y_MIN; velY = 0; }
            jugador.setY(nuevoY);
        }
    }

    // ── Loop puntaje ──────────────────────────────────────────────────────

    

    // ── Loop spawn obstáculos ─────────────────────────────────────────────

    private void loopSpawnObstaculo() {
        while (hilosActivos && partida.isActiva()) {
            int delay = OBS_SPAWN_MIN +
                (int)(Math.random() * (OBS_SPAWN_MAX - OBS_SPAWN_MIN));
            try { Thread.sleep(delay); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
            if (!hilosActivos || !partida.isActiva()) break;
            spawnObstaculo();
        }
    }

    private void spawnObstaculo() {
        int xSpawn = 1200;
        int[] opcionesVY = {-3, -2, 0, 0, 2, 3};
        int vy     = opcionesVY[(int)(Math.random() * opcionesVY.length)];
        int ySpawn = 100 + (int)(Math.random() * 500);
        Obstaculo o = new Obstaculo(xSpawn, ySpawn, 4 + (int)(Math.random() * 4), vy);
        partida.agregarObstaculo(o);
        o.iniciarHilo();
    }

    // ── Loop limpieza ─────────────────────────────────────────────────────

    private void loopLimpieza() {
        while (hilosActivos && partida.isActiva()) {
            partida.getObstaculos().removeIf(o -> {
                if (o.fueraDePantalla()) {
                    o.detenerHilo();
                    obstaculosTocados.remove(o); // ← limpia el set
                    return true;
                }
                return false;
            });
            partida.getBalasEnemigo().removeIf(b -> {
                if (b.fueraDePantalla()) {
                    b.detenerHilo();
                    balasYaGolpearon.remove(b);
                    return true;
                }
                return false;
            });
            partida.getBalasJugador().removeIf(b -> {
                if (b.fueraDePantalla()) { b.detenerHilo(); return true; }
                return false;
            });
            try { Thread.sleep(33); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
        }
    }

    // ── Loop render + colisiones ──────────────────────────────────────────

    private void loopRender() {
        while (hilosActivos && partida.isActiva()) {
            verificarColisionesObstaculos();
            verificarColisionesEscombros();
            verificarColisionesEnemigos();
            verificarColisionesBalasEnemigo();
            verificarColisionesBalasJugador();
            SwingUtilities.invokeLater(() -> vista.actualizar(jugador, partida));
            try { Thread.sleep(16); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
        }
    }

    // ── Colisiones obstáculos ─────────────────────────────────────────────

    private void verificarColisionesObstaculos() {
        int jx = jugador.getX(), jy = jugador.getY();
        boolean colisionLateral = false;
        for (Obstaculo o : partida.getObstaculos()) {
            int ox = o.getX(), oy = o.getY(), ow = o.getAncho(), oh = o.getAlto();
            boolean sx = jx + ANCHO_JUG > ox && jx < ox + ow;
            boolean sy = jy + ALTO_JUG  > oy && jy < oy + oh;
            if (!sx || !sy) continue;
            int penAbajo  = (jy + ALTO_JUG) - oy;
            int penArriba = (oy + oh) - jy;
            int minPenX   = Math.min((jx + ANCHO_JUG) - ox, (ox + ow) - jx);
            int minPenY   = Math.min(penAbajo, penArriba);
            if (minPenY <= minPenX) {
                if (penAbajo <= penArriba) { jugador.setY(oy - ALTO_JUG); velY = 0; enAire = false; }
                else                       { jugador.setY(oy + oh);       velY = 0; }
            } else {
                colisionLateral = true;
            }
        }
        if (colisionLateral)
            partida.getObstaculos().forEach(o -> o.setVelocidad(0));
        else
            partida.getObstaculos().forEach(o -> { if (o.getVelocidad() == 0) o.setVelocidad(4); });
    }

    // ── Colisiones escombros ↔ jugador (quita vida) ───────────────────────

    private void verificarColisionesEscombros() {
        int jx = jugador.getX(), jy = jugador.getY();
        for (Obstaculo o : partida.getObstaculos()) {
            if (obstaculosTocados.contains(o)) continue;
            boolean sx = jx + ANCHO_JUG > o.getX() && jx < o.getX() + o.getAncho();
            boolean sy = jy + ALTO_JUG  > o.getY() && jy < o.getY() + o.getAlto();
            if (sx && sy) {
                obstaculosTocados.add(o);
                jugador.setPuntaje(Math.max(0, jugador.getPuntaje() - 100));
                if (jugador.perderVida()) { gameOver(); return; }
            }
        }
    }

    // ── Colisiones jugador ↔ cuerpo enemigo ──────────────────────────────

    private void verificarColisionesEnemigos() {
        int jx = jugador.getX(), jy = jugador.getY();
        for (Enemigo e : partida.getEnemigos()) {
            if (enemigosTocados.contains(e)) continue;
            boolean sx = jx + ANCHO_JUG > e.getX() && jx < e.getX() + e.getAncho();
            boolean sy = jy + ALTO_JUG  > e.getY() && jy < e.getY() + e.getAlto();
            if (sx && sy) {
                enemigosTocados.add(e);
                jugador.setPuntaje(Math.max(0, jugador.getPuntaje() - PENALIZACION));
                if (jugador.perderVida()) { gameOver(); return; }
            }
        }
    }

    // ── Colisiones balas enemigo ↔ jugador ────────────────────────────────

    private void verificarColisionesBalasEnemigo() {
        int jx = jugador.getX(), jy = jugador.getY();
        for (BalaEnemigo b : partida.getBalasEnemigo()) {
            if (balasYaGolpearon.contains(b)) continue;
            boolean sx = jx + ANCHO_JUG > b.getX() && jx < b.getX() + BalaEnemigo.ANCHO;
            boolean sy = jy + ALTO_JUG  > b.getY() && jy < b.getY() + BalaEnemigo.ALTO;
            if (sx && sy) {
                balasYaGolpearon.add(b);
                b.detenerHilo();
                partida.getBalasEnemigo().remove(b);
                jugador.setPuntaje(Math.max(0, jugador.getPuntaje() - PENALIZACION));
                if (jugador.perderVida()) { gameOver(); return; }
            }
        }
    }

    // ── Colisiones balas jugador ↔ enemigo ────────────────────────────────

    private void verificarColisionesBalasJugador() {
        for (BalaJugador b : partida.getBalasJugador()) {
            for (Enemigo e : partida.getEnemigos()) {
                if (e.estaMuerto()) continue;
                boolean sx = b.getX() + BalaJugador.ANCHO > e.getX() && b.getX() < e.getX() + e.getAncho();
                boolean sy = b.getY() + BalaJugador.ALTO  > e.getY() && b.getY() < e.getY() + e.getAlto();
                if (sx && sy) {
                    b.detenerHilo();
                    partida.getBalasJugador().remove(b);
                    if (e.recibirDanio()) {
                        e.detenerHilo();
                        partida.getEnemigos().remove(e);
                        jugador.setPuntaje(jugador.getPuntaje() + PUNTOS_POR_KILL);
                        victoria();
                        return;
                    }
                    break;
                }
            }
        }
    }

    // ── Victoria ──────────────────────────────────────────────────────────

    private void victoria() {
        if (!partida.isActiva()) return;
        jugador.detenerCronometro();
        partida.terminar();
        partida.setVictoria(true);
        detenerHilos();
        MusicManager.detener();
        if (jugador.estaLogueado())
            DatabaseManager.guardarPuntaje(jugador.getIdUsuario(), (int) jugador.getTiempoTranscurrido());
        SwingUtilities.invokeLater(() -> {
            vista.actualizar(jugador, partida);
            Timer cierre = new Timer(3000, e -> vista.dispose());
            cierre.setRepeats(false);
            cierre.start();
        });
    }

    // ── Game Over ─────────────────────────────────────────────────────────

    private void gameOver() {
        if (!partida.isActiva()) return;
        jugador.detenerCronometro();
        partida.terminar();
        detenerHilos();
        MusicManager.detener();
        if (jugador.estaLogueado())
            DatabaseManager.guardarPuntaje(jugador.getIdUsuario(),  (int) jugador.getTiempoTranscurrido());
        SwingUtilities.invokeLater(() -> {
            vista.actualizar(jugador, partida);
            Timer cierre = new Timer(2000, e -> vista.dispose());
            cierre.setRepeats(false);
            cierre.start();
        });
    }
}