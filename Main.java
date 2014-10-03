import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Created by mrx on 01.10.14.
 */
public class Main {
    public static void main(String[] args) {
        FakeIndexer fakeIndexer = new FakeIndexer();
        try {
            System.out.println("Start");
            Map<String, LongList > res = fakeIndexer.makeIndex(Paths.get("/home/mrx/GitRepos/intellij-community"));
            System.out.println("Done - " + res.size());
            res.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class FakeIndexer {
        private final String TEXT_MIME_PREFIX = "text/";

        public Map<String, LongList> makeIndex(Path largeDir) throws IOException {
            List<Path> filesList = getFiles(largeDir);
            long total = filesList.size();
            System.out.println("Files list is obtained: " + total);
            BufferedReader br;
            Map<String, LongList > indexMap = new HashMap<String, LongList >();
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

        private void addWord(Map<String, LongList > res, long lastId, String word) {
            if(res.containsKey(word)) {
                LongList files = res.get(word);
                if(files.last() != lastId) {
                    res.get(word).add(lastId);
                }
            } else {
                LongList  newIds = new LongList (lastId);
//                newIds.add();
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
