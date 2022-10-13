package propra.imageconverter.image;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import propra.imageconverter.util.DataBuffer;

/**
 *
 * @author pg
 */
public class ImageWriter extends BufferedOutputStream {
    /**
     * 
     */
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
        writeHeader(buffer);
        writeContent(output);
        flush();
        
        return output;
    }
     
    /**
     * 
     * @param bytes
     * @throws IOException 
     */
    protected void writeHeader(DataBuffer bytes) throws IOException {
        if(bytes == null) {
            throw new IllegalArgumentException();
        }
        
        write(bytes.getBuffer().array());
    }
    
    /**
    * 
    * @param data
    * @return
    * @throws IOException 
    */
    protected DataBuffer writeContent(DataBuffer data) throws IOException {
        if(data == null) {
            throw new IllegalArgumentException();
        }
        
        write(data.getBuffer().array());
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
