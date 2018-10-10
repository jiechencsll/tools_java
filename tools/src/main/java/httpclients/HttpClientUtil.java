package httpclients;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONObject;


public class HttpClientUtil {

 private HttpClientUtil() {
 };

 private static final HttpClientUtil hcu = new HttpClientUtil();

 // HttpClient threadlocal
 private static final ThreadLocal<HttpClient> defaultHttpClient = new ThreadLocal<HttpClient>();

 private HttpClient httpClient = null;

 public static final HttpClientUtil getInstance() {
  return hcu;
 }

 // String Http response parse
 private ResponseHandler<String> strResponseHandler = new ResponseHandler<String>() {
  public String handleResponse(final HttpResponse response) throws ClientProtocolException,
          IOException {
   int status = response.getStatusLine().getStatusCode();
   if (status >= 200 && status < 300) {
    HttpEntity entity = response.getEntity();
    return entity != null ? EntityUtils.toString(entity) : null;
   }else{
    throw new ClientProtocolException("Unexpected response status: " + status);
   }
  }
 };


 private ResponseHandler<String> strResponseHandlerWithEx = new ResponseHandler<String>() {
  public String handleResponse(final HttpResponse response) throws ClientProtocolException,
          IOException {
   int status = response.getStatusLine().getStatusCode();
   HttpEntity entity = response.getEntity();
   if (status >= 200 && status < 300) {
    return entity != null ? EntityUtils.toString(entity) : null;
   } else {
    throw new ClientProtocolException(entity != null ? EntityUtils.toString(entity) : "");
   }
  }
 };

 private HttpClient getHttpClient() {
  if (defaultHttpClient.get() == null) {
     HttpClient hc = HttpClients.createDefault();
     defaultHttpClient.set(hc);
  }
  return defaultHttpClient.get();
 }

 public String get(String url) {
  HttpGet httpget = new HttpGet(url);
  try {
   return this.getHttpClient().execute(httpget, this.strResponseHandler);
  } catch (Exception  e) {
   e.printStackTrace();
  }
  return null;
 }

 public <T extends AbstractReply> T get(String url, Class<T> replyClass) {
  String json = get(url);
  return AbstractReply.jsonToReply(json, replyClass);
 }

 public String post(String url) {
  return this.post(url, null);
 }


 public String post(String url, Map<String, String> params) {
  try {
   if (url != null) {
    HttpPost httpPost = new HttpPost(url);
    if (params != null && params.size() > 0) {
     List<NameValuePair> nvps = new ArrayList<NameValuePair>();
     Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();
     while (it.hasNext()) {
      Map.Entry<String, String> entry = it.next();
      nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
     }

     httpPost.setEntity(new UrlEncodedFormEntity(nvps));
    }
    return this.getHttpClient().execute(httpPost, this.strResponseHandler);
   }
  } catch (IOException e) {
   e.printStackTrace();
  }
  return null;
 }

 public <T extends AbstractReply> T post(String url, Map<String, String> params, Class<T> replyClass) {
  String json = post(url, params);
  return AbstractReply.jsonToReply(json, replyClass);
 }

 public String postByJsonStr(String url, String jsonStr){
  try {
   if (url != null) {
    HttpPost httpPost = new HttpPost(url);
    if (jsonStr != null) {
     StringEntity s = new StringEntity(jsonStr);
     s.setContentEncoding("UTF-8");
     s.setContentType("application/json");
     httpPost.setEntity(s);
    }

    return this.getHttpClient().execute(httpPost, this.strResponseHandler);
   }
  } catch (Exception e) {
   e.printStackTrace();
   //throw new RuntimeException(e);
  }
  return null;
 }

 public String postByJsonStrWithEx(String url, String jsonStr) throws Exception{
  if (url != null) {
   HttpPost httpPost = new HttpPost(url);
   if (jsonStr != null) {
    StringEntity s = new StringEntity(jsonStr);
    s.setContentEncoding("UTF-8");
    s.setContentType("application/json");
    httpPost.setEntity(s);
   }
   return this.getHttpClient().execute(httpPost, this.strResponseHandlerWithEx);
  }
  return null;
 }

 public String post(JSONObject jsonParams,String url){

  try{
   if(url!=null){
    HttpPost httpPost = new HttpPost(url);
    httpPost.setHeader("Content-Type", "application/json");
    StringEntity  s = new StringEntity (jsonParams.toString(), "utf-8");
    s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
    httpPost.setEntity(s);
    return this.getHttpClient().execute(httpPost, this.strResponseHandler);
    			/* // 发送请求
    			 HttpResponse httpResponse = this.getHttpClient().execute(httpPost);
    			// 获取响应输入流
    			 InputStream inStream = httpResponse.getEntity().getContent();
    			BufferedReader reader = new BufferedReader(new InputStreamReader( inStream, "utf-8"));StringBuilder strber = new StringBuilder(); String line = null;
	    		    while ((line = reader.readLine()) != null)
	    		    strber.append(line + "\n");
	    			inStream.close();
	    			String result = strber.toString();
	    		 	System.out.println(result);
    		    }*/
   }

  }catch(Exception e){
   e.printStackTrace();
  }
  return null;
 }

 public String post(String url, Map<String, String> params, File file, String fileNameParam ){
  try {
   if (url != null) {
    // 把文件转换成流对象FileBody
    FileBody bin = new FileBody(file);
    HttpPost httpPost = new HttpPost(url);
    if (params != null && params.size() > 0) {
     MultipartEntity reqEntity = new MultipartEntity();
     Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();
     while (it.hasNext()) {
      Map.Entry<String, String> entry = it.next();
      reqEntity.addPart(entry.getKey(),new StringBody(entry.getValue()));
     }
     reqEntity.addPart(fileNameParam, bin);
     httpPost.setEntity(reqEntity);
    }
    return this.getHttpClient().execute(httpPost, this.strResponseHandler);
   }
  } catch (IOException e) {
   e.printStackTrace();
  }
  return null;
 }

 public String post(String url,File file,String fileNameParam ){
  try {
   if (url != null) {
    FileBody bin = new FileBody(file);
    HttpPost httpPost = new HttpPost(url);
    MultipartEntity reqEntity = new MultipartEntity();
    //FormBodyPart filePart = new FormBodyPart(fileNameParam, bin);
    reqEntity.addPart(fileNameParam, bin);
    httpPost.setEntity(reqEntity);
    return this.getHttpClient().execute(httpPost, this.strResponseHandler);
   }
  } catch (IOException e) {
   e.printStackTrace();
  }
  return null;
 }
}
