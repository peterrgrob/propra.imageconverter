package propra.imageconverter.image;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import propra.imageconverter.util.Checkable;
import propra.imageconverter.util.Checksum;
import propra.imageconverter.util.DataBuffer;

/**
 *
 * @author pg
 */
public abstract class ImageWriter extends BufferedOutputStream implements Checkable {
    protected ImageHeader header;
    ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
    Checksum checksumObj;
    
    /**
     * 
     * @param out 
     */
    public ImageWriter(OutputStream out) {
        super(out);
    }
    
    /**
     *
     * @param image
     * @return 
     * @throws IOException
     */
    public ImageBuffer writeImage(ImageBuffer image) throws IOException {
        if(image == null) {
            throw new IllegalArgumentException();
        }

        DataBuffer buffer = buildHeader(image.getHeader());
        ImageBuffer output = buildContent(image);
        writeHeader(buffer);
        writeContent(output);
        flush();
        return output;
    }
    
    /**
     *
     * @param info
     * @return
     */
    protected abstract DataBuffer buildHeader(ImageHeader info);
    
    /**
     * 
     * @param bytes
     * @throws IOException 
     */
    protected void writeHeader(DataBuffer bytes) throws IOException {
        if(bytes == null) {
            throw new IllegalArgumentException();
        }
        
        write(bytes.getBuffer().array());
    }
    
    /**
     *
     * @param image
     * @return
     */
    protected ImageBuffer buildContent(ImageBuffer image) {
        if(!header.isValid() 
        || image == null) {
            throw new IllegalArgumentException();
        }
        return image.convertTo(header);
    }
    
    /**
    * 
    * @param image
    * @return
    * @throws IOException 
    */
    protected ImageBuffer writeContent(ImageBuffer image) throws IOException {
        if(image == null) {
            throw new IllegalArgumentException();
        }
        
        write(image.getBuffer().array(),0,header.getTotalSize());
        return image;
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
