import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Created by mrx on 01.10.14.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        testMimeTypes();
    }

    private static void testMimeTypes() throws IOException {
        String home = System.getProperty("user.home");
        System.out.println("Home: " + home);
        String mimeTypesFileName = ".mime.types";
        Path mimeTypesPath = Paths.get(home + File.separator + mimeTypesFileName);
        System.out.println(".mime.types path: " + mimeTypesPath.toFile().getAbsolutePath());
        if(isMac()) {
            if(!mimeTypesPath.toFile().exists()) {
                Files.createFile(mimeTypesPath);
                System.out.println(Files.probeContentType(mimeTypesPath));
                FileWriter fileWriter = new FileWriter(mimeTypesPath.toFile());
                List<String> mimeTypes = Arrays.asList("text/plain\n", "text/html");
                for (String m : mimeTypes) {
                    fileWriter.append(m);
                }
                fileWriter.close();
                System.out.println(Files.probeContentType(mimeTypesPath));
//                if(!mimeTypesPath.toFile().delete()) {
//                    System.out.println("Not removed");
//                }
            } else {
                System.out.println(".mime.types exists");
                System.out.println(Files.probeContentType(mimeTypesPath));
            }
        } else {
            System.out.println("Not a mac");
        }
    }

    private static boolean isMac() {
        final String os = System.getProperty("os.name").toLowerCase();
        return os.contains("mac");
    }

    private static void testGC() {
        FakeIndexer fakeIndexer = new FakeIndexer();
        try {
            System.out.println("Start");
            Map<String, LinkedList<Long> > res = fakeIndexer.makeIndex(Paths.get("/home/mrx/GitRepos/intellij-community"));
            System.out.println("Done - " + res.size());
            res.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class FakeIndexer {
        private final String TEXT_MIME_PREFIX = "text/";

        public Map<String, LinkedList<Long>> makeIndex(Path largeDir) throws IOException {
            List<Path> filesList = getFiles(largeDir);
            long total = filesList.size();
            System.out.println("Files list is obtained: " + total);
            BufferedReader br;
            Map<String, LinkedList<Long> > indexMap = new HashMap<String, LinkedList<Long> >();
            long lastFileId = 0;
            int symbol;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < total; i++) {
                System.out.println(i + " of " + total);
                br = new BufferedReader(new FileReader(filesList.get(i).toFile().getAbsolutePath()));
                lastFileId += 1;
                symbol = br.read();
                while (symbol != -1) {
                    if(((char)symbol) != ' ' && ((char)symbol) != '\n') {
                        sb.append((char) symbol);
                    } else {
                        addWord(indexMap, lastFileId, sb.toString().intern());
                        sb.setLength(0);
                    }
                    symbol = br.read();
                }
                addWord(indexMap, lastFileId, sb.toString().intern());
                sb.setLength(0);
                br.close();
            }
            return indexMap;
        }

        private void addWord(Map<String, LinkedList<Long> > res, long lastId, String word) {
            if(res.containsKey(word)) {
                LinkedList<Long> files = res.get(word);
                if(files.getLast() != lastId) {
                    res.get(word).add(lastId);
                }
            } else {
                LinkedList<Long>  newIds = new LinkedList<Long> ();
                newIds.add(lastId);
                res.put(word, newIds);
            }
        }

        private List<Path> getFiles(Path largeDir) throws IOException {
            final List<Path> filesList = new LinkedList<Path>();
            Files.walkFileTree(largeDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String mimeType = Files.probeContentType(file);
                    if (mimeType != null && mimeType.startsWith(TEXT_MIME_PREFIX)) {
                        filesList.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            return filesList;
        }
    }
}
