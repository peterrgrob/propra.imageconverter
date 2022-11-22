package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataFormat;
import propra.imageconverter.data.DataWriter;
import propra.imageconverter.data.IDataCallback;
import propra.imageconverter.data.IDataTranscoder;

/**
 * Oberklasse für formatspezifische ImageWriter
 * 
 * @author pg
 */
public class ImageWriter extends DataWriter implements IDataCallback {
    
    // IO Variablen
    protected final int BLOCK_SIZE;
    protected ByteBuffer writeBuffer;
    protected long contentTransfered;
    
    // Formatespezifische Variablen
    protected ImageHeader header;
    protected ColorFormat readColorFormat;
    protected ColorFormat writeColorFormat;
    protected int formatHeaderSize;

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
        BLOCK_SIZE = 4096 * 3;
        writeColorFormat = new ColorFormat();
        readColorFormat = new ColorFormat();
    }
    
    /**
     * 
     * @param srcHeader
     * @throws IOException 
     */
    public void writeHeader(ImageHeader srcHeader) throws IOException {
        header = new ImageHeader(srcHeader);
        readColorFormat = srcHeader.colorFormat();
    }
    
    /**
     * 
     * @param data 
     */
    @Override
    public void dataCallback(ByteBuffer data) throws IOException {
        if(data != null) {
            write(data);
        }
    }
    
    
    /**
     * Wandelt Bilddaten in bytes um. 
     * 
     * @return
     * @throws java.io.IOException
     */
    @Override
    public ByteBuffer write(ByteBuffer buffer) throws IOException {
        if(!isValid() 
        || buffer == null) {
            throw new IllegalArgumentException();
        }

        ByteBuffer tmpBuffer = buffer;
        
        // Unterschiedliche Farbformate?
        if(!readColorFormat.equals(writeColorFormat)) {

            // Farben konvertieren
            ColorFormat.convertColorBuffer( buffer, readColorFormat, 
                                            buffer, writeColorFormat);
        }
        
        // Kompression erforderlich?
        if(encoder != null) {
            writeBuffer = ByteBuffer.allocate(buffer.capacity()<<1);
            
            // Block komprimieren
            tmpBuffer = encoder.apply(IDataTranscoder.Operation.ENCODE, 
                                    buffer, 
                                    writeBuffer);
        } 
        
        // Prüfsumme mit aktuellem Block aktualisieren
        updateChecksum(tmpBuffer); 
        
        // Block in Datei schreiben
        write(tmpBuffer, 0, tmpBuffer.limit());
        
        // Anzahl der kodierten Bytes merken
        contentTransfered += tmpBuffer.limit();
        return tmpBuffer;
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
