package propra.imageconverter.image.compression;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataTranscoder;
import propra.imageconverter.util.CheckedInputStream;
import propra.imageconverter.data.IDataTarget;
import propra.imageconverter.data.IDataTranscoder;
import propra.imageconverter.image.ImageResource;


/**
 * Basiscodec für die Konvertierung von unkomprimierten Pixelblöcken.
 * Die Ein- und Ausgabedaten für decode und encode werden von der per Konstruktor 
 * übergebenen Resource gelesen/geschrieben. Allgemein erfolgt die 
 * individuelle Konfiguration der Kompressionsklassen über die Konstruktoren.
 */
public class ImageTranscoderRaw extends DataTranscoder implements IDataTarget {
    
    // Zugeordnete Resource zur Ein-, oder Ausgabe der Daten 
    protected ImageResource resource;
    
    /**
     * 
     */
    public ImageTranscoderRaw(ImageResource resource) {
        this.resource = resource;
    }
    
    /**
     * Dekodiert die Daten der Resource blockweise an das Datenziel
     */
    @Override
    public void decode(IDataTarget target) throws IOException {
        CheckedInputStream stream = resource.getInputStream();
        
        // Lese Puffer  
        ByteBuffer data = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE);
        boolean bLast = false;
        
        /*
         * Lädt und sendet Pixelblöcke direkt an Listener  
         */
        while(resource.position() < resource.length()) {
            
            // Blockgröße anpassen
            if(resource.length() - resource.position() < data.capacity()) {
                data.limit((int)(resource.length() - resource.position()));
                bLast = true;
            }
            
            // Datenblock von Resource lesen 
            int r = stream.read(data.array(), data.position(), data.limit());
            if(r == -1) {
                throw new IOException("Lesefehler!");
            }
            
            // Daten an das Ziel senden
            target.onData(data,bLast, this);  
            data.clear();
        }
    }

    /**
     * Schreibt Datenblock 1:1 in die Resource
     */
    @Override
    public void encode(ByteBuffer block, boolean last) throws IOException {
        resource.getOutputStream()
                .write(block);
    }
    
    /**
     * Schließt laufenden ByteBuffer ab und sendet ihn an das Datenziel
     */
    protected void pushData(ByteBuffer block, boolean last, IDataTarget target) throws IOException {
        block.flip();
        target.onData(block, last, this);
        block.clear();
    }

    /**
     * 
     */
    @Override
    public void onData(ByteBuffer data, boolean lastBlock, IDataTranscoder caller) throws IOException {
        switch(operation) {
            case ENCODE, ANALYZE -> {
                encode(data, lastBlock);
            }
        }
    }
}
