import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class main {
    public static void main(String[] args) throws IOException {
        Path inputFile = Paths.get("input");
        Path outputFile = Paths.get("output");
        Path oldFramework = Paths.get("oldFramework");
        Path newFramework = Paths.get("newFramework");
        Path ignoreFile = Paths.get("ignore");
        Path replaceTemplates = Paths.get("replace");





        Files.list(inputFile).filter(p -> p.getFileName().toString().endsWith(".pbo")).forEach(
                pbo -> {
                    String tempFolder = outputFile.toAbsolutePath().toString() + "\\" + pbo.getFileName().toString().replace(".pbo", "");
                    String logFile = outputFile.toAbsolutePath().toString() + "\\" + pbo.getFileName().toString().replace(".pbo", ".log");
                    BufferedWriter logOut = null;
                    try {
                        logOut = new BufferedWriter(new FileWriter(new File(logFile)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String[] cmd = { "C:\\apps\\PBO Manager v.1.4 beta\\PBOConsole.exe", "-unpack", pbo.toString(), tempFolder};
                    Process p = null;
                    try {
                        ProcessBuilder pb = new ProcessBuilder(cmd);
                        pb.inheritIO();
                        p = pb.start();
                        p.waitFor();
                        Path primary = Paths.get(tempFolder);
                        Stream<Path> existingFiles = getChildren(primary);
                        BufferedWriter finalLogOut = logOut;
                        existingFiles.sorted(Comparator.comparing(Path::getNameCount).reversed()).filter(e -> !e.equals(primary)).forEach(existingFile -> {
                            try {
                                Path shortened = primary.relativize(existingFile);
                                if(Files.exists(oldFramework.resolve(shortened))
                                        && !Files.exists(ignoreFile.resolve(shortened))) {
                                    Files.delete(Paths.get(tempFolder + "\\" + shortened.toString()));
                                    finalLogOut.write("\ndeleting: " + shortened);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                        Stream<Path> newFiles = getChildren(newFramework);
                        newFiles.filter(e -> !e.equals(newFramework)).forEach( newFile -> {
                            try {
                                Path shortened = newFramework.relativize(newFile);
                                if(!Files.exists(ignoreFile.resolve(shortened))) {
                                    Files.copy(newFile, Paths.get(tempFolder + "\\" + shortened.toString()));
                                    finalLogOut.write("\ncopying: " + shortened);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });


                        Stream<Path> replaceFiles = getChildren(replaceTemplates);
                        replaceFiles.filter(e -> !e.equals(replaceTemplates)).forEach( replaceFile -> {
                            try {
                                Path shortened = replaceTemplates.relativize(replaceFile);
                                if(Files.exists(Paths.get(tempFolder).resolve(shortened))
                                        && !Files.isDirectory(replaceFile)) {
                                    List<String> replaceLines = Files.readAllLines(replaceFile);
                                    StringJoiner replaceLinesBunched = new StringJoiner("\r\n");
                                    replaceLines.forEach(replaceLinesBunched::add);
                                    String joined = replaceLinesBunched.toString();
                                    String[] pieces = joined.split("\\/\\|\\/");
                                    List<String> toBeReplaceLines = Files.readAllLines(Paths.get(tempFolder).resolve(shortened));
                                    StringJoiner toBeReplaceLinesBunched = new StringJoiner("\r\n");
                                    toBeReplaceLines.forEach(toBeReplaceLinesBunched::add);
                                    String toBeReplacedJoined = toBeReplaceLinesBunched.toString();
                                    for(int i = 0; i < pieces.length; i = i + 2) {
                                        String pieceNum = pieces[i].substring(0, pieces[i].indexOf("|"));
                                        pieces[i] = pieces[i].substring(pieces[i].indexOf("|")+1);
                                        String toBeReplacedJoinedPre = toBeReplacedJoined;
                                        toBeReplacedJoined = toBeReplacedJoined.replace(pieces[i], pieces[i+1]);
                                        if(!toBeReplacedJoinedPre.equals(toBeReplacedJoined)) {
                                            finalLogOut.write("\nReplaceNum: " + pieceNum + " was used");
                                        }
                                    }
                                    Files.write(Paths.get(tempFolder).resolve(shortened), Arrays.asList(toBeReplacedJoined), StandardOpenOption.TRUNCATE_EXISTING);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                        String pboOutput = outputFile.toAbsolutePath().toString() + "\\" + (pbo.getFileName().toString()).replaceAll("v(\\d+)", "v$1U");
                        String[] outputCmd = { "C:\\apps\\PBO Manager v.1.4 beta\\PBOConsole.exe", "-pack", tempFolder, pboOutput};
                        pb = new ProcessBuilder(outputCmd);
                        pb.inheritIO();
                        p = pb.start();


                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        );


    }

    private static Stream<Path> getChildren(Path root) throws IOException {
        return Files.find(root, 300, new BiPredicate<Path, BasicFileAttributes>() {
            @Override
            public boolean test(Path path, BasicFileAttributes basicFileAttributes) {
                return true;
            }
        }, FileVisitOption.FOLLOW_LINKS);
    }
}
