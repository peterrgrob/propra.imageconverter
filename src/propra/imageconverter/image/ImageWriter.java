package propra.imageconverter.image;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author pg
 */
public class ImageWriter extends BufferedOutputStream {
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
        writeHeader(image.getHeader());
        writeContent(image);
    }
   
    /**
     * 
     * @param info
     * @return
     * @throws IOException 
     */
    protected ImageHeader writeHeader(ImageHeader info) throws IOException {       
        throw new UnsupportedOperationException("Not supported yet.");
    }
      
    /**
    * 
    * @param buffer
    * @return
    * @throws IOException 
    */
    protected ImageBuffer writeContent(ImageBuffer image) throws IOException {
        if(!header.isValid()) {
            throw new IllegalArgumentException();
        }
            
        ImageBuffer dstBuffer = image;
        ColorType srcColorType = image.getHeader().getColorType();
        ColorType dstColorType = header.getColorType();
        
        if(srcColorType.compareTo(header.getColorType()) != 0 
                                || byteOrder == ByteOrder.LITTLE_ENDIAN) {
            ByteBuffer srcBuffer = image.getBuffer();
            dstBuffer = new ImageBuffer(header);
            dstBuffer.getBuffer().order(byteOrder);
                    
            byte[] color = new byte[3];
            
            for(int i=0;i<image.getHeader().getElementCount();i++) {
                image.getColor(color);
                header.getColorType().convertColor(color, srcColorType);
                dstBuffer.putColor(color);
            }
            
            dstBuffer.getBuffer().rewind();
        }
   
        write(dstBuffer.getBuffer().array(),0,header.getTotalSize());
        
        return image;
    }
}
