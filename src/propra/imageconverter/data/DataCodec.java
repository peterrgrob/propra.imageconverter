package propra.imageconverter.data;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *  Basis-Codec für die unkomprimierte Datenübertragung
 */
public class DataCodec implements IDataCodec {

    // Standardblockgröße
    public static final int DEFAULT_BLOCK_SIZE = 4096 * 8 * 3;
    
    // Zugeordnete Resource
    protected IDataResource resource;
    
    // Aktuelle Operation
    protected Operation operation;
    
    /*
     * 
     */
    public DataCodec(IDataResource resource) {
        if(resource != null) {
            throw new IllegalArgumentException();
        }
        this.resource = resource;
    }
    
    /**
     *
     * @param op
     * @throws IOException
     */
    @Override
    public void begin(Operation op) throws IOException {  
        operation = op;
    }
    
    /**
     * 
     * @return 
     */
    @Override
    public Operation getOperation() {
        return operation;
    }
    
    /**
     * 
     * @param data
     * @param last
     */
    @Override
    public void analyze(ByteBuffer data, boolean last) {
        
    }

    /**
     *
     * @param data
     * @param last
     * @throws IOException
     */
    @Override
    public void encode(ByteBuffer data, boolean last) throws IOException {
    }

    /**
     *
     * @param target
     * @throws IOException
     */
    @Override
    public void decode(IDataTarget target) throws IOException {
        
    }        

    /**
     *
     * @throws IOException
     */
    @Override
    public void end() throws IOException {
        operation = Operation.NONE;
    }
    
    /**
     * 
     * @param op
     * @return 
     */
    @Override
    public boolean analyzeNecessary(Operation op) {
        return false;
    }
}
