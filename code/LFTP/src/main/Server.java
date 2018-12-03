package main;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Date;

import service.ReceiveThread;
import service.SendThread;
import tools.FileIO;

public class Server {
	private static final int BUFFER_SIZE = 1024;
	private int cPort;					//控制端口
	private volatile DatagramSocket socket;
	private DatagramPacket send_packet;
	private DatagramPacket rcv_packet;
	private byte[] buffer;
	
	public Server(int cPort) {
		this.cPort = cPort;
		buffer = new byte[BUFFER_SIZE];
		try {
			//该socket用于与客户端之间建立联系
			//收发数据传输的控制信息
			socket = new DatagramSocket(cPort);
			rcv_packet = new DatagramPacket(buffer, buffer.length);
		} catch (SocketException e) {
			System.err.println("[ERROR]启动服务器出错: " + e.getMessage());
			//e.printStackTrace();
		}
	}
	
	public void run() throws SocketException {
		while (true) {
			//等待接收来自客户端的请求
			try {
				socket.receive(rcv_packet);
			} catch (IOException e) {
				System.err.println("[ERROR]接收数据包出错: " + e.getMessage());
			}
			String message = new String(buffer, 0, rcv_packet.getLength());
			System.out.println("[cmd]" + message);
			String operation = message.substring(0, message.indexOf(" "));
			String fileName = message.substring(message.indexOf(" ") + 1);
			//获得客户端的地址和端口
			InetAddress address = rcv_packet.getAddress();
			int port = rcv_packet.getPort();
			int dataPort;
			switch (operation) {
				case "lget":
					//创建数据端口
					dataPort = 20000 + (int)(Math.random() * 1000);
					int fileSize = FileIO.getBufferLength("server/" + fileName);
					message = "dataport:" + dataPort + "fileSize:" + fileSize;
					//告知客户端数据端口
					send_packet = new DatagramPacket(message.getBytes(), message.getBytes().length, address, port);
					try {
						socket.send(send_packet);
					} catch (IOException e) {
						System.err.println("[ERROR]发送数据包出错: " + e.getMessage());
					}
					
					DatagramSocket messSocket = new DatagramSocket(dataPort - 1);
					messSocket.setSoTimeout(5000);
					//得知客户端建立好接收线程
					try {
						messSocket.receive(rcv_packet);
					} catch (IOException e) {
						System.err.println("[ERROR]连接客户端超时: " + e.getMessage());
						//重新建立控制连接，在网络故障时有必要
						messSocket.close();
						break;
					}
					//没有网络异常时启动发送线程
					System.out.println("[INFO]开始发送文件 " + fileName);
					System.out.println("[INFO]服务器数据端口 " + dataPort);
					messSocket.close();
					Thread send_thread = new Thread(new SendThread(address, dataPort, port + 1, "server/" + fileName, false));
					send_thread.start();
					break;
				case "lsend":
					//创建数据端口
					dataPort = 30000 + (int)(Math.random() * 1000);
					message = "dataport:" + dataPort;
					
					//创建接收线程
					File dir = new File("server/");
					if (!dir.exists()) {
						dir.mkdir();
					}
					System.out.println("[INFO]开始接收文件 " + fileName);
					System.out.println("[INFO]服务器数据端口 " + dataPort);
					Thread recv_thread = new Thread(new ReceiveThread(dataPort, "server/" + fileName, address, port + 1, false, 0));
					recv_thread.start();
					//告知客户端接收线程开启以及数据端口
					//告知客户端数据端口
					send_packet = new DatagramPacket(message.getBytes(), message.getBytes().length, address, port);
					try {
						socket.send(send_packet);
					} catch (IOException e) {
						System.err.println("[ERROR]发送数据包出错: " + e.getMessage());
					}
					break;
				case "listall":
					File file=new File("server/");
					String result = "";
					for(File temp:file.listFiles()){
			            if(!temp.isDirectory()){
			                result += "[INFO]" + temp.toString().substring(temp.toString().indexOf("/")) + "\n";
			            }
			        }
					if (result == "") result = "[INFO]服务器中没有文件.";
					send_packet = new DatagramPacket(result.getBytes(), result.getBytes().length, address, port);
					try {
						socket.send(send_packet);
					} catch (IOException e) {
						System.err.println("[ERROR]发送数据包出错: " + e.getMessage());
					}
					break;
				default:
					break;
				}
		}
	}
}
