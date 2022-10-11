package propra.imageconverter.image;

/**
 *
 * @author pg
 */
public class ColorType implements Comparable<ColorType> {

    // Konstanten zur Indizierung von Farbkomponenten (Little Endian)
    public static final int BLUE = 0;
    public static final int GREEN = 1;
    public static final int RED = 2;

    // Bildet Indizes der Farkomponenten ab
    protected int[] mapping = new int[3];

    /**
     * 
     */
    public ColorType() {
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
    public ColorType(ColorType src) {
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
    public int compareTo(ColorType o) {
        if( mapping[0] == o.getMapping(0) &&
            mapping[1] == o.getMapping(1) &&
            mapping[2] == o.getMapping(2)) {
            return 0;
        }
        return -1;
    }
    
    /**
     * 
     * @param id
     * @param channelInfo
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
    
    /**
     * 
     * @param input
     * @param colorInfo
     * @return 
     */
    public byte[] convertColor(byte[] input, ColorType colorInfo) {
        if ( input == null) {
            throw new IllegalArgumentException();
        }  
        byte t0,t1,t2;
        int[] map = colorInfo.getMapping();
        
        t2 = input[map[RED]];
        t1 = input[map[GREEN]];
        t0 = input[map[BLUE]];
        
        input[mapping[RED]] = t2;
        input[mapping[GREEN]] = t1;
        input[mapping[BLUE]] = t0;
                
        return input;
    }
}
