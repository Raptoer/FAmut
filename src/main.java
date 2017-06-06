import java.io.File;
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
                    String[] cmd = { "C:\\apps\\PBO Manager v.1.4 beta\\PBOConsole.exe", "-unpack", pbo.toString(), tempFolder};
                    Process p = null;
                    try {
                        p = Runtime.getRuntime().exec(cmd);
                        p.waitFor();
                        Path primary = Paths.get(tempFolder);
                        Stream<Path> existingFiles = getChildren(primary);
                        existingFiles.filter(e -> !e.equals(primary)).forEach( existingFile -> {
                            try {
                                Path shortened = primary.relativize(existingFile);
                                if(Files.exists(oldFramework.resolve(shortened))
                                        && !Files.exists(ignoreFile.resolve(shortened))) {
                                    Files.delete(Paths.get(tempFolder + "\\" + shortened.toString()));
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
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });


                        Stream<Path> replaceFiles = getChildren(replaceTemplates);
                        replaceFiles.filter(e -> !e.equals(replaceTemplates)).forEach( replaceFile -> {
                            try {
                                Path shortened = replaceTemplates.relativize(replaceFile);
                                if(Files.exists(Paths.get(tempFolder).resolve(shortened))) {
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
                                        toBeReplacedJoined = toBeReplacedJoined.replace(pieces[i], pieces[i+1]);
                                    }
                                    Files.write(Paths.get(tempFolder).resolve(shortened), Arrays.asList(toBeReplacedJoined), StandardOpenOption.TRUNCATE_EXISTING);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                        String pboOutput = outputFile.toAbsolutePath().toString() + "\\" + (pbo.getFileName().toString() + "U").replace(".pbo", "") + ".pbo";
                        String[] outputCmd = { "C:\\apps\\PBO Manager v.1.4 beta\\PBOConsole.exe", "-pack", tempFolder, pboOutput};
                        p = Runtime.getRuntime().exec(outputCmd);
                        p.waitFor();


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
