package Modelo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PartidaModel {
    private volatile boolean activa;
    private volatile boolean gameOver;

    private final List<Obstaculo>   obstaculos      = new CopyOnWriteArrayList<>();
    private final List<Enemigo>     enemigos        = new CopyOnWriteArrayList<>();
    private final List<BalaEnemigo> balasEnemigo    = new CopyOnWriteArrayList<>();
    private final List<BalaJugador> balasJugador    = new CopyOnWriteArrayList<>();

    public PartidaModel() { this.activa = false; this.gameOver = false; }

    public synchronized void iniciar()  { this.activa = true;  this.gameOver = false; }
    public synchronized void terminar() { this.activa = false; this.gameOver = true;  }

    public boolean isActiva()   { return activa;   }
    public boolean isGameOver() { return gameOver; }

    // Obstáculos
    public List<Obstaculo> getObstaculos()        { return obstaculos; }
    public void agregarObstaculo(Obstaculo o)      { obstaculos.add(o); }
    public void limpiarObstaculos()               { obstaculos.clear(); }

    // Enemigos
    public List<Enemigo> getEnemigos()            { return enemigos; }
    public void agregarEnemigo(Enemigo e)          { enemigos.add(e); }
    public void limpiarEnemigos()                 { enemigos.clear(); }

    // Balas del enemigo
    public List<BalaEnemigo> getBalasEnemigo()    { return balasEnemigo; }
    public void agregarBalaEnemigo(BalaEnemigo b)  { balasEnemigo.add(b); }

    // Balas del jugador
    public List<BalaJugador> getBalasJugador()    { return balasJugador; }
    public void agregarBalaJugador(BalaJugador b)  { balasJugador.add(b); }
    
    //Victoria
    private volatile boolean victoria = false;

    public boolean isVictoria()         { return victoria; }
    public void setVictoria(boolean v)  { this.victoria = v; }
}