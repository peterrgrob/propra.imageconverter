package propra.imageconverter.image;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import propra.imageconverter.util.DataBuffer;


/**
 *
 * @author pg
 */
public class ImageReaderTGA extends ImageReader {
    
    static final int TGA_HEADER_SIZE = 18;
    static final int TGA_HEADER_OFFSET_ENCODING = 2;
    static final int TGA_HEADER_OFFSET_WIDTH = 12;
    static final int TGA_HEADER_OFFSET_HEIGHT = 14;
    static final int TGA_HEADER_OFFSET_BPP = 16;
    static final int TGA_HEADER_OFFSET_ORIGIN = 17;
    
    public ImageReaderTGA(InputStream in) throws IOException {
        super(in);
        byteOrder = ByteOrder.LITTLE_ENDIAN;
    }

    /**
     * 
     * @return 
     * @throws java.io.IOException 
     */
    @Override
    public ImageHeader readHeader() throws IOException {
        ImageHeader tInfo = new ImageHeader();
        
        byte[] rawBytes = new byte[TGA_HEADER_SIZE];
        if(readBytes( rawBytes,TGA_HEADER_SIZE) != TGA_HEADER_SIZE) {
            throw new java.io.IOException("Ungültiger TGA Header.");
        }
        
        DataBuffer dataBuffer = new DataBuffer(rawBytes);
        ByteBuffer byteBuffer = dataBuffer.getBuffer();
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
        switch(byteBuffer.get(TGA_HEADER_OFFSET_ENCODING)) {
            case 2:
                tInfo.setEncoding(ImageHeader.Encoding.UNCOMPRESSED);
                break;
            default:
                throw new java.lang.UnsupportedOperationException("Nicht unterstütztes Bildformat.");
        }
        
        tInfo.setWidth(byteBuffer.getShort(TGA_HEADER_OFFSET_WIDTH));
        tInfo.setHeight(byteBuffer.getShort(TGA_HEADER_OFFSET_HEIGHT));
        tInfo.setElementSize(byteBuffer.get(TGA_HEADER_OFFSET_BPP) >> 3); 
        
        if(tInfo.isValid() == false) {
            throw new java.io.IOException("Ungültiges Bildformat.");
        }
        
        return (info = tInfo);
    }
}
