package propra.imageconverter.data;

import java.nio.ByteBuffer;

/**
 *
 * @author pg
 */
public interface IDataFilter {
    /**
     * 
     */
    public void begin();
    
    /**
     * 
     * @param inOut
     * @return 
     */
    public ByteBuffer apply(ByteBuffer inOut);
    
    /**
     * 
     * @param in
     * @param out
     * @return 
     */
    public ByteBuffer apply(ByteBuffer in, ByteBuffer out);
    
    /**
     * 
     */
    public void end();
}
