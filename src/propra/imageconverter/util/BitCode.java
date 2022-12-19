package propra.imageconverter.util;

/**
 * Klasse hält den Code für ein Symbol
 *
 * @author pg
 */
public class BitCode implements Comparable<BitCode> {

   // Bits des Codes
   private int code;

   // Länge des Codes in Bit
   private int length;

   /**
    * 
    * @param code
    * @param length 
    */
   public BitCode( int code, 
                   int length) {
       this.code = code;
       this.length = length;
   }

   /**
    * 
    * @param src 
    */
   public BitCode(BitCode src) {
       this.code = src.code;
       this.length = src.length;
   }

   /**
    * Bit hinzufügen
    * 
    * @param bit
    * @return 
    */
   public BitCode addBit(boolean bit) {
       code <<= 1;
       code |= ((bit == true) ? 1 : 0);
       length++;
       return this;
   }

   /**
    * 
    * @return 
    */
   public int getCode() {
       return code;
   }

   /**
    * 
    * @return 
    */
   public int getLength() {
       return length;
   }

   /**
    * 
    * @param o
    * @return 
    */
   @Override
   public int compareTo(BitCode o) {
       return (code - o.code) + 
              (length - o.length); 
   }     
}
