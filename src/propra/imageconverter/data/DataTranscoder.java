package propra.imageconverter.data;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Basisfunktionalität für die Kompressionsklassen. 
 */
public abstract class DataTranscoder implements IDataTranscoder {
    
    // Standardblockgröße
    public static final int DEFAULT_BLOCK_SIZE = 4096 * 8 * 3;
    
    // Aktuelle Operation
    protected Operation operation;

    public DataTranscoder() {
    }    
    
    /**
     *
     */
    @Override
    public IDataTranscoder begin(Operation op) throws IOException {  
        operation = op;
        return this;
    }
    
    /**
     * 
     */
    @Override
    public Operation getOperation() {
        return operation;
    }
    
    /**
     * 
     */
    @Override
    public void analyze(ByteBuffer data, boolean last) {}
    
    /**
     * 
     */
    @Override
    public boolean analyzeNecessary(Operation op) {
        return false;
    }

    /**
     *
     * @throws IOException
     */
    @Override
    public void end() throws IOException {
        operation = Operation.NONE;
    }
}
