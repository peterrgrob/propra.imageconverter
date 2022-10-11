package propra.imageconverter.image;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import propra.imageconverter.util.DataBuffer;

/**
 *
 * @author pg
 */
public class ImageReader extends BufferedInputStream {
    /**
     * 
     */
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
     *
     * @return
     * @throws java.io.IOException
     */
    public ImageBuffer readImage() throws IOException {
        if(plugin == null) {
            throw new IllegalArgumentException("Kein Plugin gesetzt.");
        }
        readHeader();
        return readContent(plugin.getHeader().getTotalSize());
    }
    
    /**
    * 
     * @return 
     * @throws java.io.IOException
    */
    protected ImageHeader readHeader() throws IOException {
        /**
         * Header-Bytes von Stream lesen
         */
        byte[] rawBytes = new byte[plugin.getHeaderSize()];
        if(readBytes( rawBytes,plugin.getHeaderSize()) != plugin.getHeaderSize()) {
            throw new java.io.IOException("Ungültiger Dateikopf.");
        }
        
        DataBuffer dataBuffer = new DataBuffer(rawBytes);
        return plugin.bytesToHeader(dataBuffer);
    }
    
    /**
     * 
     * @param len
     * @return
     * @throws IOException 
     */
    protected ImageBuffer readContent(int len) throws IOException {
        return readContent(len, new ImageBuffer(plugin.getHeader()));
    }
    
    /**
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
 
        /*
         * Farbbytes einlesen.
         */
        byte[] bytes = new byte[len];
        if(readBytes(bytes, len) != len) {
            throw new java.io.IOException("Nicht genug Bilddaten lesbar.");
        }
        
        /*
         * Checksum über Bytes berechnen und prüfen, falls Prüfsumme vorhanden.
         */
        if(plugin.isCheckable()) {
            if(plugin.check(bytes) != plugin.getHeader().getChecksum()) {
                throw new java.io.IOException("Prüfsummenfehler.");
            }
        }
        
        image = plugin.bytesToContent(new DataBuffer(bytes));
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
     * 
     * @param len
     * @param data
     * @return
     * @throws IOException 
     */
    protected int readBytes(byte[] data, int len) throws IOException {
        if(len == 0 
        || data == null ) {
            throw new IllegalArgumentException();
        }
        return read(data, 0, len);
    }    
}
