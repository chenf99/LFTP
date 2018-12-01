package service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;
import java.util.Timer;

import tools.ByteConverter;
import tools.Packet;

public class SendThread implements Runnable {
	private List<Packet> data;		//要发送的数据
	InetAddress address;			//目的地址
	int port;						//目的端口
	private int base = 0;			//基序号
	private int nextSeq = 0;		//下一个待发送分组的序号
	private int N = 10;				//未确认的最大分组数
	private Timer timer;			//定时器
	
	
	
	public SendThread(List<Packet> data, InetAddress address, int port) {
		this.data = data;
		this.address = address;
		this.port = port;
	}

	@Override
	public void run() {
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
			System.out.println("size: " + data.size());
			for (int i = 0; i < data.size(); ++i) {
				byte[] buffer = ByteConverter.objectToBytes(data.get(i));
				DatagramPacket dp = new DatagramPacket(buffer, buffer.length, address, port);
				socket.send(dp);
				System.out.println("传输片段：" + i);
				// 休眠50ms，简单避免拥塞导致的丢包
				if((i%50)==0) {
					Thread.sleep(50);
				}
			}
			System.out.print("传输终止packet");
			byte[] buffer = ByteConverter.objectToBytes(new Packet(0, 0, false, true, 0, null));
			DatagramPacket dp = new DatagramPacket(buffer, buffer.length, address, port);
			socket.send(dp);
			System.out.println("传输完毕");
			
		} catch (SocketException e) {
			System.out.println("SendThread: 创建socket出错");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("SendThread: 发送数据包出错");
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println("Sleep: 发生出错");
			e.printStackTrace();
		}
	}
}
