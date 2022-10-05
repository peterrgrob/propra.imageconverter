package propra.imageconverter.image;

import java.io.IOException;
import java.io.OutputStream;
import propra.imageconverter.util.*;

/**
 *
 * @author pg
 */
public class ImageWriterTGA extends ImageWriter {
    static final int TGA_HEADER_SIZE = 18;
        
    /**
     * 
     * @param out 
     */
    public ImageWriterTGA(OutputStream out) {
        super(out);
    }

    /**
     * 
     * @param buffer
     * @return
     * @throws IOException 
     */
    @Override
    protected ImageBuffer writeBlock(ImageBuffer buffer) throws IOException {
        if(!info.isValid()) {
            throw new IllegalArgumentException();
        }

        DataBuffer data = new DataBufferLittle();
        data.create(info.getTotalSize());

        for(int i=0; i<info.getElementCount(); i++) {
            data.put(buffer.get(i));
        }
        
        data.setPosition(0);
        write(data.getBuffer(),0,info.getTotalSize());
        
        return buffer;
    }
    
    /**
     * 
     * @param info
     * @return
     * @throws IOException 
     */
    @Override
    protected ImageInfo writeInfo(ImageInfo info) throws IOException {
        if(info.isValid() == false) {
            throw new IllegalArgumentException();
        }
        
        DataBuffer data = new DataBufferLittle();
        data.create(TGA_HEADER_SIZE);
        data.put((byte)2,ImageReaderTGA.TGA_HEADER_OFFSET_ENCODING);
        data.put((short)info.getWidth(),ImageReaderTGA.TGA_HEADER_OFFSET_WIDTH);
        data.put((short)info.getHeight(),ImageReaderTGA.TGA_HEADER_OFFSET_HEIGHT);
        data.put((byte)(info.getElementSize() << 3),ImageReaderTGA.TGA_HEADER_OFFSET_BPP);        
        data.put((byte)(1 << 5),ImageReaderTGA.TGA_HEADER_OFFSET_ORIGIN);    
        
        write(data.getBuffer(), 0, TGA_HEADER_SIZE);
        
        return (this.info = info);
    }
    
}
