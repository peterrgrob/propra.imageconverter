package propra.imageconverter.image;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.PriorityQueue;
import propra.imageconverter.data.BitResource;

/**
 *  Implementiert einen binären Baum zur Erstellung und Abbildung der Huffmancodes 
 *  und Symbole
 */
public class HuffmanTree {

    /**
     *  Klasse implementiert einen Knoten des Huffman Baumes
     */    
    private static class Node implements Comparable<Node>{
        
        /**
         *  Knoten
         */ 
        private Node leftNode;
        private Node rightNode;

        /*
         *  Symbol und seine Häufigkeit   
         */
        private SymbolTupel symbol;
        
        /*
         *  Codewort des Knotens  
         */
        BitSet code;
        
        /**
         *  Konstruktoren
         */
        public Node() {
        }
        
        public Node(SymbolTupel s) {
            this.symbol = s;
        }
        
        public Node(SymbolTupel s,
                    Node leftNode,
                    Node rightNode) {
            this.symbol = s;
            this.leftNode = leftNode;
            this.rightNode = rightNode;
        }
        
        /**
         *  Berechnet rekursiv die Codes je Symbol nach Erstellung des Baumes
         */
        public void buildCode(BitSet c, int level) {
            // Code speichern
            code = c;
            
            // Innerer Knoten?
            if(leftNode != null 
            && rightNode != null) {
                //  Linken Teilbaum besuchen
                buildCode((BitSet)c.clone(), level + 1);
                
                //  Rechten Teilbaum besuchen
                BitSet nb = (BitSet)c.clone();
                nb.set(level);
                buildCode(nb, level + 1);
            }
        }
        
        /**
         *  Baum aus der ProPra Kodierung rekursiv wiederherstellen
         */
        public void buildFromResource(BitResource resource) throws IOException {
            /*
             * Nächstes Bit gibt an um welchen Knotentyp es sich handelt
             */
            if(resource.readBit() == 0) {
               // Innerer Knoten, daher mit Pre-Order Durchlauf fortfahren
               leftNode = new Node();
               leftNode.buildFromResource(resource);
               rightNode = new Node();
               rightNode.buildFromResource(resource); 
            } else {
               // Blatt erreicht, daher Symbol für das Blatt einlesen
               symbol = new SymbolTupel(resource.readByte(), 0);
            }
        }
        
        /**
         *  Vergleicht zwei Knoten anhand ihrer Symbolhäufigkeit, benötigt für die 
         *  Sortierung nach Häufigkeit
         */
        @Override
        public int compareTo(Node o) {
            return symbol.compareTo(o.symbol);
        }
        
        /**
         * Getter/Setter
         */
        public int getFrequency() {
            return symbol.getFrequency();
        }
        
        public BitSet getCode() {
            return code;
        }
        
        /**
         *  Kodiert einen Baum nach Konstruktionvorschrift der ProPra 
         *  Spezifikation in einen String
         */
        @Override
        public String toString() {
            if(leftNode == null 
            && rightNode == null) {
                return "1" + symbol.toString();
            } else {
                String code = "0";
                if(leftNode != null) {
                    code += leftNode.toString();
                }
                if(rightNode != null) {
                   code += rightNode.toString();
                }    
                return code;
            }
        }
    }
    
    /**
     *  Implementiert Tupel aus Symbol und Zähler
     */
    public static class SymbolTupel implements Comparable<SymbolTupel> {

        /**
         *  Symbol und seine Häufigkeit
         */
        private byte symbol;
        private int frequency;

        /**
         *  Konstruktor
         */
        public SymbolTupel( byte symbol, 
                            int frequency) {
            this.symbol = symbol;
            this.frequency = frequency;
        }
        
        /**
         *  Vergleicht zwei Symbole anhand ihrer Häufigkeit, benötigt für die 
         *  Sortierung nach Häufigkeit
         */
        @Override
        public int compareTo(SymbolTupel o) {
            return frequency - o.frequency;
        }
        
        /**
         * Getter
         */
        public int getFrequency() {
            return frequency;
        }
        
        /**
         * 
         */
        @Override
        public String toString() {
            return "[" + symbol + "]";
        }
    }
    
    /**
     *  Wurzelknoten des Huffmantrees
     */
    private Node rootNode;

    /**
     * 
     */
    public HuffmanTree() {
    }
    
    /**
     *  Rekonstruiert rekursiv einen Huffmantree aus der Resource nach 
     *  Propra-Konstruktionsvorschrift aus einem Code mit Preorder-Durchlauf
     */
    public void buildFromResource(BitResource resource) throws IOException {
        if(resource == null) {
            throw new IllegalArgumentException();
        }
        
        rootNode = new Node(new SymbolTupel((byte)0, 0));
        rootNode.buildFromResource(resource);
    }
    
    /*
     *  Erstellt einen Huffmantree aus den Eingabesymbolen und Häufigkeiten
     *  durch Verwendung einer PriorityQueue
     */
    public void buildFromHistogram(ArrayList<SymbolTupel> symbols) {
        PriorityQueue<Node> q = new PriorityQueue<>();
        
        // Symbole nach Häufigkeit sortieren und in Queue einfügen
        symbols.forEach(s -> q.add(new Node(s)));
        
        /*
         *  Baum iterativ konstruieren bis nur noch ein Knoten (Wurzel) in der 
         *  Queue enthalten ist.
         */
        while(q.size() > 1) {
                    
            /*
             *  Zwei häufigste Knoten zu einem Knoten verbinden
             *  und an das Ende der Queue einfügen
             */
            Node left = q.remove();
            Node right = q.remove();
            Node parentNode = new Node(new SymbolTupel((byte)0, 
                                                        left.getFrequency() + right.getFrequency()),
                                                        left,
                                                        right);
                    
            q.add(parentNode);
        }
        
        // Wurzel speichern
        rootNode = q.remove();
        System.out.print(toString());
    }
    
    /**
     * 
     */
    @Override
    public String toString() {
        if(rootNode != null) {
            return rootNode.toString();
        }
        return "";
    }
}
