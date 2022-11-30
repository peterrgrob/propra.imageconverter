package propra.imageconverter.image;

import java.nio.ByteBuffer;
import propra.imageconverter.data.DataFormat;
import propra.imageconverter.data.IDataCodec;

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
     * 
     */
    public static ByteBuffer convertColorBuffer(ByteBuffer input, ColorFormat srcFormat,
                                                ByteBuffer output,ColorFormat dstFormat) {
        if (input == null
        ||  output == null
        ||  srcFormat == null
        ||  dstFormat == null) {
            throw new IllegalArgumentException();
        }  
        
        byte[] inBytes = input.array();
        byte[] outBytes = output.array();
        byte r,g,b;
                
        int srcOffset = 0;
        int dstOffset = 0;
        
        int[] srcMap = srcFormat.getMapping();
        int[] dstMap = dstFormat.getMapping();
        
        for (int i=0; i<input.limit(); i+=3) {
            int sIndex = srcOffset + i;
            int dIndex = dstOffset + i;
            
            r = inBytes[sIndex + srcMap[RED]];
            g = inBytes[sIndex + srcMap[GREEN]];
            b = inBytes[sIndex + srcMap[BLUE]];
            
            outBytes[dIndex + dstMap[RED]] = r;
            outBytes[dIndex + dstMap[GREEN]] = g;
            outBytes[dIndex + dstMap[BLUE]] = b;
        }
        
        return output;
    }
    
    /**
     *  Füllt Buffer len-mal mit Farbwert und gibt neuen Offset zurück
     */
    static public int fillColor(byte[] color, byte[] array, int offset, int len) {
        byte r = color[0];
        byte g = color[1];
        byte b = color[2];

        for(int i=0;i<len;i++) {
            array[offset++] = r;
            array[offset++] = g;
            array[offset++] = b; 
        }
        return offset;
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
