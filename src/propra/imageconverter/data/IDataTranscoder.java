package propra.imageconverter.data;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.util.CheckedInputStream;
import propra.imageconverter.util.CheckedOutputStream;

/**
 *
 * @author pg
 */
public interface IDataTranscoder extends IDataTarget {
    
    // Datenoperationstypen
    public enum Operation {
        NONE,
        ENCODE,
        DECODE,
        ANALYZE,
        AUTO;
    }
    
    // Kodierungstypen der Daten
    public enum Compression {
        NONE,
        RLE,
        BASEN,
        HUFFMAN,
        AUTO;
    }

    /**
     * 
     */
    public void decode(CheckedInputStream in, IDataTarget target) throws IOException;
    
    /**
     * 
     */
    public IDataTranscoder beginOperation(Operation op, CheckedOutputStream out) throws IOException;
    
    /**
     * 
     */
    public Operation getOperation();
    
    /**
     * 
     */
    public Compression getCompression();
    
    /**
     * Analyse der ganzen Daten vor Kodierung nötig?
     */
    public boolean analyzeNecessary();
    
    
    /**
     * Kodiert Daten des Blocks und speichert diese.
     */
    public void encode(ByteBuffer data, boolean last) throws IOException;
    
    /**
     * Gibt Anzahl der kodierten Bytes zurück
     */
    public long endOperation() throws IOException;
}
