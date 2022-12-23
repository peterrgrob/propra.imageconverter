package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataCompression;
import propra.imageconverter.data.IDataCompression;
import propra.imageconverter.data.IDataTarget.Event;
import propra.imageconverter.util.CheckedInputStream;
import propra.imageconverter.data.IDataTarget;
import propra.imageconverter.image.huffman.ImageCompressionHuffman;


/**
 *  Basiscodec für die Konvertierung von unkomprimierten Pixelblöcken
 */
public class ImageCompressionRaw extends DataCompression {
    
    // Zugeordnete Resource zur Ein-, oder Ausgabe der Daten 
    protected ImageResource image;
    
    /**
     * 
     */
    public ImageCompressionRaw(ImageResource resource) {
        super(resource);
        image = resource;
    }
    
    /**
     * 
     * @param target
     * @throws IOException 
     */
    @Override
    public void decode(IDataTarget target) throws IOException {
        if(target == null) {
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
            int r = stream.read(data.array(), data.position(), data.limit());
            if(r == -1) {
                throw new IOException("Lesefehler!");
            }
            
            // Daten an Listener senden
            target.onData(Event.DATA_DECODED, 
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
    public void encode(ByteBuffer block, boolean last) throws IOException {
        if(block == null) {
            throw new IllegalArgumentException();
        }
        
        // Block direkt in Resource schreiben
        resource.getOutputStream()
                .write(block);
    }
    
    /**
     * Sendet Datenblock an target
     * 
     * @param event
     * @param target
     * @param block
     * @param last
     * @throws IOException 
     */
    protected void sendData(Event event, IDataTarget target,
                            ByteBuffer block, boolean last) throws IOException {
        block.flip();
        target.onData(event, this, block, last);
        block.clear();
    }
}
