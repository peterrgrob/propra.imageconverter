package propra.imageconverter.image;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import propra.imageconverter.util.DataBuffer;

/**
 *
 * @author pg
 */
public class ImageReader extends BufferedInputStream {
    
    protected ImageHeader info;
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
     * @return 
     * @throws java.io.IOException
    */
    public ImageHeader readHeader() throws IOException {       
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * 
     * @param len
     * @return
     * @throws IOException 
     */
    public ImageBuffer readContent(int len) throws IOException {
        return readContent(len, new ImageBuffer(info));
    }
    
    /**
    * 
    * @param len
    * @param image
    * @return
    * @throws IOException 
    */
    public ImageBuffer readContent(int len, ImageBuffer image) throws IOException {
        if(len <= 0 || image == null) {
            throw new IllegalArgumentException();
        }
        
        byte[] bytes = new byte[len];
        if(readBytes(bytes, len) != len) {
            throw new java.io.IOException("Nicht genug Bilddaten lesbar.");
        }
         
        ColorType srcColorType = info.getColorType();
        ColorType dstColorType = image.getInfo().getColorType();
        
        if(dstColorType.compareTo(info.getColorType()) != 0 
                                || byteOrder == ByteOrder.LITTLE_ENDIAN) {
            byte[] color = new byte[3];
            
            DataBuffer srcBuffer = new DataBuffer();
            srcBuffer.wrap(bytes, byteOrder);
            ByteBuffer dstBuffer = image.getBuffer();
            
            for(int i=0; i<info.getElementCount();i++) {
                srcBuffer.getColor(color);
                srcColorType.convertColor(color, dstColorType);
                dstBuffer.put( color);
            }
            dstBuffer.rewind();
        }
        else {        
            image.wrap(bytes, info);
        }
        
        return image;
    }
    
    /**
     * 
     * @return 
     */
    public ImageHeader getHeader() {
        return info;
    }
    
    /**
     * 
     * @param len
     * @param data
     * @return
     * @throws IOException 
     */
    protected int readBytes(byte[] data, int len) throws IOException {
        if (len == 0 || data == null ) {
            throw new IllegalArgumentException();
        }
        return read(data, 0, len);
    }
}
