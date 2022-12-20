package propra.imageconverter.image;

import java.nio.ByteBuffer;
import propra.imageconverter.data.DataFormat;

/**
 * 
 *  Repräsentiert ein Farbformat mit 3 Bytes. 
 *  Die Reihenfolge der Komponenten ist durch ein Mapping 
 *  konfigurierbar, die Standardreihenfolge ist Little-Endian.
 * 
 */
public class ColorFormat extends DataFormat 
                        implements Comparable<ColorFormat> {
    
    // Konstanten zur Indizierung von Farbkomponenten (Little Endian)
    public static final int BLUE = 0;
    public static final int GREEN = 1;
    public static final int RED = 2;
    
    // Standard RGB Format
    public static final ColorFormat FORMAT_RGB = new ColorFormat(RED, GREEN, BLUE);
       
    // Pixelgröße in Bytes
    public static int PIXEL_SIZE = 3;
    
    // Bildet Indizes der Farkomponenten ab
    protected int[] mapping = new int[3];

    /**
     * 
     */
    public ColorFormat() {
        /*
         * Standard RGB Farben (Little Endian)
         */
        setMapping(2,1,0);
    }
    
    /**
     * 
     */
    public ColorFormat(int r, int g, int b) {
        setMapping(r, g, b);
    }
    
    /**
     * 
     */
    public ColorFormat(ColorFormat src) {
        super(src);
        setMapping(src.mapping);
    }
    
    /**
     *
     */
    @Override
    public int compareTo(ColorFormat o) {
        if( mapping[0] == o.getMapping(0) &&
            mapping[1] == o.getMapping(1) &&
            mapping[2] == o.getMapping(2)) {
            return 0;
        }
        return -1;
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
     *  Getter/Setter 
     */
    public void setMapping(int r, int g, int b) {
        setMapping(RED, r);
        setMapping(GREEN,g);
        setMapping(BLUE, b);
    }
    
    public void setMapping(int id, int newId) {
        mapping[id] = newId;
    }
    public void setMapping(int[] map) {
        System.arraycopy(map, 0, 
                        this.mapping,0, 
                        map.length);
    }
    public int getMapping(int id) {
        return mapping[id];
    }  
    public int[] getMapping() {
        return mapping;
    }
}
