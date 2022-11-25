package propra.imageconverter.data;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.checksum.Checksum;

/**
 *
 * @author pg
 */
public class DataCodec implements IDataCodec {

    // Standardblockgröße, vielfaches der Pixelgröße
    protected final int DEFAULT_BLOCK_SIZE = 4096 * 32 * 3;
    
    // Temporärer Lesepuffer
    protected ByteBuffer readBuffer;
    
    // Zugeordnete Resource
    protected IDataResource resource;
    
    // Prüfsummenobjekt
    protected Checksum checksum;
    
    /**
     * 
     * @param resource
     * @param checksum 
     */
    public DataCodec(   IDataResource resource,
                        Checksum checksum) {
        this.checksum = checksum;
        this.resource = resource;
    }
    
    /**
     * 
     * @param resource
     * @param checksum 
     */
    public void setup(IDataResource resource, Checksum checksum) {
        this.checksum = checksum;
        this.resource = resource;
    }

    /**
     * 
     * @param op
     * @throws IOException 
     */
    @Override
    public void begin(DataFormat.Operation op) throws IOException {  
        readBuffer = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE);
        if(checksum != null) {
            checksum.beginFilter();
        }
    }

    /**
     * 
     * @param block
     * @throws IOException 
     */
    @Override
    public void encode(DataBlock block) throws IOException {
        if(!isValid()
        ||  block == null) {
            throw new IllegalArgumentException();
        }
        
        resource.write(block.data);
            
        if(checksum != null) {
            checksum.apply(block.data);
        }
    }
    
    /**
     * 
     * @param block
     * @param target
     * @throws IOException 
     */
    @Override
    public void decode( DataBlock block,
                        IDataTarget target) throws IOException {
        if(!isValid()
        ||  block == null) {
            throw new IllegalArgumentException();
        }
        
        readBuffer.clear();
        resource.read(readBuffer);

        block.data = readBuffer;
        if(resource.position() == resource.length()) {
            block.lastBlock = true;
        }
        
        if(checksum != null) {
            checksum.apply(readBuffer);
        }

        if(target != null) {
            pushDataToTarget(target, block);
        }
    }        
            

    /**
     * 
     * @return 
     * @throws java.io.IOException 
     */
    @Override
    public boolean isDataAvailable() throws IOException {
        if(resource != null) {
            return (resource.length() - resource.position() > 0);
        }
        return false;
    }

    /**
     * 
     * @throws IOException 
     */
    @Override
    public void end() throws IOException {
        if(checksum != null) {
            checksum.endFilter();
        }
    }
    
    /**
     * 
     * @return 
     */
    public boolean isValid() {
        return resource != null;
    }
    
    /**
     * 
     * @param target
     * @param block 
     * @throws java.io.IOException 
     */
    protected void pushDataToTarget(IDataTarget target, 
                                    DataBlock block) throws IOException {            
        block.data.flip();
        target.push(this, block);
        block.data.clear();
    }
}
