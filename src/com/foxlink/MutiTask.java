package com.foxlink;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class MutiTask {
	private static Logger logger = Logger.getLogger(MutiTask.class);  
	private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(); 
	
	public MutiTask() {  
        System.out.println("All Threads Started!");  
    }  
	
	public void sfc2erp(String dbName,String sql){
		Runnable runnable=new Runnable(){
			public void run(){
				 System.out.println(dbName+"连线成功,"+"查询数据sql为"+sql); 
			}
		};
		  // 第二个参数为首次执行的延时时间，第三个参数为定时执行的间隔时间  ,TimeUnit:计时单位，SECONDS表示秒
	       service.scheduleAtFixedRate(runnable, 0, 5, TimeUnit.SECONDS);    
	}
	
	public void as(){
		Runnable runnable=new Runnable(){
			public void run(){
				System.out.println("task222222~~~");  
			}
		};
		  // 第二个参数为首次执行的延时时间，第三个参数为定时执行的间隔时间  ,TimeUnit:计时单位，SECONDS表示秒
	       service.scheduleAtFixedRate(runnable, 0, 5, TimeUnit.SECONDS);    
	}	
	
	public static void main(String[] args){
		MutiTask thread=new MutiTask();
		thread.sfc2erp("dbName111","sql111");
		thread.sfc2erp("dbName222","sql222");
		thread.as();
	}

}
