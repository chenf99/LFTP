package service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Date;
import java.util.List;

import tools.ByteConverter;
import tools.Packet;

public class SendThread implements Runnable {
	private final static int BUFSIZE = 1024 * 1024;
	private int TTL = 300;							//定时器过期间隔
	private List<Packet> data;						//要发送的数据
	InetAddress address;							//目的地址
	int sourcePort;									//源端口
	int destPort;									//目的端口
	private volatile int base = 0;					//基序号
	private volatile int nextSeq = 0;				//下一个待发送分组的序号
	private int N = 10;								//未确认的最大分组数
	private volatile Date date;						//记录启动定时器的时间
	private DatagramSocket socket;					//用于发送数据包
	private volatile boolean retrans = false;		//当前是否在重传
	private volatile int currAck = -1;				//最后一个被确认的分组ack
	private volatile int rwnd = 1024;				//接收方空闲缓存空间
	private String fileName;						//文件名
	
	
	
	public SendThread(List<Packet> data, InetAddress address, int sourcePort, int destPort, String fileName) {
		this.data = data;
		this.address = address;
		this.sourcePort = sourcePort;
		this.destPort = destPort;
		this.date = new Date();
		this.fileName = fileName;
		try {
			this.socket = new DatagramSocket(sourcePort);
		} catch (SocketException e) {
			System.out.println("SendThread: 创建socket出错");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		//System.out.println("size: " + data.size());
		
		//启动接收ACK包线程
		Thread recv_ack_thread = new Thread(new RecvAck());
		recv_ack_thread.start();
		
		// 启动超时判断处理线程
		Thread time_out_threadThread;
		time_out_threadThread = new Thread(new TimeOut());
		time_out_threadThread.start();
		
		//启动发送数据包
		try {
			while (nextSeq < data.size()) {
				//接收方缓存满
				if (rwnd <= 0) {
					System.out.println("接收方缓存满，暂停发送");
				}
				else if (nextSeq < base + N && retrans == false) {
					//if (nextSeq % N != 0) {
					byte[] buffer = ByteConverter.objectToBytes(data.get(nextSeq));
					DatagramPacket dp = new DatagramPacket(buffer, buffer.length, address, destPort);
					Packet packet = ByteConverter.bytesToObject(dp.getData());
					System.out.println("发送的分组序号: " + packet.getSeq());
					socket.send(dp);
					if (base == nextSeq) startTimer();
					//}
					nextSeq++;
				}
			}
		} catch (IOException e) {
			System.out.println("SendThread: 发送数据包出错");
			e.printStackTrace();
		}
		
		//传输完成时，发送一个FIN包告知接收方
		while (true) {
			if (currAck == data.size() - 1 && rwnd > 0) {
				try {
					System.out.println("发送终止packet");
					byte[] buffer = ByteConverter.objectToBytes(new Packet(-1, -1, false, true, -1, null, fileName));
					DatagramPacket dp = new DatagramPacket(buffer, buffer.length, address, destPort);
					socket.send(dp);
					System.out.println("发送完毕");
				} catch (IOException e) {
					System.out.println("SendThread: 发送数据包出错");
					e.printStackTrace();
				}
				break;
			}
		}
	
	}
	
	//接收ACK包的线程
	class RecvAck implements Runnable {
		@Override
		public void run() {
			try {
				while (true) {
					byte[] buffer = new byte[BUFSIZE];
					DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
					socket.receive(dp);
					Packet packet = ByteConverter.bytesToObject(buffer);
					System.out.println("确认分组: " + packet.getAck());
					base = packet.getAck() + 1;
					currAck = packet.getAck();
					rwnd = packet.getRwwd();
					
					//接收方缓存已满，继续发送一个只有一个字节数据的报文段
					if (rwnd == 0) {
						byte[] tmp = ByteConverter.objectToBytes(new Packet(-1, -1, false, false, -1, null, fileName));
						DatagramPacket tmpPack = new DatagramPacket(tmp, tmp.length, address, destPort);
						socket.send(tmpPack);
					}
					
					if (base != nextSeq) startTimer();
					
					//确认接收最后一个分组
					if (packet.getAck() == data.size() - 1) break;
				}
			} catch (IOException e) {
				System.out.println("ReceiveThread: 接收数据包出错");
			}
		}
	}
	
	//判断是否超时的线程
	class TimeOut implements Runnable {
		@Override
		public void run() {
			while (true) {
				long start_time = date.getTime();
				long curr_time = new Date().getTime();
				//超过0.3秒时触发超时
				if (curr_time - start_time > TTL) timeOut();
				
				//确认接收最后一个分组时停止计时
				if (currAck == data.size() - 1) break;
			}
		}
	}
	
	//超时引发重传事件
	private void timeOut() {
		System.out.println("启动重传！");
		startTimer();
		try {
			//记录base值和nextSeq值，防止接收线程对其造成改变
			int myBase = base, myNextSeq = nextSeq;
			retrans = true;
			for (int i = myBase; i < myNextSeq; ++i) {
				while (rwnd <= 0) System.out.println("接收方缓存不够，暂停重传"); 
				byte[] buffer = ByteConverter.objectToBytes(data.get(i));
				DatagramPacket dp = new DatagramPacket(buffer, buffer.length, address, destPort);
				System.out.println("重新发送片段：" + i);
				socket.send(dp);
			}
			retrans = false;
		} catch (IOException e) {
			System.out.println("SendThread: 发送数据包出错");
			e.printStackTrace();
		}
	}
	
	//启动定时器
	private void startTimer() {
		date = new Date();
	}
}
