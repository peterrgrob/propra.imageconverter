package propra.imageconverter.data;

import java.io.IOException;

/**
 *
 * @author pg
 */
public class DataController implements  IDataController,
                                        IDataCallback {
    
    /**
     * 
     */
    IDataCodec input;
    IDataCodec output;

    /**
     * 
     * @param input
     * @param output 
     */
    public DataController(  IDataCodec input, 
                            IDataCodec output) {
        this.input = input;
        this.output = output;
    }
     
    /**
     * 
     * @param input
     * @param output 
     */
    @Override
    public void setup(  IDataCodec input, 
                        IDataCodec output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public void process() throws IOException {
        if( input == null 
        ||  output == null) {
            throw new IllegalStateException("Keine Ein- Ausgabeobjekte erstellt!");
        }
        
        DataBlock dataBlock = new DataBlock();
        
        input.begin(DataFormat.Operation.READ);
        output.begin(DataFormat.Operation.WRITE);
        
        // Datenblöcke transferieren
        while(input.isDataAvailable()) {
            
            // Nächsten Block lesen und schreiben
            input.processBlock(DataFormat.Operation.READ, dataBlock, this);
        }
        
        input.end(DataFormat.Operation.READ);
        output.end(DataFormat.Operation.WRITE);
    } 

    /**
     * 
     * @param caller
     * @param block 
     */
    public void send(IDataCodec caller, DataBlock block) throws IOException {
        if(caller == input
        && block != null) {
            output.processBlock(DataFormat.Operation.WRITE, block, this);
        }
    }
}
