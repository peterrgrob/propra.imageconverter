package propra.imageconverter.data;

import java.nio.ByteBuffer;

/**
 *
 * @author pg
 */
public interface IDataTranscoder {
    
    public enum Operation {
        ENCODE,
        DECODE,
        FILTER,
        NONE;
    }
    
    /**
     *
     */
    public void begin();
     
    /**
     *
     * @param op
     * @param in
     * @param out
     * @return Anzahl der kodierten Bytes
     */
    public long apply(  Operation op,
                        ByteBuffer in,
                        ByteBuffer out);
    
    /**
     *
     */
    public void end();
}
