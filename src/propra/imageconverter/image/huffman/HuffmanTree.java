package propra.imageconverter.image.huffman;

import java.util.Map;

/**
 *  Implementiert einen binären Baum zur Verwaltung der Huffmanncodes 
 *  und Symbole
 */
public class HuffmanTree <Symbol> {

    /**
     *  Klasse implementiert einen Knoten des Baumes
     */    
    private class Node<Symbol> {
        /**
         *  Knoten
         */ 
        private Node parentNode;
        private Node leftNode;
        private Node rightNode;

        /*
         *  Symbol und seine Häufigkeit   
         */
        Symbol symbol;
        int frequency;

        public Node() {

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
     *  Erstellt einen Huffmantree aus den Eingabesymbolen und Häufigkeiten
     */
    public void build(Map<Symbol, Integer> symbols) {
        
    }
}
