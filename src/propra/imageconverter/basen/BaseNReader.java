package propra.imageconverter.basen;

import java.io.IOException;
import propra.imageconverter.data.DataFormat.IOMode;
import propra.imageconverter.data.DataReader;
import propra.imageconverter.data.IDataCallback;

/**
 *
 * @author pg
 */
public class BaseNReader extends DataReader {
    
    private final BaseN decoder;
    private final BaseNFormat format;
    
    /**
     * 
     * @param file
     * @param format
     * @throws IOException 
     */
    public BaseNReader(String file, BaseNFormat format) throws IOException {
        super(file, IOMode.BINARY);
        this.format = format;
        decoder = new BaseN(this.format);
    }
    
    /**
     *
     * @param buffer
     * @return
     * @throws IOException
     */
    public void read(IDataCallback dataTarget) throws IOException {
        if( !isValid()
        ||  dataTarget == null) {
            throw new IllegalStateException();
        }
            
        // Alphabet vorhanden?
        if(decoder.dataFormat().getAlphabet().length() == 0) {
            
            // Alphabet aus Datei einlesen und DatenFormat ableiten
            String alphabet = binaryReader.readLine();
            decoder.dataFormat().setEncoding(alphabet);
        }

        // In Ausgabe dekodieren
        decoder.decode( binaryReader, 
                        dataTarget);

    }
}
