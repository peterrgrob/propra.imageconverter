package propra.imageconverter.image;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import propra.imageconverter.data.IDataCallback;
import propra.imageconverter.data.IDataFilter;

/**
 *
 * @author pg
 */
public class ImageTranscoderRLE extends ImageTranscoder {
    
    private byte[] writeRleColor = new byte[3];
    private int writeColorCounter;
    
    /**
     * 
     * @param colorFormat 
     * @param dataFilter 
     */
    @Override
    public void begin(  ColorFormat colorFormat, 
                        IDataFilter dataFilter) {
        // Operation initialisieren
        super.begin(colorFormat, dataFilter);
    }
    
    /**
     * 
     * @param out
     */
    @Override
    public void decode( RandomAccessFile in, 
                        IDataCallback out) throws IOException {
        
        int currentPacketHeader;
        int currentRepetitionCount;
        byte[] currentRleColor = new byte[3];
        
        ByteBuffer dataBlock = ByteBuffer.allocate(DATA_BLOCK_SIZE);
        ByteBuffer dataOut = ByteBuffer.allocate(DATA_BLOCK_SIZE);
        
        // Blockweise Daten einlesen
        while(in.getFilePointer() < in.length()) {
            
            // Blockpuffer anpassen gegen Ende
            if(in.getFilePointer() + dataBlock.capacity() > in.length()) {
                dataBlock.limit(dataBlock.capacity() - (int)((in.getFilePointer() + dataBlock.capacity()) - in.length()));
            }
            
            // Block lesen
            in.read(dataBlock.array(), 0, dataBlock.limit());

            // Block gemäß RLE verarbeiten
            while(dataBlock.remaining() != 0) {

                // Paketkopf und Wiederholungen einlesen
                currentPacketHeader =  dataBlock.get() & 0xFF;
                currentRepetitionCount = getRepetitionCount(currentPacketHeader);

                // Anzahl der zu extrahierenden Bytes
                int currentBytes = currentRepetitionCount * 3;

                // Neuen Leseblock beginnen?
                if(dataBlock.remaining() < currentBytes) {
                    in.seek( in.getFilePointer() - dataBlock.remaining() - 1);
                    break;
                } 

                // Ausgabeblock übertragen, wenn gefüllt
                if( dataOut.position() + currentBytes > dataOut.capacity()) {
                    applyCallback(out, dataOut);
                }

                // RLE oder RAW Paket?
                if(isRlePacket(currentPacketHeader)) {

                    // Farbwert auslesen
                    dataBlock.get(currentRleColor);

                    // Farbwert in Ausgabebuffer schreiben
                    for(int i=0;i<currentRepetitionCount;i++) {
                        dataOut.put(currentRleColor);
                    }
                } else {

                    // RAW Farben übertragen
                    dataOut.put(dataBlock.array(), 
                                dataBlock.position(), 
                                currentBytes);
                    dataBlock.position(dataBlock.position() + currentBytes);
                }
            }

            // Eingabedaten filtern
            dataBlock.flip();
            applyFilter(dataBlock);
            dataBlock.clear();
        } 
        
        // Restdaten ausgeben
        applyCallback(out, dataOut);
    }

    /**
     * 
     * @param in
     */
    @Override
    public void encode( RandomAccessFile out, 
                        ByteBuffer in,
                        boolean endBlock) throws IOException{

  /*      int colorSize = 3;
        int rawCounter = 0;
           
        // Über Bytes iterieren und gemäß RLE verarbeiten
        while(in.position() < in.limit()) {
         
            // Anzahl gleicher Farben zählen
            writeColorCounter = countRleColor(in, in.limit());
            if(writeColorCounter > 1) {
                
                // RLE Block verarbeiten
                // Farbwert speichern
                in.get(writeRleColor);
                
                // Paketkopf und Farbwert schreiben
                out.write((byte)(127 + writeColorCounter));
                out.write(writeRleColor);
                
                // Gleiche Farben im Eingabepuffer überspringen
                in.position(in.position() + (writeColorCounter - 1) * colorSize);
                
            } else {
                
                // Raw Block verarbeiten
                int headerPosition = (int)out.getFilePointer();
                
                // Paketkopfbyte überspringen 
                out.seek(out.getFilePointer() + 1);
                
                // Unterschiedliche Farben iterieren
                while(!compareColor(in.array(), 
                                    in.position(),
                                    in.position() + colorSize) && rawCounter <= 127) {
                    
                    // Farbe übertragen
                    in.get(color);
                    out.write(color);
                                    
                    rawCounter++;
                }
                
                // Paketkopf speichern
                long p = (int)out.getFilePointer();
                out.seek(headerPosition);
                out.write((byte)(rawCounter - 1));
                out.seek(p);               
                
                rawCounter = 0;
            }
        }*/
    }
    
    /**
     *
     * @return
     */
    @Override
    public boolean isValid() {
        return true;
    }
    
    /** 
     *
     * @return
     */
    private static boolean isRlePacket(int packet) {
        return packet > 127;
    }
    
    /**
     *
     * @return
     */
    private static int getRepetitionCount(int packet) {
        return (packet & 127) + 1;
    }
    
    /**
     * 
     * @param data
     * @return 
     */
    private int countRleColor(ByteBuffer data, int length) {
        byte[] array = data.array();
        int baseOffset = data.position();
        int runningOffset = baseOffset + 3;
        int counter = 1;
        
        // Zählen solange Farben gleich sind
        while (compareColor(array, baseOffset, runningOffset)) {

            counter++;
            runningOffset += 3;
            
            // Zähler max, oder Ende erreicht?
            if( counter > 127
            ||  runningOffset >= length) {
                break;
            }
        }
        
        return counter;
    }
    
        /**
     * 
     * @param data
     * @return 
     */
    private int countDifferentColors(ByteBuffer data) {
        byte[] src = data.array();
        int baseOffset = data.position();
        int runningOffset = baseOffset + 3;
        int counter = 1;
        
        // Zählen solange Farben ungleich sind
        while (!compareColor(src, baseOffset, runningOffset)
            && runningOffset < data.limit()
            && counter < 128) {

            counter++;
            runningOffset += 3;
        }
        
        return counter;
    }
   
    /**
     * 
     * @param array
     * @param offset0
     * @param offset1
     * @return 
     */
    boolean compareColor(byte[] array, int offset0, int offset1) {
        return (array[offset0 + 0] == array[offset1 + 0]
            &&  array[offset0 + 1] == array[offset1 + 1]
            &&  array[offset0 + 2] == array[offset1 + 2]);
    }
}
