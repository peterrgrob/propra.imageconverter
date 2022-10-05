package propra.imageconverter.image;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import propra.imageconverter.util.DataBuffer;
import propra.imageconverter.util.DataBufferLittle;

/**
 *
 * @author pg
 */
public class ImageReader extends BufferedInputStream {
    
    protected ImageInfo info;
    protected DataBuffer.Order byteOrder = DataBuffer.Order.BIG_ENDIAN;
    protected Color.ColorOrder colorOrder = new Color.ColorOrder();
    
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
     * @param byteOrder 
     */
    public void setOrder(DataBuffer.Order byteOrder) {
        this.byteOrder = byteOrder;
    }
    
    /**
     * 
     * @param colorOrder
     */
    public void setColorOrder(Color.ColorOrder colorOrder) {
        this.colorOrder = colorOrder;
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
        DataBuffer data = wrapDataBuffer(buffer);

        for(int i=0; i<info.getElementCount(); i++) {
            image.set(i, data.getColor(colorOrder));
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
     * @param buffer
     * @return
     * @throws IOException 
     */
    protected int readBytes(int len, byte[] buffer) throws IOException {
        if (len == 0 || buffer == null ) {
            throw new IllegalArgumentException();
        }
        return read(buffer, 0, len);
    }
    
    /**
     * 
     * @param buffer
     * @return 
     */
    protected DataBuffer wrapDataBuffer(byte[] buffer) {
        if(byteOrder == DataBuffer.Order.LITTLE_ENDIAN) {
            return new DataBufferLittle(buffer);
        }
        return new DataBuffer(buffer);
    }
}
