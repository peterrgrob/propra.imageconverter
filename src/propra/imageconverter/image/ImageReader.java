package propra.imageconverter.image;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import propra.imageconverter.util.DataBuffer;

/**
 * Implementierung eines BufferedInputStream zum Einlesen der unterstützten
 * Bildformate, formatspezifische Umwandlungen erfolgen durch ein Plugin.
 * 
 * @author pg
 */
public class ImageReader extends BufferedInputStream {

    // Bildformat
    ImagePlugin plugin;
    
    /**
     * 
     * @param in 
     * @param plugin 
     * @throws java.io.IOException 
     */
    public ImageReader(InputStream in, ImagePlugin plugin) throws IOException {
        super(in);
        this.plugin = plugin;
        this.plugin.setInitialAvailableBytes(available());
    } 
    
    /**
     * Liest Bild vom Stream in einen ImageBuffer
     * 
     * @return
     * @throws java.io.IOException
     */
    public ImageBuffer readImage() throws IOException {
        if(plugin == null) {
            throw new IllegalArgumentException("Kein Plugin gesetzt.");
        }
        
        // Header einlesen
        readHeader();
        
        // Genügend Daten vorhanden?
        if(available()< plugin.getHeader().getTotalSize()) {
            throw new java.io.IOException("Fehlerhafte Datenmenge!");
        }
        
        // Farben einlesen
        return readContent(plugin.getHeader().getTotalSize());
    }
    
    /**
     *  Liest Header vom Stream und gibt einen allgemeinen Header
     *  zurück
     * 
     * @return 
     * @throws java.io.IOException
    */
    protected ImageHeader readHeader() throws IOException {
        // Header-Bytes von Stream lesen
        DataBuffer rawBytes = new DataBuffer(plugin.getHeaderSize());
        if(readBytes( rawBytes,plugin.getHeaderSize()) != plugin.getHeaderSize()) {
            throw new java.io.IOException("Ungültiger Dateikopf!");
        }
        // In Header umwandeln
        return plugin.bytesToHeader(rawBytes);
    }
    
    /**
     * Liest Bilddaten vom Stream und gibt einen ImageBuffer zurück
     * 
     * @param len
     * @return
     * @throws IOException 
     */
    protected ImageBuffer readContent(int len) throws IOException {
        return readContent(len, new ImageBuffer(plugin.getHeader()));
    }
    
    /**
    * Liest Bilddaten vom Stream in übergebenen ImageBuffer
    * 
    * @param len
    * @param image
    * @return
    * @throws IOException 
    */
    protected ImageBuffer readContent(int len, ImageBuffer image) throws IOException {
        if(len <= 0 
        || image == null) {
            throw new IllegalArgumentException();
        }
 
        // Farbwerte einlesen
        DataBuffer rawBytes = new DataBuffer(len);
        if(readBytes(rawBytes, len) != len) {
            throw new java.io.IOException("Nicht genug Bilddaten lesbar.");
        }
        
        // Checksum über Bytes berechnen und prüfen, falls Prüfsumme vorhanden
        if(plugin.isCheckable()) {
            if(plugin.check(rawBytes.getBytes()) != plugin.getHeader().getChecksum()) {
                throw new java.io.IOException("Prüfsummenfehler.");
            }
        }
        
        // Farbbytes ggfs. umwandeln, je nach Plugin Objekt
        image = plugin.bytesToContent(rawBytes);
        return image;
    }
    
    /**
     * 
     * @return 
     */
    public ImageHeader getHeader() {
        if(plugin == null) {
            throw new IllegalArgumentException("Kein Plugin gesetzt.");
        }
        return plugin.getHeader();
    }
    
    /**
     * Liest Bytes vom Stream
     * 
     * @param len
     * @param data
     * @return
     * @throws IOException 
     */
    protected int readBytes(DataBuffer data, int len) throws IOException {
        if(len == 0 
        || data == null ) {
            throw new IllegalArgumentException();
        }
        return read(data.getBuffer().array(), 0, len);
    }  
    
    /**
     *
     * @return
     */
    public ImagePlugin getPlugin() {
        return plugin;
    }
}
