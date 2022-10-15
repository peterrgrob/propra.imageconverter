package propra.imageconverter.image;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import propra.imageconverter.util.DataBuffer;

/**
 * Implementierung eines BufferedOutputStream zum Schreiben der unterstützten
 * Bildformate, formatspezifische Umwandlungen erfolgen durch ein Plugin.
 * 
 * @author pg
 */
public class ImageWriter extends BufferedOutputStream {
    // Bildformat
    ImagePlugin plugin;
    
    /**
     * 
     * @param out 
     * @param plugin 
     */
    public ImageWriter(OutputStream out, ImagePlugin plugin) {
        super(out);
        this.plugin = plugin;
    }
    
    /**
     * Schreibt ImageBuffer in den Stream 
     * 
     * @param image
     * @return 
     * @throws IOException
     */
    public ImageBuffer writeImage(ImageBuffer image) throws IOException {
        if( image == null
        ||  plugin == null) {
            throw new IllegalArgumentException();
        }

        // Finale Daten zur Ausgabe erstellen durch Plugin
        DataBuffer buffer = plugin.headerToBytes(image.getHeader());
        ImageBuffer output = plugin.contentToBytes(image, buffer);
        
        // Erstellte Daten in den Stream schreiben
        writeData(buffer);
        writeData(output);
        flush();
        
        return output;
    }

    /**
    * Schreibt Daten in den Stream
    * 
    * @param data
    * @return
    * @throws IOException 
    */
    protected DataBuffer writeData(DataBuffer data) throws IOException {
        if(data == null) {
            throw new IllegalArgumentException();
        }
        
        write(data.getBytes());
        return data;
    }
    
    /**
     *
     * @return
     */
    public ImagePlugin getPlugin() {
        return plugin;
    }
    
    
}
