package propra.imageconverter.data;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author pg
 */
@FunctionalInterface
public interface IDataTarget {
    
    // Daten Events
    public enum Event {
        DATA_DECODED,
        DATA_ENCODED,
    }
    
    /**
     * 
     * @param event
     * @param data
     * @param caller
     * @param lastBlock
     * @throws java.io.IOException
     */
    public void onData( Event event, IDataCompression caller, 
                        ByteBuffer data, boolean lastBlock) throws IOException;  
}
