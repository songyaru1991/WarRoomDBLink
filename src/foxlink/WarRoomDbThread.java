package foxlink;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import DAO.WarRoomDbLinksDAO;
import Utils.CustomException;
/** 
 * ScheduledExecutorService是从Java SE5的java.util.concurrent里，做为并发工具类被引进的，这是最理想的定时任务实现方式。  
 * 有以下好处： 
 * 1>相比于Timer的单线程，它是通过线程池的方式来执行任务的  
 * 2>可以很灵活的去设定第一次执行任务delay时间 
 * 3>提供了良好的约定，以便设定执行的时间间隔 
 * @author yaru_song 
 *  
 */  
public class WarRoomDbThread {
	private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(); 
	private static Logger logger = Logger.getLogger(WarRoomDbThread.class);  

	public WarRoomDbThread() {  
       
    }  
	
	public void dbLinkTask(String dbLinkName){
		Runnable runnable=new Runnable(){
			public void run(){
				WarRoomDbLinksDAO warRoom =new WarRoomDbLinksDAO();				 			 				
				try {
					Date day=new Date();    
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
					String nowTime=df.format(day);   
					
					Boolean isDbLinkSuccess = warRoom.isDbLinkSuccess(dbLinkName);
					
					 if(!isDbLinkSuccess){					
							warRoom.insertMessageStatus(dbLinkName);
							logger.info(nowTime + ":" + dbLinkName+"DSG近10分鐘連線異常，請盡快排除異常!");
							System.out.println(nowTime + ":" + dbLinkName+" DSG近10分鐘連線異常，請盡快排除異常!"); 
					 }
					 else{
						 System.out.println(nowTime + ":" + dbLinkName+" DSG近10分鐘連線正常!"); 
						 logger.info(nowTime + ":" + dbLinkName+"DSG近10分鐘連線正常!");
					 }
				} catch(CustomException e){
					logger.error(e);
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
	       service.scheduleAtFixedRate(runnable, 0, 10*60, TimeUnit.SECONDS);    
	}
			
}
