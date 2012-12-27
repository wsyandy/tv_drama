package com.jumplife.tvdrama.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jumplife.sqlite.SQLiteTvDrama;
import com.jumplife.tvdrama.entity.Chapter;
import com.jumplife.tvdrama.entity.Drama;
import com.jumplife.tvdrama.entity.News;
import com.jumplife.tvdrama.entity.Section;

import android.app.Activity;
import android.util.Log;

public class DramaAPI {

	private String urlAddress;
	private HttpURLConnection connection;
	private String requestedMethod;
	private Activity mActivity;
	private int connectionTimeout;
	private int readTimeout;
	private boolean usercaches;
	private boolean doInput;
	private boolean doOutput;
	
	public static final String TAG = "DRAMA_API";
	public static final boolean DEBUG = true;
	
	public DramaAPI(String urlAddress, int connectionTimeout, int readTimeout) {
		this.urlAddress = new String(urlAddress + "/");
		this.connectionTimeout = connectionTimeout;
		this.readTimeout = readTimeout;
		this.usercaches = false;
		this.doInput = true;
		this.doOutput = true;
	}
	public DramaAPI(String urlAddress) {
		this(new String(urlAddress), 5000, 5000);
	}
	
	public DramaAPI(Activity a) {
		this(new String("http://106.187.40.45"));
		this.mActivity = a;
	}
	
	public DramaAPI() {
		this(new String("http://106.187.40.45"));
	}
	
	public int connect(String requestedMethod, String apiPath) {
		int status = -1;
		try {
			URL url = new URL(urlAddress + apiPath);
			
			if(DEBUG)
				Log.d(TAG, "URL: " + url.toString());
			connection = (HttpURLConnection) url.openConnection();
					
			connection.setRequestMethod(requestedMethod);
			connection.setReadTimeout(this.readTimeout);
			connection.setConnectTimeout(this.connectionTimeout);
			connection.setUseCaches(this.usercaches);
			connection.setDoInput(this.doInput);
			connection.setDoOutput(this.doOutput);
			connection.setRequestProperty("Content-Type",  "application/json;charset=utf-8");
			
			connection.connect();

		} 
		catch (MalformedURLException e1) {
			e1.printStackTrace();
			return status;
		}
		catch (IOException e) {
			e.printStackTrace();
			return status;
		}
		
		return status;
	}
	
	public void disconnect()
	{
		connection.disconnect();
	}
	
