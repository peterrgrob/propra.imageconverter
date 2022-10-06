package propra.imageconverter.image;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

/**
 *
 * @author pg
 */
public class ImageReader extends BufferedInputStream {
    
    protected ImageInfo info;
    ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
    
    /**
     * 
     * @param in 
     * @throws java.io.IOException 
     */
    public ImageReader(InputStream in) throws IOException {
        super(in);
    } 
    
    /**
    * 
    */
    public ImageInfo readInfo() throws IOException {       
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * 
     * @param len
     * @return
     * @throws IOException 
     */
    public ImageBuffer readBlock(int len) throws IOException {
        return readBlock(len, new ImageBuffer(info));
    }
    
    /**
    * 
    * @param len
    * @param image
    * @return
    * @throws IOException 
    */
    public ImageBuffer readBlock(int len, ImageBuffer image) throws IOException {
        if(len <= 0 || image == null) {
            throw new IllegalArgumentException();
        }
        
        byte[] buffer = new byte[len];
        if(readBytes(len, buffer) != len) {
            throw new java.io.IOException("Nicht genug Bilddaten lesbar.");
        }
        image = wrapDataBuffer(buffer);
        image.getBuffer().order(byteOrder);
        return image;
    }
    
    /**
     * 
     * @return 
     */
    public ImageInfo getInfo() {
        return info;
    }
    
    /**
     * 
     * @param len
     * @param data
     * @return
     * @throws IOException 
     */
    protected int readBytes(int len, byte[] data) throws IOException {
        if (len == 0 || data == null ) {
            throw new IllegalArgumentException();
        }
        return read(data, 0, len);
    }
    
    /**
     * 
     * @param data
     * @return 
     */
    protected ImageBuffer wrapDataBuffer(byte[] data) {
        if (data == null ) {
            throw new IllegalArgumentException();
        }
        ImageBuffer buffer = new ImageBuffer();
        buffer.wrap(data, info);
        return buffer;
    }
}
