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
        this.byteOrder = ByteOrder.LITTLE_ENDIAN;
    }
    
    /**
     * 
     * @param info
     * @return
     * @throws IOException 
     */
    @Override
    public ImageInfo writeInfo(ImageInfo info) throws IOException {
        if(info.isValid() == false) {
            throw new IllegalArgumentException();
        }
        
        DataBuffer dataBuffer = new DataBuffer();
        dataBuffer.create(ImageReaderTGA.TGA_HEADER_SIZE);
        ByteBuffer byteBuffer = dataBuffer.getBuffer();
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
        this.info = new ImageInfo(info);
        this.info.getColorType().setChannel(Color.RED,new Color.ChannelInfo(2));
        this.info.getColorType().setChannel(Color.GREEN,new Color.ChannelInfo(1));
        this.info.getColorType().setChannel(Color.BLUE,new Color.ChannelInfo(0));
 
        byteBuffer.put(ImageReaderTGA.TGA_HEADER_OFFSET_ENCODING, (byte)2);
        byteBuffer.putShort(ImageReaderTGA.TGA_HEADER_OFFSET_WIDTH, (short)info.getWidth());
        byteBuffer.putShort(ImageReaderTGA.TGA_HEADER_OFFSET_HEIGHT, (short)info.getHeight());
        byteBuffer.put(ImageReaderTGA.TGA_HEADER_OFFSET_BPP, (byte)(info.getElementSize() << 3));        
        byteBuffer.put(ImageReaderTGA.TGA_HEADER_OFFSET_ORIGIN, (byte)(1 << 5));    
        
        write(byteBuffer.array(), 0, TGA_HEADER_SIZE);
        
        return info;
    }
    
}
