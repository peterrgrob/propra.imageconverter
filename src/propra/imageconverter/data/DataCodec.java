package propra.imageconverter.data;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.checksum.Checksum;

/**
 *
 * @author pg
 */
public class DataCodec implements IDataCodec {

    protected final int DEFAULT_BLOCK_SIZE = 4096 * 1 * 3;
    protected ByteBuffer dataBuffer;
    protected IDataResource resource;
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
        dataBuffer = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE);
        if(checksum != null) {
            checksum.beginFilter();
        }
    }

    /**
     * 
     * @param op
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
    public void decode( DataBlock block,
                        IDataTarget target) throws IOException {
        if(!isValid()
        ||  block == null) {
            throw new IllegalArgumentException();
        }
        
        resource.read(dataBuffer);

        block.data = dataBuffer;
        block.sourcePosition = resource.position();
        block.sourceLength = resource.length();

        if(checksum != null) {
            checksum.apply(dataBuffer);
        }

        if(target != null) {
            pushDataToTarget(target, block);
        }
    }        
            

    /**
     * 
     * @return 
     */
    @Override
    public boolean isDataAvailable() {
        if(resource != null) {
            try {
                return (resource.length() - resource.position() > 0);
            } catch (IOException ex) {
                throw new IllegalStateException();
            }
        }
        return false;
    }

    /**
     * 
     * @param op
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
     */
    protected void pushDataToTarget(IDataTarget target, DataBlock block) throws IOException {
        
        block.sourceLength = resource.length();
        block.sourcePosition = resource.position();
                    
        block.data.flip();
        target.push(this, block);
        block.data.clear();
    }
}
