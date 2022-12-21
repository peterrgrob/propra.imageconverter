package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataCodec;
import propra.imageconverter.data.IDataTarget.Event;
import propra.imageconverter.util.CheckedInputStream;
import propra.imageconverter.data.IDataTarget;


/**
 *  Basiscodec für die Konvertierung von unkomprimierten Pixelblöcken
 */
public class ImageCodec extends DataCodec {
    
    // Zugeordnete Resource zur Ein-, oder Ausgabe der Daten 
    protected ImageResource image;
    
    /**
     * 
     */
    public ImageCodec(ImageResource resource) {
        super(resource);
        image = resource;
    }
    
    /**
     * 
     * @param listener
     * @throws IOException 
     */
    @Override
    public void decode(IDataTarget listener) throws IOException {
        if(!isValid()) {
            throw new IllegalArgumentException();
        }
        
        CheckedInputStream stream = resource.getInputStream();
        
        // Lese Puffer  
        ByteBuffer data = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE);
        boolean bLast = false;
        
        /*
         * Lädt und sendet Pixelblöcke direkt an Listener  
         */
        while(resource.position() < resource.length()) {
            
            // Blockgröße anpassen
            if(resource.length() - resource.position() < data.capacity()) {
                data.limit((int)(resource.length() - resource.position()));
                bLast = true;
            }
            
            // Datenblock von Resource lesen 
            int r = stream.read(data);
            if(r == -1) {
                throw new IOException("Lesefehler!");
            }
            
            // Daten an Listener senden
            listener.onData(Event.DATA_BLOCK_DECODED, 
                            this, 
                            data,
                            bLast);  
            data.clear();
        }
    }

    /**
     * 
     * @param block
     * @param last
     * @param listener
     * @throws IOException 
     */
    @Override
    public void encode( ByteBuffer block,
                        boolean last) throws IOException {
        if(!isValid()
        ||  block == null) {
            throw new IllegalArgumentException();
        }
        
        // Block direkt in Resource schreiben
        resource.getOutputStream()
                .write(block);
    }
    
    /**
     * Sendet Datenblock an Listener
     * 
     * @param event
     * @param listener
     * @param block
     * @param last
     * @throws IOException 
     */
    protected void dispatchData(Event event,
                                IDataTarget listener,
                                ByteBuffer block,
                                boolean last) throws IOException {
        block.flip();
        listener.onData(event, this, block, last);
        block.clear();
    }
}
