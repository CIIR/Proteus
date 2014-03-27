package ciir.proteus.server;

import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.Utility;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * @author jfoley
 */
public class StaticContentHandler {
  private static final Logger log = Logger.getLogger(StaticContentHandler.class.getName());
  public final File baseDirectory;
  public final File defaultPath;

  public StaticContentHandler(Parameters argp) {
    Parameters p = argp.getMap("content");

    // assert that we have a content directory that seems to exist
    this.baseDirectory = new File(p.getString("dir"));
    if(!baseDirectory.exists())
      throw new IllegalArgumentException("content.dir: "+baseDirectory.getAbsolutePath()+" does not exist!");
    if(!baseDirectory.isDirectory())
      throw new IllegalArgumentException("content.dir: "+baseDirectory.getAbsolutePath()+" is not a directory!");

    // assert that this seems like the right directory
    this.defaultPath = new File(baseDirectory, p.get("default", "index.html"));
    if(!defaultPath.exists()) {
      throw new IllegalArgumentException("Default content file: "+defaultPath.getAbsolutePath()+" is not found!");
    }
  }

  public boolean isDescendentOf(File target, File dir) {
    // return true if target is in some subfolder of dir
    File bottom = new File("/");
    while(true) {
      target = target.getParentFile();
      if(target.equals(dir)) {
        return true;
      } else if(target.equals(bottom)) {
        return false;
      }
    }
  }

  public void handle(String path, Parameters reqp, HttpServletResponse resp) throws HTTPError, IOException {
    if(path.contains("..")) {
      throw new HTTPError(HTTPError.BadRequest, "Malformed GET path.");
    }
    if(path.equals("/") || path.equals("index.html")) {
      sendFile(defaultPath, path, resp);
    } else {
      File target = new File(baseDirectory, path);
      assert(isDescendentOf(target, baseDirectory));
      sendFile(target, path, resp);
    }
  }

  String determineContentType(String path) {
    int extensionIndex = path.lastIndexOf('.');
    String extension = path.substring(extensionIndex+1);

    if(extension.equals("html")) {
      return "text/html";
    } else if(extension.equals("js")) {
      return "application/javascript";
    } else if(extension.equals("css")) {
      return "text/css";
    } else {
      log.warning("Unhandled extension type: '"+extension+"' for file path: "+path);
    }
    return "text/plain"; // default
  }

  /**
   * Send a file back to the client
   * @param fp java.io.File object representing the real resource
   * @param path the path requested by the client
   * @param resp the response object
   * @throws HTTPError not found
   * @throws IOException socket screwups
   */
  private void sendFile(File fp, String path, HttpServletResponse resp) throws HTTPError, IOException {
    if(!fp.exists()) {
      sendError(resp, HTTPError.NotFound, path+" not found");
      return;
    }

    OutputStream out = null;
    try {
      out = resp.getOutputStream();
      Utility.copyFileToStream(fp, out);
      out.close();
      resp.setContentType(determineContentType(fp.getPath()));
      resp.setStatus(200);
    } catch (IOException e) {
      throw new HTTPError(e);
    } finally {
      if(out != null) {
        out.close();
      }
    }
  }

  /**
   * Separate function to hide this IOException
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
