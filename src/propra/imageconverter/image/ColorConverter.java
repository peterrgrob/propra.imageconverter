package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.IDataTranscoder;
import propra.imageconverter.data.IDataTarget;
import propra.imageconverter.image.ColorUtil.ColorOp;

/**
 * Wendet eine ConverterOperation auf die empfangenen Farben an und leitet den Puffer 
 * weiter an das Datenziel
 */
public class ColorConverter implements IDataTarget {
    
    // Ziel an das der Puffer weitergeleitet wird
    private final IDataTarget target;
    
    // Operation die auf den Farben ausgeführt wird
    private final ColorOp op;

    /**
     * Initialisiert mit Konverterierungsoperation und Datenziel
     */
    public ColorConverter(ColorOp op, IDataTarget target) {
        this.target = target;
        this.op = op;
    }
    
    /**
     * Empfängt Pixel zum Konvertieren und weiterleiten
     */
    @Override
    public void onData(ByteBuffer data, boolean lastBlock, IDataTranscoder caller) throws IOException {
        if(op != null) {
            ColorUtil.filterColorBuffer(data, data, op);
        }
        target.onData(data, lastBlock, caller);
    }
}
