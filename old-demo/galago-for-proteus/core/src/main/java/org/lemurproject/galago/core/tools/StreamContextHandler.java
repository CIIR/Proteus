// BSD License (http://lemurproject.org/galago-license)
package org.lemurproject.galago.core.tools;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mortbay.jetty.handler.ContextHandler;

/**
 * Uses Java object serialization to answer requests over the 
 * wire. Doesn't generalize, but it's probably the fastest method.
 *
 * This handler does nothing but forward the remote java request to
 * the retrieval object attached to the Search object.
 *
 * @author irmarc
 */
public class StreamContextHandler extends ContextHandler {

  Search search;

  public StreamContextHandler(Search search) {
    this.search = search;
  }

  @Override
  public void handle(String target, HttpServletRequest request,
          HttpServletResponse response, int dispatch) throws IOException, ServletException {

    try {
      // Recover method
      ObjectInputStream ois = new ObjectInputStream(request.getInputStream());
      String methodName = ois.readUTF();

      // Get arguments
      int numArgs = (int) ois.readShort();
      Class argTypes[] = new Class[numArgs];

      for (int i = 0; i < numArgs; i++) {
        argTypes[i] = (Class) ois.readObject();
      }

      Object[] arguments = new Object[numArgs];
      for (int i = 0; i < numArgs; i++) {
        arguments[i] = ois.readObject();
      }

      // NOW we can get the method itself and invoke it on our retrieval object
      // with the extracted arguments
      Method m = search.retrieval.getClass().getMethod(methodName, argTypes);
      Object result = m.invoke(search.getRetrieval(), arguments);

      // Finally send back our result
      ObjectOutputStream oos = new ObjectOutputStream(response.getOutputStream());
      oos.writeObject(result);
      response.flushBuffer();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
