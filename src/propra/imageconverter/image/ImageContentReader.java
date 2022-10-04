package propra.imageconverter.image;

import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 *
 * @author pg
 */
public class ImageContentReader extends BufferedInputStream {

    public ImageContentReader(InputStream in) {
        super(in);
    }
    
}
