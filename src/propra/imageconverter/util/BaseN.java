package propra.imageconverter.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author pg
 */
public class BaseN implements DataTranscoder {
    
    // Standard Base32 Alphabet
    public static String BASE_32_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUV";
    
    // Datenformat 
    DataFormat format;
    
    // Operation 
    Operation op = Operation.PASS;
    
    /**
     *
     * @param alphabet
     */
    public BaseN(DataFormat format, Operation op) {
        this.format = format;
        this.op = op;
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
            
            // Index und Inkremente setzen
            int byteOffset = 0;
            int byteCount = 0;
            int blockLength = format.getBlockLength();
            int totalBits = in.getCurrDataLength() << 3;
            
            // Temporäre Puffer erstellen
            DataBuffer blockBuffer = new DataBuffer(8);
            DataBuffer charBuffer = new DataBuffer(8);
            ByteBuffer inBuffer = in.getBuffer();
            ByteBuffer outBuffer = out.getBuffer();            
            
            // Anzahl der Ausgabezeichen ermitteln
            int characterCount = totalBits / format.getBitCount();
            if(totalBits % format.getBitCount() != 0) {
                characterCount++;
            }
            
            // Größe des Ausgabepuffer prüfen
            if(out.getSize() < characterCount) {
                // Puffer anpassen
                out = new DataBuffer(characterCount);
                outBuffer = out.getBuffer();
            }

            // Über Eingabebytes iterieren und Binärblöcke kodieren
            while(byteOffset < in.getCurrDataLength()) {
                
                // Blockgröße prüfen und anpassen
                byteCount = blockLength;
                if(byteOffset + byteCount > in.getBuffer().capacity()) {
                    byteCount -= byteOffset + byteCount - in.getBuffer().capacity();
                }
                
                // Bytes zur Verabeitung kopieren
                inBuffer.get(blockBuffer.getBytes(), 0, byteCount);
                blockBuffer.setCurrDataLength(byteCount);
                
                // Bitblöcke in Zeichen umwandeln
                encodeBitBlocks(blockBuffer, charBuffer);
                
                // Ausgabe kopieren
                outBuffer.put(charBuffer.getBytes(), 0, charBuffer.getCurrDataLength());
                
                byteOffset += byteCount;
            }
            
            return outBuffer.position();
        }
        
        return 0;
    }

    /**
     *
     */
    @Override
    public void end() {
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isValid() {
        return true;
    }
    
    /**
     *
     * @param data
     * @param bitLen
     */
    public void encodeBitBlocks(DataBuffer in, 
                                DataBuffer out) {
        
        byte[] alphabet = format.getAlphabet().getBytes();
        int bitLength = format.getBitCount();
        long mask = (1 << bitLength) - 1;

        // Bytes in long umwandeln
        long value = Utility.bytesToLong(in.getBytes(), in.getCurrDataLength());
        
        // Endblock behandeln, wenn nötig
        int characterCount = (in.getCurrDataLength() << 3) / bitLength;
        int mod = (in.getCurrDataLength() << 3) % bitLength;
        if(mod != 0) {
            value <<= bitLength - mod;
            characterCount++;
        }
        
        // Bitblöcke iterieren
        for(int i=0; i<characterCount; i++) { 

            // Wert extrahieren
            int bitValue = (int)(value & mask);
            
            // zum nächsten Bitblock schieben
            value = value >> bitLength;
            
            // Blockwert speichern
            out.getBuffer().put(characterCount - 1 - i, alphabet[bitValue]);
        }
        
        // Puffer aktualisieren
        out.getBuffer().rewind();
        out.setCurrDataLength(characterCount);
    }
}
