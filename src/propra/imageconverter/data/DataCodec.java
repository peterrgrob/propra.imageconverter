package propra.imageconverter.data;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.checksum.Checksum;

/**
 *
 * @author pg
 */
public class DataCodec implements IDataCodec {

    protected final int DEFAULT_BLOCK_SIZE = 4096 * 32 * 3;
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
    @Override
    public void setup(IDataResource resource, Checksum checksum) {
        this.checksum = checksum;
        this.resource = resource;
    }

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
    public void processBlock(   DataFormat.Operation op, 
                                DataBlock block) throws IOException {
        if(!isValid()
        ||  block == null) {
            throw new IllegalArgumentException();
        }
        
        if(op == DataFormat.Operation.READ) {
            resource.read(dataBuffer);
            block.data = dataBuffer;
            block.sourcePosition = resource.position();
            block.sourceLength = resource.length();
            if(checksum != null) {
                checksum.apply(dataBuffer);
            }
        } else if(op == DataFormat.Operation.WRITE) {
            resource.write(block.data);
            if(checksum != null) {
                checksum.apply(block.data);
            }
        }
    }

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

    @Override
    public void end(DataFormat.Operation op) throws IOException {
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
}
