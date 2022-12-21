package propra.imageconverter.data;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataFormat.Operation;

/**
 *
 * @author pg
 */
public interface IDataCodec {
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
     * Dekodiert Daten von Resource, sendet diese an Listener und speichert in 
     * Data, falls übergeben. Die Blockgröße kann je nach Codec unterschiedlich 
     * sein.
     * 
     * @param listener
     * @throws IOException
     */
    public void decode(IDataTarget listener) throws IOException;
    
    /**
     * Kodiert Daten des Blocks und speichert diese in der Resource.
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
