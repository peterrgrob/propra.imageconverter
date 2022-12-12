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
            
    /**
     * 
     */
    public void encode(DataBlock data, IDataListener listener) throws IOException;
    
    /**
     * 
     */
    public void decode(DataBlock data, IDataListener listener) throws IOException;
    
    /**
     * 
     */
    public void end() throws IOException;
}
