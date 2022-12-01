package propra.imageconverter.image;

import java.io.IOException;
import propra.imageconverter.data.DataBlock;
import propra.imageconverter.data.DataCodecRaw;
import propra.imageconverter.data.IDataListener;
import propra.imageconverter.data.IDataListener.Event;

/**
 *
 * 
 */
public class ImageCodecRaw extends DataCodecRaw {
    
    // Zugeordnete Resource zur Ein-, oder Ausgabe der Daten 
    protected Image image;
    
    /**
     * 
     */
    public ImageCodecRaw(   Image resource) {
        super(resource);
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
            super.decode(block, listener);

            // Pixelformat ggfs. konvertieren
            if(image.getHeader().colorFormat().compareTo(ColorFormat.FORMAT_RGB) != 0) {  
                ColorFormat.convertColorBuffer( block.data, 
                                image.getHeader().colorFormat(), 
                                block.data,
                                ColorFormat.FORMAT_RGB);
            }

            // Daten an Listener senden
            dispatchEvent(  Event.DATA_BLOCK_DECODED, 
                            listener, 
                            block);

        }
    }

    /**
     * 
     */
    @Override
    public void encode( DataBlock block,
                        IDataListener listener) throws IOException {
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
        super.encode(block, listener);
        
        // Daten an Listener senden
        dispatchEvent(  Event.DATA_BLOCK_DECODED, 
                            listener, 
                            block);
    }
    
    /**
     * 
     */
    @Override
    public void end() throws IOException {
        super.end();
    }
}
