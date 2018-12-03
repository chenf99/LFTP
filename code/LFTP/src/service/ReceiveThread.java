package service;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tools.Percentage;
import tools.ByteConverter;
import tools.FileIO;
import tools.Packet;

public class ReceiveThread implements Runnable {
	private final static int BUFSIZE = 1024 * 1024;
	private final static int MAX_RWND = 1024 * 10;	// 接收方接收窗口为10Mb，用于流量控制
	private DatagramSocket socket;					// UDP连接DatagramSocket
	private int recvPort;							// 接收方接收端口
	private int expectedseqnum;						// 期望收到的序列号
	InetAddress sendInetAddress;					// 发送方IP地址
	int sendPort;									// 发送方端口
	int rwnd = MAX_RWND;						
	String downloadDir;								// 存储位置
	private boolean isClient;						// 该接收线程是否是客户端创建
	private int fileSize;							// 接收的文件大小,客户端接收时需要得知
	
	public ReceiveThread(int recvPort, String dir, InetAddress sendInetAddress, int sendPort, boolean isClient, int fileSize) {
		this.recvPort = recvPort;
		this.downloadDir = dir;
		this.sendInetAddress = sendInetAddress;
		this.sendPort = sendPort;
		this.isClient = isClient;
		this.fileSize = fileSize;
		expectedseqnum = 0;
	}
	
	public InetAddress getSendInetAddress() {
		return sendInetAddress;
	}
	public void setSendInetAddress(InetAddress ia) {
		this.sendInetAddress = ia;
	}
	public int getSendPort() {
		return sendPort;
	}
	public void setSendPort(int port) {
		this.sendPort = port;
	}
	
	@Override
	public void run() {
		try {
			socket = new DatagramSocket(recvPort);
			byte[] buffer = new byte[BUFSIZE];
			List<byte[]> data = new ArrayList<>();
			DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
			// 阻塞等待第一个数据包
			socket.receive(dp);
			/*// 获取客户端IP和发送端口
			setClientInetAddress(dp.getAddress());
			setClientPort(dp.getPort());*/
			System.out.println("[INFO]发送方地址---" + sendInetAddress.toString().substring(1) + ":" + sendPort);
			String[] fileStringList = downloadDir.split("/");
			String fileName = fileStringList[fileStringList.length - 1];
			
			final Date startTime = new Date();
			//如果是客户端接收文件，显示进度条信息
			Thread percentageThread = new Thread(new Runnable() {
				@Override
				public void run() {
					while(expectedseqnum < fileSize) {
						Percentage.showPercentage(fileSize, startTime, expectedseqnum-1);
					}
					Percentage.showPercentage(fileSize, startTime, expectedseqnum-1);
				}
			});
			if(isClient){
				percentageThread.start();
			}
			
			
			while (true) {		
				// 将收到的数据包转换成封装的Packet
				Packet packet = ByteConverter.bytesToObject(buffer);
				// 从第一个数据包中获取发送的文件名,并清空服务端的文件内容
				if(expectedseqnum == 0) {
			        File file=new File(downloadDir);
			         if(file.exists()&&file.isFile()) {
			             file.delete();
			         }
				}
				// 接收到完成发送的FIN信号数据包，跳出循环
				if (packet.isFIN() == true) break;
				// 判断当前rwnd窗口是否已满，满了进行文件写入
				if(rwnd == 0) {
					FileIO.byte2file(downloadDir, data);
					//System.out.println("窗口满了，写入 " + MAX_RWND / 1024 + "Mb数据.");
					// 清空List,回收内存，重置接收窗口空闲空间
					data = null;
					System.gc();
					data = new ArrayList<>();
					rwnd = MAX_RWND;
					Packet ackPacket = new Packet(expectedseqnum-1, -1, false, false, rwnd, null);
					byte[] ackBuffer = ByteConverter.objectToBytes(ackPacket);
					DatagramPacket ackdp = new DatagramPacket(ackBuffer, ackBuffer.length, sendInetAddress, sendPort);
					socket.send(ackdp);
					//System.out.println("ACK(rwnd): " + (expectedseqnum-1) + "——————expect: " + expectedseqnum);
				}
				// 接收到期望收到的数据包
				else if(packet.getSeq() == expectedseqnum) {
					// 提取数据包，递交数据
					data.add(packet.getData());
					// 期待下一个序列号的数据包
					expectedseqnum++;
					// 接受空闲窗口减少1
					rwnd--;
					//System.out.println("接收片段：" + packet.getSeq());
					// 返回一个正确接受的ACK包
					Packet ackPacket = new Packet(expectedseqnum-1, -1, true, false, rwnd, null);
					byte[] ackBuffer = ByteConverter.objectToBytes(ackPacket);
					DatagramPacket ackdp = new DatagramPacket(ackBuffer, ackBuffer.length, sendInetAddress, sendPort);
					socket.send(ackdp);
					//System.out.println("ACK(right): " + (expectedseqnum-1) + "——————expect: " + expectedseqnum + "——————get: " + packet.getSeq());
				}
				// 接受到非期望数据包
				else {
					// 返回一个错误接受的ACK包
					Packet ackPacket = new Packet(expectedseqnum-1, -1, true, false, rwnd, null);
					byte[] ackBuffer = ByteConverter.objectToBytes(ackPacket);
					DatagramPacket ackdp = new DatagramPacket(ackBuffer, ackBuffer.length, sendInetAddress, sendPort);
					socket.send(ackdp);
					//System.out.println("ACK(wrong): " + (expectedseqnum-1) + "——————expect: " + expectedseqnum + "——————get: " + packet.getSeq());
				}
				// 阻塞等待下一个数据包
				socket.receive(dp);
			}
			FileIO.byte2file(downloadDir, data);
			if(isClient) {
				percentageThread.join();
			}
			System.out.println("[INFO]成功接收文件" + fileName);
		}
		catch (SocketException e) {
			System.out.println("ReceiveThread: 创建socket出错");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("ReceiveThread: 接收数据包出错");
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println("ReceiveThread: 进度线程出错");
			e.printStackTrace();
		}
	}
	

}
