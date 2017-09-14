package com.platfrom.springboot.util;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


/**
 * 用来发送httpclient请求的工具类
 * @author yangchd
 */
public class HttpClientUtil {
	
	public static String doGetMethod(String url,Map<String,Object> headerMap){
		String result = "";
		//创建默认实例
		CloseableHttpClient httpClient = HttpClients.createDefault();
		try{
			//创建post
			HttpGet get = new HttpGet(url);
			addHeader(get,headerMap);
			//执行get请求
			CloseableHttpResponse httpResponse = httpClient.execute(get);
			try{
				HttpEntity entity = httpResponse.getEntity();
				if(null != entity){
					//处理返回值
					result = EntityUtils.toString(entity);
					System.out.println(result);
				}else{
					result = "转发请求发生了错误";
				}
			}finally{
				closeHttp(httpResponse);
			}
		}catch(Exception e){
			result = e.getMessage();
			System.out.println(e.getMessage());
//			MALogger.error(e.getMessage());
		}finally{
			try{
				closeHttp(httpClient);
			}catch(Exception e){
				System.out.println(e.getMessage());
//				MALogger.error(e.getMessage());
			}
		}
		return result;
	}
	
	
	public static String doPostMethod(String url,Map<String,Object> values,Map<String,Object> headerMap){
		String result = "";
		//创建默认实例
		CloseableHttpClient httpClient = HttpClients.createDefault();
		try{
			//创建post
			HttpPost httpPost = new HttpPost(url);
			addHeader(httpPost,headerMap);
			
			//根据参数，创建参数队列
			List<NameValuePair> list = new ArrayList<NameValuePair>();
			Set<Entry<String,Object>> entries = values.entrySet();
			for(Entry<String,Object> entry:entries){
				list.add(new BasicNameValuePair(entry.getKey(),entry.getValue().toString()));
			}
			
			//设置url编码
			UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(list,"UTF-8");
			httpPost.setEntity(uefEntity);
			
			//执行post请求
			CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
			try{
				HttpEntity entity = httpResponse.getEntity();
				if(null != entity){
					//处理返回值
					result = EntityUtils.toString(entity);
					System.out.println(result);
				}else{
					result = "转发请求发生了错误";
				}
			}finally{
				closeHttp(httpResponse);
			}
		}catch(Exception e){
			result = e.getMessage();
			System.out.println(e.getMessage());
//			MALogger.error(e.getMessage());
		}finally{
			try{
				closeHttp(httpClient);
			}catch(Exception e){
				System.out.println(e.getMessage());
//				MALogger.error(e.getMessage());
			}
		}
		return result;
	}
	
	//添加请求头方法
	private static void addHeader(HttpGet http,Map<String,Object> headerMap){
		if(headerMap!=null){
			Set<Entry<String,Object>> entries = headerMap.entrySet();
			for(Entry<String,Object> entry:entries){
				http.setHeader(entry.getKey(), entry.getValue()==null?"":entry.getValue().toString());
			}
		}
	}
	private static void addHeader(HttpPost http,Map<String,Object> headerMap){
		if(headerMap!=null){
			Set<Entry<String,Object>> entries = headerMap.entrySet();
			for(Entry<String,Object> entry:entries){
				http.setHeader(entry.getKey(), entry.getValue()==null?"":entry.getValue().toString());
			}
		}
	}

	//关闭方法
	private static void closeHttp(CloseableHttpClient client) throws IOException{
		if(client!=null){
			client.close();
		}
	}
	private static void closeHttp(CloseableHttpResponse client) throws IOException{
		if(client!=null){
			client.close();
		}
	}
	
	//获取长城需要的请求头
	public static Map<String,Object> getRequestHeader(HttpServletRequest request){
		Map<String,Object> header = new HashMap<String, Object>();
		String user_code = request.getHeader("User-Code");
		String user_token = request.getHeader("User-Token");
		String client_info = request.getHeader("Client-Info");
		header.put("User-Code", user_code);
		header.put("User-Token", user_token);
		header.put("Client-Info", client_info);
		return header;
	}
	
	//根据json字符获取所有参数
	public static Map<String,Object> getParaByString(String parameter){
		Map<String,Object> paraMap = new HashMap<String, Object>();
		JSONObject pajson = JSONObject.fromObject(parameter);
		for(Object map:pajson.entrySet()){
			paraMap.put(((Entry<String, Object>)map).getKey(),
					((Entry<String, Object>)map).getValue());
//			System.out.println(((Map.Entry<String, Object>)map).getKey()+((Map.Entry<String, Object>)map).getValue().toString());
		}
		return paraMap;
	}
	
	//get方法拼接请求url
	public static String getGETUrl(String url,String parameter){
		Map<String,Object> paraMap = getParaByString(parameter);
		StringBuffer sb = new StringBuffer();
		for(Entry<String, Object> map : paraMap.entrySet()){
			sb.append(map.getKey()).append("=").append(map.getValue()).append("&");
		}
		sb.deleteCharAt(sb.length()-1);
		url = url+"?"+sb.toString();
		return url;
	}
	

	
	public static void main(String[] args) throws Exception {
		
		Map<String,Object> header = new HashMap<String, Object>();
		header.put("User-Code", "tianye");
		header.put("User-Token", "d3c0d46f6a625e5b0e26fd1b7a4db5aa3c5f1bcb4ccf77a42344542206008122");
		header.put("Client-Info", "");
		
//		String url = "http://10.168.71.148:8130/maserver/ifbpmob/util/httpclient?url=http://10.168.71.148:8130/maserver/ifbpmob/login/sendSMS&parameter={'user_code':'tianye'}";
//		doGetMethod(url,header);
		
		
		String url = "http://10.168.71.148:8130/maserver/ifbpmob/util/httpclient";
////
		Map<String,Object> para = new HashMap<String, Object>();
		para.put("parameter", "{'EmpID':'32'}");
		para.put("url", "http://10.168.71.129/GWOAWebApi.ToDoCenter/api/ToDoList/GetToDoList");
		doPostMethod(url,para,header);

//		String aa = "{\"result\":{\"verifycode\":\"526060\",\"msg\":\"获取验证码成功\",\"retflag\":\"0\"}}";
//		getParaByString(aa);
	}

}
