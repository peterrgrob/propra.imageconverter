package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import propra.imageconverter.data.IDataTranscoder;
import propra.imageconverter.data.IDataTarget;

/**
 * Wendet eine Operation auf die übergebene Farben an und leitet den Puffer weiter an 
 * das nächstes Datenziel
 */
public class ColorFilter implements IDataTarget {
    
    // Ziel an das der Puffer weitergeleitet wird
    private final IDataTarget target;
    
    // Operation die auf den Farben ausgeführt wird
    private final ColorOperation op;

    /**
     * 
     */
    public ColorFilter(ColorOperation op, IDataTarget target) {
        this.target = target;
        this.op = op;
    }
    
    /**
     * 
     */
    @Override
    public void onData(ByteBuffer data, boolean lastBlock, IDataTranscoder caller) throws IOException {
        if(op != null) {
            ColorOperations.filterColorBuffer(data, data, op);
        }
        target.onData(data, lastBlock, caller);
    }
}
