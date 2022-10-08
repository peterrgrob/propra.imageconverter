package propra.imageconverter.image;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import propra.imageconverter.util.ChecksumPropra;
import propra.imageconverter.util.DataBuffer;

/**
 *
 * @author pg
 */
public class ImageReader extends BufferedInputStream {
    
    protected ImageHeader info;
    ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
    ChecksumPropra checksum = new ChecksumPropra();
    
    /**
     * 
     * @param in 
     * @throws java.io.IOException 
     */
    public ImageReader(InputStream in) throws IOException {
        super(in);
        checksum.test();
    } 
    
    /**
     *
     * @return
     * @throws java.io.IOException
     */
    public ImageBuffer readImage() throws IOException {
        readHeader();
        return readContent(info.getTotalSize());
    }
    
    /**
    * 
     * @return 
     * @throws java.io.IOException
    */
    protected ImageHeader readHeader() throws IOException {       
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * 
     * @param len
     * @return
     * @throws IOException 
     */
    protected ImageBuffer readContent(int len) throws IOException {
        return readContent(len, new ImageBuffer(info));
    }
    
    /**
    * 
    * @param len
    * @param image
    * @return
    * @throws IOException 
    */
    protected ImageBuffer readContent(int len, ImageBuffer image) throws IOException {
        if(len <= 0 || image == null) {
            throw new IllegalArgumentException();
        }
 
        byte[] bytes = new byte[len];
        if(readBytes(bytes, len) != len) {
            throw new java.io.IOException("Nicht genug Bilddaten lesbar.");
        }
        
        if(info.getChecksum() == 0) {
            info.setChecksum(checkBytes(bytes));
        }
        else {
            if(checkBytes(bytes) != info.getChecksum()) {
                throw new java.io.IOException("Prüfsummenfehler.");
            }
        }
           
        ColorType srcColorType = info.getColorType();
        ColorType dstColorType = image.getHeader().getColorType();
        byte[] color = new byte[3];

        DataBuffer srcBuffer = new DataBuffer();
        srcBuffer.wrap(bytes, byteOrder);
        ImageBuffer dstBuffer = image;

        for(int i=0; i<info.getElementCount();i++) {
            srcBuffer.getColor(color);
            dstBuffer.putColor(color, dstColorType);
        }
        dstBuffer.getBuffer().rewind();
        
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
    
    /**
     *
     * @param data
     * @param checkSum
     * @return
     * @throws java.io.IOException
     */
    protected long checkBytes(byte[] data) {
        return checksum.update(data);
    }
}
