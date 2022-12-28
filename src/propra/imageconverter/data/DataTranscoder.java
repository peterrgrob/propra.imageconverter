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
        operation = Operation.NONE;
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
     */
    @Override
    public IDataTranscoder beginOperation(Operation op) throws IOException {  
        operation = op;
        return this;
    }
    
    /**
     *
     * @throws IOException
     */
    @Override
    public void endOperation() throws IOException {
        operation = Operation.NONE;
    }
}
