package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.checksum.Checksum;
import propra.imageconverter.data.DataBlock;
import propra.imageconverter.data.DataFormat;
import propra.imageconverter.data.IDataCallback;

/**
 *
 * @author pg
 */
public class ImageCodecRLE extends ImageCodec {
    
    private byte[] readRleColor = new byte[3];
    private int readColorCounter;
    private int readPacketHeader;

    /**
     * 
     * @param resource
     * @param checksum 
     */
    public ImageCodecRLE(   ImageResource resource, 
                            Checksum checksum) {
        super(resource, checksum);
    }
    
    /**
     * 
     * @param op
     * @param block
     * @throws IOException 
     */
    @Override
    public void processBlock(   DataFormat.Operation op, 
                                DataBlock block,
                                IDataCallback target) throws IOException {
        if(!isValid()
        ||  block == null) {
            throw new IllegalArgumentException();
        }
        
        switch(op) {
            case READ -> {
                decode(block, target);
            }
            case WRITE -> {
                encode(block, target);
            }
        }
    }
    
    /**
     * 
     */
    public void decode( DataBlock block, IDataCallback target) throws IOException {
        
        ByteBuffer dataBlock = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE);
        
        if(block.data == null) {
            block.data = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE);
        }
        
        ByteBuffer dataOut = block.data;
        
        // Blockweise Daten einlesen und dekodieren bis Ausgabeblock gefüllt
        while(resource.position() < resource.length()) {
            
            // Blockpuffer anpassen gegen Ende
            if(resource.position() + dataBlock.capacity() > resource.length()) {
                dataBlock.limit(dataBlock.capacity() - (int)((resource.position() + dataBlock.capacity()) - resource.length()));
            }
            
            // Block lesen
            resource.read(dataBlock);

            // Block gemäß RLE verarbeiten
            while(dataBlock.remaining() != 0) {

                if(readColorCounter == 0) {
                    // Paketkopf und Wiederholungen einlesen
                    readPacketHeader =  dataBlock.get() & 0xFF;
                    readColorCounter = getRepetitionCount(readPacketHeader);
                }
                
                // Anzahl der zu extrahierenden Bytes
                int currentBytes = readColorCounter * 3;

                // Neuen Leseblock beginnen?
                if(dataBlock.remaining() < currentBytes) {
                    resource.position(resource.position() - dataBlock.remaining());
                    break;
                } 

                // Wenn gefüllt, Datenblock zum Empfänger senden
                if( dataOut.position() + currentBytes > dataOut.capacity()) {
                    block.sourceLength = resource.length();
                    block.sourcePosition = resource.position();
                    dataOut.flip();
                    target.send(this, block);
                    dataOut.clear();
                }

                // RLE oder RAW Paket?
                if(isRlePacket(readPacketHeader)) {

                    // Farbwert auslesen
                    dataBlock.get(readRleColor);

                    // Farbwert in Ausgabebuffer schreiben
                    for(int i=0;i<readColorCounter;i++) {
                        dataOut.put(readRleColor);
                    }
                } else {

                    // RAW Farben übertragen
                    dataOut.put(dataBlock.array(), 
                                dataBlock.position(), 
                                currentBytes);
                    dataBlock.position(dataBlock.position() + currentBytes);
                }
                
                readColorCounter = 0;
            }

            // Eingabedaten filtern
            if(checksum != null) {
                dataBlock.flip();
                checksum.apply(dataBlock);
            }
            
            dataBlock.clear();
        } 
        
                    block.sourceLength = resource.length();
                    block.sourcePosition = resource.position();
                    dataOut.flip();
                    target.send(this, block);
                    dataOut.clear();
    }

    /**
     * 
     * @param in
     */
    public void encode( DataBlock block, IDataCallback target) throws IOException{

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
