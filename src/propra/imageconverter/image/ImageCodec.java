package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataCodec;
import propra.imageconverter.data.IDataListener;
import propra.imageconverter.data.IDataListener.Event;


/**
 *  Basiscodec für die Konvertierung von unkomprimierten Pixelblöcken
 */
public class ImageCodec extends DataCodec {
    
    // Standardblockgröße, muss vielfaches der Pixelgröße sein
    public static final int DEFAULT_IMAGEBLOCK_SIZE = 4096 * 8;
    
    
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
     */
    @Override
    public void decode(IDataListener listener) throws IOException {
        if(!isValid()) {
            throw new IllegalArgumentException();
        }
        
        // Lese Puffer 
        ColorBuffer data = new ColorBuffer(DEFAULT_IMAGEBLOCK_SIZE, image.getHeader().colorFormat());
        boolean bLast = false;
        
        /*
         * Lädt, konvertiert und sendet Pixelblöcke an Listener  
         */
        while(resource.position() < resource.length()) {
            
            // Blockgröße anpassen
            if(resource.length() - resource.position() < data.capacity()) {
                data.limit((int)(resource.length() - resource.position()));
                bLast = true;
            }
            
            // Datenblock von Resource lesen 
            int r = resource.getInputStream()
                            .read(data);
            if(r == -1) {
                throw new IOException("Lesefehler!");
            }

            /*ColorBuffer pb = new ColorBuffer(data, image.colorFormat);
            while(pb.iterator().hasNext()) {
                Pixel p = pb.iterator().next();
            }*/
            
            // Pixelformat ggfs. konvertieren
            if(image.getHeader().colorFormat().compareTo(ColorFormat.FORMAT_RGB) != 0) {  
                ColorFormat.convertColorBuffer(data.getBuffer(), 
                                                image.getHeader().colorFormat(), 
                                                data.getBuffer(),
                                                ColorFormat.FORMAT_RGB);
            }

            // Daten an Listener senden
            dispatchEvent(  Event.DATA_BLOCK_DECODED, 
                            listener, 
                            data.getBuffer(),
                            bLast);  
            data.clear();
        }
    }

    /**
     * 
     */
    @Override
    public void encode( ByteBuffer block,
                        boolean last,
                        IDataListener listener) throws IOException {
        if(!isValid()
        ||  block == null) {
            throw new IllegalArgumentException();
        }
        
        // Farbkonvertierung
        if(image.getHeader().colorFormat().compareTo(ColorFormat.FORMAT_RGB) != 0) {   
            ColorFormat.convertColorBuffer( block, 
                                            ColorFormat.FORMAT_RGB, 
                                            block,
                                            image.getHeader().colorFormat());
        }
        
        // Block in Resource kodieren
        resource.getOutputStream()
                .write(block);
        
        // Daten an Listener senden
        dispatchEvent(  Event.DATA_BLOCK_ENCODED, 
                            listener, 
                            block,
                            last);
    }
    
    /**
     *  Sendet Datenblock an Listener
     */
    protected void dispatchData(Event event,
                                IDataListener listener,
                                ByteBuffer block,
                                boolean last) throws IOException {
        block.flip();
        dispatchEvent(event, listener, block, last);
        block.clear();
    }
}
