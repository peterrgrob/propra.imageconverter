package propra.imageconverter.data;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.checksum.Checksum;

/**
 *
 */
public class DataCodecRaw implements IDataCodec {

    // Standardblockgröße, muss vielfaches der Pixelgröße sein
    protected final int DEFAULT_BLOCK_SIZE = 4096 * 16 * 3;
    
    // Temporärer Lesepuffer
    protected ByteBuffer readBuffer;
    
    // Zugeordnete Resource
    protected IDataResource resource;
    
    // Prüfsummenobjekt
    protected Checksum checksum;
    
    /*
     * 
     */
    public DataCodecRaw(IDataResource resource,
                        Checksum checksum) {
        this.checksum = checksum;
        this.resource = resource;
    }
    
    /*
     * 
     */
    public void setup(  IDataResource resource, 
                        Checksum checksum) {
        this.checksum = checksum;
        this.resource = resource;
    }

    /*
     * 
     */
    @Override
    public void begin(DataFormat.Operation op) throws IOException {  
        readBuffer = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE);
        if(checksum != null) {
            checksum.beginFilter();
        }
    }

    /*
     * 
     */
    @Override
    public void encode(DataBlock block) throws IOException {
        if(!isValid() 
        ||  block == null) {
            throw new IllegalArgumentException();
        }
        
        // Datenblock in Resource schreiben
        resource.write(block.data);
        
        // Gelesene Daten filtern
        dispatchEvent(Event.DATA_IO_WRITE, this, block);
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
        
        // Gelesene Daten zu den Listener senden
        dispatchEvent(Event.DATA_IO_READ, this, block);
        dispatchEvent(Event.DATA_BLOCK_DECODED, target, block);
    }        

    /*
     * 
     */
    @Override
    public void end() throws IOException {
        if(checksum != null) {
            checksum.endFilter();
        }
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
    
    /*
     *
     */
    @Override
    public void onData( Event event,
                        IDataCodec caller, 
                        DataBlock block) throws IOException     {
        if(event == Event.DATA_BLOCK_DECODED) {
            encode(block);
        } else if(  event == Event.DATA_IO_READ
                ||  event == Event.DATA_IO_WRITE) {
            if(checksum != null) {
                checksum.apply(block.data);
            }
        }
    }
}
