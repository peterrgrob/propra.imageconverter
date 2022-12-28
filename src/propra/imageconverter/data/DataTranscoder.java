package propra.imageconverter.data;

import java.io.IOException;
import propra.imageconverter.util.CheckedOutputStream;
import propra.imageconverter.util.PropraException;

/**
 * Basisfunktionalität für die Kompressionsklassen. 
 */
public abstract class DataTranscoder implements IDataTranscoder {
    
    // Standardblockgröße
    public static final int DEFAULT_BLOCK_SIZE = 4096 * 8 * 3;
    
    // Aktuelle Operation
    protected Operation operation;
    
    // Kodierte Bytes
    protected long encodedBytes;
    
    // Ausgabestream
    protected CheckedOutputStream outStream;

    /**
     * 
     */
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
    public boolean analyzeNecessary() {
        return false;
    }

    /**
     *
     */
    @Override
    public IDataTranscoder beginOperation(Operation op, CheckedOutputStream out) throws IOException {  
        PropraException.assertArgument(out);
        operation = op;
        encodedBytes = 0;
        this.outStream = out;
        return this;
    }
    
    /**
     *
     * @throws IOException
     */
    @Override
    public long endOperation() throws IOException {
        operation = Operation.NONE;
        return encodedBytes;
    }
}
