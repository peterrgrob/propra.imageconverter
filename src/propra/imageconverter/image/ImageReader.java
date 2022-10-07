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
     * @return 
     * @throws java.io.IOException
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
        
        byte[] bytes = new byte[len];
        if(readBytes(len, bytes) != len) {
            throw new java.io.IOException("Nicht genug Bilddaten lesbar.");
        }
        
        ByteBuffer dstBuffer = image.getBuffer();
        ByteBuffer srcBuffer = ByteBuffer.wrap(bytes);
        
        Color dstColorType = image.getInfo().getColorType();
        
        if(dstColorType.compareTo(info.getColorType()) != 0 
                                || byteOrder == ByteOrder.LITTLE_ENDIAN) {
            byte[] color = new byte[3];
            byte[] convColor = new byte[3];
            
            for(int i=0; i<info.getElementCount();i++) {
                srcBuffer.get(i*getInfo().getElementSize(), color);
                info.getColorType().convertColor(color, dstColorType,convColor);
                if(byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    DataBuffer.colorToLittleEndian(convColor, color);
                    DataBuffer.copyColor(color, convColor);
                }
                dstBuffer.put(i*image.getInfo().getElementSize(), color);
            }
        }
        else {        
            image = wrapDataBuffer(bytes);
        }
        
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
