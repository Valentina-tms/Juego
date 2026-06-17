package Modelo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GestorImagenes {

    private static final Map<String, BufferedImage> cache = new HashMap<>();

    public static BufferedImage get(String nombre) {
        if (cache.containsKey(nombre)) return cache.get(nombre);
        try {
            BufferedImage img = ImageIO.read(
                GestorImagenes.class.getResourceAsStream("/resources/" + nombre + ".png")
            );
            cache.put(nombre, img);
            return img;
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("No se pudo cargar: " + nombre + ".png");
            return null;
        }
    }
}
