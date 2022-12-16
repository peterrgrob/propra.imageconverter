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
        value = 8;
        bitIndex = 0;
    }
    
    /**
     *  Schreibt ein Bit in den Stream
     */
    public void write(int bit) {
        if(bitIndex > 7) {
            
        }
    }
    
    /**
     *  Schreibt einen BitCode in den Stream
     */
    public void write(BitCode code) {
        
        int c = code.getCode();
    }
}
