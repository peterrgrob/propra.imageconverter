package propra.imageconverter.image;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import propra.imageconverter.util.ChecksumPropra;

/**
 *
 * @author pg
 */
public abstract class ImageReader extends BufferedInputStream {
    
    protected ImageHeader header;
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
        return readContent(header.getTotalSize());
    }
    
    /**
    * 
     * @return 
     * @throws java.io.IOException
    */
    protected abstract ImageHeader readHeader() throws IOException;
    
    /**
     * 
     * @param len
     * @return
     * @throws IOException 
     */
    protected ImageBuffer readContent(int len) throws IOException {
        return readContent(len, new ImageBuffer(header));
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
        
        if(header.getChecksum() == 0) {
            header.setChecksum(checkBytes(bytes));
        }
        else {
            if(checkBytes(bytes) != header.getChecksum()) {
                throw new java.io.IOException("Prüfsummenfehler.");
            }
        }

        image.wrap(bytes, header, byteOrder);
        return image;
    }
    
    /**
     * 
     * @return 
     */
    public ImageHeader getHeader() {
        return header;
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
     * @return
     */
    protected long checkBytes(byte[] data) {
        if (data == null ) {
            throw new IllegalArgumentException();
        }
        return checksum.update(data);
    }
}
