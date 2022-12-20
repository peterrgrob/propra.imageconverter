package propra.imageconverter.image;

/**
 * 
 */
public class Pixel {

    // Farbwerte
    protected byte[] values;

    // Index des Pixels im Puffer
    private int index;

    /**
     * 
     */
    public Pixel() {
         values = new byte[ColorFormat.PIXEL_SIZE];
    }
   
    /**
    * 
    */
    public Pixel(byte[] values) {
        set(values);
    }
    
    /**
     * 
     */
    public Pixel(byte[] values,
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
