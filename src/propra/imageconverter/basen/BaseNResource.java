package propra.imageconverter.basen;

import java.io.IOException;
import propra.imageconverter.data.DataBlock;
import propra.imageconverter.data.DataBlock;
import propra.imageconverter.data.DataFormat.IOMode;
import propra.imageconverter.data.DataResource;
import propra.imageconverter.data.DataResource;
import propra.imageconverter.data.IDataCodec;
import propra.imageconverter.data.IDataCodec;
import propra.imageconverter.data.IDataListener;
import propra.imageconverter.data.IDataListener;

/**
 *
 */
public class BaseNResource extends DataResource implements IDataListener {
    
    // BaseN Kodierungsformat
    private final BaseNFormat format;
    
    /**
     * 
     */
    public BaseNResource(   String file, 
                            BaseNFormat format,
                            boolean write) throws IOException {
        super(file, IOMode.BINARY, write);
        
        this.format = format;
        if(format == null) {
            format = new BaseNFormat(); 
        }
    }
    
    /**
     * Alphabet aus Datei einlesen und DatenFormat ableiten
     */
    public String readAlphabet() throws IOException {
        String alphabet = binaryFile.readLine();
        format.setEncoding(alphabet);
        return alphabet;
    }
    
    /**
     * Alphabet in Datei schreiben
     */
    public void writeAlphabet(String alphabet) throws IOException {
        if(!isValid()) {
            throw new IllegalArgumentException();
        }
        
        // Alphabet in Datei schreiben 
        if(format.getBaseEncoding() != BaseNFormat.BaseNEncoding.BASE_32) {
            txtWriter.write(alphabet + "\n");
        }
    }
    
    /**
     * 
     * @return BaseNFormat
     */
    public BaseNFormat getFormat() {
        return format;
    }
    
    /**
     * 
     */
    @Override
    public void onData( Event event, 
                        IDataCodec caller, 
                        DataBlock block) throws IOException {
    }
}
