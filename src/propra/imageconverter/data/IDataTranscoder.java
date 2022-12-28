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
        ANALYZE;
    }

    /**
     * 
     */
    public void decode(IDataTarget target) throws IOException;
    
    /**
     * 
     */
    public IDataTranscoder beginOperation(Operation op) throws IOException;
    
    /**
     * 
     */
    public Operation getOperation();
    
    /**
     * Analyse der ganzen Daten vor Kodierung nötig?
     */
    public boolean analyzeNecessary(Operation op);
    
    
    /**
     * Kodiert Daten des Blocks und speichert diese.
     */
    public void encode(ByteBuffer data, boolean last) throws IOException;
    
    /**
     * 
     */
    public void endOperation() throws IOException;
}
