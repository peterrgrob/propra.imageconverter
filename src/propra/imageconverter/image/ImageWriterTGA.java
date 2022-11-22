package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import propra.imageconverter.data.DataFormat;

/**
 *  Schreibt TGA Header
 * 
 * @author pg
 */
public class ImageWriterTGA extends ImageWriter {  

    /**
     *
     * @param file
     * @param mode
     * @throws java.io.IOException
     */
    public ImageWriterTGA(String file, DataFormat.IOMode mode) throws IOException {
        super(file, mode);
        fileHeaderSize = ImageReaderTGA.TGA_HEADER_SIZE;
        writeColorFormat = new ColorFormat(2, 1, 0);
    }
    
    /**
     * Schreibt allgemeinen Header als TGA Header
     * 
     * @param srcHeader
     * @throws java.io.IOException
     */
    @Override
    public void writeHeader(ImageHeader srcHeader) throws IOException {
        if(srcHeader.isValid() == false) {
            throw new IllegalArgumentException();
        }
        
        super.writeHeader(srcHeader);
        header.colorFormat().setMapping(writeColorFormat.getMapping());
                
        // DataBuffer für Header erstellen
        ByteBuffer byteBuffer = ByteBuffer.allocate(ImageReaderTGA.TGA_HEADER_SIZE);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                
        // Headerfelder in Buffer schreiben
        byteBuffer.put(ImageReaderTGA.TGA_HEADER_OFFSET_ENCODING, 
                        (byte)2);
        byteBuffer.putShort(ImageReaderTGA.TGA_HEADER_OFFSET_WIDTH, 
                            (short)srcHeader.width());
        byteBuffer.putShort(ImageReaderTGA.TGA_HEADER_OFFSET_HEIGHT, 
                            (short)srcHeader.height());
        byteBuffer.putShort(ImageReaderTGA.TGA_HEADER_OFFSET_Y0, 
                            (short)srcHeader.height());
        byteBuffer.put(ImageReaderTGA.TGA_HEADER_OFFSET_BPP, 
                            (byte)(srcHeader.pixelSize() << 3));        
        byteBuffer.put(ImageReaderTGA.TGA_HEADER_OFFSET_ORIGIN, 
                            (byte)(1 << 5)); 
        
        // Kompression
        switch(header.colorFormat().encoding()) {
            case RLE -> {
                byteBuffer.put(ImageReaderTGA.TGA_HEADER_OFFSET_ENCODING, 
                                    (byte)ImageReaderTGA.TGA_HEADER_ENCODING_RLE);
            }
            case NONE -> {
                byteBuffer.put(ImageReaderTGA.TGA_HEADER_OFFSET_ENCODING, 
                                    (byte)ImageReaderTGA.TGA_HEADER_ENCODING_NONE);
            }
            default -> {
                throw new IllegalArgumentException("Ungültige Kompression.");
            }                   
        }
        
        // In Stream schreiben
        binaryWriter.seek(0);
        write(byteBuffer, 0, fileHeaderSize);
    }
    
}
