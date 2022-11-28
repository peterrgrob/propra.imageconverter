package propra.imageconverter.image;

import java.io.IOException;
import propra.imageconverter.checksum.Checksum;
import propra.imageconverter.data.DataBlock;
import propra.imageconverter.data.DataCodecRaw;
import propra.imageconverter.data.IDataListener;

/**
 *
 * 
 */
public class ImageCodecRaw extends DataCodecRaw {
    
    // Zugeordnete Resource zur Ein-, oder Ausgabe der Daten 
    protected ImageResource image;
    
    /**
     * 
     */
    public ImageCodecRaw(   ImageResource resource, 
                            Checksum checksum) {
        super(resource, checksum);
        image = resource;
    }
    
    /**
     * 
     */
    @Override
    public void decode( DataBlock block, 
                        IDataListener listener) throws IOException {
        if(!isValid()
        ||  block == null) {
            throw new IllegalArgumentException();
        }
        
        /*
         * Lädt, konvertiert und sendet Pixelblöcke an Listener  
         */
        while(resource.position() < resource.length()) {
            
            // Block dekodieren 
            super.decode(block, null);

            // Pixelformat ggfs. konvertieren
            if(image.getHeader().colorFormat().compareTo(ColorFormat.FORMAT_RGB) != 0) {  
                ColorFormat.convertColorBuffer( block.data, 
                                image.getHeader().colorFormat(), 
                                block.data,
                                ColorFormat.FORMAT_RGB);
            }

            // Daten an Listener senden
            if(listener != null) {
                listener.onData(Event.DATA_BLOCK_DECODED, 
                                this, 
                                block);
            }
        }
    }

    /**
     * 
     */
    @Override
    public void encode(DataBlock block) throws IOException {
        if(!isValid()
        ||  block == null) {
            throw new IllegalArgumentException();
        }
        
        // Farbkonvertierung
        if(image.getHeader().colorFormat().compareTo(ColorFormat.FORMAT_RGB) != 0) {   
            ColorFormat.convertColorBuffer( block.data, 
                                            ColorFormat.FORMAT_RGB, 
                                            block.data,
                                            image.getHeader().colorFormat());
        }
        
        // Block in Resource kodieren
        super.encode(block);
    }
    
    /**
     * 
     */
    @Override
    public void end() throws IOException {
        super.end();
        if(checksum != null) {
            image.header.checksum(checksum.getValue());
        }
    }
}
