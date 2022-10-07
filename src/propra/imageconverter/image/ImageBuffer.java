package propra.imageconverter.image;

import propra.imageconverter.util.DataBuffer;
import java.nio.ByteBuffer;

/**
 *
 * @author pg
 */
public class ImageBuffer extends DataBuffer {
    /**
     * 
     */
    protected ImageHeader info;  
  
    /**
     * 
     */
    ImageBuffer() {
        
    }
    
    /**
     * 
     * @param info 
     */
    ImageBuffer(ImageHeader info) {
        create(info);
    }
    
    /**
     * 
     * @param info 
     */
    ImageBuffer(byte[] data, ImageHeader info) {
        wrap(data, info);
    }
    
    /**
     * 
     * @param info
     */
    public void create(ImageHeader info) {
        if (!info.isValid()) {
            throw new IllegalArgumentException();
        }        
        this.info = new ImageHeader(info);
        create(info.getTotalSize());
    }
    
    /**
     * 
     * @param data
     * @param info 
     */
    public void wrap(byte[] data, ImageHeader info) {
        buffer = ByteBuffer.wrap(data);
        this.info = new ImageHeader(info);
    }
    
    /**
     * 
     * @return 
     */
    public ImageHeader getInfo() {
        return info;
    }
}
