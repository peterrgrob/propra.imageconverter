package propra.imageconverter.image;

import propra.imageconverter.util.DataBuffer;

/**
 * 
 *  Repräsentiert ein Farbformat mit 3 Bytes. 
 *  Die Reihenfolge der Komponenten ist durch ein Mapping 
 *  konfigurierbar, die Standardreihenfolge ist Little-Endian.
 * 
 * @author pg
 */
public class ColorFormat implements Comparable<ColorFormat> {

    // Konstanten zur Indizierung von Farbkomponenten (Little Endian)
    public static final int BLUE = 0;
    public static final int GREEN = 1;
    public static final int RED = 2;

    // Bildet Indizes der Farkomponenten ab
    protected int[] mapping = new int[3];

    /**
     * 
     */
    public ColorFormat() {
        /*
         * Standard RGB Farben (Little Endian)
         */
        setMapping(RED, 2);
        setMapping(GREEN,1);
        setMapping(BLUE, 0);
    }
    
    
    /**
     * 
     * @param src
     */
    public ColorFormat(ColorFormat src) {
        setMapping(RED,src.getMapping(RED));
        setMapping(GREEN,src.getMapping(GREEN));
        setMapping(BLUE,src.getMapping(BLUE));
    }
    
    /**
     *
     * @param o
     * @return
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
      
    /*
     * 
     */
    public static DataBuffer convertColorBuffer(DataBuffer input, ColorFormat srcFormat,
                                                DataBuffer output,ColorFormat dstFormat) {
        if (input == null
        ||  output == null
        ||  srcFormat == null
        ||  dstFormat == null) {
            throw new IllegalArgumentException();
        }  
        byte t0,t1,t2;
        int sIndex;
        int dIndex;
        
        byte[] inBytes = input.getBytes();
        byte[] outBytes = output.getBytes();
                
        int srcOffset = input.getCurrDataOffset();
        int dstOffset = output.getCurrDataOffset();
        
        int[] srcMap = srcFormat.getMapping();
        int[] dstMap = dstFormat.getMapping();
        
        for (int i=0; i<input.getCurrDataLength(); i+=3) {
            sIndex = srcOffset + i;
            dIndex = dstOffset + i;
            
            t2 = inBytes[sIndex + srcMap[RED]];
            t1 = inBytes[sIndex + srcMap[GREEN]];
            t0 = inBytes[sIndex + srcMap[BLUE]];

            outBytes[dIndex + dstMap[RED]] = t2;
            outBytes[dIndex + dstMap[GREEN]] = t1;
            outBytes[dIndex + dstMap[BLUE]] = t0;
        }
        
        return output;
    }
    
    /*
     *  Getter/Setter 
     */
    public void setMapping(int id, int newId) {
        mapping[id] = newId;
    }

    public int getMapping(int id) {
        return mapping[id];
    }

    public int[] getMapping() {
        return mapping;
    }
}