	public ArrayList<Integer> getDramasId(){
		ArrayList<Integer> dramasId = new ArrayList<Integer>(100);
		String message = getMessageFromServer("GET", "api/v1/dramas.json", null);
		if(message == null) {
			return null;
		}
		else {			
			try {
				JSONArray dramaIdsJson;
				dramaIdsJson = new JSONArray(message.toString());
				for(int i=0; i<dramaIdsJson.length(); i++)
					dramasId.add(dramaIdsJson.getInt(i));
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return dramasId;
	}
	
	public ArrayList<Drama> getDramasIdwithViews(){
		ArrayList<Drama> dramas = new ArrayList<Drama>(100);
		String message = getMessageFromServer("GET", "api/v1/dramas/dramas_with_views.json", null);
		if(message == null) {
			return null;
		}
		else {			
			try {
				JSONArray dramasArray = new JSONArray(message.toString());
				for(int i=0; i<dramasArray.length(); i++) {
					JSONObject dramaObject = dramasArray.getJSONObject(i);
					Drama tmp = new Drama();
					tmp.setId(dramaObject.getInt("id"));
					tmp.setViews(dramaObject.getInt("views"));
					dramas.add(tmp);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return dramas;
	}
	
	public ArrayList<Drama> getDramasIdViewsEps(){
		ArrayList<Drama> dramas = new ArrayList<Drama>(100);
		String message = getMessageFromServer("GET", "api/v1/dramas/dramas_with_views.json", null);
		if(message == null) {
			return null;
		}
		else {			
			try {
				JSONArray dramasArray = new JSONArray(message.toString());
				for(int i=0; i<dramasArray.length(); i++) {
					JSONObject dramaObject = dramasArray.getJSONObject(i);
					Drama tmp = new Drama();
					tmp.setId(dramaObject.getInt("id"));
					tmp.setViews(dramaObject.getInt("views"));
					tmp.setEps(dramaObject.getString("eps_num_str"));
					dramas.add(tmp);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return dramas;
	}
	
	//取得電影時刻表
	public ArrayList<Chapter> getDramaChapter(int dramaId) {
		ArrayList<Chapter> chapterList = new ArrayList<Chapter>(10);
	    String message = getMessageFromServer("GET", "api/v1/eps.json?drama_id=" + dramaId, null);
		
		if(message == null) {
			return null;
		}
		else {
			try {
				JSONArray chaptersJson = new JSONArray(message.toString());
				for(int i=0; i<chaptersJson.length(); i++) {
					JSONObject chapterJson = chaptersJson.getJSONObject(i);
					Chapter chapter = new Chapter();
					
					if(!chapterJson.isNull("id"))
						chapter.setId(chapterJson.getInt("id"));
					
					if(!chapterJson.isNull("num"))
						chapter.setNumber(chapterJson.getInt("num"));
					
					chapterList.add(chapter);
				}				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return chapterList;
			}
		}

		return chapterList;
	}
	
	//取得電影時刻表
	public ArrayList<Section> getChapterSection(int chapterId) {
		ArrayList<Section> sectionList = new ArrayList<Section>(10);
	    String message = getMessageFromServer("GET", "api/v1/youtube_sources.json?ep_id=" + chapterId, null);
		
		if(message == null) {
			return null;
		}
		else {
			try {
				JSONArray sectionsJson = new JSONArray(message.toString());
				for(int i=0; i<sectionsJson.length(); i++) {
					JSONObject sectionJson = sectionsJson.getJSONObject(i);
					Section section = new Section();
					
					if(!sectionJson.isNull("id"))
						section.setId(sectionJson.getInt("id"));
					
					if(!sectionJson.isNull("link"))
						section.setUrl(sectionJson.getString("link"));
					
					sectionList.add(section);
				}				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return sectionList;
			}
		}

		return sectionList;
	}
		
	//取得電影時刻表
	public ArrayList<Section> getChapterSectionNew(int dramaId, int chapterNo) {
		ArrayList<Section> sectionList = new ArrayList<Section>(10);
	    String message = getMessageFromServer("GET", "api/v1/" +
	    		"youtube_sources/find_by_drama_and_ep_num.json?drama_id=" + dramaId + "&num=" + chapterNo, null);
		
		if(message == null) {
			return null;
		}
		else {
			try {
				JSONArray sectionsJson = new JSONArray(message.toString());
				for(int i=0; i<sectionsJson.length(); i++) {
					JSONObject sectionJson = sectionsJson.getJSONObject(i);
					Section section = new Section();
					
					if(!sectionJson.isNull("ep_id"))
						section.setId(sectionJson.getInt("ep_id"));
					
					if(!sectionJson.isNull("link"))
						section.setUrl(sectionJson.getString("link"));
					
					sectionList.add(section);
				}				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return sectionList;
			}
		}

		return sectionList;
	}
		
	public void AddDramasFromInfo(String idlst) {
		String message = getMessageFromServer("GET", "api/v1/dramas/new_dramas_info.json?dramas_id=" + idlst, null);
		SQLiteTvDrama sqlTvDrama = new SQLiteTvDrama(mActivity);
		
		if(message != null) {
			try {
				JSONArray dramaArray;		
				dramaArray = new JSONArray(message.toString());
				ArrayList<Drama> dramas = new ArrayList<Drama>();
				for (int i = 0; i < dramaArray.length() ; i++) {
					JSONObject dramaJson = dramaArray.getJSONObject(i);
					Drama drama = DramaJsonToClass(dramaJson);
					dramas.add(drama);
				}
				if(dramas != null && dramas.size() > 0)
					sqlTvDrama.insertDramas(dramas);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	public String getMessageFromServer(String requestMethod, String apiPath, JSONObject json) {
		URL url;
		try {
			url = new URL(this.urlAddress +  apiPath);
			if(DEBUG)
				Log.d(TAG, "URL: " + url);
			
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(requestMethod);
			
			connection.setRequestProperty("Content-Type",  "application/json;charset=utf-8");
			if(requestMethod.equalsIgnoreCase("POST"))
				connection.setDoOutput(true);
			else
				connection.setDoInput(true);
			connection.connect();
			
			
			if(requestMethod.equalsIgnoreCase("POST")) {
				OutputStream outputStream;
				
				outputStream = connection.getOutputStream();
				if(DEBUG)
					Log.d("post message", json.toString());
				
				outputStream.write(json.toString().getBytes());
				outputStream.flush();
				outputStream.close();
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder lines = new StringBuilder();;
			String tempStr;
			
			while ((tempStr = reader.readLine()) != null) {
	            lines = lines.append(tempStr);
	        }
			if(DEBUG)
				Log.d(TAG, lines.toString());
			
			reader.close();
			connection.disconnect();
			
			return lines.toString();
		} 
		catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} 
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Drama DramaJsonToClass (JSONObject dramaJson) {
		Drama drama = null;
		String releaseDate = "";
		Boolean isShow = true;
		
		if(dramaJson == null) {
			return null;
		}
		else {
			try {
				if(!dramaJson.isNull("release"))
					releaseDate = dramaJson.getString("release");
				else if(!dramaJson.isNull("release_date"))
					releaseDate = dramaJson.getString("release_date");
						
				if(!dramaJson.isNull("is_show"))
					isShow = dramaJson.getBoolean("is_show");
				
				drama = new Drama(dramaJson.getInt("id"), dramaJson.getString("name"), dramaJson.getString("poster_url"), 
						dramaJson.getString("introduction"), dramaJson.getInt("area_id"), releaseDate, isShow, 0,
						dramaJson.getString("eps_num_str"));
				 
			} 
			catch (JSONException e) {
				e.printStackTrace();
				return null;
			}	
		}
		return drama;
	}
	
	public void getPromotion(String picUrl, String link, String title, String description) {
				
		String message = getMessageFromServer("GET", "api/promotion.json", null);
		
		try{
			JSONObject responseJson = new JSONObject(message);
			
			picUrl = responseJson.getString("picture_link");
			link = responseJson.getString("link");
			title = responseJson.getString("tilte");
			description = responseJson.getString("description");
		} 
		catch (JSONException e){
			e.printStackTrace();
		}
	}
	
	public String[] getPromotion() {
				
		String message = getMessageFromServer("GET", "api/promotion.json", null);
		String[] tmp = new String[5];
				
		if(message == null) {
			return null;
		}
		try{
			JSONObject responseJson = new JSONObject(message);
			
			tmp[0] = (responseJson.getString("picture_link"));
			tmp[1] = (responseJson.getString("link"));
			tmp[2] = (responseJson.getString("tilte"));
			tmp[3] = (responseJson.getString("description"));
			tmp[4] = (responseJson.getString("version"));
		} 
		catch (JSONException e){
			e.printStackTrace();
			return null;
		}
		
		return tmp;
	}
	
	public boolean updateViews(int DramaId) {
		boolean result = false;

		try{
			DefaultHttpClient httpClient = new DefaultHttpClient();
			String url = "http://106.187.51.230:8000//api/v1/dramas/" + DramaId + ".json";						
			if(DEBUG)
				Log.d(TAG, "URL : " + url);
			
			HttpPut httpPut = new HttpPut(url);
			HttpResponse response = httpClient.execute(httpPut);
			
			StatusLine statusLine =  response.getStatusLine();
			if (statusLine.getStatusCode() == 200){
				result = true;
			}
		} 
	    catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return result;
		} 
		catch (ClientProtocolException e) {
			e.printStackTrace();
			return result;
		} 
		catch (IOException e){
			e.printStackTrace();
			return result;
		}	
		return result;
	}
	
	//取得新聞列表
	public ArrayList<News> getNewsList(int page) {
		ArrayList<News> newsList = new ArrayList<News>(10);
		
		String message = getMessageFromServer("GET", "/api/v1/news.json?page=" + page, null);
		
		if(message == null) {
			return null;
		}
		else {
			JSONArray newsArray;
			
			try {
				newsArray = new JSONArray(message.toString());
				for(int i = 0; i < newsArray.length(); i++) {
					JSONObject newsJson = newsArray.getJSONObject(i).getJSONObject("news");
					String title = newsJson.getString("title");
					String thumbnailUrl = newsJson.getString("thumbnail_url");
					
					News news = new News();
				
					if(!(newsJson.isNull("created_at"))){
						DateFormat releaseFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
						Date date = releaseFormatter.parse(newsJson.getString("created_at"));
						news.setReleaseDate(date);
					}
					
					if(!(newsJson.isNull("source")))
						news.setSource(newsJson.getString("source"));
					
					news.setTitle(title);
					news.setThumbnailUrl(thumbnailUrl);
					int type = newsJson.getInt("news_type");
					news.setType(type);
					
					if(type == News.TYPE_LINK){
						if(!(newsJson.isNull("link"))){
							String link = newsJson.getString("link");
							news.setLink(link);
						}
					}
					else if (type == News.TYPE_PIC){
						if(!(newsJson.isNull("picture_url"))){
							String pictureUrl = newsJson.getString("picture_url");
							news.setPictureUrl(pictureUrl);
						}
						if(!(newsJson.isNull("content"))){
							String content = newsJson.getString("content");
							news.setContent(content);
						}
					}
					
					newsList.add(news);
					
					int a = 0;
					a = a + 1;
				}
			} 
			catch (JSONException e) {
				e.printStackTrace();
				return null;
			} catch (ParseException e){
				e.printStackTrace();
				return null;
			}
			
		}
		
		return newsList;
	}
		
	public String getUrlAddress() {
		return urlAddress;
	}
	public void setUrlAddress(String urlAddress) {
		this.urlAddress = urlAddress;
	}
	public HttpURLConnection getConnection() {
		return connection;
	}
	public void setConnection(HttpURLConnection connection) {
		this.connection = connection;
	}
	public String getRequestedMethod() {
		return requestedMethod;
	}
	public void setRequestedMethod(String requestedMethod) {
		this.requestedMethod = requestedMethod;
	}
	public int getConnectionTimeout() {
		return connectionTimeout;
	}
	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}
	public int getReadTimeout() {
		return readTimeout;
	}
	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}
	public boolean isUsercaches() {
		return usercaches;
	}
	public void setUsercaches(boolean usercaches) {
		this.usercaches = usercaches;
	}
	public boolean isDoInput() {
		return doInput;
	}
	public void setDoInput(boolean doInput) {
		this.doInput = doInput;
	}
	public boolean isDoOutput() {
		return doOutput;
	}
	public void setDoOutput(boolean doOutput) {
		this.doOutput = doOutput;
	}
}