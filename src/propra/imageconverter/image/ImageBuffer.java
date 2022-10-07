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
    protected ImageInfo info;  
  
    /**
     * 
     */
    ImageBuffer() {
        
    }
    
    /**
     * 
     * @param info 
     */
    ImageBuffer(ImageInfo info) {
        create(info);
    }
    
    /**
     * 
     * @param info 
     */
    ImageBuffer(byte[] data, ImageInfo info) {
        wrap(data, info);
    }
    
    /**
     * 
     * @param info
     */
    public void create(ImageInfo info) {
        if (!info.isValid()) {
            throw new IllegalArgumentException();
        }        
        this.info = new ImageInfo(info);
        create(info.getTotalSize());
    }
    
    /**
     * 
     * @param data
     * @param info 
     */
    public void wrap(byte[] data, ImageInfo info) {
        buffer = ByteBuffer.wrap(data);
        this.info = new ImageInfo(info);
    }
    
    /**
     * 
     * @return 
     */
    public ImageInfo getInfo() {
        return info;
    }
}
