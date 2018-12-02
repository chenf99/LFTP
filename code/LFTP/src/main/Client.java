package main;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;




import service.SendThread;
import tools.*;



public class Client {
	
	public static void main(String[] args) {
    	String filePath = "test.mp4";
    	String address = "127.0.0.1";
    	int sourcePort = 3777;
    	int dstPort = 3888;
    	System.out.println("正在往 " + address + ":" + dstPort + " 传送文件: " + filePath);
    	try {
    		System.out.println("准备传输：");
        	InetAddress ia = InetAddress.getByName(address);
        	Thread send_thread = new Thread(new SendThread(ia, sourcePort, dstPort, filePath));
        	send_thread.start();
        	send_thread.join();
        	System.exit(0);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
    	
    	
    	
	}
    
}
