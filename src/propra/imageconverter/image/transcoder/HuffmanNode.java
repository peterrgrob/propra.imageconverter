package propra.imageconverter.image.transcoder;

import propra.imageconverter.data.BitCode;
import java.io.IOException;
import propra.imageconverter.PropraException;
import propra.imageconverter.data.BitInputStream;
import propra.imageconverter.data.BitOutputStream;

/**
 *  Klasse implementiert einen Knoten des Huffman Baumes
 */    
public class HuffmanNode implements Comparable<HuffmanNode>{
    
    // Kindknoten 
    private HuffmanNode leftNode;
    private HuffmanNode rightNode;

    // Symbol und seine Häufigkeit   
    private int symbol;
    private long frequency;

    // Code des Symbols
    private BitCode code;

    /**
     *  Konstruktoren
     */
    public HuffmanNode() {
    }

    /**
     *
     */
    public HuffmanNode(int s, long frequency) {
       this.symbol = s;
       this.frequency = frequency;
   }

    /**
     *
     */
    public HuffmanNode(int s, long frequency,
                       HuffmanNode leftNode, 
                       HuffmanNode rightNode) {
       this.symbol = s;
       this.frequency = frequency;
       this.leftNode = leftNode;
       this.rightNode = rightNode;
   }

   /**
    * Liest rekursiv einen Baum aus dem Stream ein gemäß der Propra-Kodierung
    */
   public void buildTreeFromResource(BitInputStream resource, HuffmanNode[] nodeMap, int leafCtr) throws IOException, PropraException {
        /*
         * Nächstes Bit gibt an um welchen Knotentyp es sich handelt
         */
        if(resource.readBit() == 0) {
           // Innerer Knoten, daher mit Pre-Order Durchlauf fortfahren
           leftNode = new HuffmanNode();
           leftNode.buildTreeFromResource(resource, nodeMap, leafCtr);
           rightNode = new HuffmanNode();
           rightNode.buildTreeFromResource(resource, nodeMap, leafCtr); 

        } else {
            // Fehlerhafte Kodierung prüfen
            if( nodeMap.length == 0
            ||  leafCtr >= 256) {
                throw new PropraException("Ungültige Baumkodierung!");
            }

            // Blatt erreicht, daher Symbol für das Blatt einlesen
            symbol = resource.readByte() & 0xFF;

            // Fehlerbehandlung bei möglicherweise korrupten Daten
            if(nodeMap[symbol] != null) {
                throw new IOException("Doppeltes Huffman-Blatt, Daten korrupt?");
            }

            // Blatt vermerken
            nodeMap[symbol] =  this;
            leafCtr++;
        }
   }

   /**
    * Speichert Baum gemäß der Propra-Kodierung im Stream
    */
   public void storeTreeInStream(BitOutputStream stream) throws IOException {
       if( leftNode == null 
       &&  rightNode == null) {
           stream.write(1);
           stream.writeByte(symbol);
       } else {
           stream.write(0);
           leftNode.storeTreeInStream(stream);
           rightNode.storeTreeInStream(stream);
       }
   }
   
   /**
    * Berechnet rekursiv die Codes je Symbol nach Erstellung des Baumes
    */
   public void buildBitCodes(BitCode c) {
       // Code speichern
       code = c;

       // Innerer Knoten?
       if(leftNode != null 
       && rightNode != null) {
           //  Linken und rechten Teilbaum besuchen
           leftNode.buildBitCodes(new BitCode(c).addBit(false));
           rightNode.buildBitCodes(new BitCode(c).addBit(true));

       } 
   }

   /**
    * Durchläuft den Baum bestimmt durch die Bits im Stream, gibt -1 bei EOF zurück, 
    * ansonsten gefundenes Symbol
    */
   public int decodeSymbol(BitInputStream stream) throws IOException {
       // Symbol erreicht?
       if( leftNode == null
       &&  rightNode == null) {
           return symbol;
       }

       // Nächstes Bit lesen und rekursiv dem Pfad folgen
       switch(stream.readBit()) {
           case 1 -> {
               return rightNode.decodeSymbol(stream);
           }
           case 0 -> {
               return leftNode.decodeSymbol(stream);
           }
           case -1 -> {
               return -1;
           }
       }

       return -1;
   }

   /**
    *  Vergleicht zwei Knoten anhand ihrer Symbolhäufigkeit, benötigt für die 
    *  Sortierung nach Häufigkeit
    */
   @Override
   public int compareTo(HuffmanNode o) {
       return (int)(frequency - o.frequency);
   }

   /**
    * Getter/Setter
    */
    public long getFrequency() {
        return frequency;
    }

    public BitCode getCode() {
       return code;
    }
}
