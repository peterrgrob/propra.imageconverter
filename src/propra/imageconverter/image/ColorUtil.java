package propra.imageconverter.image;

import java.nio.ByteBuffer;


/**
 * Diverse statische Utility Methoden zur Bildverarbeitung, zT verwendbar als 
 * statische Methoden Referenz für ColorConverter
 */
public class ColorUtil {
   /**
    * FunctionalInterface für Farboperationen
    */
   @FunctionalInterface
   interface ColorOp {
       void apply( byte[] in, int offset1, byte[] out, int offset2);
   }
    
    /**
     * BGR -> RBG
     */
    static public void convertBGRtoRBG(byte[] in, int offset1, byte[] out, int offset2) {
        byte b = in[offset1];
        byte g = in[offset1 + 1];
        
        out[offset2] = in[offset1 + 2];
        out[offset2 + 1] = b;
        out[offset2 + 2] = g;
    }
 
    /**
     * RBG -> BGR
     */
    static public void convertRBGtoBGR(byte[] in, int offset1, byte[] out, int offset2) {
        byte r = in[offset1];

        out[offset2] = in[offset1 + 1];
        out[offset2 + 1] = in[offset1 + 2];
        out[offset2 + 2] = r;
    }
   
    /**
     * Wendet eine ColorOperation auf den ByteBuffer an
     */
    static void filterColorBuffer(ByteBuffer in, ByteBuffer out, ColorOp filter) {
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
     */
    static public boolean compareColor(byte[] array, int offset0, int offset1) {
        return (array[offset0 + 0] == array[offset1 + 0]
            &&  array[offset0 + 1] == array[offset1 + 1]
            &&  array[offset0 + 2] == array[offset1 + 2]);
    }
    
    /**
     * Füllt Buffer len-mal mit Farbwert und gibt neuen Offset zurück
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
}

