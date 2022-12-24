package propra.imageconverter.data.basen;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataCompression;
import propra.imageconverter.data.IDataTarget;
import propra.imageconverter.data.IDataTarget.Event;
import propra.imageconverter.util.BitInputStream;
import propra.imageconverter.util.BitOutputStream;
import propra.imageconverter.util.CheckedInputStream;
import propra.imageconverter.util.CheckedOutputStream;

/**
 * Klasse für allgemeine Base-N Kodierung, die Parametrisierung erfolgt
 * über das per Konstruktor übergebene Ressourcen Objekt. 
 *  
 */
public class BaseN extends DataCompression {

    // Aktuelle BaseN Resource  
    private final BaseNResource baseResource;
    
    // Aktuelle BaseN Kodierung
    private final BaseNEncoding baseEncoding;

    /** 
    *  Kodierungstypen der Daten mit parametrisierten
    *  Einstellungen für die Base-N Kodierung, 
    */
    public enum BaseNEncoding {
        NONE(0),
        BASE_2(1),
        BASE_4(2),
        BASE_8(3),
        BASE_16(4),       
        BASE_32(5),
        BASE_64(6),
        BASE_DEFAULT_32(5);
                
        // Parameter für Base-N Kodierungen
        private final int bitCount;
        
        // Standard Base32 Alphabet
        public static String BASE_32_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUV";

        /**
         * 
         * @param bitCount
         */
        private BaseNEncoding(int bitCount) {
            this.bitCount = bitCount;
        }

        public int getBitCount() {
            return bitCount;
        }  
    }
    
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
        
        // Ausgabepuffer als BitStream erstellen
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(getEncodedLength((int)resource.length()));
        BitOutputStream bitStream = new BitOutputStream(outStream);
        
        CheckedInputStream inStream = resource.getInputStream();
        int bitLen = baseEncoding.getBitCount();
        byte[] map = baseResource.getAlphabetMap();
        int c;
        
        // Zeichen iterieren und BitCodes in Puffer schreiben
        while((c = inStream.read()) != -1) {
            bitStream.writeBits(map[c & 0xFF], bitLen);
        }
                
        //  Daten an Listener senden
        bitStream.flushByte();
        ByteBuffer out = ByteBuffer.wrap(outStream.toByteArray());
        target.onData(Event.DATA_DECODED, this, out, true);
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
        
        ByteArrayInputStream inStream = new ByteArrayInputStream(inBuffer.array());
        BitInputStream bitStream = new BitInputStream(inStream);
        CheckedOutputStream outStream = resource.getOutputStream();
        
        int bitLen = baseEncoding.getBitCount();
        String alphabet = baseResource.getAlphabet();

        long blockCount = inBuffer.limit() / bitLen;
        long blockCtr = 0;
        
        while(blockCtr < blockCount) {
            int c = alphabet.charAt(bitStream.readBits(bitLen) & 0xFF);
            outStream.write(c);
            blockCtr++;
        }
    }        

    /**
     * @param buffer
     * @return 
     */
    private int getEncodedLength(int size) {
        int totalBits = size << 3;
        int len = totalBits / baseEncoding.getBitCount();

        // Aufzufüllende Bits berücksichtigen
        if(totalBits % baseEncoding.getBitCount() != 0) {
            len++;
        }

        return len;
    }
}

