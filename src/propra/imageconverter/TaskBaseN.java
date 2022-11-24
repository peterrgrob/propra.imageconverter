package propra.imageconverter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.basen.BaseNCodec;
import propra.imageconverter.basen.BaseNFormat;
import propra.imageconverter.basen.BaseNResource;
import propra.imageconverter.data.DataBlock;
import propra.imageconverter.data.DataCodec;
import propra.imageconverter.data.DataFormat;
import propra.imageconverter.data.DataFormat.IOMode;
import propra.imageconverter.data.DataResource;

/**
 * Klasse implementiert BaseN Programmfunktionalität
 * 
 * @author pg
 */
public class TaskBaseN {
    
    // Kommandozeile
    private final CmdLine cmdLine;
    
    // Ein- und Ausgabeobjekt 
    private BaseNResource baseNFile;
    private DataResource binaryFile;
    
    /**
     * 
     */
    public TaskBaseN() {
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
    public TaskBaseN(CmdLine cmd) throws FileNotFoundException, IOException {
        if(cmd == null) {
            throw new IllegalArgumentException();
        }
        
        this.cmdLine = cmd;
        
        // Ausgabedatei Pfad ableiten
        String outPath = cmd.getOption(CmdLine.Options.INPUT_FILE);      
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

        System.out.println(outPath);
        
        // Resourcenobjekte erstellen
        if(cmd.isBaseNDecode()) {
            baseNFile = new BaseNResource(  cmd.getOption(CmdLine.Options.INPUT_FILE), 
                                            dataFormat);
            binaryFile = new DataResource(  outPath, 
                                            IOMode.BINARY);
        } else {
            baseNFile = new BaseNResource(  outPath, 
                                            dataFormat);
            binaryFile = new DataResource(  cmd.getOption(CmdLine.Options.INPUT_FILE), 
                                            IOMode.BINARY);
        }  
    }
    
    /**
     *
     * @return true, wenn IO gesetzt
     */
    public boolean isValid() {
      /*  return (    reader    != null
                &&  writer   != null);*/
      return true;
    }
    
    /**
     *
     * @return Statusstring
     */
    @Override
    public String toString() {
        String stateString = "";

        if(isValid()) {
        }
        
        return stateString;
    }
    
    /**
     * Aufgabe ausführen
     * 
     * @throws java.io.IOException 
     */
    public void doTask() throws IOException {
        if(!isValid()) {
            throw new IllegalStateException();
        }
        
        if(cmdLine.isBaseNDecode()) {
            
            // Alphabet laden?
            if(!baseNFile.getFormat().isValidAlphabet()) {
                baseNFile.getFormat().setEncoding(baseNFile.readAlphabet());
            }

            
            BaseNCodec decoder = new BaseNCodec(baseNFile, 
                                                baseNFile.getFormat());
            DataCodec encoder = new DataCodec(binaryFile, null);
                        
            DataBlock block = new DataBlock();
            block.data = ByteBuffer.allocate((int)baseNFile.length());
            baseNFile.read(block.data);
            
            decoder.begin(DataFormat.Operation.DECODE);
            decoder.processBlock(DataFormat.Operation.DECODE, block);
            decoder.end(DataFormat.Operation.DECODE);
            
            binaryFile.write(block.data);
        }
        
        baseNFile.close();
        binaryFile.close();
    }
}
