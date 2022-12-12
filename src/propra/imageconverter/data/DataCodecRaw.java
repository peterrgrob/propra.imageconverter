package propra.imageconverter.data;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataFormat.Operation;
import propra.imageconverter.data.IDataListener.Event;

/**
 *
 */
public class DataCodecRaw implements IDataCodec {

    // Standardblockgröße, muss vielfaches der Pixelgröße sein
    public static final int DEFAULT_BLOCK_SIZE = 4096 * 16 * 3;
    
    // Temporärer Lesepuffer
    protected ByteBuffer readBuffer;
    
    // Zugeordnete Resource
    protected IDataResource resource;
    
    // Aktuelle Operation
    protected DataFormat.Operation operation;
    
    /*
     * 
     */
    public DataCodecRaw(IDataResource resource) {
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
        readBuffer = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE);
    }
    
    /**
     * 
     */
    @Override
    public void analyze(DataBlock data) {
        
    }

    /*
     * 
     */
    @Override
    public void encode( DataBlock block, 
                        IDataListener listener) throws IOException {
        if(!isValid() 
        ||  block == null) {
            throw new IllegalArgumentException();
        }
        
        // Datenblock in Resource schreiben
        resource.write(block.data);
        
        // Gelesene Daten filtern
        dispatchEvent(Event.DATA_IO_WRITE, listener, block);
    }
    
    /*
     * 
     */
    @Override
    public void decode( DataBlock block,
                        IDataListener target) throws IOException {
        if(!isValid()
        || block == null) {
            throw new IllegalArgumentException();
        }
        
        // Datenblock von Resource lesen 
        resource.read(readBuffer.clear());
        block.data = readBuffer;
        
        // Letzter Block der Operation?
        if(resource.position() == resource.length()) {
            block.lastBlock = true;
        }
        
        // Gelesene Daten filtern
        dispatchEvent(Event.DATA_IO_READ, target, block);
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
                                    DataBlock block) throws IOException {            
        if(listener != null) {
            listener.onData(event,this, block);
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
