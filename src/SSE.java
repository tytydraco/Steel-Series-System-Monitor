import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class SSE {
	
	public static String GAME;
	public static String GAME_DISPLAY;
	public static String GAME_COLOR_ID;
	public static String SSE_URL;
	
	public static void init(String game, String game_display, String game_color_id, String sse_url) {
		GAME = game;
		GAME_DISPLAY = game_display;
		GAME_COLOR_ID = game_color_id;
		SSE_URL = sse_url;
	}
	
	public static void registerMeta() throws JSONException {
		JSONObject meta = new JSONObject();
		meta.put("game", GAME);
		meta.put("game_display_name", GAME_DISPLAY);
		meta.put("icon_color_id", GAME_COLOR_ID);
		post(meta.toString(), SSE_URL + "/game_metadata");
	}
	
	public static void registerEvent(String name, JSONObject event) throws JSONException {
		
		JSONObject regEvent = new JSONObject();
		regEvent.put("game", GAME);
		regEvent.put("event", name);
		regEvent.put("handlers", new JSONObject[] {event});
		post(regEvent.toString(), SSE_URL + "/bind_game_event");
	}
	
	public static void sendEvent(String event, Integer value) throws JSONException {
		JSONObject ssEvent = new JSONObject();
		ssEvent.put("game", GAME);
		ssEvent.put("event", event);
		ssEvent.put("data", new JSONObject().put("value", value));
		post(ssEvent.toString(), SSE_URL + "/game_event");
	}
	
	public static void post(String postContent, String url) {
		HttpClient httpClient = HttpClientBuilder.create().build();
		try {

		    HttpPost request = new HttpPost(url);
		    StringEntity params = new StringEntity(postContent);
		    params.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
		    request.setEntity(params);
		 
		    HttpResponse response = httpClient.execute(request);
		    HttpEntity responseEntity = response.getEntity();
		    String responseString = EntityUtils.toString(responseEntity, "UTF-8");
		    //System.out.println(responseString);

		} catch (Exception e) {
			System.out.println("error:" + e);
		}
	}
	
	
	
	public static String getSSEURL() {
    	String jsonAddress = "";
    	// First open the config file to see what port to connect to.
		// Try to open Windows path one first
    	try {
			String corePropsFileName = System.getenv("PROGRAMDATA") + "\\SteelSeries\\SteelSeries Engine 3\\coreProps.json";
			BufferedReader coreProps = new BufferedReader(new FileReader(corePropsFileName));
	    	jsonAddress = coreProps.readLine();
	    	System.out.println("Opened coreprops.json and read: " + jsonAddress);
	    	coreProps.close();
    	} catch (FileNotFoundException e) {
			//e.printStackTrace();
			System.out.println("coreprops.json not found (Mac check)");
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Something terrible happened looking for coreProps.json");
			return null;
		}
		
		// If not on Windows, jsonAddress is probably still "", so try to open w/ Mac path
    	if(jsonAddress == "") {
	    	try {
				String corePropsFileName = "/Library/Application Support/SteelSeries Engine 3/coreProps.json";
				BufferedReader coreProps = new BufferedReader(new FileReader(corePropsFileName));
		    	jsonAddress = coreProps.readLine();
		    	System.out.println("Opened coreprops.json and read: " + jsonAddress);
		    	coreProps.close();
			} catch (FileNotFoundException e) {
				//e.printStackTrace();
				System.out.println("coreprops.json not found (Windows check)");
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Something terrible happened looking for coreProps.json");
				return null;
			}
    	}
		
		try {
			// If we got a json string of address of localhost:<port> open a connection to it
			if(jsonAddress != "") {
				JSONObject obj = new JSONObject(jsonAddress);
				return "http://" + obj.getString("address");
			} else {
				// Debug default:
				return "http://localhost:3000";
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
			System.out.println("Something terrible happened creating JSONObject from coreProps.json.");
			return null;
		}
    }
}
