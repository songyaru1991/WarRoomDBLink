package foxlink;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import DAO.AlarmMessageDAO;
import DAO.WeChatUserInfoDAO;
import Model.AccessTokenBean;
import Model.AlarmMessage;
import Utils.CustomException;

public class WeChatThread {
	private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(); 
	private static Logger logger = Logger.getLogger(WarRoomDbThread.class);  
	private static String weChatAccessToken=null;
	public final static String access_token_url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=APPID&corpsecret=CORPSECRET";
	public static String app_id="wx91d365d1fb695837";
	public static String app_secret="Vcrj6mdd4FvdzUc53uNjncSzpCn0utSc7xjd_Ta7ix-08J37POrmhMwwM38P-EA9";
	
	public WeChatThread(String accessToken){
		weChatAccessToken=accessToken;
	}
    public WeChatThread(){
		
	}
	private void getAccessToken(){
		AccessTokenBean accessToken=null;
		String requestURL=access_token_url.replace("APPID", app_id).replace("CORPSECRET", app_secret);
		JSONObject jsonObject=HttpRequest(requestURL);
		
		if(null!=jsonObject){
			accessToken=new AccessTokenBean();
			accessToken.setAccessToken(jsonObject.get("accessToken").toString());
			accessToken.setExpiresIn(Integer.parseInt(jsonObject.get("expiresIn").toString()));
			System.out.println(String.format("獲取Access Token中，token:%s", accessToken.getAccessToken()));
			logger.info(String.format("獲取Access Token中，token:%s", accessToken.getAccessToken()));
			this.weChatAccessToken=jsonObject.get("accessToken").toString();
		}
	}
	
