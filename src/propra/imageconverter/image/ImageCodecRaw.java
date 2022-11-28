package propra.imageconverter.image;

import java.io.IOException;
import propra.imageconverter.checksum.Checksum;
import propra.imageconverter.data.DataBlock;
import propra.imageconverter.data.DataCodecRaw;
import propra.imageconverter.data.IDataListener;

/**
 *
 * @author pg
 */
public class ImageCodecRaw extends DataCodecRaw {
    
    /* Zugeordnete Resource zur Ein-, oder Ausgabe der Daten */
    protected ImageResource image;
    
    /**
     * 
     * @param resource
     * @param checksum 
     */
    public ImageCodecRaw(   ImageResource resource, 
                            Checksum checksum) {
        super(resource, checksum);
        
        image = resource;
    }
    
    /**
     * 
     * @param block
     * @throws IOException 
     */
    @Override
    public void decode( DataBlock block, 
                        IDataListener target) throws IOException {
        if(!isValid()
        ||  block == null) {
            throw new IllegalArgumentException();
        }
        
        /*
         * Lädt, konvertiert und sendet Pixelblöcke an das Ziel  
         */
        while(resource.position() < resource.length()) {
            
            // Datenoperation 
            super.decode(block, null);

            // Farbkonvertierung
            if(image.getHeader().colorFormat().compareTo(ColorFormat.FORMAT_RGB) != 0) {  
                ColorFormat.convertColorBuffer( block.data, 
                                image.getHeader().colorFormat(), 
                                block.data,
                                ColorFormat.FORMAT_RGB);
            }

            if(target != null) {
                target.onData(Event.DATA_BLOCK_DECODED, this, block);
            }
        }
    }

    /**
     * 
     * @param block
     * @throws IOException 
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
        
        // Datenoperaion
        super.encode(block);
    }
    
    /**
     * 
     * @throws IOException 
     */
    @Override
    public void end() throws IOException {
        super.end();
        if(checksum != null) {
            image.header.checksum(checksum.getValue());
        }
    }
}
