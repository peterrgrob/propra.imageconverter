package propra.imageconverter.basen;

import java.nio.ByteBuffer;
import propra.imageconverter.data.DataBuffer;
import propra.imageconverter.data.DataFormat;
import propra.imageconverter.data.DataTranscoder;
import propra.imageconverter.data.Utility;

/**
 * Klasse für allgemeine Base-N Kodierung, die Parametrisierung erfolgt
 * über das per Konstruktor übergebene DataFormat Objekt. 
 *  
 * @author pg
 */
public class BaseN implements DataTranscoder {
    
    // Standard Base32 Alphabet
    public static String BASE_32_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUV";
    
    // Datenformat 
    private final DataFormat format;
    
    
    /**
     *
     * @param format
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
    public long apply(Operation op, DataBuffer in, DataBuffer out) {
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
     *
     * @return
     */
    public boolean isValid() {
        return true;
    }
    
    /**
     * 
     * @param in
     * @param out
     * @return 
     */
    private long decode(DataBuffer in, DataBuffer out) {
        
        int charCtr = 0;
        int charCount = 0;
        int byteCtr = 0;
        
        // Größe der binären Base-N Byteblöcke
        int blockLength = format.getBlockLength();
        
        // Zeichen iterieren und dekodieren
        while(charCtr < in.getDataLength()) {
            
            // Anzahl der Zeichen pro Byteblock
            charCount = format.getCharLength();
            
            // Bei einem Endblock die Größe anpassen
            if(charCtr + blockLength >= in.getDataLength()) {
                charCount = in.getDataLength() - charCtr;
            }
            
            // Zeichen in Bitblöcke dekodieren
            byteCtr += decodeCharacters(in, 
                                    charCtr, 
                                    charCount, 
                                    out);
            
            charCtr += charCount;
        }
        
        // Ausgabepuffer setzen
        out.getBuffer().rewind();
        out.setDataLength(byteCtr);
        
        return byteCtr;
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
        int totalBits = in.getDataLength() << 3;

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
            out.create(totalCharacterCount);
            outBuffer = out.getBuffer();
        }

        // Über Bitblöcke iterieren und kodieren
        while(byteOffset < in.getDataLength()) {
            
            byteCount = blockLength;
                    
            // Endblock?
            if(byteOffset + blockLength > in.getDataLength()) {
                byteCount = in.getDataLength() - byteOffset;
            }
            
            // Bitblöcke in Zeichen umwandeln
            encodeBits( in, 
                        byteOffset, 
                        byteCount, 
                        charBuffer);

            // In Ausgabe kopieren
            outBuffer.put(charBuffer.getBytes(), 
                        0, 
                        charBuffer.getDataLength());

            byteOffset += byteCount;
        }
        
        out.setDataLength(outBuffer.position());
        outBuffer.rewind();

        return outBuffer.position();
    }
    
    /**
     * 
     * @param in
     * @param inOffset
     * @param inLength
     * @param out
     * @return 
     */
    private int decodeCharacters(   DataBuffer in, 
                                    int inOffset, 
                                    int inLength, 
                                    DataBuffer out) {
        
        long value = 0;
        int bitCount = 0;
        
        byte[] bytes = in.getBytes();
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
        out.getBuffer().put(Utility.longToBytes(value, byteCount), 
                            0, 
                            byteCount);
        
        return byteCount;
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
        out.setDataLength(characterCount);
    }
    
    /**
     * 
     * @param dataFormat 
     */
    public void dataFormat(DataFormat dataFormat) {
        
    }

    /**
     *
     * @return
     */
    @Override
    public DataFormat dataFormat() {
        return format;
    }
}
