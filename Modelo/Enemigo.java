package Modelo;

import java.util.List;
import Modelo.Jugador;

public class Enemigo implements Runnable {
    private final int x, y;
    private volatile int vida;
    private static final int ANCHO = 60, ALTO = 80;
    private static final int VIDA_MAX = 5;

    // Referencia a la lista de balas del modelo para poder disparar
    private List<BalaEnemigo> listaBalaEnemigo;
    
    // Campo nuevo
    private Jugador jugadorRef;

    public void setJugador(Jugador j) { this.jugadorRef = j; }

    private Thread hilo;
    private volatile boolean corriendo = false;

    public Enemigo(int x, int y) {
        this.x    = x;
        this.y    = y;
        this.vida = VIDA_MAX;
    }

    public void setListaBalaEnemigo(List<BalaEnemigo> lista) {
        this.listaBalaEnemigo = lista;
    }

    public void iniciarHilo() {
        corriendo = true;
        hilo = new Thread(this, "Hilo-Enemigo");
        hilo.setDaemon(true);
        hilo.start();
    }

    public void detenerHilo() {
        corriendo = false;
        if (hilo != null) hilo.interrupt();
    }

    @Override
    public void run() {
        while (corriendo && !estaMuerto()) {
            disparar();
            try { Thread.sleep(1800); }   // dispara cada 1.8 s
            catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
        }
    }

    private void disparar() {
    if (listaBalaEnemigo == null) return;

    int bx     = x;
    int byCentro = y + ALTO / 2 - BalaEnemigo.ALTO / 2;

    // Calcula dirección hacia el jugador si está disponible
    int vyApuntado = 0;
    if (jugadorRef != null) {
        int diferenciaY = (jugadorRef.getY() + 30) - byCentro;  // centro del jugador
        // Normaliza a una velocidad manejable (-5 a 5)
        vyApuntado = Math.max(-5, Math.min(5, diferenciaY / 40));
    }

    // Bala dirigida al jugador
    crearBala(bx, byCentro, 10, vyApuntado);

    // Bala diagonal arriba
    crearBala(bx, byCentro, 9, vyApuntado - 3);

    // Bala diagonal abajo
    crearBala(bx, byCentro, 9, vyApuntado + 3);
    }

    private void crearBala(int x, int y, int vx, int vy) {
    BalaEnemigo b = new BalaEnemigo(x, y, vx, vy);
    listaBalaEnemigo.add(b);
    b.iniciarHilo();
    }

    /** Recibe un impacto; devuelve true si el enemigo muere. */
    public synchronized boolean recibirDanio() {
        if (vida > 0) vida--;
        return vida == 0;
    }

    public boolean estaMuerto()    { return vida <= 0; }
    // El enemigo nunca sale de pantalla por sí solo (es estático)
    public boolean fueraDePantalla() { return false; }

    public int getX()       { return x; }
    public int getY()       { return y; }
    public int getAncho()   { return ANCHO; }
    public int getAlto()    { return ALTO; }
    public int getVida()    { return vida; }
    public int getVidaMax() { return VIDA_MAX; }
}