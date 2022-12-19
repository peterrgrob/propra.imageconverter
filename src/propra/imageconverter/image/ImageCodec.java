package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataBlock;
import propra.imageconverter.data.DataCodec;
import static propra.imageconverter.data.DataCodec.DEFAULT_BLOCK_SIZE;
import propra.imageconverter.data.IDataListener;
import propra.imageconverter.data.IDataListener.Event;


/**
 *
 * 
 */
public class ImageCodec extends DataCodec {
    
    // Zugeordnete Resource zur Ein-, oder Ausgabe der Daten 
    protected ImageResource image;
    
    /**
     * 
     */
    public ImageCodec(   ImageResource resource) {
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
        
        // Lese Puffer 
        block.data = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE);
        
        /*
         * Lädt, konvertiert und sendet Pixelblöcke an Listener  
         */
        while(resource.position() < resource.length()) {
            
            // Blockgröße anpassen
            if(resource.length() - resource.position() < block.data.capacity()) {
                block.data.limit((int)(resource.length() - resource.position()));
                block.lastBlock = true;
            }
            
            // Datenblock von Resource lesen 
            int r = resource.getInputStream()
                            .read(block.data);
            if(r == -1) {
                throw new IOException("Lesefehler!");
            }

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
            block.data.clear();
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
        resource.getOutputStream()
                .write(block.data);
        
        // Daten an Listener senden
        dispatchEvent(  Event.DATA_BLOCK_ENCODED, 
                            listener, 
                            block);
    }
    
    /**
     *  Sendet Datenblock an Listener
     */
    protected void dispatchData(Event event,
                                IDataListener listener,
                                DataBlock block) throws IOException {
        block.data.flip();
        dispatchEvent(event, listener, block);
        block.data.clear();
    }
}
