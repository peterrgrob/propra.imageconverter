package propra.imageconverter.image;

import java.io.IOException;
import propra.imageconverter.checksum.Checksum;
import propra.imageconverter.data.DataBlock;
import propra.imageconverter.data.DataCodec;
import propra.imageconverter.data.IDataTarget;

/**
 *
 * @author pg
 */
public class ImageCodec extends DataCodec {
    
    /* Zugeordnete Resource zur Ein-, oder Ausgabe der Daten */
    private ImageResource image;
    
    /**
     * 
     * @param resource
     * @param checksum 
     */
    public ImageCodec(  ImageResource resource, 
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
                        IDataTarget target) throws IOException {
        if(!isValid()
        ||  block == null) {
            throw new IllegalArgumentException();
        }
        
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
                target.push(this, block);
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
}
