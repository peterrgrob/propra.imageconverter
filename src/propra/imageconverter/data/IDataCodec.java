package propra.imageconverter.data;

import java.io.IOException;

/**
 *
 * @author pg
 */
public interface IDataCodec {
    /**
     * 
     */
    public void begin(DataFormat.Operation op) throws IOException;
    
    /**
     * 
     */
    public boolean analyzeNecessary(DataFormat.Operation op);
    
    /**
     * 
     */
    public void analyze(DataBlock data);
            
    /*
     *  Kodiert Daten des Blocks und speichert diese in der Resource.
     */
    public void encode(DataBlock data, IDataListener listener) throws IOException;
    
    /*
     *  Dekodiert Daten von Resource, sendet diese an Listener und speichert in 
     *  Data, falls übergeben. Die Blockgröße kann je nach Codec unterschiedlich 
     *  sein.
     */
    public void decode(DataBlock data, IDataListener listener) throws IOException;
    
    /**
     * 
     */
    public void end() throws IOException;
}
