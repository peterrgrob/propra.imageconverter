package propra.imageconverter.data;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author pg
 */
public interface IDataCompression {
    
    // Datenoperation des Codecs
    public enum Operation {
        ENCODE,
        DECODE,
        ENCODE_ANALYZE,
        DECODE_ANALYZE,
        NONE;
    }
    
    /**
     * 
     */
    public void begin(Operation op) throws IOException;
    
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
