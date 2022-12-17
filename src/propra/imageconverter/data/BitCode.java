package propra.imageconverter.data;

/**
 *  Implementiert den Code für ein Symbol
 */
public class BitCode implements Comparable<BitCode> {

   // Bits des Codes
   private int code;

   // Länge des Codes in Bit
   private int length;

   /**
    *  Constructor
    */
   public BitCode( int code, 
                   int length) {
       this.code = code;
       this.length = length;
   }

   public BitCode(BitCode src) {
       this.code = src.code;
       this.length = src.length;
   }

   /**
    * Bit hinzufügen
    */
   public BitCode addBit(boolean bit) {
       code <<= 1;
       code |= ((bit == true) ? 1 : 0);
       length++;
       return this;
   }

   /**
    *  Getter/Setter
    */
   public int getCode() {
       return code;
   }

   public int getLength() {
       return length;
   }

   @Override
   public int compareTo(BitCode o) {
       return (code - o.code) + 
              (length - o.length); 
   }     

    @Override
    public String toString() {
        return "BitCode{" + "code=" + Integer.toBinaryString(code) + ", length=" + length + '}';
    }
}
