package foxlink;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class MutiTask {
	public MutiTask() {  
	       
    }  
	private static Logger logger = Logger.getLogger(MutiTask.class);
	
	public static void main(String[] args){
		logger.info("WarRoomDB Test Started.");
		Date date =new Date();
		SimpleDateFormat format=new SimpleDateFormat("yyyy/mm/dd HH:mm:ss");
		logger.info("Current Time: "+format.format(date));
		/** 
		 * 
		 * @author yaru_song 
		 *  
		 */  
		new WarRoomDbThread().dbLinkTask("AS");
		new WarRoomDbThread().dbLinkTask("SFC2ERP");
		new WarRoomDbThread().dbLinkTask("EXCALIBUR");
		new WarRoomDbThread().dbLinkTask("TAURUSDG");
		new WarRoomDbThread().dbLinkTask("B9");
		new WarRoomDbThread().dbLinkTask("CEBU");
		new WarRoomDbThread().dbLinkTask("CEBUNC");
		new WarRoomDbThread().dbLinkTask("KSSCBG");
		new WarRoomDbThread().dbLinkTask("KSCSBG");
		new WarRoomDbThread().dbLinkTask("MASSCBG");
		new WarRoomDbThread().dbLinkTask("XZCEBU");
		new WeChatThread().sendMessage();
	}

}
