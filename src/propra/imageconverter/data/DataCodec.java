package propra.imageconverter.data;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataFormat.Operation;
import propra.imageconverter.data.IDataListener.Event;

/**
 *  Codec für die unkomprimierte Datenübertragung
 */
public class DataCodec implements IDataCodec {

    // Standardblockgröße, muss vielfaches der Pixelgröße sein
    public static final int DEFAULT_BLOCK_SIZE = 4096 * 16 * 3;
    
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
                        boolean last, 
                        IDataListener listener) throws IOException {
    }
    
    /*
     * 
     */
    @Override
    public void decode( ByteBuffer data, 
                        boolean last,
                        IDataListener target) throws IOException {
        
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
    
    /*
     *
     */
    protected void dispatchEvent(   Event event,
                                    IDataListener listener, 
                                    ByteBuffer data,
                                    boolean lastBlock) throws IOException {            
        if(listener != null) {
            listener.onData(event,this, data, lastBlock);
        }
    }
    
    /**
     * 
     */
    @Override
    public boolean analyzeNecessary(DataFormat.Operation op) {
        return false;
    }
}
