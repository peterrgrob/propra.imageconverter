package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataFormat;
import propra.imageconverter.data.DataReader;
import propra.imageconverter.data.IDataTranscoder;

/**
 *
 * @author pg
 */
public class ImageReader extends DataReader {

    protected final int BLOCK_SIZE;
    protected int formatHeaderSize;
    protected ByteBuffer readBuffer;
    protected long contentTransfered;

    protected ImageTranscoder decoder;    
    protected ImageHeader header;

    
    public ImageReader(String file, DataFormat.IOMode mode) throws IOException {
        super(file, mode);
        BLOCK_SIZE = 1024 * 4096 * 3;
        this.readBuffer = ByteBuffer.allocate(BLOCK_SIZE);
    }
    
    /**
     *
     */
    @Override
    public void begin() throws IOException {
        
        super.begin();
        
        // Format für die Konvertierung setzen
        //colorConverter.outFormat(header.colorFormat());
        //colorConverter.begin();
        
        // Dekoder erstellen
        decoder = header.colorFormat().createTranscoder();
        if(decoder != null) {
            decoder.begin(header.colorFormat());
        }
        
        contentTransfered = 0;
    }
    
    /**
     * 
     * @throws IOException 
     */
    public ImageHeader readHeader() throws IOException {
        return null;
    }
    
    
    /**
     * Erstellt Image aus Byte Daten 
     * 
     * @param buffer
     * @return
     * @throws java.io.IOException
     */
    public int read(ByteBuffer buffer) throws IOException {
        if(!isValid() 
        || buffer == null) {
            throw new IllegalArgumentException();
        }
        
        int len = 0;
        
        // Dekompression erforderlich?
        if(decoder != null) {
            
            // Aktuelle Blockgröße berechnen
            len = readBuffer.capacity();
            long remainingBytes = binaryReader.length() - binaryReader.getFilePointer();
            if(readBuffer.capacity() > remainingBytes) {
                len = (int)(remainingBytes);
            }
            
            // Block aus der Datei lesen
            read(readBuffer, 0, len);
            
            // Prüfsumme mit aktuellem Block vor dekodierung aktualisieren
            updateChecksum(readBuffer);  
            
            // Block dekomprimieren
            decoder.apply(IDataTranscoder.Operation.DECODE, 
                                        readBuffer, 
                                        buffer);
            
            len = buffer.limit();
            
        } else {
            
            // Aktuelle Blockgröße berechnen
            len = buffer.capacity();
            if(contentTransfered + buffer.capacity() > header.imageSize()) {
                len = (int)(header.imageSize() - contentTransfered);
            }
            
            // Daten aus der Datei direkt in buffer lesen ohne Dekompression
            read(buffer, 0, len);
            
            // Prüfsumme mit aktuellem Block aktualisieren
            updateChecksum(buffer);  
        } 
        
        // Anzahl der dekodierten Bytes merken
        contentTransfered += len; 
        
        return len;
    }
    
    /**
     *
     * @return
     */
    @Override
    public long end() {
        
        super.end();
        
        // Checksumme im Header vermerken
        if(checksumObj != null) {
            header.checksum(getChecksum());
        }
        
        //// Farbkonvertierung abschließen
        //colorConverter.end(); 
        
        // Kompression abschließen
        if(decoder != null) {
            decoder.end();
        }
        
        // Anzahl aller bearbeiteten Bytes zurückgeben
        return contentTransfered;
    }
    
    /**
     *
     * @return
     * @throws IOException
     */
    public boolean hasMoreData() throws IOException {
        return (header.imageSize() - contentTransfered) != 0;
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
