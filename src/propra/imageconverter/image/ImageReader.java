package propra.imageconverter.image;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author pg
 */
public class ImageReader extends BufferedInputStream {
    
    protected ImageInfo info;
    
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
    protected ImageInfo readInfo() throws IOException {       
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * 
     * @param len
     * @return
     * @throws IOException 
     */
    protected ImageBuffer readBlock(int len) throws IOException {
        return readBlock(len, new ImageBuffer(info));
    }
    
    /**
    * 
    * @param len
    * @return
    * @throws IOException 
    */
    protected ImageBuffer readBlock(int len, ImageBuffer buff) throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
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
     * @return
     * @throws IOException 
     */
    protected byte[] readBytes(int len) throws IOException {
        if (len == 0) {
            return null;
        }
        byte[] buff = new byte[len];
        read(buff, 0, len);
        return buff;
    }
}
