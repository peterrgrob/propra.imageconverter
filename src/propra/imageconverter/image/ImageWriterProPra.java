package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import propra.imageconverter.checksum.ChecksumPropra;
import propra.imageconverter.data.DataBuffer;
import propra.imageconverter.data.DataFormat;

/**
 *
 * @author pg
 */
public class ImageWriterProPra extends ImageWriter {
    /**
     *
     * @param file
     * @param mode
     * @throws java.io.IOException
     */
    public ImageWriterProPra(String file, DataFormat.Mode mode) throws IOException {
        super(file, mode);
        formatHeaderSize = ImageReaderProPra.PROPRA_HEADER_SIZE;
        checksumObj = new ChecksumPropra();
    }
    
    /**
     *  Wandelt einen allgemeinen Header in einen ProPra Header um
     * 
     * @param srcHeader
     */
    public void writeHeader(ImageHeader srcHeader) throws IOException {
        if(srcHeader.isValid() == false) {
            throw new IllegalArgumentException();
        }
        
        // DataBuffer für Header erstellen
        DataBuffer dataBuffer = new DataBuffer(ImageReaderProPra.PROPRA_HEADER_SIZE);
        ByteBuffer byteBuffer = dataBuffer.getBuffer();
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // ProPra spezifisches RBG Farbmapping setzen
        header = new ImageHeader(srcHeader);
        header.colorFormat().setMapping(ColorFormat.RED,0);
        header.colorFormat().setMapping(ColorFormat.GREEN,2);
        header.colorFormat().setMapping(ColorFormat.BLUE,1);
        
        // Headerfelder in ByteBuffer schreiben
        dataBuffer.put(ImageReaderProPra.PROPRA_VERSION,0);
        byteBuffer.put(ImageReaderProPra.PROPRA_HEADER_OFFSET_ENCODING, (byte)0);
        byteBuffer.putShort(ImageReaderProPra.PROPRA_HEADER_OFFSET_WIDTH,(short)srcHeader.width());
        byteBuffer.putShort(ImageReaderProPra.PROPRA_HEADER_OFFSET_HEIGHT,(short)srcHeader.height());
        byteBuffer.put(ImageReaderProPra.PROPRA_HEADER_OFFSET_BPP,(byte)(srcHeader.pixelSize() << 3));
        byteBuffer.putInt(ImageReaderProPra.PROPRA_HEADER_OFFSET_CHECKSUM, (int)srcHeader.checksum());
        
        // Kompression 
        switch(header.colorFormat().getEncoding()) {
            case RLE -> {
                byteBuffer.put(ImageReaderProPra.PROPRA_HEADER_OFFSET_ENCODING, (byte)ImageReaderProPra.PROPRA_HEADER_ENCODING_RLE);
                byteBuffer.putLong(ImageReaderProPra.PROPRA_HEADER_OFFSET_DATALEN,srcHeader.encodedSize());
            }
            case NONE -> {
                byteBuffer.put(ImageReaderProPra.PROPRA_HEADER_OFFSET_ENCODING, (byte)ImageReaderProPra.PROPRA_HEADER_ENCODING_NONE);
                byteBuffer.putLong(ImageReaderProPra.PROPRA_HEADER_OFFSET_DATALEN,(long)srcHeader.imageSize());
            }
            default -> {
                throw new IllegalArgumentException("Ungültige Kompression.");
            }                   
        }
        
        // In Stream schreiben
        binaryWriter.seek(0);
        write(dataBuffer,0 ,formatHeaderSize);
    }
}
