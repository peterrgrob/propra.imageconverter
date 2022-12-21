package propra.imageconverter.data;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author pg
 */
public interface IDataTarget {
    
    // Daten Events
    public enum Event {
        DATA_BLOCK_DECODED,
        DATA_BLOCK_ENCODED,
    }
    
    /**
     * 
     * @param event
     * @param data
     * @param caller
     * @param lastBlock
     * @throws java.io.IOException
     */
    public void onData( Event event, 
                        IDataCodec caller, 
                        ByteBuffer data,
                        boolean lastBlock) throws IOException;  
}
