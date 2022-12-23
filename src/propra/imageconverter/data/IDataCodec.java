package propra.imageconverter.data;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author pg
 */
public interface IDataCodec {
    
    // Datenoperation
    public enum Operation {
        ENCODE,
        DECODE,
        ENCODE_ANALYZE,
        DECODE_ANALYZE,
        NONE;
    }
    
    /**
     * 
     * @param op
     * @throws java.io.IOException
     */
    public void begin(Operation op) throws IOException;
    
    /**
     * 
     * @return 
     */
    public Operation getOperation();
    
    /**
     * Analyse der ganzen Daten vor Kodierung nötig?
     * @param op
     * @return 
     */
    public boolean analyzeNecessary(Operation op);
    
    /**
     * Ermöglicht die Analyse der Daten vor der Kodierung
     * @param data
     * @param last
     */
    public void analyze(ByteBuffer data, boolean last);

    /**
     * Dekodiert Daten in Blöcken und sendet diese an Daten Ziel. 
     * Die Blockgröße kann je nach Codec unterschiedlich 
     * sein.
     * 
     * @param target
     * @throws IOException
     */
    public void decode(IDataTarget target) throws IOException;
    
    /**
     * Kodiert Daten des Blocks und speichert diese.
     * @param data
     * @param last
     * @throws IOException
     */
    public void encode(ByteBuffer data, boolean last) throws IOException;
    
    /**
     * 
     * @throws java.io.IOException
     */
    public void end() throws IOException;
}
