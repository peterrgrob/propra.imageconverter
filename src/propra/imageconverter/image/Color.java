package propra.imageconverter.image;

/**
 * Enthält einen Farbwert, oder indiziert einen Farbwert in einem 
 * byte-Array (Referenz)
 */
public class Color {

    // Farbformat
    public enum Format {
        COLOR_BGR,
        COLOR_RBG,
    }

    // Pixelgröße in Bytes
    public static int PIXEL_SIZE = 3;
    
    // Farbwerte
    protected byte[] values;

    // Index des Pixels im Puffer
    private int index;

    /**
     * 
     */
    public Color() {
         values = new byte[PIXEL_SIZE];
    }
   
    /**
     * 
     * @param values 
     */
    public Color(byte[] values) {
        set(values);
    }

    /**
     * 
     * @param values
     * @param index 
     */
    public Color(byte[] values,
                 int index) {
        this.values = values;
        this.index = index;
    }
   
   /**
    * 
    * @return 
    */
    public byte[] get() {
        return values;
    }
   
    /**
     * 
     * @return 
     * @return  
     */
    public int getIndex() {
        return index;
    }
   

    /**
     * 
     * @param values 
     */
    public void set(byte[] values) {
        System.arraycopy(this.values, 0, 
                        values, index, 
                        PIXEL_SIZE);
    }
    
    /**
     * Vergleicht zwei Farbwerte
     * 
     * @param c1
     * @param c2
     * @return 
     */
    public static boolean compareColor(Color c1, Color c2) {
        return (c1.values[c1.index + 0] == c2.values[c2.index + 0]
            &&  c1.values[c1.index + 1] == c2.values[c2.index + 1]
            &&  c1.values[c1.index + 2] == c2.values[c2.index + 2]);
    }
}
