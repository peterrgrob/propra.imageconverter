package propra.imageconverter.data;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author pg
 */
@FunctionalInterface
public interface IDataTarget {
    /**
     * 
     */
    public void onData(ByteBuffer data, boolean lastBlock, IDataTranscoder caller) throws IOException;  
}
