package propra.imageconverter.image;

import java.io.IOException;
import propra.imageconverter.checksum.Checksum;
import propra.imageconverter.data.DataBlock;
import propra.imageconverter.data.DataCodec;
import propra.imageconverter.data.DataFormat;

/**
 *
 * @author pg
 */
public class ImageCodec extends DataCodec {
    
    private ImageResource image;
    
    /**
     * 
     * @param resource
     * @param checksum 
     */
    public ImageCodec(ImageResource resource, Checksum checksum) {
        super(resource, checksum);
        
        image = resource;
    }
    
    /**
     * 
     * @param op
     * @param block
     * @throws IOException 
     */
    @Override
    public void processBlock(DataFormat.Operation op, DataBlock block) throws IOException {
        if(!isValid()
        ||  block == null) {
            throw new IllegalArgumentException();
        }
        
        // Datenoperation
        super.processBlock(op, block);
        
        // Farbkonvertierung
        if(image.getHeader().colorFormat().compareTo(ColorFormat.FORMAT_RGB) != 0) {   
            if(op == DataFormat.Operation.READ) {
                ColorFormat.convertColorBuffer( block.data, image.getHeader().colorFormat(), 
                                                    block.data, ColorFormat.FORMAT_RGB);
            } else if(op == DataFormat.Operation.WRITE) {
                ColorFormat.convertColorBuffer( block.data, ColorFormat.FORMAT_RGB, 
                                                    block.data, image.getHeader().colorFormat());
            }
        }
    }
}
