package propra.imageconverter.image.compression;

import propra.imageconverter.util.BitOutputStream;
import propra.imageconverter.util.BitInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import propra.imageconverter.image.ImageResource;
import propra.imageconverter.data.IDataTarget;
import propra.imageconverter.data.IDataTranscoder;

/**
 * 
 * @author pg
 */
public class ImageTranscoderHuffman extends ImageTranscoderRaw {
    
    //  Histogramm der Daten
    private final long[] histogram = new long[256]; 
    
    //  Huffman-Baum zur Kodierung
    private HuffmanTree huffmanTree;
    
    // Ausgabe Bitstream
    private BitOutputStream outStream;  
    
    /**
     *
     */
    public ImageTranscoderHuffman(ImageResource resource) {
        super(resource);
    }

    /**
     * Bereitet die Huffmankodierung vor
     */
    @Override
    public IDataTranscoder begin(Operation op) throws IOException {
        super.begin(op);
        
        switch(op) {
            case ENCODE_ANALYZE -> {
                Arrays.fill(histogram, 0);
            }
            case ENCODE -> {
                /*
                 *  BitStream erstellen und den durch Analyse erstellten 
                 *  Baum als Bitfolge in Stream kodieren
                 */
                outStream = new BitOutputStream(resource.getOutputStream());
                huffmanTree.storeTreeInStream(outStream);
            }
        }
        return this;
    }
 
    /**
     * Analyse für die Huffman Kompression. Ermittelt die Häufigkeit der Symbole 
     * im Datenblock
     */
    @Override
    public void analyze(ByteBuffer block, boolean last) {
        if(operation == Operation.ENCODE_ANALYZE) {           
            byte[] buffer = block.array();
            int offset = 0;

            while(offset < block.limit()) {
                histogram[buffer[offset] & 0xFF]++;
                offset++;
            }
        } 
    }
    
    /**
     * Schließt die Huffman-Kodierung/Analyse ab
     */
    @Override
    public void end() throws IOException {
        switch(operation) {
            case ENCODE_ANALYZE -> {
               /*
                *  Histogram prüfen
                */
                long sum = 0;
                for(long i:histogram) {
                    sum += i;
                }  
                if(sum != resource.getAttributes().getImageSize()) {
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
                outStream.flush();
                resource.getAttributes().setDataLength(outStream.getByteCounter()); 
            }
        }

        super.end();
    }
    
    /**
     * Die Kompression benötigt einen Analyse-Durchlauf des Eingabebildes
     */
    @Override
    public boolean analyzeNecessary(Operation op) {
        return op == Operation.ENCODE;
    }

    /**
     * Dekodiert die Huffman komprimierten Daten der Resource
     */
    @Override
    public void decode(IDataTarget output) throws IOException {
        
        // Ausgabepuffer vorbereiten
        int symbolCtr = 0;
        
        // BitStream erstellen
        BitInputStream stream = new BitInputStream(resource.getInputStream());
        ByteBuffer data = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE);
        
        // Kodierten Baum einlesen und erstellen
        huffmanTree = new HuffmanTree();
        huffmanTree.buildTreeFromResource(stream);
        
        // Lädt, dekodiert und sendet Pixelblöcke an Listener  
        while(symbolCtr++ < resource.getAttributes().getImageSize()) {
            
            // Symbol dekodieren
            int symbol = huffmanTree.decodeSymbol(stream);
            if(symbol == -1) {
                break;
            }
            
            // Symbol speichern
            data.put((byte)symbol);

            // Wenn Blockgröße erreicht an Listener senden
            if(data.capacity() == data.position()) {                
                sendData(IDataTarget.Event.DATA_DECODED, output, 
                            data,false);    
            }
        }
        
        // Restliche Daten im Puffer übertragen
        sendData(IDataTarget.Event.DATA_DECODED,output, 
                        data,true);     
    }


    /**
     * Komprimiert Datenblock und speichert ihn in der Resource
     */
    @Override
    public void encode(ByteBuffer block, boolean last) throws IOException {
        if(huffmanTree == null) {
            throw new IllegalStateException("Huffmann-Tree nicht initialisiert.");
        }
        
        /**
         *  Symbole im Puffer iterieren, per Huffmantree zu Bitcode umsetzen und 
         *  diesen in der Resource speichern
         */
        byte[] c = new byte[3];
        while(block.position() < block.limit()) {
            
            // Pixel lesen und Farbe konvertieren
            block.get(c);
                    
            outStream.write(huffmanTree.encodeSymbol(c[0] & 0xFF));
            outStream.write(huffmanTree.encodeSymbol(c[1] & 0xFF));
            outStream.write(huffmanTree.encodeSymbol(c[2] & 0xFF));
        }
    }   
}
