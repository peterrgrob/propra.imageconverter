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
     * @return Ausgabepuffer
     */
    public ByteBuffer apply(  Operation op,
                            ByteBuffer in,
                            ByteBuffer out);
    
    /**
     *
     */
    public void end();
    
    /**
     * 
     * @param op
     * @param buffer
     * @return 
     */
    public int transcodedBufferLength(Operation op, ByteBuffer buffer);
}
