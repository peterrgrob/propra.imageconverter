package propra.imageconverter.data;

import java.io.IOException;
import java.nio.ByteBuffer;

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
        ENCODE_ANALYZE,
        DECODE_ANALYZE;
    }
    
    /**
     * 
     */
    public IDataTranscoder begin(Operation op) throws IOException;
    
    /**
     * 
     */
    public Operation getOperation();
    
    /**
     * Analyse der ganzen Daten vor Kodierung nötig?
     */
    public boolean analyzeNecessary(Operation op);
    
    /**
     * Ermöglicht die Analyse der Daten vor der Kodierung
     */
    public void analyze(ByteBuffer data, boolean last);

    /**
     * 
     */
    public void decode(IDataTarget target) throws IOException;
    
    /**
     * Kodiert Daten des Blocks und speichert diese.
     */
    public void encode(ByteBuffer data, boolean last) throws IOException;
    
    /**
     * 
     */
    public void end() throws IOException;
}
