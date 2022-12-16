package propra.imageconverter.data;

import java.io.IOException;

/**
 * 
 */
public class BitOutputStream {
    
    // Zugeordneter Stream von dem Bits gelesen werden
    private final DataOutputStream stream;
    
    // Aktuelles Schreib-Byte
    private byte value;
    
    // Aktueller Bit-Index
    private byte bitIndex;
    
    /**
     *  Konstruktor
     */
    public BitOutputStream(DataOutputStream stream) {
        this.stream = stream;
        value = 0;
        bitIndex = 0;
    }
    
    /**
     *  Schreibt ein Bit in den Stream
     */
    public void write(int bit) throws IOException {
        
        value |= (bit & 1) << (7 - bitIndex);
        
        if(bitIndex >= 7) {
            stream.write(value);
            value = 0;
            bitIndex = 0;
        }
        
        bitIndex++;
    }
    
    /**
     *  Schreibt einen BitCode in den Stream
     */
    public void write(BitCode code) throws IOException {  
        int c = code.getCode();
        
        for(int i=code.getLength()-1; i>=0; i--) {
            write((c >>> i) & 1);
        }
    }
}
