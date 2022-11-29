package propra.imageconverter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.CmdLine.Options;
import propra.imageconverter.basen.BaseNCodec;
import propra.imageconverter.basen.BaseNFormat;
import propra.imageconverter.basen.BaseNResource;
import propra.imageconverter.data.DataBlock;
import propra.imageconverter.data.DataFormat;
import propra.imageconverter.data.DataFormat.IOMode;
import propra.imageconverter.data.DataResource;

/**
 * Klasse implementiert BaseN Programmfunktionalität
 * 
 * @author pg
 */
public class BaseNOperation implements AutoCloseable {
    
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
     * Konstruktor, initialisiert die Operation
     * 
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
        
        // BaseN Kodierung ableiten
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
            // Prüfen ob ein gültiges Alphabet übergeben wurde für BaseN
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
     */
    public void run() throws IOException {
        
        if(cmdLine.isBaseNDecode()) {
            /**
             *  Dekodiert BaseN Datei
             */
            
            // Alphabet aus Datei laden?
            if(!baseNFile.getFormat().isValidAlphabet()) {
                baseNFile.getFormat().setEncoding(baseNFile.readAlphabet());
            }

            // Decoder erstellen
            BaseNCodec decoder = new BaseNCodec(baseNFile, 
                                                baseNFile.getFormat());

            // Puffer für dekodierte Daten erstellen 
            DataBlock block = new DataBlock();
            block.data = ByteBuffer.allocate((int)baseNFile.length());
            
            // Datei in Puffer dekodieren
            decoder.begin(DataFormat.Operation.DECODE);
            decoder.decode(block, null);
            decoder.end();
            
            // Daten in Daei schreiben 
            binaryFile.write(block.data);
            
        } else {
            /**
             *  Kodiert eine Datei als BaseN Datei
             */
                        
            // Encoder erstellen
            BaseNCodec encoder = new BaseNCodec(baseNFile, 
                                                baseNFile.getFormat());
            
            // Puffer für kodierte Daten erstellen 
            DataBlock block = new DataBlock();
            block.data = ByteBuffer.allocate((int)binaryFile.length());
            
            // Daten von Datei lesen
            binaryFile.read(block.data);
            
            // Daten in Resource dekodieren
            encoder.begin(DataFormat.Operation.DECODE);
            encoder.encode(block);
            encoder.end();
        }
        
        baseNFile.close();
        binaryFile.close();
    }
    
    /**
     * Schließt geöffnete Resourcen, wird automatisch bei Verwendung mit 
     * try-with-resources aufgerufen
     */
    @Override
    public void close() throws Exception {
        if(baseNFile != null) {
            baseNFile.close();
        }
        if(binaryFile != null) {
            baseNFile.close();            
        }
    }
}
