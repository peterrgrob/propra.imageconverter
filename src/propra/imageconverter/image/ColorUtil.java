package propra.imageconverter.image;

import java.nio.ByteBuffer;

/**
 * FunctionalInterface für Farbkonvertierungsfunktionen
 * @author pg
 */
@FunctionalInterface
interface ColorFilter {
    void apply(byte[] in, int offset1,
                 byte[] out, int offset2);
}

/**
 * Diverse Utility Methoden zur Bildverarbeitung
 * @author pg
 */
public class ColorUtil {
    /**
     * 
     * @param in
     * @param out 
     */
    static public void convertBGRtoRBG( byte[] in, int offset1,
                                        byte[] out, int offset2) {
        byte b = in[offset1];
        byte g = in[offset1 + 1];
        
        out[offset2] = in[offset1 + 2];
        out[offset2 + 1] = b;
        out[offset2 + 2] = g;
    }
    
    /**
     * 
     * @param in
     * @param out 
     */
    static public void convertRBGtoBGR( byte[] in, int offset1,
                                        byte[] out, int offset2) {
        byte r = in[offset1];

        out[offset2] = in[offset1 + 1];
        out[offset2 + 1] = in[offset1 + 2];
        out[offset2 + 2] = r;
    }
   
    /**
     * 
     * @param in
     * @param out 
     */
    static void filterColorBuffer(ByteBuffer in, ByteBuffer out, ColorFilter filter) {
        byte[] inBytes = in.array();
        byte[] outBytes = out.array();
                
        int srcOffset = 0;
        int dstOffset = 0;
        
        for (int i=0; i<in.limit(); i+=3) {
            int sIndex = srcOffset + i;
            int dIndex = dstOffset + i;
            
            filter.apply(inBytes, sIndex, outBytes, dIndex);
        }
    }
    
     /**
     * Vergleicht zwei Pixel in einem Byte-Array
     * @param array
     * @param offset0
     * @param offset1
     * @return 
     */
    static public boolean compareColor(byte[] array, int offset0, int offset1) {
        return (array[offset0 + 0] == array[offset1 + 0]
            &&  array[offset0 + 1] == array[offset1 + 1]
            &&  array[offset0 + 2] == array[offset1 + 2]);
    }
    
    /**
     * Füllt Buffer len-mal mit Farbwert und gibt neuen Offset zurück
     * 
     * @param buffer
     * @param color
     * @param len
     * @return 
     */
    public static int fill(ByteBuffer buffer, Color color, int len) {
        byte[] b = buffer.array();
        byte[] bs = color.values;
        int offs = buffer.position();
        int co = color.getIndex();
        
        for(int i=0; i<len; i++){
            b[offs++] = bs[co];
            b[offs++] = bs[co + 1];
            b[offs++] = bs[co + 2];            
        }
        buffer.position(buffer.position() + len * Color.PIXEL_SIZE);
        return buffer.position();
    }
    
    /**
     * Vergleicht zwei Farbwerte in Array
     * 
     * @param c1
     * @param index1
     * @param c2
     * @param index2
     * @return 
     */
    public static boolean compareColor( byte[] c1, int index1,
                                        byte[] c2, int index2) {
        return (c1[index1 + 0] == c2[index2 + 0]
            &&  c1[index1 + 1] == c2[index2 + 1]
            &&  c1[index1 + 2] == c2[index2 + 2]);
    }
}
