package main;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;




import service.SendThread;
import tools.*;



public class Client {
	
	public static void main(String[] args) {
    	String dir = "test.mp4";
    	String address = "localhost";
    	int sourcePort = 3777;
    	int dstPort = 3888;
    	String fileName;
    	String[] split_dir = dir.split("/");
    	int split_dir_len = split_dir.length;
    	fileName = split_dir[split_dir_len-1];
    	System.out.println("正在往 " + address + ":" + dstPort + " 传送文件: " + dir);
    	List<byte[]> byteList = FileIO.file2byte(dir);
    	List<Packet> packageList = new ArrayList<>();
    	Packet data;
    	for(int i = 0; i < byteList.size(); i++) {
    		data = new Packet(0, i, false, false, 50, byteList.get(i), fileName);
    		packageList.add(data);
    	}
    	try {
    		System.out.println("准备传输：");
        	InetAddress ia = InetAddress.getByName(address);
        	Thread send_thread = new Thread(new SendThread(packageList, ia, sourcePort, dstPort, fileName));
        	send_thread.start();
        	send_thread.join();
        	System.exit(0);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
    	
    	
    	
	}
    
}
