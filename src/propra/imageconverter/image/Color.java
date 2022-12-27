package propra.imageconverter.image;

/**
 * Enthält einen Farbwert, oder indiziert einen Farbwert in einem 
 * byte-Array (Referenz)
 */
public class Color implements Comparable<Color> {

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
     */
    public Color(byte[] values) {
        set(values);
    }

    /**
     * 
     */
    public Color(byte[] values, int index) {
        this.values = values;
        this.index = index;
    }
   
   /**
    * 
    */
    public byte[] get() {
        return values;
    }
   
    /**
     * 
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
     */
    @Override
    public int compareTo(Color color) {
        return (values[index + 0] == color.values[color.index + 0]
            &&  values[index + 1] == color.values[color.index + 1]
            &&  values[index + 2] == color.values[color.index + 2]) == true ? 0 : 1;
    }
}
