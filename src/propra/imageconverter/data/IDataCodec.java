package propra.imageconverter.data;

import java.io.IOException;

/**
 *
 * @author pg
 */
public interface IDataCodec {
    /**
     * 
     * @param op
     * @throws IOException 
     */
    public void begin(DataFormat.Operation op) throws IOException;
    
    /**
     * 
     * @param data
     * @throws IOException 
     */
    public void encode(DataBlock data, IDataListener listener) throws IOException;
    
    /**
     * 
     * @param data
     * @param target
     * @throws IOException 
     */
    public void decode(DataBlock data, IDataListener listener) throws IOException;
    
    /**
     * 
     * @throws IOException 
     */
    public void end() throws IOException;
}
