package propra.imageconverter.data;

import java.io.IOException;

/**
 *
 * @author pg
 */
public interface IDataCallback {
    /**
     * 
     * @param caller
     * @param block 
     */
    public void send(IDataCodec caller, DataBlock block) throws IOException;
}
