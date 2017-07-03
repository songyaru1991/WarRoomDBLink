package com.foxlink;

public class TaskDemo {

	/*public static void main(String[] args) {
		// TODO Auto-generated method stub
		  //创建三个任务  
        Runnable printA = new TestDBLink("sfc2erp","sfc2erpSql");  
        Runnable printB = new TestDBLink("as","asSql");  
        //创建三个线程  
        Thread thread1 = new Thread(printA);  
        Thread thread2 = new Thread(printB);  
        //启动三个线程，将同时执行创建的三个任务  
        thread1.start();  
        thread2.start();  
    }  */
}  
//此任务将一个字符打印指定次数  
class TestDBLink implements Runnable {  
	final long timeInterval = 1000;  
    private String dbName;  
    private String sql;  
    //构造函数  
    public TestDBLink(String dbName,String sql) {  
        this.dbName = dbName;  
        this.sql = sql;  
    }  
    //重写接口Runable中的run方法，告诉系统此任务的内容  
    @Override  
    public void run() {        
        while (true) {  
            // ------- code for task to run  
        	 System.out.print(dbName+"连线成功,"+"查询数据sql为"+sql); 
            // ------- ends here  
            try {  
                Thread.sleep(timeInterval);  
            } catch (InterruptedException e) {  
                e.printStackTrace();  
            }  
        }  
    }  
    
}  
