package propra.imageconverter.image;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Closeable;
import propra.imageconverter.util.Checkable;
import propra.imageconverter.util.Checksum;
import propra.imageconverter.util.DataBuffer;
import propra.imageconverter.util.DataTranscoder;
import propra.imageconverter.util.Validatable;

/**
 * Basisklasse für Bildformatspezifische Konvertierungen. 
 * 
 * @author pg
 */
public abstract class ImageModel implements Closeable, 
                                            Checkable, 
                                            Validatable {
        
    protected final int BLOCK_SIZE = 1024 * 4096 * 3;
    protected DataBuffer temporaryBuffer = new DataBuffer(BLOCK_SIZE);
    protected long bytesTransfered;

    // Farbverarbeitung
    protected ImageFilterColor colorFilter; 
    protected ImageTranscoder encoding;   
     
    protected RandomAccessFile stream;
    protected Checksum checksumObj;  
    protected ImageHeader header;
    protected int headerSize;


    /**
     *
     * @param stream
     */
    public ImageModel(RandomAccessFile stream) {
        this.stream = stream;
        this.colorFilter = new ImageFilterColor();
    }
            
    /** 
     * @param info
     * @throws java.io.IOException
     */
    public abstract void writeHeader(ImageHeader info) throws IOException;

    /**
     * Wandelt Bytes in einen allgmeinen ImageHeader um. 
     * 
     * @return
     * @throws java.io.IOException
     */
    public abstract ImageHeader readHeader() throws IOException;
    
    
    /**
     *
     */
    public void beginImageBlocks() {
        // Prüfsumme initialisieren
        if(isCheckable()) {
            checksumObj.begin();
        }
        
        // Eingabeformat für Konvertierung setzen
        colorFilter.setOutFormat(header.getColorFormat());
        colorFilter.begin();
        
        // Kompression initialisieren
        encoding = header.getColorFormat().createTranscoder();
        if(encoding != null) {
            encoding.begin(header.getColorFormat());
        }
        
        bytesTransfered = 0;
    }
    
    /**
     * Wandelt Bilddaten in bytes um. 
     * 
     * @param data
     * @param colorFormat
     * @return
     * @throws java.io.IOException
     */
    public DataBuffer writeImageBlock(DataBuffer data, ColorFormat colorFormat) throws IOException {
        if(!isValid() 
        || data == null) {
            throw new IllegalArgumentException();
        }
        
        // Ausgabeformat für Konvertierung setzen und Block konvertieren
        colorFilter.setInFormat(colorFormat);
        colorFilter.filter(data);   
        
        DataBuffer writeBuffer = data;
        
        // Kompression erforderlich?
        if(encoding != null) {
            
            // Block komprimieren
            encoding.transcode( DataTranscoder.Operation.ENCODE, 
                                data, 
                                temporaryBuffer);
            writeBuffer = temporaryBuffer;
        } 
        
        // Prüfsumme mit aktuellem Block aktualisieren
        updateChecksum(writeBuffer); 
        
        // Block in Datei schreiben
        writeDataToStream(writeBuffer, 0, writeBuffer.getCurrDataLength());
        
        // Anzahl der kodierten Bytes merken
        bytesTransfered += writeBuffer.getCurrDataLength();
        
        return data;
    }
    
    /**
     * Erstellt Image aus Byte Daten 
     * 
     * @param buffer
     * @return
     * @throws java.io.IOException
     */
    public int readImageBlock(DataBuffer buffer) throws IOException {
        if(!isValid() 
        || buffer == null) {
            throw new IllegalArgumentException();
        }
        
        // Dekompression erforderlich?
        if(encoding != null) {
            
            // Aktuelle Blockgröße berechnen
            int len = temporaryBuffer.getSize();
            long remainingBytes = stream.length() - stream.getFilePointer();
            if(temporaryBuffer.getSize() > remainingBytes) {
                len = (int)(remainingBytes);
            }
            
            // Block aus der Datei lesen
            readDataFromStream(temporaryBuffer, 0, len);
            
            // Prüfsumme mit aktuellem Block vor dekodierung aktualisieren
            updateChecksum(temporaryBuffer);  
            
            // Block dekomprimieren
            encoding.transcode( DataTranscoder.Operation.DECODE, 
                                        temporaryBuffer, 
                                        buffer);
            
        } else {
            
            // Aktuelle Blockgröße berechnen
            int len = buffer.getSize();
            if(bytesTransfered + buffer.getSize() > header.getImageSize()) {
                len = (int)(header.getImageSize() - bytesTransfered);
            }
            // Daten aus der Datei direkt in buffer lesen ohne Dekompression
            readDataFromStream(buffer, 0, len);
            
            // Prüfsumme mit aktuellem Block aktualisieren
            updateChecksum(buffer);  
        } 
        
        // Anzahl der dekodierten Bytes merken
        bytesTransfered += buffer.getCurrDataLength(); 
        
        return buffer.getCurrDataLength();
    }
     
    /**
     *
     * @return
     */
    public long endImageBlocks() {
        
        if(isCheckable()) {
            checksumObj.end();
        }
        
        // Farbkonvertierung abschließen
        colorFilter.end(); 
        
        // Kompression abschließen
        if(encoding != null) {
            encoding.end();
        }
        
        // Anzahl aller bearbeiteten Bytes zurückgeben
        return bytesTransfered;
    }
    
    /**
     *
     * @return
     * @throws IOException
     */
    public boolean hasMoreImageData() throws IOException {
        return (header.getImageSize() - bytesTransfered) != 0;
    }
    
    /** 
     * 
     * @return
     */
    @Override
    public boolean isCheckable() {
        return (checksumObj != null);
    }
    
    /**
     *
     * @throws java.io.IOException
     */
    @Override
    public void close() throws IOException {
        if(stream != null) {
            stream.close();
        }
    }

    /**
     *
     * @param buffer
     * @param offset
     * @param len
     * @return
     * @throws IOException
     */
    protected DataBuffer readDataFromStream(DataBuffer buffer, int offset, int len) throws IOException {
        if(stream == null
        || buffer == null) {
            throw new IllegalArgumentException();
        }
        
        // Bytes vom Stream lesen
        int rl = stream.read(buffer.getBytes(), offset, len);
        if(rl != len) {
            throw new IOException();
        }
        
        // Tatsächlich gelesene Bytes im Buffer vermerken
        buffer.setCurrDataLength(len);
        
        return buffer;
    }
    
    /**
     *
     * @param buffer
     * @param offset
     * @param len
     * @return
     * @throws IOException
     */
    protected DataBuffer writeDataToStream(DataBuffer buffer, int offset, int len) throws IOException {
        if(stream == null 
        || buffer == null) {
            throw new IllegalArgumentException();
        }
        
        // Bytes in Stream schreiben
        stream.write(buffer.getBytes(), offset, len);
        
        return buffer;
    }
    
    /**
     *
     * @return
     */
    @Override
    public Checksum getChecksumObj() {
        return checksumObj;
    }

    /**
     *
     * @return
     */
    @Override
    public long getChecksum() {
        if( isCheckable()
        &&  checksumObj != null) {
            return checksumObj.getValue();
        }
        return 0;
    }
    
    /**
     *
     * @param bytes
     */
    protected void updateChecksum(DataBuffer bytes) {
        if (bytes == null ) {
            throw new IllegalArgumentException();
        }
        if(isCheckable()) {
            checksumObj.filter(bytes);
        }
    } 
    
    /**
     *
     * @return
     */
    @Override
    public boolean isValid() {
        return (    header != null 
                &&  stream != null);
    }

    /**
     *
     * @return
     */
    public int getHeaderSize() {
        return headerSize;
    }

    /**
     *
     * @return
     */
    public int getBlockSize() {
        return BLOCK_SIZE;
    }

    /**
     *
     * @return
     */
    public ImageHeader getHeader() {
        return header;
    }
    
    /**
     *
     * @return
     */
    public ColorFormat getColorFormat() {
        if(header == null) {
           throw new IllegalArgumentException();
        }
        return header.getColorFormat();
    }
}
