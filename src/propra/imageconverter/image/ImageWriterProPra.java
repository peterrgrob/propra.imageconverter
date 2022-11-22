package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import propra.imageconverter.checksum.ChecksumPropra;
import propra.imageconverter.data.DataFormat;

/**
 * Schreibt ProPra Header
 * @author pg
 */
public class ImageWriterProPra extends ImageWriter {
    /**
     *
     * @param file
     * @param mode
     * @throws java.io.IOException
     */
    public ImageWriterProPra(String file, DataFormat.IOMode mode) throws IOException {
        super(file, mode);
        fileHeaderSize = ImageReaderProPra.PROPRA_HEADER_SIZE;
        checksumObj = new ChecksumPropra();
        
        this.writeColorFormat = new ColorFormat(0, 2, 1);
    }
    
    /**
     * Schreibt allgemeinen Header als ProPra Header
     * 
     * @param srcHeader
     */
    @Override
    public void writeHeader(ImageHeader srcHeader) throws IOException {
        if(srcHeader.isValid() == false) {
            throw new IllegalArgumentException();
        }
        
        super.writeHeader(srcHeader);
        
        // DataBuffer für Header erstellen
        ByteBuffer byteBuffer = ByteBuffer.allocate(ImageReaderProPra.PROPRA_HEADER_SIZE);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // ProPra spezifisches RBG Farbmapping setzen
        header.colorFormat().setMapping(writeColorFormat.getMapping());
        
        // Headerfelder in ByteBuffer schreiben
        DataFormat.putStringToByteBuffer(byteBuffer, 0, ImageReaderProPra.PROPRA_VERSION);
        byteBuffer.put(ImageReaderProPra.PROPRA_HEADER_OFFSET_ENCODING, (byte)0);
        byteBuffer.putShort(ImageReaderProPra.PROPRA_HEADER_OFFSET_WIDTH,(short)srcHeader.width());
        byteBuffer.putShort(ImageReaderProPra.PROPRA_HEADER_OFFSET_HEIGHT,(short)srcHeader.height());
        byteBuffer.put(ImageReaderProPra.PROPRA_HEADER_OFFSET_BPP,(byte)(srcHeader.pixelSize() << 3));
        byteBuffer.putInt(ImageReaderProPra.PROPRA_HEADER_OFFSET_CHECKSUM, (int)srcHeader.checksum());
        
        // Kompression 
        switch(header.colorFormat().encoding()) {
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
        write(byteBuffer,0 ,fileHeaderSize);
    }
}
