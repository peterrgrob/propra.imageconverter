package propra.imageconverter.data;

import java.io.IOException;

/**
 *
 * @author pg
 */
public class DataController implements  IDataController,
                                        IDataTarget {
    
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
            input.decode(dataBlock, this);
        }
        
        input.end();
        output.end();
    } 

    /**
     * 
     * @param caller
     * @param block 
     */
    public void push(IDataCodec caller, DataBlock block) throws IOException {
        if(caller == input
        && block != null) {
            output.encode(block);
        }
    }
}
