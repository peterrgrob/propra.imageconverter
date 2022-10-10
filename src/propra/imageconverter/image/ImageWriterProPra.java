package propra.imageconverter.image;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import propra.imageconverter.util.ChecksumPropra;
import propra.imageconverter.util.DataBuffer;

/**
 *
 * @author pg
 */
public class ImageWriterProPra extends ImageWriter {

    /**
     *
     * @param out
     */
    public ImageWriterProPra(OutputStream out) {
        super(out);
        this.byteOrder = ByteOrder.LITTLE_ENDIAN;
        checksumObj = new ChecksumPropra();
    }

    /**
     *
     * @param info
     * @return
     * @throws IOException
     */
    @Override
    protected DataBuffer buildHeader(ImageHeader info) {
        if(info.isValid() == false) {
            throw new IllegalArgumentException();
        }
        
        /*
         * DataBuffer für Header erstellen
         */
        DataBuffer dataBuffer = new DataBuffer();
        dataBuffer.create(ImageReaderPropra.PROPRA_HEADER_SIZE);
        ByteBuffer byteBuffer = dataBuffer.getBuffer();
        byteBuffer.order(byteOrder);
        
        header = new ImageHeader(info);
        header.getColorType().setMapping(ColorType.RED,0);
        header.getColorType().setMapping(ColorType.GREEN,2);
        header.getColorType().setMapping(ColorType.BLUE,1);
        
        /*
         * Headerfelder in Buffer schreiben
         */
        dataBuffer.put(ImageReaderPropra.PROPRA_VERSION,0);
        byteBuffer.put(ImageReaderPropra.PROPRA_HEADER_OFFSET_ENCODING, (byte)0);
        byteBuffer.putShort(ImageReaderPropra.PROPRA_HEADER_OFFSET_WIDTH,(short)info.getWidth());
        byteBuffer.putShort(ImageReaderPropra.PROPRA_HEADER_OFFSET_HEIGHT,(short)info.getHeight());
        byteBuffer.put(ImageReaderPropra.PROPRA_HEADER_OFFSET_BPP,(byte)(info.getElementSize() << 3));
        byteBuffer.putLong(ImageReaderPropra.PROPRA_HEADER_OFFSET_DATALEN,(long)info.getTotalSize());
       
        return dataBuffer;
    }
    
        
    /**
     *
     * @param image
     * @throws IOException
     */
    public ImageBuffer writeImage(ImageBuffer image) throws IOException {
        if(image == null) {
            throw new IllegalArgumentException();
        }

        DataBuffer buffer = buildHeader(image.getHeader());
        ImageBuffer output = buildContent(image);
        
        if(isCheckable()) {
            output.getHeader().setChecksum(check(output.getBuffer().array()));
            buffer.getBuffer().putInt(ImageReaderPropra.PROPRA_HEADER_OFFSET_CHECKSUM, 
                                    (int)header.getChecksum());
        }
        
        writeHeader(buffer);
        writeContent(output);
        flush();
        return output;
    }
}
