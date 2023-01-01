package propra.imageconverter.image.transcoder;

import propra.imageconverter.data.BitOutputStream;
import propra.imageconverter.data.BitInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.IDataTarget;
import propra.imageconverter.data.IDataTranscoder;
import propra.imageconverter.image.ImageAttributes;
import propra.imageconverter.data.CheckedInputStream;
import propra.imageconverter.data.CheckedOutputStream;

/**
 * Transcoder Implementierung für die Huffman Kodierung gemäß der 
 * Propra-Spezifikation
 */
public class ImageTranscoderHuffman extends ImageTranscoderRaw {
    
    //  Histogramm der Daten
    private final long[] histogram = new long[256]; 
    
    //  Huffman-Baum zur Kodierung
    private HuffmanTree huffmanTree;
    
    // Ausgabe Bitstream
    private BitOutputStream bitOutStream;  
    
    /**
     *  Konstruktor
     */
    public ImageTranscoderHuffman(ImageAttributes attributes) {
        super(attributes);
    }

    @Override
    public Compression getCompression() {
        return Compression.HUFFMAN;
    }
    
    /**
     * Bereitet die Huffmankodierung der Blöcke vor
     */
    @Override
    public IDataTranscoder beginEncoding(EncodeMode op, CheckedOutputStream out) throws IOException {
        super.beginEncoding(op, out);
        
        switch(op) {
            case ENCODE -> {
                /*
                 *  BitStream erstellen und den durch Analyse erstellten 
                 *  Baum als Bitfolge in Stream kodieren
                 */
                bitOutStream = new BitOutputStream(outStream);
                huffmanTree.storeTreeInStream(bitOutStream);
            }
        }
        return this;
    }
    
    /**
     * Schließt die Huffman-Kodierung/Analyse ab
     */
    @Override
    public long endEncoding() throws IOException {
        switch(operation) {
            case ANALYZE -> {
               /*
                *  Histogram prüfen
                */
                long sum = 0;
                for(long i:histogram) {
                    sum += i;
                }  
                if(sum != attributes.getImageSize()) {
                    throw new IOException("Fehlerhafte Bilddaten (Histogram)");
                }

                /*
                 *  Nach der Encoder-Analyse den entsprechenden Huffman Baum aus dem 
                 *  ermittelten Histogram erstellen
                 */
                huffmanTree = new HuffmanTree();
                huffmanTree.buildTreeFromHistogram(histogram);
            }
            case ENCODE -> {
                /*
                 *  Stream flushen und kodierte Datengröße aktualisieren
                 */
                bitOutStream.flush();
                encodedBytes = bitOutStream.getByteCounter(); 
            }
        }

        return super.endEncoding();
    }
    
    /**
     * Die Kompression benötigt einen Analyse-Durchlauf des Eingabebildes
     */
    @Override
    public boolean analyzeNecessary() {
        return true;
    }

    /**
     * Dekodiert die Huffman komprimierten Daten der Resource
     */
    @Override
    public void decode(CheckedInputStream in, IDataTarget output) throws IOException {
        
        // Ausgabepuffer vorbereiten
        int symbolCtr = 0;
        
        // BitStream erstellen
        BitInputStream stream = new BitInputStream(in);
        ByteBuffer data = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE);
        
        // Kodierten Baum einlesen und erstellen
        huffmanTree = new HuffmanTree();
        huffmanTree.buildTreeFromResource(stream);
        
        // Lädt, dekodiert und sendet Pixelblöcke an Listener  
        while(symbolCtr++ < attributes.getImageSize()) {
            
            // Symbol dekodieren
            int symbol = huffmanTree.decodeSymbol(stream);
            if(symbol == -1) {
                break;
            }
            
            // Symbol speichern
            data.put((byte)symbol);

            // Wenn Blockgröße erreicht an Listener senden
            if(data.capacity() == data.position()) {                
                pushData(data, false, output);    
            }
        }
        
        // Restliche Daten im Puffer übertragen
        pushData(data,true, output);     
    }


    /**
     * Analysiert, oder komprimiert den Datenblock und speichert ihn in der Resource
     */
    @Override
    public void encode(ByteBuffer block, boolean last) throws IOException {
        // Analyse- oder Kodiermodus?
        if(operation == EncodeMode.ANALYZE) {           
            byte[] buffer = block.array();
            int offset = 0;

            while(offset < block.limit()) {
                histogram[buffer[offset] & 0xFF]++;
                offset++;
            }
        } else {    
            if(huffmanTree == null) {
                throw new IllegalStateException("Huffmann-Tree nicht initialisiert.");
            }
            
            /**
             *  Symbole im Puffer iterieren, per Huffmantree zu Bitcode umsetzen und 
             *  diesen in der Resource speichern
             */
            byte[] color = block.array(); 
            int offs = block.position();
            int lim = block.limit();
            
            while(offs < lim) {
           
                bitOutStream.write(huffmanTree.encodeSymbol(color[offs + 0] & 0xFF));
                bitOutStream.write(huffmanTree.encodeSymbol(color[offs + 1] & 0xFF));
                bitOutStream.write(huffmanTree.encodeSymbol(color[offs + 2] & 0xFF));
                
                offs += 3;
            }
        }
    }   
}
