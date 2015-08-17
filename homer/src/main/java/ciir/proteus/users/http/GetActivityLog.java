package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.error.DBError;
import org.lemurproject.galago.utility.Parameters;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author michaelz.
 */
public class GetActivityLog extends DBAction {

    public GetActivityLog(ProteusSystem proteus) {
        super(proteus);
    }

    @Override
    public Parameters handle(String method, String path, Parameters reqp, HttpServletRequest req) throws HTTPError, DBError, IOException {

        List<String> fileList = new ArrayList<String>();
        // code from: http://stackoverflow.com/questions/3008043/list-all-files-from-directories-and-subdirectories-in-java
        try {
            Path startPath = Paths.get("logs");
            Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir,
                                                         BasicFileAttributes attrs) {
                    //System.out.println("Dir: " + dir.toString());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    //System.out.println("File: " + file.toString());
                    fileList.add(file.toString());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException e) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        // make sure we're in the correct order
        Collections.sort(fileList);

        Parameters events = Parameters.create();
        StringBuilder sb = new StringBuilder();

        for (String fp : fileList){

            BufferedReader br = new BufferedReader(new FileReader(fp));
            try {

                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }

            } finally {
                br.close();
            }
        }

        events.put("events", sb.toString());
        return events;
    }
}
