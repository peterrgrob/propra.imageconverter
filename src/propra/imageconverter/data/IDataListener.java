package propra.imageconverter.data;

import java.io.IOException;

/**
 *
 * @author pg
 */
public interface IDataListener {
    
    public enum Event {
        DATA_IO_READ,
        DATA_IO_WRITE,
        DATA_BLOCK_DECODED,
        DATA_BLOCK_ENCODED,
    }
    
    public void onData( Event event, 
                        IDataCodec caller, 
                        DataBlock block) throws IOException;  
}
