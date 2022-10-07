package propra.imageconverter.image;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import propra.imageconverter.util.DataBuffer;

/**
 *
 * @author pg
 */
public class ImageWriter extends BufferedOutputStream {
    protected ImageInfo info;
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
     * @param info
     * @return
     * @throws IOException 
     */
    public ImageInfo writeInfo(ImageInfo info) throws IOException {       
        throw new UnsupportedOperationException("Not supported yet.");
    }
      
    /**
    * 
    * @param buffer
    * @return
    * @throws IOException 
    */
    public ImageBuffer writeBlock(ImageBuffer buffer) throws IOException {
        if(!info.isValid()) {
            throw new IllegalArgumentException();
        }
        
        ByteBuffer byteBuffer = buffer.getBuffer();
        byteBuffer.order(byteOrder);
                
        Color srcColorType = buffer.getInfo().getColorType();
        
        if(srcColorType.compareTo(info.getColorType()) != 0 
                                || byteOrder == ByteOrder.LITTLE_ENDIAN) {
            byte[] color = new byte[3];
            byte[] convColor = new byte[3];
            
            for(int i=0; i<buffer.getInfo().getElementCount();i++) {
                byteBuffer.get(i*buffer.getInfo().getElementSize(), color);
                info.getColorType().convertColor(color, srcColorType,convColor);
                if(byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    DataBuffer.colorToLittleEndian(convColor, color);
                    DataBuffer.copyColor(color, convColor);
                }
                byteBuffer.put(i*buffer.getInfo().getElementSize(), color);
            }
        }
   
        write(byteBuffer.array(),0,info.getTotalSize());
        
        return buffer;
    }
}
