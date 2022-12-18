package propra.imageconverter.data;

import java.io.IOException;
import propra.imageconverter.data.DataFormat.Operation;

/**
 *
 */
public interface IDataCodec {
    /**
     * 
     */
    public void begin(Operation op) throws IOException;
    
    /**
     * 
     */
    public Operation getOperation();
    
    /**
     *  Analyse der ganzen Daten vor Kodierung nötig?
     */
    public boolean analyzeNecessary(Operation op);
    
    /**
     *  Ermöglicht die Analyse der Daten vor der Kodierung
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
