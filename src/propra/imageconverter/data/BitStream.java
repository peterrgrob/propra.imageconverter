package propra.imageconverter.data;

import java.io.IOException;

/**
 *
 */
public class BitStream {
    
    // Zugeordneter Stream von dem Bits gelesen werden
    private final IStreamable inStream;
    
    // Aktuelles Byte
    private byte value;
    
    // Aktueller Bit-Index
    private byte bitIndex;
    
    /**
     *  Konstruktor
     */
    public BitStream(IStreamable inStream) {
        this.inStream = inStream;
        bitIndex = 8;
    }
    
    /**
     *  Liest ein Bit von der Resource, gibt -1 zurück bei Dateiende
     *  Speichert gelesene Bytes in bytes
     */
    public int readBit() throws IOException {
        /*
         *  Wenn Bytegrenze überschritten, neues Byte einlesen. 
         */
        if(bitIndex > 7) {
            int rv = inStream.readByte();
            if(rv == -1) {
                return -1;
            }
            
            // Speichern
            value = (byte)(rv & 0xFF);
            bitIndex = 0;
        }
        
        // Extrahiert und gibt den Bitwert zurück
        byte b = (byte)((value >>> (7 - bitIndex)) & 1);
        
        bitIndex++;
        return b;
    }
    
    /**
     *  Liest ein Byte ausgehend von der aktuellen Bit Position ein 
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
