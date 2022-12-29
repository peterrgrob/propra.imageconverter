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
    
    // Encodingmodus
    public enum EncodeMode {
        NONE,
        ENCODE,
        ANALYZE,
        AUTO;
    }
    
    // Komprimierungen
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
    public EncodeMode getOperation();
    
    /**
     * 
     */
    public Compression getCompression();
    
    /**
     * Analyse der ganzen Daten vor Kodierung nötig?
     */
    public boolean analyzeNecessary();
    
    /**
     * 
     */
    public IDataTranscoder beginEncoding(EncodeMode op, CheckedOutputStream out) throws IOException;
   
    /**
     * Kodiert Daten des Blocks und speichert diese.
     */
    public void encode(ByteBuffer data, boolean last) throws IOException;
    
    /**
     * Gibt Anzahl der kodierten Bytes zurück
     */
    public long endEncoding() throws IOException;
}
