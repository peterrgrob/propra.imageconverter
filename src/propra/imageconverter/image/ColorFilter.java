package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.IDataTranscoder;
import propra.imageconverter.data.IDataTarget;

/**
 * Filtert übergebene Farben mit der ColorOperation und leitet Daten weiter an 
 * das nächstes Datenziel
 */
public class ColorFilter implements IDataTarget {
    
    // Ziel an das die Farben weitergeleitet werden
    private final IDataTarget target;
    
    // Operation die auf den Farben ausgeführt wird
    private final ColorOperation op;

    public ColorFilter(ColorOperation op, IDataTarget target) {
        this.target = target;
        this.op = op;
    }
    
    /**
     * 
     */
    @Override
    public void onData( Event event, IDataTranscoder caller, 
                        ByteBuffer data, boolean lastBlock) throws IOException {
        if(op != null) {
            ColorOperations.filterColorBuffer(data, data, op);
        }
        target.onData(event, caller, data, lastBlock);
    }
}
