package propra.imageconverter.data.basen;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataCompression;
import propra.imageconverter.data.DataUtil;
import propra.imageconverter.data.IDataTarget;
import propra.imageconverter.data.IDataTarget.Event;

/**
 * Klasse für allgemeine Base-N Kodierung, die Parametrisierung erfolgt
 * über das per Konstruktor übergebene DataFormat Objekt. 
 *  
 */
public class BaseN extends DataCompression {
    
    // BaseN Kodierung 
    private final BaseNResource baseResource;
    private final BaseNEncoding baseEncoding;
    
    
    /**
     *
     * @param resource
     * @param format
     */
    public BaseN(BaseNResource resource) {
        super(resource);
        this.baseResource = resource;
        this.baseEncoding = resource.getBaseNEncoding();
    }

    /**
     * Dekodiert BaseNCodec kodierte Daten
     * 
     * @param target
     * @throws java.io.IOException
     */
    @Override
    public void decode(IDataTarget target) throws IOException {
        // Größe der binären Base-N Byteblöcke
        int blockLength = baseEncoding.getBlockLength();
        int charCtr = 0;
        
        // Daten lesen
        ByteBuffer data = ByteBuffer.allocate((int)resource.length());
        resource.getInputStream().read(data.array());

        // Ausgabepuffer erstellen
        ByteBuffer out = ByteBuffer.allocate((int)resource.length());
        
        // Zeichen iterieren und dekodieren
        while(charCtr < out.limit()) {
            
            // Anzahl der Zeichen pro Byte
            int charCount = baseEncoding.getCharLength();
            
            // Bei einem Endblock die Größe anpassen
            if(charCtr + blockLength >= out.limit()) {
                charCount = out.limit() - charCtr;
            }
            
            // Zeichen in Bitblöcke dekodieren
            decodeCharacters(data,charCtr, 
                            charCount, out);
            
            charCtr += charCount;
        }
                
        //  Daten an Listener senden
        target.onData(Event.DATA_DECODED,this,
                    out.flip(),false);
    }
    
    /**
     * Kodiert Binärdaten in BaseNCodec
     * 
     * @param inBuffer
     * @param last
     * @throws IOException 
     */
    @Override
    public void encode(ByteBuffer inBuffer, boolean last) throws IOException {
        if(inBuffer == null) {
            throw new IllegalArgumentException();
        }   
        
        ByteBuffer charBuffer = ByteBuffer.allocate(8);         

        // Anzahl der Ausgabezeichen ermitteln
        int inBitCount = inBuffer.limit() << 3;
        int characterCount = inBitCount / baseEncoding.getBitCount();
        int blockLength = baseEncoding.getBlockLength();
        
        // Bei einem Rest benötigen wir ein Zeichen mehr
        if(inBitCount % baseEncoding.getBitCount() != 0) {
            characterCount++;
        }

        /*
         * Über Bitblöcke iterieren und in Zeichen kodieren
         */ 
        int inOffset = 0;
        while(inOffset < inBuffer.limit()) {
            
            // Anzahl der Bytes per Kodierungsblock
            int byteCount = blockLength;
                    
            // Endblock?
            if(inOffset + blockLength > inBuffer.limit()) {
                byteCount = inBuffer.limit() - inOffset;
            }
            
            // Bitblöcke in Zeichen umwandeln
            encodeBits( inBuffer, inOffset, 
                        byteCount,charBuffer);

            // Kodierte Zeichen in Resource schreiben
            resource.getOutputStream()
                    .write(charBuffer);

            inOffset += byteCount;
        }
    }
    
    /**
     * 
     */
    private int decodeCharacters(   ByteBuffer in, 
                                    int inOffset, 
                                    int inLength, 
                                    ByteBuffer out) {
        
        byte[] bytes = in.array();
        byte[] alphabetMap = baseResource.getAlphabetMap();
        int bitLength = baseEncoding.getBitCount();
        long decodedValue = 0;
        int bitCount = 0;
        int c;
        
        // Zeichen iterieren und in Bitblock Dekodieren
        for(int i=0; i<inLength; i++) {
            
            // Bitpakete nach links schieben
            decodedValue <<= bitLength; 
            
            /*  
             *  Eingabezeichen mit Alphabet-Map dekodieren
             *  und zum Wert aufaddieren 
             */
            c = bytes[i + inOffset] & 0xFF;
            decodedValue += (byte)alphabetMap[(byte)c];
            
            bitCount += bitLength;
        }
        
        /*
         * Am Ende der Daten müssen ggfs. aufgefüllte Bits verworfen werden
         */
        int mod = (inLength * bitLength) % 8;
        if( mod != 0) {
            decodedValue >>= mod;
            bitCount -= mod;
        }
        
        // Bits in den Ausgabepuffer schreiben
        int byteCount = bitCount >> 3;
        out.put(DataUtil.longToBytes(decodedValue, byteCount), 
                0,byteCount);
        
        return byteCount;
    }
    
    /**
     *
     */
    private void encodeBits(ByteBuffer in, 
                            int inOffset, 
                            int inLen, 
                            ByteBuffer out) {
        
        byte[] alphabet = baseResource.getAlphabet().getBytes();
        int bitLength = baseEncoding.getBitCount();

        // Eingabe-Bytes zum maskieren in long umwandeln
        long value = DataUtil.bytesToLong(in.array(), 
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
     * @param buffer
     * @return 
     */
    public int encodedBufferLength(ByteBuffer buffer) {
        if( buffer == null) {
            throw new IllegalArgumentException();
        }
        
        int totalBits = buffer.limit() << 3;
        int len = totalBits / baseEncoding.getBitCount();

        // Aufzufüllende Bits berücksichtigen
        if(totalBits % baseEncoding.getBitCount() != 0) {
            len++;
        }

        return len;
    }
}
