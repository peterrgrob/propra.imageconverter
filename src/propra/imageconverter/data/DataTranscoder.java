package propra.imageconverter.data;

import java.io.IOException;
import propra.imageconverter.PropraException;

/**
 * Basisfunktionalität für Transcoderklassen
 */
public abstract class DataTranscoder implements IDataTranscoder {
    
    // Standardblockgröße
    public static final int DEFAULT_BLOCK_SIZE = 4096 * 8 * 3;
    
    // Aktuelle Operation
    protected EncodeMode operation;
    
    // Kodierte Bytes
    protected long encodedBytes;
    
    // Ausgabestream
    protected CheckedOutputStream outStream;

    public DataTranscoder() {
        operation = EncodeMode.NONE;
    }    
    
    @Override
    public EncodeMode getOperation() {
        return operation;
    }
    
    /**
     * Gibt true zurück, wenn der Transcoder für die Kodierung einen 
     * Analyselauf benötigt
     */
    @Override
    public boolean analyzeNecessary() {
        return false;
    }

    /**
     * Initialisiert die Kodierung von Datenblöcken
     */
    @Override
    public IDataTranscoder beginEncoding(EncodeMode op, CheckedOutputStream out) throws IOException {  
        PropraException.assertArgument(out);
        operation = op;
        encodedBytes = 0;
        this.outStream = out;
        return this;
    }
    
    /**
     * Beendet die Kodierung
     */
    @Override
    public long endEncoding() throws IOException {
        operation = EncodeMode.NONE;
        return encodedBytes;
    }
}
