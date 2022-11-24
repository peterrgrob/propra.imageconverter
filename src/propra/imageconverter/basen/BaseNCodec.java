package propra.imageconverter.basen;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataBlock;
import propra.imageconverter.data.DataCodec;
import propra.imageconverter.data.DataFormat;
import propra.imageconverter.data.IDataResource;

/**
 * Klasse für allgemeine Base-N Kodierung, die Parametrisierung erfolgt
 * über das per Konstruktor übergebene DataFormat Objekt. 
 *  
 * @author pg
 */
public class BaseNCodec extends DataCodec {
    
    // Standard Base32 Alphabet
    public static String BASE_32_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUV";
    
    // Datenformat 
    private final BaseNFormat format;
    
    
    /**
     *
     * @param resource
     * @param format
     */
    public BaseNCodec(  IDataResource resource,
                        BaseNFormat format) {
        super(resource, null);
        this.format = format;
    }

    /**
     * @return
     */
    public boolean isValid() {
        return format != null;
    }
    
    /**
     * 
     * @param op
     * @param block
     * @throws IOException 
     */
    @Override
    public void processBlock(   DataFormat.Operation op, 
                                DataBlock block) throws IOException {
        if(!isValid()
        ||  block == null) {
            throw new IllegalArgumentException();
        }
        
        if(op == DataFormat.Operation.DECODE) {
            decode(block);
        } else if(op == DataFormat.Operation.ENCODE) {
            encode(block);
        }
    }
    
    /**
     * @param buffer
     * @return Gibt die Datenmenge nach Kodierung zurück
     */
    public int encodedBufferLength(ByteBuffer buffer) {
        if( buffer == null 
        ||  !isValid()) {
            throw new IllegalArgumentException();
        }
        
        int totalBits = buffer.limit() << 3;
        int len = totalBits / format.getBitCount();

        // Aufzufüllende Bits berücksichtigen
        if(totalBits % format.getBitCount() != 0) {
            len++;
        }

        return len;
    }
    
    /**
     * Dekodiert BaseNCodec kodierte Daten
     * 
     * @param block
     * @throws java.io.IOException
     */
    public void decode(DataBlock block) throws IOException {
                
        // Größe der binären Base-N Byteblöcke
        int blockLength = format.getBlockLength();
        
        ByteBuffer out = block.data;
        ByteBuffer inData = ByteBuffer.allocate(encodedBufferLength(out));
        ByteBuffer tData = ByteBuffer.allocate(blockLength);
        
        // Daten einlesen
        resource.read(inData);
        
        int charCtr = 0;
        
        // Zeichen iterieren und dekodieren
        while(charCtr < inData.limit()) {
            
            // Anzahl der Zeichen pro Byteblock
            int charCount = format.getCharLength();
            
            // Bei einem Endblock die Größe anpassen
            if(charCtr + blockLength >= inData.limit()) {
                charCount = inData.limit() - charCtr;
            }
            
            // Zeichen in Bitblöcke dekodieren
            decodeCharacters(inData, 
                            charCtr, 
                            charCount, 
                            tData);
            
            tData.flip();
            out.put(tData);
            tData.clear();
            
            charCtr += charCount;
        }
    }
    
    /**
     * Kodiert Binärdaten in BaseNCodec
     * @param in
     */
    public void encode( DataBlock block) throws IOException {
        
        ByteBuffer in = block.data;
        
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
            resource.write(charBuffer);

            byteOffset += byteCount;
        }
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
