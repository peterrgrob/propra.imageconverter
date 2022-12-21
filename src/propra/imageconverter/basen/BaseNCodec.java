package propra.imageconverter.basen;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataCodec;
import propra.imageconverter.data.DataFormat;
import propra.imageconverter.data.IDataResource;
import propra.imageconverter.data.IDataTarget;
import propra.imageconverter.data.IDataTarget.Event;

/**
 * Klasse für allgemeine Base-N Kodierung, die Parametrisierung erfolgt
 * über das per Konstruktor übergebene DataFormat Objekt. 
 *  
 */
public class BaseNCodec extends DataCodec {
    
    // BaseN Kodierung 
    private final BaseNFormat format;
    
    
    /**
     *
     * @param resource
     * @param format
     */
    public BaseNCodec(  IDataResource resource,
                        BaseNFormat format) {
        super(resource);
        this.format = format;
    }

    /**
     * 
     */
    @Override
    public boolean isValid() {
        return format != null;
    }
    
    /**
     * Dekodiert BaseNCodec kodierte Daten
     * 
     */
    @Override
    public void decode(IDataTarget target) throws IOException {
        if(!isValid()) {
            throw new IllegalArgumentException();
        }   
        
        // Größe der binären Base-N Byteblöcke
        int blockLength = format.getBlockLength();
        int charCtr = 0;
        
        // Daten lesen
        ByteBuffer data = ByteBuffer.allocate((int)resource.length());
        resource.getInputStream().read(data.array());

        // Ausgabepuffer erstellen
        ByteBuffer out = ByteBuffer.allocate((int)resource.length());
        
        // Zeichen iterieren und dekodieren
        while(charCtr < out.limit()) {
            
            // Anzahl der Zeichen pro Byte
            int charCount = format.getCharLength();
            
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
        target.onData(Event.DATA_BLOCK_DECODED,this,
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
    public void encode( ByteBuffer inBuffer, 
                        boolean last) throws IOException {
        if(!isValid()
        ||  inBuffer == null) {
            throw new IllegalArgumentException();
        }   
        
        ByteBuffer charBuffer = ByteBuffer.allocate(8);         

        // Anzahl der Ausgabezeichen ermitteln
        int inBitCount = inBuffer.limit() << 3;
        int characterCount = inBitCount / format.getBitCount();
        int blockLength = format.getBlockLength();
        
        // Bei einem Rest benötigen wir ein Zeichen mehr
        if(inBitCount % format.getBitCount() != 0) {
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
        byte[] alphabetMap = format.getAlphabetMap();
        int bitLength = format.getBitCount();
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
        out.put(DataFormat.longToBytes(decodedValue, byteCount), 
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
     *
     * @return Aktuelles Daten Format
     */
    public BaseNFormat dataFormat() {
        return format;
    }
}
