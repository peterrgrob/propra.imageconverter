package propra.imageconverter.data;

/**
 *
 * @author pg
 */
public class DataController implements IDataController {
    
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
    @Override
    public void setup(IDataCodec input, IDataCodec output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public void process() {
        if( input == null 
        ||  output == null) {
            throw new IllegalStateException("Keine Ein- Ausgabeobjekte erstellt!");
        }
        
        DataBlock dataBlock = new DataBlock();
        
        input.begin(DataFormat.Operation.DECODE);
        output.begin(DataFormat.Operation.ENCODE);
        
        // Datenblöcke transferieren
        while(input.isDataAvailable()) {
            
            // Nächsten Block lesen und schreiben
            input.processBlock(DataFormat.Operation.DECODE, dataBlock);
            output.processBlock(DataFormat.Operation.ENCODE, dataBlock);
        }
        
        input.end(DataFormat.Operation.DECODE);
        output.end(DataFormat.Operation.ENCODE);
    } 
}
