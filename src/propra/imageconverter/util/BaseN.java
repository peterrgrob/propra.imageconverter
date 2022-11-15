package propra.imageconverter.util;

import java.nio.ByteBuffer;

/**
 *
 * @author pg
 */
public class BaseN implements DataTranscoder {
    
    // Standard Base32 Alphabet
    public static String BASE_32_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUV";
    
    // Datenformat 
    DataFormat format;
    
    
    /**
     *
     * @param format
     * @param alphabet
     */
    public BaseN(DataFormat format) {
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
            return encode(in, out);
        } else if(op == Operation.ENCODE) {
            return 0;
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
     * @param in
     * @param out
     * @return 
     */
    private long encode(DataBuffer in, DataBuffer out) {
        
        // Index und Inkremente setzen
        int byteOffset = 0;
        int byteCount = 0;
        int blockLength = format.getBlockLength();
        int totalBits = in.getCurrDataLength() << 3;

        DataBuffer charBuffer = new DataBuffer(8);
        ByteBuffer inBuffer = in.getBuffer();
        ByteBuffer outBuffer = out.getBuffer();            

        // Anzahl der Ausgabezeichen ermitteln
        int totalCharacterCount = totalBits / format.getBitCount();
        if(totalBits % format.getBitCount() != 0) {
            totalCharacterCount++;
        }

        // Größe des Ausgabepuffer prüfen
        if(out.getSize() < totalCharacterCount) {
            
            // Puffer anpassen
            out = new DataBuffer(totalCharacterCount);
            outBuffer = out.getBuffer();
        }

        // Über Bitblöcke iterieren und kodieren
        while(byteOffset < in.getCurrDataLength()) {
            
            byteCount = blockLength;
                    
            // Endblock?
            if(byteOffset + blockLength > in.getCurrDataLength()) {
                byteCount = in.getCurrDataLength() - byteOffset;
            }
            
            // Bitblöcke in Zeichen umwandeln
            encodeBits( in, 
                        inBuffer.position(), 
                        byteCount, 
                        charBuffer);

            // In Ausgabe kopieren
            outBuffer.put(charBuffer.getBytes(), 
                        0, 
                        charBuffer.getCurrDataLength());

            byteOffset += byteCount;
        }
        
        return outBuffer.position();
    }
    
    /**
     *
     * @param data
     * @param bitLen
     */
    private void encodeBits(DataBuffer in, 
                            int inOffset, 
                            int inLen, 
                            DataBuffer out) {
        
        byte[] alphabet = format.getAlphabet().getBytes();
        int bitLength = format.getBitCount();

        // Eingabe-Bytes zum maskieren in long umwandeln
        long value = Utility.bytesToLong(in.getBytes(), 
                                        inOffset, 
                                        inLen);
        
        // Anzahl der Zeichen ermitteln
        int characterCount = (inLen << 3) / bitLength;
        
        // Falls Endbyte Nullen auffüllen
        int mod = (inLen << 3) % bitLength;
        if(mod != 0) {
            value <<= bitLength - mod;
            characterCount++;
        }
        
        // Bitblöcke iterieren
        for(int i=0; i<characterCount; i++) { 

            // Wert des aktuellen Bitblocks extrahieren
            int bitValue = (int)(value & ((1 << bitLength) - 1));
            
            // Wert per alphabet umwandeln und speichern
            out.getBuffer().put(characterCount - 1 - i, alphabet[bitValue]);
            
            // zum nächsten Bitblock schieben
            value = value >> bitLength;
        }
        
        // Puffer aktualisieren
        out.getBuffer().rewind();
        out.setCurrDataLength(characterCount);
    }
}
