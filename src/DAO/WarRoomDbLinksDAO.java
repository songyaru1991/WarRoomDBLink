package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import Model.AlarmMessage;
import Utils.DatabaseUtility;

public class WarRoomDbLinksDAO {
	private static Logger logger=Logger.getLogger(AlarmMessageDAO.class);
	
	/*隨機生成event_id*/
	public long getEventID(){
		Date dNow=new Date();
		SimpleDateFormat format=new SimpleDateFormat("yyyyMMddHHmmss");
		String id=format.format(dNow)+(int)(Math.random()*9999+1000);
		long eventID= Long.parseLong(id);
		return eventID;
	}	
	public boolean insertMessageStatus(String dbLinkName)throws Exception{
		boolean isInsert=false;
		Connection Conn=null;
		PreparedStatement pstmt=null;
		int effectRows=-1;
		String sSQL="INSERT INTO alarm_message "
				  + "(event_id, system_name, app_name, factory_code, message_title, message_content, "
				  + " message_send_type, receiver_priority, message_status, message_sender, "
				  + "send_by_department, record_time, received_group_id, append_paramter,"
				  + " wechat_app_id, create_time, is_single) "
				  + "VALUES "
				  + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, ?, NULL, ?, SYSDATE, ?)";
		try{
			DatabaseUtility dbUtility=new DatabaseUtility("AlarmOracleDB");
			Conn=dbUtility.makeConnection();
			pstmt=Conn.prepareStatement(sSQL);			
			pstmt.setLong(1, this.getEventID());
			pstmt.setString(2, "SFC");
			pstmt.setString(3, "MsgBox");
			if(dbLinkName=="KSCSBG" || dbLinkName=="KSSCBG"){
			pstmt.setString(4, "KS");
			}else if(dbLinkName=="MASSCBG"){
				pstmt.setString(4, "MAS");	
			}
			else if(dbLinkName=="XZCEBU"){
				pstmt.setString(4, "XZ");	
			}
			else{
				pstmt.setString(4, "HQ");
			}
			pstmt.setString(5,dbLinkName+"連線異常通知");
			pstmt.setString(6,"注意！"+dbLinkName+ "近10分鐘連線異常，請盡快排除異常!");
			pstmt.setInt(7, 43);
			pstmt.setInt(8, 0);
			pstmt.setInt(9, 0);
			pstmt.setString(10, "5726");
			pstmt.setString(11, "9569");
			pstmt.setString(12, "buttoncywang");
			pstmt.setInt(13, 1);
			pstmt.setString(14, "1");
			
			synchronized(pstmt){
				effectRows=pstmt.executeUpdate();
			}
			if(effectRows==1)
				isInsert=true;
			pstmt.close();
			Conn.close();
		}
		catch(Exception ex){
			logger.error("isInsert message status is failed, due to : ",ex);
		}
		finally{
			if(!Conn.isClosed())
				Conn.close();
		}
		return isInsert;
	}
	
	public boolean isDbLinkSuccess(String dbLinkName)throws Exception{
		boolean isSuccess=false;
		String sSQL="",sCloseDBlinkSql="";
		int effectRows=-1;
		Connection Conn=null;
		switch(dbLinkName){
		//select * from dual@db_linkName   也可测试dblink是否能连通
		case "AS":
			sSQL="select count(*) from emesp.tp_production_rec@AAS where rownum<10";
		    break;
		case "SFC2ERP":
			sSQL="select count(*) from FLMES.MES_PROD_DATA_DETAIL@SFC2ERP where rownum<10";
		    break;		
		case "EXCALIBUR":
			sSQL="select count(*) from sajet.g_sn_count@EXCA where rownum<10";
		    break;
		case "TAURUSDG":
			sSQL="select count(*) from sajet.g_sn_count@TAURUSDG where rownum<10";
		    break;
		case "B9":
			sSQL="select count(*) from sajet.g_sn_count@B9 where rownum<10";
		    break;
		case "CEBU":
			sSQL="select count(*) from sajet.g_sn_count@CEBU where rownum<10";
		    break;
		case "CEBUNC":
			sSQL="select count(*) from sajet.g_sn_count@CEBUNC where rownum<10";
		    break;
		case "KSSCBG":    //KSTAURUS
			sSQL="select count(*) from sajet.g_sn_count@KSSCBG where rownum<10";
			break;
		case "KSCSBG":    //B282/B312
			sSQL="select count(*) from sajet.g_sn_count@KSCSBG where rownum<10";
		    break;
		case "MASSCBG":   
			sSQL="select count(*) from sajet.g_sn_count@MASSCBG where rownum<10";
		    break;   
		case "XZCEBU":   
			sSQL="select count(*) from sajet.g_sn_count@XZCEBU where rownum<10";
		    break;   
		default:
			 break;
		}
		
		ResultSet rs=null;
		 PreparedStatement pstm = null;
		try{
			DatabaseUtility dbUtility=new DatabaseUtility("warRoomDB");
			Conn=dbUtility.makeConnection();
			pstm=Conn.prepareStatement(sSQL);
			rs=pstm.executeQuery();
			while(rs.next()){
				effectRows=rs.getInt(1);
				if(effectRows>=0){
					isSuccess=true;
				}
			}
						
		/*	Conn.commit();
		 * sCloseDBlinkSql="alter session close database link dbLinkName";
			pstm=Conn.prepareStatement(sCloseDBlinkSql);
			pstm.execute();*/
			
			rs.close();
			pstm.close();
			Conn.close();
		}
		catch(Exception ex){
			logger.error("select data from " +dbLinkName+" dsg is failed, due to : ",ex);
		}
		finally{
			if(!Conn.isClosed())
				Conn.close();
		}
		return isSuccess;
	}
	
}
