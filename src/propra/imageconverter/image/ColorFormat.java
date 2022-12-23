package propra.imageconverter.image;

import java.nio.ByteBuffer;
import propra.imageconverter.data.DataFormat;

/**
 * FunctionalInterfaces für Farbkonvertierungsfunktionen
 * @author pg
 */
@FunctionalInterface
interface ColorFilter {
    void apply(byte[] in, int offset1,
                 byte[] out, int offset2);
}

/**
 *  Farbformat der Resource
 */
public class ColorFormat extends DataFormat 
                         implements Comparable<ColorFormat> {
    
    // Farbreihenfolge
    public enum ColorOrder {
        ORDER_BGR,
        ORDER_RBG,
    }
       
    // Pixelgröße in Bytes
    public static int PIXEL_SIZE = 3;
    
    // Reihenfolge
    private ColorOrder order;
    
    /**
     * 
     */
    public ColorFormat() {
        order = ColorOrder.ORDER_BGR;
    }
    
    /**
     * 
     * @param order 
     */
    public ColorFormat(ColorOrder order) {
        this.order = order;
    }
    
    /**
     * 
     * @param src
     * @param src
     */
    public ColorFormat(ColorFormat src) {
        super(src);
        this.order = src.order;
    }

    /**
     *
     * @param o
     * @return 
     */
    @Override
    public int compareTo(ColorFormat o) {
        if(this.order == o.order) {
            return 0;
        }
        return -1;
    }
    
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
     * 
     * @return 
     */
    public ColorOrder getOrder() {
        return order;
    }

    /**
     * 
     * @param order 
     */
    public void setOrder(ColorOrder order) {
        this.order = order;
    }
}
