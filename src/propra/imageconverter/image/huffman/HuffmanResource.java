package propra.imageconverter.image.huffman;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import propra.imageconverter.data.DataFormat;
import propra.imageconverter.image.ImageMeta;
import propra.imageconverter.image.ImageResource;

/**
 *
 * @author pg
 */
public class HuffmanResource extends ImageResource {
 
    
    /*
     *  Konstruktor
     */
    public HuffmanResource( String file, 
                            DataFormat.IOMode mode) throws IOException {
        super(file, mode);
    }

    /**
     * 
     */
    @Override
    public ImageMeta readHeader() throws IOException {
        return header;
    }

    /**
     * 
     */
    @Override
    public void writeHeader(ImageMeta srcHeader) throws IOException {
 
    }    
}
