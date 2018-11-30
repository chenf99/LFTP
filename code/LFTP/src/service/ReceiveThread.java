package service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import tools.ByteConverter;
import tools.FileIO;
import tools.Packet;

public class ReceiveThread implements Runnable {
	private final static int BUFSIZE = 1024 * 1024;
	private int port;							//接收端口
	
	public ReceiveThread(int port) {
		this.port = port;
	}
	
	@Override
	public void run() {
		try {
			DatagramSocket socket = new DatagramSocket(port);
			byte[] buffer = new byte[BUFSIZE];
			List<byte[]> data = new ArrayList<>();
			DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
			socket.receive(dp);
			int seq = 0;
			while (dp.getLength() != 0) {
				Packet packet = ByteConverter.bytesToObject(buffer);
				data.add(packet.getData());
				socket.receive(dp);
			}
			String dirString = "output.txt";
			FileIO.byte2file(dirString, data);
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
