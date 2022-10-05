package propra.imageconverter.image;

import java.io.IOException;
import java.io.OutputStream;
import propra.imageconverter.util.DataBuffer;
import propra.imageconverter.util.DataBufferLittle;

/**
 *
 * @author pg
 */
public class ImageWriterProPra extends ImageWriter {

    public ImageWriterProPra(OutputStream out) {
        super(out);
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
        
        DataBufferLittle data = new DataBufferLittle();
        data.create(ImageReaderPropra.PROPRA_HEADER_SIZE);
        data.put(ImageReaderPropra.PROPRA_VERSION,0);
        data.put((byte)0,ImageReaderPropra.PROPRA_HEADER_OFFSET_ENCODING);
        data.put((short)info.getWidth(),ImageReaderPropra.PROPRA_HEADER_OFFSET_WIDTH);
        data.put((short)info.getHeight(),ImageReaderPropra.PROPRA_HEADER_OFFSET_HEIGHT);
        data.put((byte)(info.getElementSize() >> 3),ImageReaderPropra.PROPRA_HEADER_OFFSET_BPP);
        data.put((long)info.getTotalSize(),ImageReaderPropra.PROPRA_HEADER_OFFSET_DATALEN);
        data.put(info.getChecksum(),ImageReaderPropra.PROPRA_HEADER_OFFSET_CHECKSUM);
    
        write(data.getBuffer(), 0, ImageReaderPropra.PROPRA_HEADER_SIZE);
        
        return (this.info = info);
    }
    
}
