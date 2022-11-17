package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import propra.imageconverter.data.DataBuffer;
import propra.imageconverter.data.DataFormat;

/**
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
    public ImageWriterTGA(String file, DataFormat.Mode mode) throws IOException {
        super(file, mode);
        formatHeaderSize = ImageReaderTGA.TGA_HEADER_SIZE;   
    }
    
    /**
     * Wandelt einen allgemeinen Header in einen TGA Header um
     * 
     * @param srcHeader
     * @throws java.io.IOException
     */
    public void writeHeader(ImageHeader srcHeader) throws IOException {
        if(srcHeader.isValid() == false) {
            throw new IllegalArgumentException();
        }
        
        // DataBuffer für Header erstellen
        DataBuffer dataBuffer = new DataBuffer(ImageReaderTGA.TGA_HEADER_SIZE);
        ByteBuffer byteBuffer = dataBuffer.getBuffer();
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // Header speichern für blockweise Verarbeitung der Bilddaten
        header = new ImageHeader(srcHeader);
        header.colorFormat().setMapping(ColorFormat.RED,2);
        header.colorFormat().setMapping(ColorFormat.GREEN,1);
        header.colorFormat().setMapping(ColorFormat.BLUE,0);
        
        // Headerfelder in Buffer schreiben
        byteBuffer.put(ImageReaderTGA.TGA_HEADER_OFFSET_ENCODING, 
                        (byte)2);
        byteBuffer.putShort(ImageReaderTGA.TGA_HEADER_OFFSET_WIDTH, 
                            (short)srcHeader.width());
        byteBuffer.putShort(ImageReaderTGA.TGA_HEADER_OFFSET_HEIGHT, 
                            (short)srcHeader.height());
        byteBuffer.put(ImageReaderTGA.TGA_HEADER_OFFSET_BPP, 
                            (byte)(srcHeader.pixelSize() << 3));        
        byteBuffer.put(ImageReaderTGA.TGA_HEADER_OFFSET_ORIGIN, 
                            (byte)(1 << 5)); 
        
        // Kompression
        switch(header.colorFormat().getEncoding()) {
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
        write(dataBuffer, 0, formatHeaderSize);
    }
    
}
