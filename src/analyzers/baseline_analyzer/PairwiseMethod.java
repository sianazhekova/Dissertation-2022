package analyzers.baseline_analyzer;

import java.io.*;

public class PairwiseMethod implements PairwiseMethodInterface {

    // These will be needed for the parsing statistics. For now, they will be avoided
    private int numLoopStarts;
    private int numLoopEnds;
    private int numClosedLoopEnds;
    private String filePath;

    public PairwiseMethod() {
        numLoopStarts = 0;
        numLoopEnds = 0;
        numClosedLoopEnds = 0;
        // For now, just try with a single file path
        filePath = "C:\\Users\\siana\\Work\\Year 3\\New Dissertation Part ll\\Static Analysis\\Generated text files\\nasa_int_sort_2_loops_stores_only.txt";
    }

    // The driver program
    public void pairwiseMethod() throws IOException {

        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Start:") || line.startsWith("End:")) {
                    System.out.println("A loop has been encountered");

                } else {
                    assert(line.startsWith("Store") || line.startsWith("Load"));



                }





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

}