	@SuppressWarnings("unchecked")
	private  JSONObject HttpRequest(String requestURL){
		JSONObject jsonObject=null;
		try {
			URL url = new URL(requestURL);
		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			conn.setConnectTimeout(15000);
			conn.setReadTimeout(15000);
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "+ conn.getResponseCode());
			}	
			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
		    String response=br.readLine();
				if(response!=null){
					jsonObject=new JSONObject();
					JSONParser parser=new JSONParser();
					JSONObject accessTokenObj=(JSONObject)parser.parse(response);
					jsonObject.put("accessToken", accessTokenObj.get("access_token").toString());
					jsonObject.put("expiresIn", accessTokenObj.get("expires_in").toString());//expires_in
				}
				conn.disconnect();
		}
		catch(SocketTimeoutException ex){
		   System.out.println("Get Access Token is failed,due to: Connection Timed Out.");
		   logger.info("Get Access Token is failed,due to: Connection Timed Out.");
		}
		catch (ConnectException ce) {  
		   System.out.println("Get Access Token is failed,due to: Connection Timed Out.");
		   logger.info("Get Access Token is failed,due to: Connection Timed Out.");
		} 
		catch (Exception e) {  
		   String result = String.format("Https Request Error:%s", e);  
		   System.out.println(result); 
		}  
		return jsonObject;
	}
	
	private boolean CheckUserIsFollow(String userID,String accessToken){
		boolean isFollow=true;
		JSONObject userDetailInfos=null;
		HttpURLConnection Conn=null;
		String checkUserIsFollowURL="https://qyapi.weixin.qq.com/cgi-bin/user/get?access_token=ACCESS_TOKEN&userid=USERID";
		try{
			checkUserIsFollowURL=checkUserIsFollowURL.replace("ACCESS_TOKEN", accessToken).replace("USERID", userID);
			URL url=new URL(checkUserIsFollowURL);
			Conn=(HttpURLConnection)url.openConnection();
			Conn.setDoOutput(true);
			Conn.setDoOutput(true);
			Conn.setConnectTimeout(15000);
			Conn.setReadTimeout(15000);
			Conn.setRequestMethod("GET");
			
			if (Conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "+ Conn.getResponseCode());
			}	
			BufferedReader br = new BufferedReader(new InputStreamReader((Conn.getInputStream())));
		    String response=br.readLine();
				if(response!=null){
					userDetailInfos=new JSONObject();
					JSONParser parser=new JSONParser();
					userDetailInfos=(JSONObject)parser.parse(response);
					WeChatUserInfoDAO wechatUserInfo=new WeChatUserInfoDAO();
					if(Integer.valueOf(userDetailInfos.get("status").toString())==4){
						//未關注
						wechatUserInfo.UpdateUserWeChatStatus(userID,4);
						isFollow=false;
					}
					else if(Integer.valueOf(userDetailInfos.get("status").toString())==1){
						//已關注
						wechatUserInfo.UpdateUserWeChatStatus(userID,1);
						isFollow=true;
					}
					else{						
						wechatUserInfo.UpdateUserWeChatStatus(userID,2);
						isFollow=false;
					}
				}
				Conn.disconnect();
		}
		catch(SocketTimeoutException e){
			logger.info("Request time out",e);
			Conn.disconnect();
		}
		catch(Exception ex){
			logger.error("Send Text Message Failed, due to: ",ex);
			Conn.disconnect();
		}
		finally{
			Conn.disconnect();
		}
		return isFollow;
	}	
	
	@SuppressWarnings("unchecked")
	private boolean sendWeChatTextMessage(AlarmMessage message){
		boolean sendSuccess=true;
		boolean isUserFollow=true;
		HttpURLConnection Conn=null;
		try{
			this.getAccessToken();
			String uploadURL="https://qyapi.weixin.qq.com/cgi-bin/chat/send?access_token=ACCESS_TOKEN";
			uploadURL=uploadURL.replace("ACCESS_TOKEN", weChatAccessToken);
			URL url=new URL(uploadURL);
			Conn=(HttpURLConnection)url.openConnection();
			Conn.setDoOutput(true);
			Conn.setDoOutput(true);
			Conn.setConnectTimeout(15000);
			Conn.setReadTimeout(15000);
			Conn.setRequestMethod("POST");
			Conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
			JSONObject inputValue=new JSONObject();
			inputValue.put("sender", "FoxLink_IT");
			inputValue.put("msgtype", "text");
			JSONObject receiver=new JSONObject();
			if(message.getSingleMsg()==1){
				receiver.put("type", "single");
				isUserFollow=this.CheckUserIsFollow(message.getDrivedGroupID(),this.weChatAccessToken);
			}
			else{
				receiver.put("type", "group");
			}

			receiver.put("id", message.getDrivedGroupID());
			JSONObject text=new JSONObject();
			text.put("content", message.getMessageContent());
			inputValue.put("receiver", receiver);
			inputValue.put("text", text);
			
			if(isUserFollow){
				OutputStream os = Conn.getOutputStream();
				os.write(inputValue.toString().getBytes());
				os.flush();
				os.close();
				
				if (Conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
					sendSuccess=false;
					throw new RuntimeException("Failed : HTTP error code : "
						+ Conn.getResponseCode());
				}
		 
				BufferedReader br = new BufferedReader(new InputStreamReader(
						(Conn.getInputStream())));
		 
				String output;
				System.out.println("Output from Server .... \n");
				System.out.println("WeChat ID:"+message.getDrivedGroupID());
				while ((output = br.readLine()) != null) {
					System.out.println(output);
					JSONParser parser=new JSONParser();
					JSONObject sendResults=(JSONObject)parser.parse(output);
					if(sendResults.get("errcode").toString().equals("0")){
						AlarmMessageDAO manipulateMessage=new AlarmMessageDAO();
						if(manipulateMessage.updateMessageStatus(message))
							sendSuccess=true;
					}
					else{
						sendSuccess=false;
					}
				}		
			}
		}
		catch(SocketTimeoutException e){
			logger.info("Request time out",e);
			System.out.println("Request time out ");
			sendSuccess=false;
			Conn.disconnect();
		}
		catch(Exception ex){
			logger.error("Send Text Message Failed, due to: ",ex);
			Conn.disconnect();
		}
		finally{
			Conn.disconnect();
		}
		
		return sendSuccess;
	}
	
	public void sendMessage(){
		Runnable runnable=new Runnable(){
			public void run() {
				// TODO Auto-generated method stub
					System.out.println("--- WeChat Group Chat Message Send Started ---");
					logger.info("--- WeChat Group Chat Message Send Started ---");
					try{
						//當Alarm Message List 有資料時
						AlarmMessageDAO getAlarmMessage=new AlarmMessageDAO();
						List<AlarmMessage> messages=new ArrayList<AlarmMessage>();
						messages=getAlarmMessage.getAlarmMessages();
						if(messages.size()>0){
							
							Iterator<AlarmMessage> messageIterator=messages.iterator();
							while(messageIterator.hasNext()){
								AlarmMessage message=messageIterator.next();
								if(message.getMessageSendType()==43){
									//发送文字群聊訊息
									if(sendWeChatTextMessage(message)){
										AlarmMessageDAO manipulateMessage=new AlarmMessageDAO();
										if(manipulateMessage.updateMessageStatus(message))
											logger.info("Send text message to group chat is successed.");
									}
									else{
										logger.info("Sned text message to group chat is failed.");
										//throw new CustomException("Thread Interrupted.", "Current Thread is interrupted.");
									}
								}
							}
						}
						else{
							//當沒有Alarm Message
							logger.info("----- No Messages          -----");
							System.out.println("----- No Messages          -----");
						}
					}
					catch(CustomException e){
						logger.info(e);
						if (Thread.interrupted())
							try {
								throw new InterruptedException();
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
					}
					catch(Exception ex){
						logger.error(ex);
						if (Thread.interrupted())
							try {
								throw new InterruptedException();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					}
			}
		};
		  // 第二个参数为首次执行的延时时间，第三个参数为定时执行的间隔时间  ,TimeUnit:计时单位，SECONDS表示秒
	       service.scheduleAtFixedRate(runnable, 5, 5*60, TimeUnit.SECONDS);    
	}
	
}
