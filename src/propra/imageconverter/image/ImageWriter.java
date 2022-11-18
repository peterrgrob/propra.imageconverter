package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataFormat;
import propra.imageconverter.data.DataWriter;
import propra.imageconverter.data.IDataTranscoder;

/**
 *
 * @author pg
 */
public class ImageWriter extends DataWriter {
    
    protected final int BLOCK_SIZE;
    protected int formatHeaderSize;
    protected ByteBuffer writeBuffer;
    protected long contentTransfered;
    protected ImageHeader header;
    protected ColorFormat writeColorFormat;

    // Farbverarbeitung
    protected ImageTranscoder encoder;    


    /**
     * 
     * @param file
     * @param mode
     * @throws IOException 
     */
    public ImageWriter(String file, DataFormat.IOMode mode) throws IOException {
        super(file, mode);
        BLOCK_SIZE = 1024 * 4096 * 3;
        this.writeBuffer = ByteBuffer.allocate(BLOCK_SIZE);
        writeColorFormat = new ColorFormat();
    }
    
    /**
     * 
     * @param srcHeader
     * @throws IOException 
     */
    public void writeHeader(ImageHeader srcHeader) throws IOException {
        srcHeader = new ImageHeader(header);
    }
    
    /**
     *
     * @throws java.io.IOException
     */
    @Override
    public void begin() throws IOException {     
        super.begin();
        
        // Encoder erstellen
        encoder = header.colorFormat().createTranscoder();
        if(encoder != null) {
            encoder.begin(header.colorFormat());
        }
        
        contentTransfered = 0;
    }
    
    /**
     * Wandelt Bilddaten in bytes um. 
     * 
     * @return
     * @throws java.io.IOException
     */
    @Override
    public int write(ByteBuffer buffer) throws IOException {
        if(!isValid() 
        || buffer == null) {
            throw new IllegalArgumentException();
        }
        
        // Farbkonvertierung
        if(!header.colorFormat().equals(writeColorFormat)) {
            ColorFormat.convertColorBuffer(buffer, header.colorFormat(), 
                                            buffer, writeColorFormat);
        }

        ByteBuffer tmpBuffer = buffer;
        
        // Kompression erforderlich?
        if(encoder != null) {
            // Block komprimieren
            encoder.apply(IDataTranscoder.Operation.ENCODE, 
                                buffer, 
                                writeBuffer);
            tmpBuffer = writeBuffer;
        } 
        
        // Prüfsumme mit aktuellem Block aktualisieren
        updateChecksum(tmpBuffer); 
        
        // Block in Datei schreiben
        write(tmpBuffer, 0, tmpBuffer.limit());
        
        // Anzahl der kodierten Bytes merken
        contentTransfered += tmpBuffer.limit();
        return tmpBuffer.limit();
    }
    
    /**
     *
     */
    @Override
    public void end() {
        
        super.end();
        
        // Checksumme im Header vermerken
        if(checksumObj != null) {
            checksumObj.end();
            header.checksum(getChecksum());
        }
        
        // Kompression abschließen
        if(encoder != null) {
            encoder.end();
            header.encodedSize(contentTransfered);
        }
    }
    
    /**
     *
     * @return
     */
    public int getHeaderSize() {
        return formatHeaderSize;
    }

    /**
     *
     * @return
     */
    public int getBlockSize() {
        return BLOCK_SIZE;
    }

    /**
     *
     * @return
     */
    public ImageHeader getHeader() {
        return header;
    }
}
