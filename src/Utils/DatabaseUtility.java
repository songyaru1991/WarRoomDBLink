package Utils;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import foxlink.WarRoomDbThread;

public class DatabaseUtility {
	private static Logger logger = Logger.getLogger(DatabaseUtility.class);  
	private String server;
	private String serverName;
	private String port;
	private String userName;
	private String password;
	private String SID;

	public DatabaseUtility(String server) throws Exception {
		this.server = server;
		this.getDatabaseConfig();
	}

	public Connection makeConnection() throws Exception {
		Connection conn;
		DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
		/* Oracle DB */
		String URL = "jdbc:oracle:thin:@//" + serverName + ":" + port + "/" + SID + "";
		// String URL="jdbc:oracle:thin:@//10.8.91.142:1521/AlarmTestDB";
		conn = DriverManager.getConnection(URL, userName, password);
		return conn;
	};

	private void getDatabaseConfig() throws Exception {
		try {
			// Windows:c:/FoxLinkNotification/Database_Config/dbConfig.json
			// Mac: /Users/sfc/Desktop/dbConfig.json
			JSONParser parseDBconfig = new JSONParser();
			/*	JSONObject getDBconfig = (JSONObject) parseDBconfig
				.parse(new FileReader("C:/Users/Yaru_Song/Desktop/WarRoomDbLink/dbConfig.json"));*/
			//		String filePath = System.getProperty("user.dir") + "//dbConfig.json";
			JSONObject getDBconfig = (JSONObject) parseDBconfig
				.parse(new FileReader("C:/Users/Administrator/Desktop/WarRoomDbLink/dbConfig.json"));		
			JSONObject getDBconfigDetail = (JSONObject) getDBconfig.get(server);
			if (getDBconfigDetail != null) {
				this.serverName = (String) getDBconfigDetail.get("serverName");
				this.port = (String) getDBconfigDetail.get("port");
				this.userName = (String) getDBconfigDetail.get("user");
				this.password = (String) getDBconfigDetail.get("password");
				this.SID = (String) getDBconfigDetail.get("SID");
			}
		}catch (Exception e) {  
			e.printStackTrace();
			logger.error("Get dbConfig.json is failed, due to: ",e);
		}  
		
	}
}
