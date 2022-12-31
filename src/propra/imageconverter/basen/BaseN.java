package propra.imageconverter.basen;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataTranscoder;
import propra.imageconverter.data.IDataTarget;
import propra.imageconverter.data.IDataTranscoder;
import propra.imageconverter.data.BitInputStream;
import propra.imageconverter.data.BitOutputStream;
import propra.imageconverter.data.CheckedInputStream;

/**
 * Transcoder Klasse für allgemeine Base-N Kodierung, die Parametrisierung erfolgt
 * über das per Konstruktor übergebene Ressourcen Objekt und dem Alphabet 
 */
public class BaseN extends DataTranscoder {

    // Aktuelle BaseN Resource  
    private final BaseNResource resource;
    
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

        private BaseNEncoding(int bitCount) {
            this.bitCount = bitCount;
        }

        public int getBitCount() {
            return bitCount;
        }  
    }
    
    public BaseN(BaseNResource resource) {
        this.resource = resource;
        this.baseEncoding = resource.getBaseNEncoding();
    }
    
    @Override
    public Compression getCompression() {
        return Compression.BASEN;
    }

    /**
     * Dekodiert BaseN kodierte Daten im Stream und sendet diese an target
     */
    @Override
    public void decode(CheckedInputStream in, IDataTarget target) throws IOException {
        
        // Ausgabepuffer als BitStream erstellen
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(getEncodedLength((int)resource.length()));
        BitOutputStream bitStream = new BitOutputStream(outStream);
        
        CheckedInputStream inStream = resource.getInputStream();
        int bitLen = baseEncoding.getBitCount();
        byte[] map = resource.getAlphabetMap();
        int c;
        
        // Zeichen iterieren und BitCodes in Puffer schreiben
        while((c = inStream.read()) != -1) {
            bitStream.writeBits(map[c & 0xFF], bitLen);
        }
                
        //  Daten an Listener senden
        ByteBuffer out = ByteBuffer.wrap(outStream.toByteArray());
        target.onData(out, true, this);
    }
    
    /**
     * Kodiert Binärdaten in inBuffer in den OutStream
     */
    @Override
    public void encode(ByteBuffer inBuffer, boolean last) throws IOException { 
        
        ByteArrayInputStream inStream = new ByteArrayInputStream(inBuffer.array());
        BitInputStream bitStream = new BitInputStream(inStream);
        
        int bitLen = baseEncoding.getBitCount();
        String alphabet = resource.getAlphabet();

        long blockCount = inBuffer.limit() / bitLen;
        long blockCtr = 0;
        
        while(blockCtr < blockCount) {
            int c = alphabet.charAt(bitStream.readBits(bitLen) & 0xFF);
            outStream.write(c);
            blockCtr++;
        }
    }        

    /**
     *  Gibt die Größe einer BaseN Kodierung zurück 
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
    
    @Override
    public void onData(ByteBuffer data, boolean lastBlock, IDataTranscoder caller) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}

