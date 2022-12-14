package propra.imageconverter.data;

import java.nio.ByteBuffer;

/**
 *
 * @author pg
 */
public class DataBlock implements IStreamable {
    
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
    public DataBlock(int size) {
        data = ByteBuffer.allocate(size);
    }
    
    /**
     * 
     */
    public DataBlock(ByteBuffer data, boolean lastBlock) {
        this.data = data;
        this.lastBlock = lastBlock;
    }
    
    /**
     * 
     */
    public byte[] array() {
        return data.array();
    }

    /**
     * Gibt nächstes Byte im Puffer zurück
     */
    @Override
    public byte readByte() {
        return data.get();
    }
}
