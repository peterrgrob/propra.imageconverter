package propra.imageconverter.image.huffman;

import java.io.IOException;
import propra.imageconverter.data.DataFormat;
import propra.imageconverter.image.ImageMeta;
import propra.imageconverter.image.ImageResource;

/**
 *
 * @author pg
 */
public class HuffmanResource extends ImageResource {
    
    public HuffmanResource(String file, 
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
}
