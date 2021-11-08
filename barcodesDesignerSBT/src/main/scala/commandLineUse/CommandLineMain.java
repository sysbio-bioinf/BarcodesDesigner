package main.code.commandLineUse;

import main.code.algorithm.*;

import static main.code.commandLineUse.CommandLineHelpers.generateBarcodePattern;

public class CommandLineMain {

    /**
     * Starts the selection process based on command line arguments and without
     * displaying the GUI. Results are written to stdout, while errors and
     * progress messages are written to stderr.
     *
     * @param args
     *            The command line arguments
     */
    public static void main(String[] args) {
        // specify the valid command line arguments and their types and default
        // values
        CommandLineParser parser = new CommandLineParser();
        parser.add("--type", "select|generate", ArgType.TYPE_STRING(), "", false);
        parser.add(new String[] { "-file", "-f" }, "barcode text file",
                ArgType.TYPE_STRING(), "", true);
        parser.add(new String[] { "-nbarcodes", "-nb" },
                "number of barcode candidates", ArgType.TYPE_INT(), 100, true);
        parser.add(new String[] { "-length", "-l" }, "barcode length",
                ArgType.TYPE_INT(), 12, true);
        parser.add(new String[] { "-pattern", "-p" }, "barcode pattern",
                ArgType.TYPE_STRING(),
                generateBarcodePattern(12), true);
        parser.add("-gcmin", "minimum G/C percentage", ArgType.TYPE_DOUBLE(),
                40.0, true);
        parser.add("-gcmax", "maximum G/C percentage", ArgType.TYPE_DOUBLE(),
                60.0, true);
        parser.add(new String[] { "-hamming", "-h" }, "", ArgType.TYPE_BOOL(),
                false, true);
        parser.add(new String[] { "-popsize", "-ps" },
                "population size for Genetic Algorithm", ArgType.TYPE_INT(),
                100, true);
        parser.add(new String[] { "-niter", "-ni" },
                "number of iterations for Genetic Algorithm",
                ArgType.TYPE_INT(), 1000, true);
        parser.add(new String[] { "-nrun", "-nr" },
                "number of runs for Genetic Algorithm", ArgType.TYPE_INT(), 1,
                true);
        parser.add(new String[] { "-mindist", "-md" },
                "minimum distance for early stopping", ArgType.TYPE_INT(), Integer.MAX_VALUE,
                true);
        parser.add(new String[] { "-balancecolors", "-bc" }, "",
                ArgType.TYPE_BOOL(), false, true);
        parser.add(new String[] { "-quiet", "-q" }, "", ArgType.TYPE_BOOL(),
                false, true);
        parser.add(new String[] { "-outtype", "-ot" },
                "output format (text or json)", ArgType.TYPE_STRING(), "text",
                true);
        parser.add(new String[] { "-outputfile", "-of" },
                "output file (specify file type by ending)",
                ArgType.TYPE_STRING(), "", true);
        parser.add(new String[] { "-distMetric", "-dm"},
                "distance metric (levenshtein or hamming)", ArgType.TYPE_STRING(), "levenshtein", true);
        parser.add(new String[] { "-parallel", "-par"},
                "amount of parallel streams for recombination/mutation/fitness update", ArgType.TYPE_INT(), 4, true);
        try {
            // parse and verify the supplied command line arguments
            parser.parseArgs(args);

            String type = parser.getString("--type");
            if (!type.equals("select") && !type.equals("generate"))
                throw new ParseException(
                        "--type must be either \"select\" or \"generate\"!");


            // depending on whether the user requested a quiet run
            // build a listener that outputs the progress or a listener
            // that does nothing
            BarcodeProgressListener progress;
            if (!parser.getBoolean("-quiet")) {
                progress = (percentage, message) -> {
                    System.err.println(message);
                    return true;
                };
            } else {
                progress = (percentage, message) -> true;
            }

            // get GA parameters
            int numIterations = parser.getInt("-niter");
            int numIndividuals = parser.getInt("-popsize");
            int numRuns = parser.getInt("-nrun");
            int numStreams = parser.getInt("-parallel");

            // verify range of GA parameters
            if (numIterations <= 0 || numIndividuals <= 0 || numRuns <= 0)
                throw new ParseException(
                        "-niter, -nrun and -popsize must be greater than 0!");
            if (numStreams <= 0 || numStreams > 12) {
                throw new ParseException(
                        "-p must be an integer, with 0 < p < 11");
            }
            // check output format
            String outputType = parser.getString("-outtype").toLowerCase();
            if (!outputType.equals("text") && !outputType.equals("json"))
                throw new ParseException(
                        "-outtype must be \"text\" or \"json\"");
            String[] barcodes;

            // check distance metric
            String distanceMetric = parser.getString("-distMetric").toLowerCase();
            if (!distanceMetric.equals("levenshtein") && !distanceMetric.equals("hamming"))
                throw new ParseException(
                        "-distMetric must be \"levenshtein\" or \"hamming\"");
            if (distanceMetric.equals("hamming")) {
                BarcodeDistanceCalculator.setDistanceType(0);
            } else {
                BarcodeDistanceCalculator.setDistanceType(1);
            }
            double minGC = parser.getDouble("-gcmin");
            double maxGC = parser.getDouble("-gcmax");
            boolean generateHamming = parser.getBoolean("-hamming");

            if (minGC < 0 || maxGC < 0 || minGC > 100 || maxGC > 100)
                throw new ParseException(
                        "-gcmin and -gcmax must be >= 0 and <= 100!");

            if (minGC > maxGC)
                throw new ParseException(
                        "-gcmin must not be greater than -gcmax!");

            if (type.equals("select")) {
                // barcode subset selection

                if (parser.isSet("-mindist"))
                    throw new ParseException(
                            "Parameter \"-mindist\" is not allowed with \"--type select\"!");

                boolean balanceColors = parser.getBoolean("-balancecolors");

                if (parser.isSet("-file")) {
                    // barcodes should be loaded from a file

                    // ensure that generation parameter are not set at the same
                    // time
                    if (parser.isSet("-length") || parser.isSet("-pattern")
                            || parser.isSet("-nb") || parser.isSet("-gcmin")
                            || parser.isSet("-gcmax"))
                        throw new ParseException(
                                "-file cannot be combined with the generation parameters -length, -pattern, -gcmin and -gcmax!");

                    // load the barcodes from a file
                    String file = parser.getString("-file");
                    progress.progress(0, "Loading barcodes from file...");
                    barcodes = BarcodeReader.readBarcodes(file);

                } else {
                    // barcodes should be generated randomly

                    // if only the length has been supplied, adapt the default
                    // barcode pattern
                    if (!parser.isSet("-pattern") && parser.isSet("-length"))
                        parser.setString("-pattern", generateBarcodePattern(parser
                                        .getInt("-length")));

                    String barcodePattern = parser.getString("-pattern");

                    // verify that specified barcode length and pattern length
                    // are
                    // the same
                    if (parser.getInt("-length") != barcodePattern.length())
                        throw new ParseException(
                                "Specified barcode length and pattern length do not coincide!");

                    // no empty barcodes
                    if (barcodePattern.length() == 0)
                        throw new ParseException(
                                "Barcode length must be greater than 0!");

                    // barcodes must contain nucleotides
                    if (!barcodePattern.matches("[ACGTacgt_]*"))
                        throw new ParseException(
                                "Invalid characters in barcode pattern! Must only contain the characters: " +
                                        "a, c, g, t, A, C, G, T, _");

                    // read and verify further generation parameters
                    int numBarcodes = parser.getInt("-nbarcodes");

                    if (numBarcodes <= 0)
                        throw new ParseException(
                                "-nbarcodes must be greater than 0!");

                    // start generation of barcodes
                    progress.progress(0, "Generating barcodes...");
                    BarcodeGenerator generator = new BarcodeGenerator(
                            barcodePattern, minGC / 100.0, maxGC / 100.0, null);
                    barcodes = generator.generateBarcodes(numBarcodes, 1000,
                            generateHamming);

                }
                // start selection of barcode subsets
                progress.progress(0, "Selecting barcode subsets...");
                BarcodeSelector selector = new BarcodeSelector(barcodes);
                BarcodeSetCollection selectedSets = selector
                        .selectBarcodesGA(numIndividuals, numIndividuals * 2,
                                numRuns, numIterations, balanceColors,
                                numStreams, InitType.INIT_FORWARD(), progress);
                progress.progress(0, "Barcode selection finished!");

                // print out results
                String outString;
                if (outputType.equals("text")) {
                    outString = selectedSets.toString();
                } else {
                    outString = selectedSets.toJSON();
                }
                System.out.println(outString);
                if (parser.isSet("-outputfile")) {
                    String outputPath = parser.getString("-outputfile");
                    FileSaver.saveIfPossible(outputPath, outString);
                }
            } else {
                // barcode set generation

                if (parser.isSet("-balancecolors") || parser.isSet("-file"))
                    throw new ParseException(
                            "Parameters \"-balancecolors\" and \"-file\" are not allowed with \"--type generate\"!");

                // if only the length has been supplied, adapt the default
                // barcode pattern
                if (!parser.isSet("-pattern") && parser.isSet("-length"))
                    parser.setString("-pattern", generateBarcodePattern(parser
                                    .getInt("-length")));

                String barcodePattern = parser.getString("-pattern");

                // verify that specified barcode length and pattern length are the same
                if (parser.getInt("-length") != barcodePattern.length())
                    throw new ParseException(
                            "Specified barcode length and pattern length do not coincide!");

                // no empty barcodes
                if (barcodePattern.length() == 0)
                    throw new ParseException(
                            "Barcode length must be greater than 0!");

                // barcodes must contain nucleotides
                if (!barcodePattern.matches("[ACGTacgt_]*"))
                    throw new ParseException(
                            "Invalid characters in barcode pattern!");

                // read and verify further generation parameters
                int numBarcodes = parser.getInt("-nbarcodes");

                if (numBarcodes <= 0)
                    throw new ParseException(
                            "-nbarcodes must be greater than 0!");

                int minDist = parser.getInt("-mindist");
                if (minDist <= 0)
                    throw new ParseException(
                            "-mindist must be greater than 0!");

                BarcodeSetOptimizer gen = new BarcodeSetOptimizer(
                        barcodePattern, numBarcodes,
                        minGC / 100.0, maxGC / 100.0,
                        generateHamming);
                BarcodeSetCollection generatedSets = gen.optimizeBarcodeSets(
                        numIndividuals, 2 * numIndividuals, numRuns,
                        numIterations, minDist, numStreams, progress);

                // print out results
                String outString;
                if (outputType.equals("text")) {
                    outString = generatedSets.toString();
                } else {
                    outString = generatedSets.toJSON();
                }
                System.out.println(outString);
                if (parser.isSet("-outputfile")) {
                    String outputPath = parser.getString("-outputfile");
                    FileSaver.saveIfPossible(outputPath, outString);
                }
            }
            System.exit(0);
        } catch (ParseException ex) {
            // print error and usage line if something went wrong
            System.err.println("Error: " + ex.getMessage());
            System.err.println();
            System.err.println(parser.usage());
            System.exit(1);
        }
    }
}
