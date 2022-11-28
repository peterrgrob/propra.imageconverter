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
    public void beginFilter();
    
    /**
     * 
     * @param inOut
     * @return 
     */
    public ByteBuffer apply(ByteBuffer inOut);
    
    /**
     * 
     */
    public void endFilter();
}
