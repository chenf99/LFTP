package service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Date;
import java.util.List;
import java.util.Timer;

import tools.ByteConverter;
import tools.Packet;

public class SendThread implements Runnable {
	private final static int BUFSIZE = 1024 * 1024;
	private List<Packet> data;		//要发送的数据
	InetAddress address;			//目的地址
	int sourcePort;					//源端口
	int destPort;					//目的端口
	private int base = 0;			//基序号
	private int nextSeq = 0;		//下一个待发送分组的序号
	private int N = 10;				//未确认的最大分组数
	private Date date;				//记录启动定时器的时间
	private DatagramSocket socket;	//用于发送数据包
	
	
	
	public SendThread(List<Packet> data, InetAddress address, int sourcePort, int destPort) {
		this.data = data;
		this.address = address;
		this.sourcePort = sourcePort;
		this.destPort = destPort;
		this.date = new Date();
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
		Thread time_out_threadThread = new Thread(new TimeOut());
		time_out_threadThread.start();
		
		//启动发送数据包
		try {
			while (nextSeq < data.size()) {
				if (nextSeq < base + N) {
					byte[] buffer = ByteConverter.objectToBytes(data.get(nextSeq));
					DatagramPacket dp = new DatagramPacket(buffer, buffer.length, address, destPort);
					socket.send(dp);
					if (base == nextSeq) startTimer();
					nextSeq++;
				}
			}
		} catch (IOException e) {
			System.out.println("SendThread: 发送数据包出错");
			e.printStackTrace();
		}
		
		//传输完成时，发送一个FIN包告知接收方
		try {
			System.out.print("发送终止packet");
			byte[] buffer = ByteConverter.objectToBytes(new Packet(0, 0, false, true, 0, null));
			DatagramPacket dp = new DatagramPacket(buffer, buffer.length, address, destPort);
			socket.send(dp);
			System.out.println("发送完毕");
		} catch (IOException e) {
			System.out.println("SendThread: 发送数据包出错");
			e.printStackTrace();
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
					if (base != nextSeq) startTimer();
					
					//确认接收最后一个分组
					if (packet.getAck() == data.size()) break;
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
				//超过3秒时触发超时
				if (curr_time - start_time > 3000) {
					System.out.println("启动重传！");
					timeOut();
				}
				
				//确认接收最后一个分组时停止计时
				if (base == nextSeq) break;
			}
			
		}
	}
	
	//超时引发重传事件
	private void timeOut() {
		startTimer();
		try {
			for (int i = base; i < nextSeq; ++i) {
				byte[] buffer = ByteConverter.objectToBytes(data.get(i));
				DatagramPacket dp = new DatagramPacket(buffer, buffer.length, address, destPort);
				System.out.println("重新发送片段：" + i);
				socket.send(dp);
			}
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
