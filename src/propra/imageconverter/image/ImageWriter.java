package propra.imageconverter.image;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author pg
 */
public class ImageWriter extends BufferedOutputStream {
    protected ImageInfo info;
    
    /**
     * 
     * @param out 
     */
    public ImageWriter(OutputStream out) {
        super(out);
    }
   
    /**
     * 
     * @param info
     * @return
     * @throws IOException 
     */
    protected ImageInfo writeInfo(ImageInfo info) throws IOException {       
        throw new UnsupportedOperationException("Not supported yet.");
    }
      
    /**
    * 
    * @param buffer
    * @return
    * @throws IOException 
    */
    protected ImageBuffer writeBlock(ImageBuffer buffer) throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
    }
}
