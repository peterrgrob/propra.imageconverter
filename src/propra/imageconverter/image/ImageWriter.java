package propra.imageconverter.image;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;

/**
 *
 * @author pg
 */
public abstract class ImageWriter extends BufferedOutputStream {
    protected ImageHeader header;
    ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
            
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
     * @throws IOException
     */
    public void writeImage(ImageBuffer image) throws IOException {
        if(image == null) {
            throw new IllegalArgumentException();
        }
        writeHeader(image.getHeader());
        writeContent(image);
    }
   
    /**
     * 
     * @param info
     * @return
     * @throws IOException 
     */
    protected abstract ImageHeader writeHeader(ImageHeader info) throws IOException;
      
    /**
    * 
    * @param image
    * @return
    * @throws IOException 
    */
    protected ImageBuffer writeContent(ImageBuffer image) throws IOException {
        if(!header.isValid() 
        || image == null) {
            throw new IllegalArgumentException();
        }
        ImageBuffer output = image.convertTo(header);
        write(output.getBuffer().array(),0,header.getTotalSize());
        return image;
    }
}
