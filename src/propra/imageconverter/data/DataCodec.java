package propra.imageconverter.data;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataFormat.Operation;

/**
 *  Basis-Codec für die unkomprimierte Datenübertragung
 */
public class DataCodec implements IDataCodec {

    // Standardblockgröße
    public static final int DEFAULT_BLOCK_SIZE = 4096 * 8 * 3;
    
    // Zugeordnete Resource
    protected IDataResource resource;
    
    // Aktuelle Operation
    protected DataFormat.Operation operation;
    
    /*
     * 
     */
    public DataCodec(IDataResource resource) {
        this.resource = resource;
    }
    
    /*
     * 
     */
    public void setup(  IDataResource resource) {
        this.resource = resource;
    }

    /*
     * 
     */
    @Override
    public void begin(DataFormat.Operation op) throws IOException {  
        operation = op;
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
    public void analyze(ByteBuffer data, 
                        boolean last) {
        
    }

    /*
     * 
     */
    @Override
    public void encode( ByteBuffer data, 
                        boolean last) throws IOException {
    }
    
    /*
     * 
     */
    @Override
    public void decode(IDataListener target) throws IOException {
        
    }        

    /*
     * 
     */
    @Override
    public void end() throws IOException {
        operation = Operation.NONE;
    }
    
    /*
     * 
     */
    public boolean isValid() {
        return resource != null;
    }
    
    /**
     * 
     */
    @Override
    public boolean analyzeNecessary(DataFormat.Operation op) {
        return false;
    }
}
