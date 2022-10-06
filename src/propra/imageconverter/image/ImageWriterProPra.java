package propra.imageconverter.image;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import propra.imageconverter.util.DataBuffer;

/**
 *
 * @author pg
 */
public class ImageWriterProPra extends ImageWriter {

    public ImageWriterProPra(OutputStream out) {
        super(out);
        info.getColorType().setChannel(Color.RED,new Color.ChannelInfo(2));
        info.getColorType().setChannel(Color.GREEN,new Color.ChannelInfo(0));
        info.getColorType().setChannel(Color.BLUE,new Color.ChannelInfo(1));

    }

    @Override
    protected ImageBuffer writeBlock(ImageBuffer buffer) throws IOException {
        return buffer;
    }

    @Override
    protected ImageInfo writeInfo(ImageInfo info) throws IOException {
        if(info.isValid() == false) {
            throw new IllegalArgumentException();
        }
        
        DataBuffer dataBuffer = new DataBuffer();
        dataBuffer.create(ImageReaderPropra.PROPRA_HEADER_SIZE);
        ByteBuffer byteBuffer = dataBuffer.getBuffer();
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
        dataBuffer.put(ImageReaderPropra.PROPRA_VERSION,0);
        byteBuffer.put(ImageReaderPropra.PROPRA_HEADER_OFFSET_ENCODING, (byte)0);
        byteBuffer.putShort(ImageReaderPropra.PROPRA_HEADER_OFFSET_WIDTH,(short)info.getWidth());
        byteBuffer.putShort(ImageReaderPropra.PROPRA_HEADER_OFFSET_HEIGHT,(short)info.getHeight());
        byteBuffer.put(ImageReaderPropra.PROPRA_HEADER_OFFSET_BPP,(byte)(info.getElementSize() >> 3));
        byteBuffer.putLong(ImageReaderPropra.PROPRA_HEADER_OFFSET_DATALEN,(long)info.getTotalSize());
        byteBuffer.putInt(ImageReaderPropra.PROPRA_HEADER_OFFSET_CHECKSUM,info.getChecksum());
    
        write(byteBuffer.array(), 0, ImageReaderPropra.PROPRA_HEADER_SIZE);
        
        return (this.info = info);
    }
    
}
