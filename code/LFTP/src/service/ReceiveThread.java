package service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import tools.ByteConverter;
import tools.Packet;

public class ReceiveThread implements Runnable {
	private final static int BUFSIZE = 1024;
	private int port;							//接收端口
	
	public ReceiveThread(int port) {
		this.port = port;
	}
	
	@Override
	public void run() {
		try {
			DatagramSocket socket = new DatagramSocket(port);
			byte[] buffer = new byte[BUFSIZE];
			while (true) {
				DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
				socket.receive(dp);
				Packet packet = ByteConverter.bytesToObject(buffer);
				System.out.println(packet.getSeq());
			}
		}
		catch (SocketException e) {
			System.out.println("ReceiveThread: 创建socket出错");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("ReceiveThread: 接收数据包出错");
			e.printStackTrace();
		}
	}
}
