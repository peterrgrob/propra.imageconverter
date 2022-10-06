package propra.imageconverter.image;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

        ByteBuffer byteBuffer = buffer.getBuffer();
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        write(byteBuffer.array(),0,info.getTotalSize());
        
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
        
        DataBuffer dataBuffer = new DataBuffer();
        dataBuffer.create(ImageReaderTGA.TGA_HEADER_SIZE);
        ByteBuffer byteBuffer = dataBuffer.getBuffer();
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
 
        byteBuffer.put(ImageReaderTGA.TGA_HEADER_OFFSET_ENCODING, (byte)2);
        byteBuffer.putShort(ImageReaderTGA.TGA_HEADER_OFFSET_WIDTH, (short)info.getWidth());
        byteBuffer.putShort(ImageReaderTGA.TGA_HEADER_OFFSET_HEIGHT, (short)info.getHeight());
        byteBuffer.put(ImageReaderTGA.TGA_HEADER_OFFSET_BPP, (byte)(info.getElementSize() << 3));        
        byteBuffer.put(ImageReaderTGA.TGA_HEADER_OFFSET_ORIGIN, (byte)(1 << 5));    
        
        write(byteBuffer.array(), 0, TGA_HEADER_SIZE);
        
        return (this.info = info);
    }
    
}
