package ciir.proteus.util;

import org.lemurproject.galago.core.index.disk.DiskNameReverseReader;

import java.io.IOException;

/**
 * Created by michaelz on 12/7/2015.
 * Singleton class.
 */
public class LoadNamesReverseUtil {
  private static LoadNamesReverseUtil ourInstance = null; // new LoadNamesReverseUtil();
  private DiskNameReverseReader reader;

  public static DiskNameReverseReader getReader(String namesReverseSourcePath) {
    if (ourInstance == null){
      ourInstance = new LoadNamesReverseUtil(namesReverseSourcePath);
    }
    return ourInstance.reader;
  }

  private LoadNamesReverseUtil(String namesReverseSourcePath) {
    try {
      reader = new DiskNameReverseReader(namesReverseSourcePath);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
