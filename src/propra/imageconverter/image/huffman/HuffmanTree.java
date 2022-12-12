package propra.imageconverter.image.huffman;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.PriorityQueue;

/**
 *  Implementiert einen binären Baum zur Erstellung der Huffmancodes 
 *  und Symbole
 */
public class HuffmanTree <Symbol> {

    /**
     *  Klasse implementiert einen Knoten des Baumes
     */    
    private static class Node<Symbol> implements Comparable<Node>{
        /**
         *  Knoten
         */ 
        private Node parentNode;
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
         *  Codes rekursiv berechnen
         */
        public void buildCode(BitSet c, int level) {
            code = c;
            
            if(leftNode != null 
            && rightNode != null) {
                //  Linker Teilbaum 
                buildCode((BitSet)c.clone(), level + 1);
                
                //  Rechter Teilbaum
                BitSet nb = (BitSet)c.clone();
                nb.set(level);
                buildCode(nb, level + 1);
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
        
        public void SetParent(Node p) {
            parentNode = p;
        }
    }
    
    /**
     *  Implementiert Tupel aus Symbol und Zähler
     */
    public static class SymbolTupel<Symbol> implements Comparable<SymbolTupel> {

        /**
         *  Symbol und seine Häufigkeit
         */
        private Symbol symbol;
        private int frequency;

        /**
         *  Konstruktor
         */
        public SymbolTupel( Symbol symbol, 
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
            return o.frequency - frequency;
        }
        
        /**
         * Getter
         */
        public int getFrequency() {
            return frequency;
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
    
    /*
     *  Erstellt einen Huffmantree aus den Eingabesymbolen und Häufigkeiten
     *  durch Verwendung einer PriorityQueue
     */
    public void build(ArrayList<SymbolTupel> symbols) {
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
            Node parentNode = new Node(new SymbolTupel<>(  null, 
                                                            left.getFrequency() + right.getFrequency()),
                                                            left,
                                                            right);
            
            left.SetParent(parentNode);
            right.SetParent(parentNode);         
            q.add(parentNode);
        }
        
        // Wurzel speichern
        rootNode = q.remove();
    }
}
