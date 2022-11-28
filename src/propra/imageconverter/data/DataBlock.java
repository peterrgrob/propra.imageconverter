package propra.imageconverter.data;

import java.nio.ByteBuffer;

/**
 *
 * @author pg
 */
public class DataBlock {
    
    // Enthaltene Daten im Block
    public ByteBuffer data;
    
    // true, wenn letzter Block der Operation
    public boolean lastBlock;
    
    /**
     * 
     */
    public DataBlock() {}
    
    /**
     * 
     */
    public DataBlock(ByteBuffer data, boolean lastBlock) {
        this.data = data;
        this.lastBlock = lastBlock;
    }
}
