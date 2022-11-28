package propra.imageconverter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.CmdLine.Options;
import propra.imageconverter.basen.BaseNCodec;
import propra.imageconverter.basen.BaseNFormat;
import propra.imageconverter.basen.BaseNResource;
import propra.imageconverter.data.DataBlock;
import propra.imageconverter.data.DataCodecRaw;
import propra.imageconverter.data.DataFormat;
import propra.imageconverter.data.DataFormat.IOMode;
import propra.imageconverter.data.DataResource;

/**
 * Klasse implementiert BaseN Programmfunktionalität
 * 
 * @author pg
 */
public class BaseNOperation {
    
    // Kommandozeilenobjekt
    private final CmdLine cmdLine;
    
    // Ein- und Ausgabeobjekt 
    private BaseNResource baseNFile;
    private DataResource binaryFile;
    
    /**
     * 
     */
    public BaseNOperation() {
        cmdLine = null;
        baseNFile = null;
        binaryFile = null;
    }
    
    /**
     * Konstruktor, initialisiert den Task 
     * 
     * @param cmd
     * @throws java.io.FileNotFoundException
     */
    public BaseNOperation(CmdLine cmd) throws FileNotFoundException, IOException {
        if(cmd == null) {
            throw new IllegalArgumentException();
        }
        this.cmdLine = cmd;
        
        // Ausgabedatei Pfad ableiten
        String outPath = cmd.getOption(Options.INPUT_FILE);      
        if(outPath == null) {
            throw new FileNotFoundException("Keine Eingabedatei gegeben!");            
        }
        
        // Datenformat ableiten
        BaseNFormat dataFormat = cmd.getBaseNDataFormat();  
        if(dataFormat == null) {
            throw new IOException("Kein Alphabet gegeben!");            
        }
            
        // Ausgabeendung ableiten
        String outExt;
        if(cmd.isBase32()) {
            outExt = ".base-32";
        } else {
            outExt = ".base-n";
        }
        
        // Pfad für die Ausgabedatei anpassen
        if(cmd.isBaseNDecode()) {
            outPath = outPath.replaceAll(outExt, "");  
        } else {    
            // Prüfen ob ein gültiges Alphabet übergeben wurde
            if(!dataFormat.isValid()) {
                throw new IllegalArgumentException("Ungültiges Base-N Alpahabet.");
            }
            outPath = outPath.concat(outExt);
        }          
        ImageConverter.printMessage(outPath);
        
        // Resourcenobjekte erstellen
        if(cmd.isBaseNDecode()) {
            baseNFile = new BaseNResource(  cmd.getOption(Options.INPUT_FILE), 
                                            dataFormat);
            binaryFile = new DataResource(  outPath, 
                                            IOMode.BINARY);
        } else {
            baseNFile = new BaseNResource(  outPath, 
                                            dataFormat);
            binaryFile = new DataResource(  cmd.getOption(Options.INPUT_FILE), 
                                            IOMode.BINARY);
        }  
    }
    
    /**
     *
     * @return Statusstring
     */
    @Override
    public String toString() {
        String stateString = "";
        return stateString;
    }
    
    /**
     * Aufgabe ausführen
     * 
     * @throws java.io.IOException 
     */
    public void run() throws IOException {
        
        if(cmdLine.isBaseNDecode()) {
            
            // Alphabet laden?
            if(!baseNFile.getFormat().isValidAlphabet()) {
                baseNFile.getFormat().setEncoding(baseNFile.readAlphabet());
            }

            BaseNCodec decoder = new BaseNCodec(baseNFile, 
                                                baseNFile.getFormat());
            DataCodecRaw encoder = new DataCodecRaw(binaryFile, null);
                        
            DataBlock block = new DataBlock();
            block.data = ByteBuffer.allocate((int)baseNFile.length());
            
            decoder.begin(DataFormat.Operation.DECODE);
            decoder.decode(block, null);
            decoder.end();
            
            binaryFile.write(block.data);
        } else {
            
        }
        
        baseNFile.close();
        binaryFile.close();
    }
}
