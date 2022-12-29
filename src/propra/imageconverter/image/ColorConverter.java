package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.IDataTranscoder;
import propra.imageconverter.data.IDataTarget;

/**
 * Wendet eine Operation auf die empfangenen Farben an und leitet den Puffer 
 * weiter an das Datenziel
 */
public class ColorConverter implements IDataTarget {
    
    // Ziel an das der Puffer weitergeleitet wird
    private final IDataTarget target;
    
    // Operation die auf den Farben ausgeführt wird
    private final ColorOperation op;

    /**
     * 
     */
    public ColorConverter(ColorOperation op, IDataTarget target) {
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
