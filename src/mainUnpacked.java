import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Stream;

public class mainUnpacked {
    public static void main(String[] args) throws IOException {
        Path inputFile = Paths.get("input");
        Path outputFile = Paths.get("output");
        Path replaceContents = Paths.get("replaceContents");
        Path delete = Paths.get("remove");
        Path add = Paths.get("add");
        if(outputFile.toFile().exists()) {
            Files.walk(outputFile, FileVisitOption.FOLLOW_LINKS)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        Files.createDirectory(outputFile);





        Files.list(inputFile).forEach(
                folder -> {
                    String logFile = outputFile.toAbsolutePath().toString() + "\\" + folder.getFileName().toString() + ".log";
                    Path outputDirectory = outputFile.resolve(inputFile.relativize(folder));
                    try {
                        Files.createDirectory(outputDirectory);
                        BufferedWriter logOut = new BufferedWriter(new FileWriter(new File(logFile), false));

                        Stream<Path> existingFiles = getChildren(folder);
                        existingFiles.sorted(Comparator.comparing(Path::getNameCount).reversed()).filter(e -> !e.equals(folder)).forEach(existingFile -> {
                            try {
                                Path shortened = folder.relativize(existingFile);
                                //simply ignore files that exist in the delete dir
                                if(!Files.exists(delete.resolve(shortened))) {
                                    //those in the replaceContents need to be processed
                                    if(Files.exists(replaceContents.resolve(shortened))) {
                                        Path replaceTemplate = replaceContents.resolve(shortened);
                                        replaceAndWrite(existingFile, folder, replaceTemplate, logOut, outputDirectory);
                                    }else {
                                        //otherwise just copy them
                                        Files.copy(existingFile, outputDirectory.resolve(shortened));
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                        //add those in the add dir
                        Stream<Path> addFiles = getChildren(add);
                        addFiles.filter(e -> !e.equals(add)).forEach(addFile -> {
                            Path shortened = add.relativize(addFile);
                            try {
                                Files.copy(addFile, outputDirectory.resolve(shortened));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        });


                        logOut.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );


    }

    private static void replaceAndWrite(Path inputFile, Path inputFolder, Path replaceTemplate, BufferedWriter logWriter, Path outputFolder) throws IOException {

        //ensure parent directories exist
        replaceTemplate.iterator().forEachRemaining(dir -> {
            try {
                if(!dir.equals(replaceTemplate.getName(0)) && !replaceTemplate.endsWith(dir)) {
                    Files.createDirectory(outputFolder.resolve(dir));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        List<String> replaceLines = Files.readAllLines(replaceTemplate);
        StringJoiner replaceLinesBunched = new StringJoiner("\r\n");
        replaceLines.forEach(replaceLinesBunched::add);
        String joined = replaceLinesBunched.toString();
        String[] pieces = joined.split("(?=\\|\\d+\\|)");
        List<String> toBeReplaceLines = Files.readAllLines(inputFile);
        StringJoiner toBeReplaceLinesBunched = new StringJoiner("\r\n");
        toBeReplaceLines.forEach(toBeReplaceLinesBunched::add);
        String toBeReplacedJoined = toBeReplaceLinesBunched.toString();
        for(int i = 0; i < pieces.length; i = i + 2) {
            String pieceNum = pieces[i].substring(1, pieces[i].indexOf("|", 1));
            pieces[i] = pieces[i].substring(pieces[i].indexOf("|", 1)+1).trim();

            String replacePieceNum = pieces[i+1].substring(1, pieces[i+1].indexOf("|", 1));
            pieces[i+1] = pieces[i+1].substring(pieces[i+1].indexOf("|", 1)+1).trim();

            if(!pieceNum.equals(replacePieceNum)) {
                logWriter.write("\nWARNING ReplaceNum: " + pieceNum + " had matching replace piece num" + replacePieceNum);
            }
            String toBeReplacedJoinedPre = toBeReplacedJoined;
            toBeReplacedJoined = toBeReplacedJoined.replace(pieces[i], pieces[i+1]);
            if(!toBeReplacedJoinedPre.equals(toBeReplacedJoined)) {
                logWriter.write("\nFile:" + inputFile + " ReplaceNum: " + pieceNum + " was used");
            }
        }
        Files.write(outputFolder.resolve(inputFolder.relativize(inputFile)), Arrays.asList(toBeReplacedJoined), StandardOpenOption.CREATE,StandardOpenOption.CREATE_NEW, StandardOpenOption.TRUNCATE_EXISTING);

    }

    private static Stream<Path> getChildren(Path root) throws IOException {
        return Files.find(root, 300, (path, basicFileAttributes) -> true, FileVisitOption.FOLLOW_LINKS);
    }
}
