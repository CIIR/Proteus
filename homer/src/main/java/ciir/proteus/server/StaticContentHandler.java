package ciir.proteus.server;

import org.lemurproject.galago.utility.Parameters;
import org.lemurproject.galago.utility.StreamUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author jfoley
 */
public class StaticContentHandler {

    private static final Logger log = Logger.getLogger(StaticContentHandler.class.getName());
    public final List<File> baseDirs;
    public final File defaultPath;

    public StaticContentHandler(Parameters argp) {
        Parameters p = argp.getMap("content");

        // collect list of content directories
        baseDirs = new ArrayList<>();
        for (String dir : p.getAsList("dir", String.class)) {
            // assert that we have a content directory that seems to exist
            File baseDirectory = new File(dir);
            if (!baseDirectory.exists()) {
                throw new IllegalArgumentException("content.dir: " + baseDirectory.getAbsolutePath() + " does not exist!");
            }
            if (!baseDirectory.isDirectory()) {
                throw new IllegalArgumentException("content.dir: " + baseDirectory.getAbsolutePath() + " is not a directory!");
            }

            baseDirs.add(baseDirectory);
        }

        // assert that this seems like the right directory
        this.defaultPath = resolvePath(p.get("default", "index.html"));
        if (defaultPath == null) {
            throw new IllegalArgumentException("Default content file: " + p.get("default", "index.html") + " is not found!");
        }
    }

    /**
     * Make sure we don't leak files that aren't children of a base directory.
     */
    public boolean isDescendentOf(File target, File dir) {
        // return true if target is in some subfolder of dir
        File bottom = new File("/");
        while (true) {
            target = target.getParentFile();
            if (target.equals(dir)) {
                return true;
            } else if (target.equals(bottom)) {
                return false;
            }
        }
    }

    /**
     * Resolve a path by searching in the list of directories, in order.
     */
    public File resolvePath(String path) {
        for (File base : baseDirs) {
            File possible = new File(base, path);
            if (possible.exists()) {
                assert (isDescendentOf(possible, base));
                return possible;
            }
        }
        return null;
    }

    public void handle(String path, Parameters reqp, HttpServletResponse resp) throws HTTPError, IOException {
        if (path.contains("..")) {
            throw new HTTPError(HTTPError.BadRequest, "Malformed GET path.");
        }
        if (path.equals("/")) {
            sendFile(defaultPath, path, resp);
        } else {
            File target = resolvePath(path);
            sendFile(target, path, resp);
        }
    }

    String determineContentType(String path) {
        int extensionIndex = path.lastIndexOf('.');
        String extension = path.substring(extensionIndex + 1);

        switch (extension) {
            case "html":
                return "text/html";
            case "js":
                return "application/javascript";
            case "css":
                return "text/css";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            default:
                log.warning("Unhandled extension type: '" + extension + "' for file path: " + path);
                break;
        }
        return "text/plain"; // default
    }

    /**
     * Send a file back to the client
     *
     * @param fp java.io.File object representing the real resource
     * @param path the path requested by the client
     * @param resp the response object
     * @throws HTTPError not found
     * @throws IOException socket screwups
     */
    private void sendFile(File fp, String path, HttpServletResponse resp) throws HTTPError, IOException {
        if (fp == null || !fp.exists()) {
            sendError(resp, HTTPError.NotFound, path + " not found");
            return;
        }
        OutputStream out = null;
        try {
            resp.setContentType(determineContentType(fp.getPath()));
            resp.setStatus(200);
            out = resp.getOutputStream();
            StreamUtil.copyFileToStream(fp, out);
            out.close();
        } catch (IOException e) {
            throw new HTTPError(e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Separate function to hide this IOException
     *
     * @param resp HTTP response
     * @param code HTTP error code
     * @param message HTTP error message
     */
    private void sendError(HttpServletResponse resp, int code, String message) {
        try {
            resp.sendError(code, message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
