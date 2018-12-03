package main;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import service.ReceiveThread;
import service.SendThread;
import tools.*;



public class Client {
	private static final int BUFFER_SIZE = 1024;
	private int port;
	private String fileName;
	private String operation;
	private InetAddress  serverAddress;				//服务器地址
	private int serverCPort;						//服务器控制端口
	private int serverDPort;						//服务器数据端口
	private DatagramSocket socket;
	private DatagramPacket send_packet;
	private volatile DatagramPacket rcv_packet;
	private byte[] buffer;
	private volatile Date start_date;						//用于记录是否超时
	private volatile boolean isRecved = false;
	
	public Client(int port, String operation, String fileName, InetAddress severAddress, int serverCPort) {
		this.port = port;
		this.fileName = fileName;
		this.operation = operation;
		this.serverAddress = severAddress;
		this.serverCPort = serverCPort;
		try {
			//该socket用于与服务器之间建立联系
			//建立好联系后用于传输数据
			buffer = new byte[BUFFER_SIZE];
			socket = new DatagramSocket(port);
			rcv_packet = new DatagramPacket(buffer, buffer.length);
		} catch (SocketException e) {
			System.err.println("[ERROR]启动客户端出错: " + e.getMessage());
		}
	}
	
	public void run() {
		String message = operation + " " + fileName;
		send_packet = new DatagramPacket(message.getBytes(), message.getBytes().length, serverAddress, serverCPort);
		//发送包给服务器告知客户端请求
		try {
			socket.send(send_packet);
		} catch (IOException e) {
			System.err.println("[ERROR]客户端发送数据包出错: " + e.getMessage());
		}
		start_date = new Date();
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (isRecved == false) {
					if (new Date().getTime() - start_date.getTime() > 5000) {
						System.err.println("[ERROR]网络故障，请重试");
						System.exit(0);
					}
				}
			}
		}).start();
		
		try {
			//接收到服务器回应的包
			socket.receive(rcv_packet);
			isRecved = true;
			switch (operation) {
				case "listall":
					String result = new String(buffer, 0, rcv_packet.getLength());
					System.out.println("[INFO]服务器中存在的文件: \n" + result);
					break;
				case "lget":
					//启动接收线程,此时服务器端发送线程还未开启
					message = new String(buffer, 0, rcv_packet.getLength());
					InetAddress sendInetAddress = rcv_packet.getAddress();
					String dataPort = message.substring(message.indexOf(":")+1, message.indexOf("fileSize"));
					String fileSize = message.substring(message.lastIndexOf(":")+1);
					File dir = new File("download/");
					if (!dir.exists()) {
						dir.mkdir();
					}
					Thread rcv_thread = new Thread(new ReceiveThread(port + 1, "download/" + fileName, sendInetAddress, Integer.parseInt(dataPort), true, Integer.parseInt(fileSize)));
					rcv_thread.start();
					//发送一个包到服务器数据端口，告知接收线程开启
					socket.send(new DatagramPacket(new byte[1], 1, sendInetAddress, Integer.parseInt(dataPort) - 1));
					break;
				case "lsend":
					//启动发送线程,此时服务器端接收线程已开启
					message = new String(buffer, 0, rcv_packet.getLength());
					InetAddress serverAddress = rcv_packet.getAddress();
					dataPort = message.substring(message.indexOf(":")+1);
					Thread send_thread = new Thread(new SendThread(serverAddress, port + 1, Integer.parseInt(dataPort), fileName, true));
					send_thread.start();
					break;
				default:
					System.err.println("[ERROR]无效命令");
					break;
			}
		} catch (IOException e) {
			System.err.println("[ERROR]客户端接收数据包出错: " + e.getMessage());
		}
	}
	
	/*public static void main(String[] args) {
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
	}*/
	
    
}
