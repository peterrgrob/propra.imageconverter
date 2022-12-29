package propra.imageconverter.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * @author pg
 */
public class BitInputStream {
    
    // Zugeordneter Stream von dem Bits gelesen werden
    private final InputStream inStream;
    
    // Aktuelles Lese-Byte
    private byte value;
    
    // Aktueller Bit-Index
    private byte bitIndex;
    
    /**
     * 
     * @param inStream 
     */
    public BitInputStream(InputStream inStream) {
        this.inStream = inStream;
        bitIndex = 8;
    }
    
    /**
     * Liest ein Bit von der Resource, gibt -1 zurück bei Dateiende
     */
    public int readBit() throws IOException {
        /*
         *  Wenn Bytegrenze überschritten, neues Byte einlesen. 
         */
        if(bitIndex > 7) {
            int rv = inStream.read();
            if(rv == -1) {
                return -1;
            }

            value = (byte)(rv & 0xFF);
            bitIndex = 0;
        }
        
        // Extrahiert und gibt den Bitwert zurück
        byte b = (byte)((value >>> (7 - bitIndex)) & 1);
        
        bitIndex++;
        return b;
    }
    
    /**
     * 
     */
    public int readBits(int bitLen) throws IOException {
        byte b = 0;
        
        // Iteriert die Bits von vorne nach hinten und konstruiert ein Byte
        for(int i=bitLen-1; i>=0; i--) {
            b |= readBit() << i;
        }
        
        return b;
    }
    
    /**
     * Liest ein Byte ausgehend von der aktuellen Bit Position ein 
     */
    public byte readByte() throws IOException {
        byte b = 0;
        
        // Iteriert die Bits von vorne nach hinten und konstruiert ein Byte
        for(int i=7; i>=0; i--) {
            b |= readBit() << i;
        }
        
        return b;
    }  
}
