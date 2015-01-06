package ciir.proteus.util;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.lemurproject.galago.utility.Parameters;
import org.lemurproject.galago.utility.StreamUtil;
import org.lemurproject.galago.utility.json.JSONUtil;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author jfoley.
 */
public class HTTPUtil {
  private final static Logger log =Logger.getLogger(HTTPUtil.class.getName());

  public static Parameters fromHTTPRequest(HttpServletRequest req) throws IOException {
    Parameters reqp = Parameters.create();

    String contentType = req.getContentType();
    // chrome likes to send:
    //   application/x-www-form-urlencoded; charset=UTF-8 len:96
    if(contentType != null && contentType.contains(";")) {
      contentType = contentType.substring(0, contentType.indexOf(";"));
    }

    // GET or POST form parameters handling
    if(contentType == null || "application/x-www-form-urlencoded".equals(contentType)) {
      Map<String, String[]> asMap = (Map<String, String[]>) req.getParameterMap();

      for (Map.Entry<String, String[]> kv : asMap.entrySet()) {
        String arg = kv.getKey();
        String[] values = kv.getValue();

        if (values.length == 1) {
          reqp.put(arg, JSONUtil.parseString(values[0]));
        } else {
          reqp.set(arg, new ArrayList());
          for (String val : values) {
            reqp.getList(arg, Object.class).add(JSONUtil.parseString(val));
          }
        }
      }
      return reqp;
    } else if(contentType.equals("application/json")) {
      // request body as JSON handling
      ServletInputStream sis = req.getInputStream();
      String body = StreamUtil.copyStreamToString(sis);
      sis.close();

      return Parameters.parseString(body);
    } else if(req.getContentLength() > 0) {
      throw new UnsupportedOperationException("Unknown data kind sent to server: "+contentType+" len:"+req.getContentLength());
    }

    return reqp;
  }

  public static String encode(String data) {
    try {
      return URLEncoder.encode(data, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Build a list of form values from a Parameters object, if possible
   * @param p the parameters object
   * @return a list of NameValuePair objects for Apache's HttpClient
   */
  private static List<NameValuePair> fromParameters(Parameters p) {
    List<NameValuePair> parms = new ArrayList<>();
    for(String key : p.keySet()) {
      if(p.isList(key)) {
        for(Object val : p.getList(key)) {
          parms.add(new BasicNameValuePair(key, val.toString()));
        }
      } else {
        parms.add(new BasicNameValuePair(key, p.getAsString(key)));
      }
    }
    return parms;
  }

  public static String encodeInURL(String url, Parameters p) {
    StringBuilder urlb = new StringBuilder();
    urlb.append(url);
    char before = '?';
    for(NameValuePair kv : fromParameters(p)) {
      urlb.append(before)
          .append(encode(kv.getName()))
          .append('=')
          .append(encode(kv.getValue()));
      before = '&';
    }
    return urlb.toString();
  }

  public static Response post(String url, String path, Parameters p) throws IOException {
    log.info("POST url="+url+" path="+path+" p="+p);
    assert(path.startsWith("/"));

    try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
      HttpPost post = new HttpPost(url + path);
      post.setEntity(new UrlEncodedFormEntity(fromParameters(p)));
      HttpResponse response = client.execute(post);
      return new Response(response);
    }
  }

  public static Response get(String url, String path, Parameters p) throws IOException {
    log.info("GET url="+url+" path="+path+" p="+p);
    assert(path.startsWith("/"));

    try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
      String urlWithParams = encodeInURL(url + path, p);
      HttpGet get = new HttpGet(urlWithParams);
      HttpResponse response = client.execute(get);
      return new Response(response);
    }
  }

  public static Response postJSON(String url, String path, Parameters body) throws IOException {
    log.info("DELETE url="+url+" path="+path+" body="+body);
    assert(path.startsWith("/"));

    try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
      HttpPost post = new HttpPost(url + path);
      post.setHeader("Content-Type", "application/json");
      post.setEntity(new StringEntity(body.toString()));
      return new Response(client.execute(post));
    }
  }

  public static class Response {
    public final int status;
    public final String reason;
    public final String body;

    Response(HttpResponse response) throws IOException {
      this.status = response.getStatusLine().getStatusCode();
      this.reason = response.getStatusLine().getReasonPhrase();
      this.body = StreamUtil.copyStreamToString(response.getEntity().getContent());
    }

    @Override
    public String toString() {
      return String.format("%d %s\n%s", status, reason, body);
    }
  }

}
