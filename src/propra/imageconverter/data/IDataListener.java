package propra.imageconverter.data;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author pg
 */
public interface IDataListener {
    
    public enum Event {
        DATA_BLOCK_DECODED,
        DATA_BLOCK_ENCODED,
    }
    
    /**
     * 
     */
    public void onData( Event event, 
                        IDataCodec caller, 
                        ByteBuffer data,
                        boolean lastBlock) throws IOException;  
}
