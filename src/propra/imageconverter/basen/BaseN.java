package propra.imageconverter.basen;

import java.nio.ByteBuffer;
import propra.imageconverter.data.DataFormat;
import propra.imageconverter.data.IDataTranscoder;

/**
 * Klasse für allgemeine Base-N Kodierung, die Parametrisierung erfolgt
 * über das per Konstruktor übergebene DataFormat Objekt. 
 *  
 * @author pg
 */
public class BaseN implements IDataTranscoder {
    
    // Standard Base32 Alphabet
    public static String BASE_32_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUV";
    
    // Datenformat 
    private final BaseNFormat format;
    
    
    /**
     *
     * @param format
     */
    public BaseN(BaseNFormat format) {
        this.format = format;
    }
    
    /**
     *
     */
    @Override
    public void begin() {

    }

    /**
     * Führt die gewünschte Operation durch
     * @param op
     * @param in
     * @param out
     * @return Anzahl der verarbeiteten Bytes
     */
    @Override
    public long apply(Operation op, ByteBuffer in, ByteBuffer out) {
        if( !isValid()
        ||  in == null
        ||  out == null) {
            throw new IllegalArgumentException();
        }
        
        // Operation ausführen 
        if(op == Operation.ENCODE) {
            return encode(in, out);
        } else if(op == Operation.DECODE) {
            return decode(in, out);
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
     * @return
     */
    public boolean isValid() {
        return true;
    }
    
    /**
     * @param op
     * @param buffer
     * @return Gibt die vorraussichtliche Datenmenge einer Operation zurück
     */
    @Override
    public int transcodedBufferLength(Operation op, ByteBuffer buffer) {
        if( buffer == null 
        ||  !isValid()) {
            throw new IllegalArgumentException();
        }
        
        if(op == Operation.ENCODE) {
            
            int totalBits = buffer.limit() << 3;
            int len = totalBits / format.getBitCount();
            
            // Aufzufüllende Bits berücksichtigen
            if(totalBits % format.getBitCount() != 0) {
                len++;
            }
            
            return len;
        } else {
            return buffer.limit();
        }
            
    }
    
    /**
     * Dekodiert BaseN kodierte Daten
     * 
     * @param in
     * @param out
     * @return Datenmenge
     */
    private long decode(ByteBuffer in, ByteBuffer out) {
        
        int charCtr = 0;
        int byteCtr = 0;
        
        // Größe der binären Base-N Byteblöcke
        int blockLength = format.getBlockLength();
        
        // Zeichen iterieren und dekodieren
        while(charCtr < in.limit()) {
            
            // Anzahl der Zeichen pro Byteblock
            int charCount = format.getCharLength();
            
            // Bei einem Endblock die Größe anpassen
            if(charCtr + blockLength >= in.limit()) {
                charCount = in.limit() - charCtr;
            }
            
            // Zeichen in Bitblöcke dekodieren
            byteCtr += decodeCharacters(in, 
                                    charCtr, 
                                    charCount, 
                                    out);
            
            charCtr += charCount;
        }
        
        // Ausgabepuffer setzen
        out.rewind();
        out.limit(byteCtr);
        
        return byteCtr;
    }
    
    /**
     * Kodiert Binärdaten in BaseN
     * @param in
     * @param out
     * @return Datenmenge
     */
    private long encode(ByteBuffer in, ByteBuffer out) {
        
        // Index und Inkremente setzen
        int byteOffset = 0;
        int blockLength = format.getBlockLength();
        int totalBits = in.limit() << 3;

        ByteBuffer charBuffer = ByteBuffer.allocate(8);         

        // Anzahl der Ausgabezeichen ermitteln
        int totalCharacterCount = totalBits / format.getBitCount();
        if(totalBits % format.getBitCount() != 0) {
            totalCharacterCount++;
        }

        // Größe des Ausgabepuffer prüfen
        if(out.capacity() < totalCharacterCount) {
            throw new IllegalStateException("Puffergröße ungültig!");
        }

        // Über Bitblöcke iterieren und kodieren
        while(byteOffset < in.limit()) {
            
            int byteCount = blockLength;
                    
            // Endblock?
            if(byteOffset + blockLength > in.limit()) {
                byteCount = in.limit() - byteOffset;
            }
            
            // Bitblöcke in Zeichen umwandeln
            encodeBits( in, 
                        byteOffset, 
                        byteCount, 
                        charBuffer);

            // In Ausgabe kopieren
            out.put(charBuffer.array(), 
                    0, 
                    charBuffer.limit());

            byteOffset += byteCount;
        }
        
        out.limit(out.position());
        out.rewind();

        return out.limit();
    }
    
    /**
     * 
     * @param in
     * @param inOffset
     * @param inLength
     * @param out
     * @return 
     */
    private int decodeCharacters(   ByteBuffer in, 
                                    int inOffset, 
                                    int inLength, 
                                    ByteBuffer out) {
        
        long value = 0;
        int bitCount = 0;
        
        byte[] bytes = in.array();
        byte[] alphabetMap = format.getAlphabetMap();
        int bitLength = format.getBitCount();
        
        // Zeichen iterieren und Dekodieren
        for(int i=0; i<inLength; i++) {
            
            // Bitpakete nach links schieben
            value <<= bitLength; 
            
            // Eingabezeichen mit Alphabet-Map dekodieren
            // und zum Wert aufaddieren
            int v = bytes[i + inOffset];
            value += (byte)alphabetMap[(byte)v];
            
            bitCount += bitLength;
        }
        
        // Endpaket?
        int modulo = (inLength * bitLength) % 8;
        if( modulo != 0) {
            // Überschüssige Bits wieder verwerfen 
            value >>= modulo;
            bitCount -= modulo;
        }
        
        // Bitblöcke in den Ausgabepuffer schreiben
        int byteCount = bitCount >> 3;
        out.put(DataFormat.longToBytes(value, byteCount), 
                0, 
                byteCount);
        
        return byteCount;
    }
    
    /**
     *
     * @param data
     * @param bitLen
     */
    private void encodeBits(ByteBuffer in, 
                            int inOffset, 
                            int inLen, 
                            ByteBuffer out) {
        
        byte[] alphabet = format.getAlphabet().getBytes();
        int bitLength = format.getBitCount();

        // Eingabe-Bytes zum maskieren in long umwandeln
        long value = DataFormat.bytesToLong(in.array(), 
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
            out.put(characterCount - 1 - i, alphabet[bitValue]);
            
            // zum nächsten Bitblock schieben
            value = value >> bitLength;
        }
        
        // Puffer aktualisieren
        out.rewind();
        out.limit(characterCount);
    }
    
    /**
     *
     * @return Aktuelles Daten Format
     */
    public BaseNFormat dataFormat() {
        return format;
    }
}
