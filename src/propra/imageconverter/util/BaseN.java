package propra.imageconverter.util;

/**
 *
 * @author pg
 */
public class BaseN implements DataTranscoder {
    
    // BaseN Alphabet
    private String alphabet;
    
    // Binärblockgröße in Bit
    private int binaryBlockSize;
    
    // Datenformat 
    DataFormat format;
    
    /**
     *
     * @param alphabet
     */
    public BaseN(DataFormat format, String alphabet) {
        alphabet = new String(alphabet);
        this.format = format;
        

    }
    
    /**
     *
     * @param format
     */
    @Override
    public void begin() {

    }

    @Override
    public long transcode(Operation op, DataBuffer in, DataBuffer out) {
        if( !isValid()
        ||  in == null
        ||  out == null) {
            throw new IllegalArgumentException();
        }
        
        // Operation ausführen 
        if(op == Operation.ENCODE) {
            
            // Anzahl der Ausgabezeichen ermitteln
            int outLen = (in.getCurrDataLength() << 8) / binaryBlockSize;
                    
            // Index und Inkremente setzen
            int byteIndex = 0;
            int bitIndex = 0;
            int bitIncrement = 8 % binaryBlockSize;
            int byteIncrement = 8 / binaryBlockSize;
            
            // Größe des Ausgabepuffer prüfen
            if(out.getSize() < outLen) {
                
                // Puffer anpassen
                out = new DataBuffer(outLen);
            }

            // Über Eingabebytes iterieren und Blöcke kodieren
            while(byteIndex < in.getCurrDataLength()) {
                
                // Block extrahieren
                int value = (int)Utility.extractBits(in.getBuffer(), 
                                                    byteIndex,
                                                    bitIndex,
                                                    binaryBlockSize);
                
                // Mit Alphabet kodieren und in Ausgabepuffer speichern
                out.getBuffer().put(alphabet.getBytes()[value]);
                
                // Position aktualisieren
                if(bitIncrement > 0) {   
                    // Base-N mit N < 8 
                    bitIndex += bitIncrement;
                    if(bitIndex > 8) {
                        byteIndex++;
                        bitIndex = 0;
                    }
                } else {
                    // Base-N mit N >= 8 
                    byteIndex += byteIncrement;
                }
            }
        }
        
        return 0;
    }

    @Override
    public void end() {
    }

    @Override
    public boolean isValid() {
        return (    alphabet.length() > 0
                &&  binaryBlockSize > 0);
    }
}
