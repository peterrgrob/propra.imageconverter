package propra.imageconverter.image;

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
    
    /**
     * Allgemeine Methode zum Konvertieren einer Farbe in ein Zielfarbformat.
     * 
     * @param input
     * @param colorInfo
     * @return 
     */
    public byte[] convertColor(byte[] input, ColorFormat colorInfo) {
        if ( input == null) {
            throw new IllegalArgumentException();
        }  
        return convertColor(input, 0, colorInfo);
    }
    
    
    /**
     * Allgemeine Methode zum Konvertieren einer Farbe in ein Zielfarbformat.
     * 
     * @param input
     * @return 
     */
    public byte[] convertColor(byte[] input, int inputOffset, ColorFormat inputFormat) {
        if ( input == null) {
            throw new IllegalArgumentException();
        }  
        byte t0,t1,t2;
        int[] map = inputFormat.getMapping();
        
        t2 = input[inputOffset + map[RED]];
        t1 = input[inputOffset + map[GREEN]];
        t0 = input[inputOffset + map[BLUE]];
        
        input[inputOffset + mapping[RED]] = t2;
        input[inputOffset + mapping[GREEN]] = t1;
        input[inputOffset + mapping[BLUE]] = t0;
                
        return input;
    }
      
    /*
     * 
     */
    public static byte[] convertColorArray( byte[] input, int srcOffset, ColorFormat srcFormat,
                                            byte[] output, int dstOffset, ColorFormat dstFormat, 
                                            int len) {
        if (input == null
        ||  output == null
        ||  srcFormat == null
        ||  dstFormat == null) {
            throw new IllegalArgumentException();
        }  
        byte t0,t1,t2;
        int sIndex;
        int dIndex;
        int[] srcMap = srcFormat.getMapping();
        int[] dstMap = dstFormat.getMapping();
        
        for (int i=0; i<len; i+=3) {
            sIndex = srcOffset + i;
            dIndex = dstOffset + i;
            
            t2 = input[sIndex + srcMap[RED]];
            t1 = input[sIndex + srcMap[GREEN]];
            t0 = input[sIndex + srcMap[BLUE]];

            output[dIndex + dstMap[RED]] = t2;
            output[dIndex + dstMap[GREEN]] = t1;
            output[dIndex + dstMap[BLUE]] = t0;
        }
        
        return input;
    }
    
    /**
     * 
     * @param id
     * @param newId
     */
    public void setMapping(int id, int newId) {
        mapping[id] = newId;
    }
    
    /**
     *
     * @param id
     * @return
     */
    public int getMapping(int id) {
        return mapping[id];
    }
    
    /**
     *
     * @return
     */
    public int[] getMapping() {
        return mapping;
    }
}
