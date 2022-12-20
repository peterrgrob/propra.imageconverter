package propra.imageconverter.image;

/**
 * 
 */
public class Color {

    // Farbwerte
    protected byte[] values;

    // Index des Pixels im Puffer
    private int index;

    /**
     * 
     */
    public Color() {
         values = new byte[ColorFormat.PIXEL_SIZE];
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
                        ColorFormat.PIXEL_SIZE);
    }

   /**
    * 
    * @param values
    * @param offs
    * @param len 
    */
   public void set(byte[] values,
                  int offs,
                  int len) {
       System.arraycopy(this.values, offs, 
                       values, index, 
                       ColorFormat.PIXEL_SIZE * len);
   }
}
