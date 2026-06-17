package Modelo;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class MusicManager {

    private static Clip clipActual = null;
    private static float volumen   = 0.05f; 

    public static void reproducir(String rutaEnClasspath) {
        detener();
        try {
            URL url = MusicManager.class.getResource(rutaEnClasspath);
            if (url == null) {
                System.err.println("[MusicManager] No se encontró el archivo: " + rutaEnClasspath);
                return;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            clipActual = AudioSystem.getClip();
            clipActual.open(ais);
            aplicarVolumen();
            clipActual.loop(Clip.LOOP_CONTINUOUSLY);
            clipActual.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("[MusicManager] Error al reproducir " + rutaEnClasspath + ": " + e.getMessage());
        }
    }
    
    public static void detener() {
        if (clipActual != null) {
            if (clipActual.isRunning()) clipActual.stop();
            clipActual.close();
            clipActual = null;
        }
    }

    public static void pausar() {
        if (clipActual != null && clipActual.isRunning()) {
            clipActual.stop();
        }
    }

    public static void reanudar() {
        if (clipActual != null && !clipActual.isRunning()) {
            clipActual.start();
        }
    }

    public static void setVolumen(float nuevoVolumen) {
        volumen = Math.max(0f, Math.min(1f, nuevoVolumen));
        aplicarVolumen();
    }

    public static float getVolumen() {
        return volumen;
    }

    public static boolean estaReproduciendo() {
        return clipActual != null && clipActual.isRunning();
    }

    private static void aplicarVolumen() {
        if (clipActual == null) return;
        try {
            FloatControl gainControl =
                (FloatControl) clipActual.getControl(FloatControl.Type.MASTER_GAIN);
            
            float dB = (volumen == 0f)
                ? gainControl.getMinimum()
                : (float)(20.0 * Math.log10(volumen));
            
            dB = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB));
            gainControl.setValue(dB);
        } catch (IllegalArgumentException e) {
            
        }
    }
}
