package propra.imageconverter.image;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import propra.imageconverter.util.Checkable;
import propra.imageconverter.util.Checksum;

/**
 *
 * @author pg
 */
public abstract class ImageReader extends BufferedInputStream implements Checkable {
    /**
     * 
     */
    protected ImageHeader header;
    int intialAvailableBytes;
    ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
    Checksum checksumObj;
    
    /**
     * 
     * @param in 
     * @throws java.io.IOException 
     */
    public ImageReader(InputStream in) throws IOException {
        super(in);
        intialAvailableBytes = available();
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
        if(len <= 0 
        || image == null) {
            throw new IllegalArgumentException();
        }
 
        /*
         * Farbbytes einlesen.
         */
        byte[] bytes = new byte[len];
        if(readBytes(bytes, len) != len) {
            throw new java.io.IOException("Nicht genug Bilddaten lesbar.");
        }
        
        /*
         * Checksum über Bytes berechnen und prüfen, falls Prüfsumme vorhanden.
         */
        if(isCheckable()) {
            if(check(bytes) != header.getChecksum()) {
                throw new java.io.IOException("Prüfsummenfehler.");
            }
        }
        
        // Bytes an ImageBuffer übergeben.
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
        if(len == 0 
        || data == null ) {
            throw new IllegalArgumentException();
        }
        return read(data, 0, len);
    }
  
    @Override
    public boolean isCheckable() {
        return (checksumObj != null);
    }

    @Override
    public Checksum getChecksumObj() {
        return checksumObj;
    }

    @Override
    public long check(byte[] bytes) {
        if (bytes == null ) {
            throw new IllegalArgumentException();
        }
        return checksumObj.update(bytes);
    }    
}
