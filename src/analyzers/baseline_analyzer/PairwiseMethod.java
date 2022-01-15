package analyzers.baseline_analyzer;

import analyzers.readers.EventType;
import analyzers.readers.InstructionsFileReader;
import analyzers.readers.MemBufferBlock;
import jdk.jfr.Event;

import java.io.*;
import java.util.*;

public class PairwiseMethod implements PairwiseMethodInterface {

    // These will be needed for the parsing statistics. For now, they will be avoided
    private int numLoopStarts;
    private int numLoopEnds;
    private int numClosedLoopEnds;
    private long numTrips;
    private String filePath;


    public PairwiseMethod() {
        numLoopStarts = 0;
        numLoopEnds = 0;
        numClosedLoopEnds = 0;
        numTrips = 0;
        // For now, just try with a single file path
        filePath = "C:\\Users\\siana\\Work\\Year 3\\New Dissertation Part ll\\Static Analysis\\Generated text files\\nasa_int_sort_2_loops_stores_only.txt";
    }

    // The driver program
    public void pairwiseMethod() throws IOException {
        try {
            Scanner sc = new Scanner(new File(this.filePath));
            String line;

            LoopStack loopStack = new LoopStack();

            List<MemBufferBlock> memBufferList = new ArrayList<>();
            int parityCount = 0;
            while (sc.hasNextLine()) {
                line = sc.nextLine();
                MemBufferBlock currMemBuff = InstructionsFileReader.parseTraceFileLine(line);
                if (currMemBuff.getEvent() == EventType.STORE || currMemBuff.getEvent() == EventType.LOAD)
                    this.numTrips++;

                System.out.println("Current MemBufferBlock with PC address " + InstructionsFileReader.toHexString(currMemBuff.getAddressPC()) +
                        ", with event type " + EventType.getStringEventType(currMemBuff.getEvent()) +
                        ", and number of trips is " + numTrips
                );

                /*if (currMemBuff.getEvent() != EventType.START && currMemBuff.getEvent() != EventType.END) {
                    System.out.println("Current MemBufferBlock with PC address " + InstructionsFileReader.toHexString(currMemBuff.getAddressPC()) +
                            ", with approx mem ref address " + InstructionsFileReader.toHexString(currMemBuff.getAddressRef()) +
                            ", with size of access " + currMemBuff.getSizeOfAccess() +
                            ", with event type " + EventType.getStringEventType(currMemBuff.getEvent())
                    );
                } else {
                    System.out.println("Current MemBufferBlock with PC address " + InstructionsFileReader.toHexString(currMemBuff.getAddressPC()) +
                            ", with event type " + EventType.getStringEventType(currMemBuff.getEvent())
                    );
                } */

                if (parityCount++ < 2) {
                    memBufferList.add(currMemBuff);
                    //System.out.println("The current list of memory buffers is of size " + memBufferList.size());
                    if (sc.hasNextLine() && memBufferList.size() != 2)
                        continue;
                }
                //System.out.println("h The current list of memory buffers is of size " + memBufferList.size());

                //loopStack.encounterNewAccess(memBufferList, numTrips);


                memBufferList.clear();
                parityCount = 0;
            }

        } catch (FileNotFoundException fileNotFoundException) {
            System.out.println("The provided file name shortcut or filepath is invalid.");
            fileNotFoundException.printStackTrace();
            return;
        }
    }

    public static void main(String[] args) throws IOException {
        // TODO: Add a configuration .json file, mapping the shortcut file names with the respective file paths.
        //  In the args section, only specify the path name shortcut

        PairwiseMethod pm = new PairwiseMethod();

        try {
            pm.pairwiseMethod();
        } catch(IOException ioe) {
            System.out.println("There has been an error in reading the file lines");
        }

    }

    public long getNumTrips() {
        return numTrips;
    }
}
