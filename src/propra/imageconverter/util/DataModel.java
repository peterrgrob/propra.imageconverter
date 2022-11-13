package propra.imageconverter.util;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author pg
 */
public class DataModel implements Validatable {
    
    protected DataTranscoder encoder;
    protected DataTranscoder decoder;
    protected RandomAccessFile readFile;
    protected RandomAccessFile writeFile;
        
    public enum IOMode {
        READ,
        WRITE,
        NONE;
    } 
    
    /**
     *
     */
    public DataModel() {
        
    }
    
    /**
     *
     * @param mode
     * @param file
     * @param coder
     */
    public DataModel(IOMode mode,
                    RandomAccessFile file, 
                    DataTranscoder coder) {
        if(mode == IOMode.READ) {
            readFile = file;
            decoder = coder;
        } else if(mode == IOMode.WRITE) {
            writeFile = file;
            encoder = coder;            
        }
    }
    
    /**
     *
     * @param mode
     */
    public void begin(IOMode mode) {
        if(mode == IOMode.READ) {
            if(decoder != null) {
                decoder.begin();
            }
        } else if(mode == IOMode.WRITE) {
            if(encoder != null) {
                encoder.begin();
            }
        }
    }
    
    /**
     *
     * @param data
     * @return
     * @throws IOException
     */
    public long write(DataBuffer data) throws IOException {
        if(!isValid()
        ||  readFile == null) {
            throw new IllegalStateException();
        }
        
        DataBuffer writeBuffer = data;
        
        if(encoder != null) {
            writeBuffer = new DataBuffer();
            encoder.transcode(DataTranscoder.Operation.ENCODE, data, writeBuffer);
        }
        
        // Bytes in Stream schreiben
        readFile.write(writeBuffer.getBytes(), 0, writeBuffer.getCurrDataLength());
        return writeBuffer.getCurrDataLength();
    }
    
    /**
     *
     * @param buffer
     * @return
     * @throws IOException
     */
    public long read(DataBuffer buffer) throws IOException {
        if(!isValid()
        ||  readFile == null) {
            throw new IllegalStateException();
        }
        
        DataBuffer readBuffer = buffer;
        
        readFile.read(buffer.getBytes(), 0, buffer.getCurrDataLength());
        
        if(decoder != null) {
            readBuffer = new DataBuffer();
            encoder.transcode(DataTranscoder.Operation.DECODE, buffer, readBuffer);
        }

        return readBuffer.getCurrDataLength();
    }
    
    public void end(IOMode mode) {
        if(mode == IOMode.READ) {
            decoder.end();
        } else if(mode == IOMode.WRITE) {
            encoder.end();
        }
    }

    @Override
    public boolean isValid() {
        return true;
    }
    
}
