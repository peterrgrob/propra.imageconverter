package propra.imageconverter.image;

import propra.imageconverter.data.DataBuffer;
import propra.imageconverter.data.DataFormat;

/**
 * 
 *  Repräsentiert ein Farbformat mit 3 Bytes. 
 *  Die Reihenfolge der Komponenten ist durch ein Mapping 
 *  konfigurierbar, die Standardreihenfolge ist Little-Endian.
 * 
 * @author pg
 */
public class ColorFormat extends DataFormat implements Comparable<ColorFormat> {
    
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
        super(src);
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
        
        byte[] inBytes = input.getBytes();
        byte[] outBytes = output.getBytes();
        byte r,g,b;
                
        int srcOffset = input.getDataOffset();
        int dstOffset = output.getDataOffset();
        
        int[] srcMap = srcFormat.getMapping();
        int[] dstMap = dstFormat.getMapping();
        
        for (int i=0; i<input.getDataLength(); i+=3) {
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
     *
     * @return
     */
    public ImageTranscoder createTranscoder() {
        switch(encoding) {

            case NONE -> {
                return null;
            }
            case RLE -> {
                return new ImageTranscoderRLE();
            }
        }
        return null;
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
