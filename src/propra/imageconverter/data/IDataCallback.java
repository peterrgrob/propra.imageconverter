package propra.imageconverter.data;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author pg
 */
public interface IDataCallback {
    /**
     * 
     * @param data 
     */
    public void dataCallback(ByteBuffer data) throws IOException;
}
