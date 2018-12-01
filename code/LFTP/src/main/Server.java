package main;

import java.io.File;

import service.ReceiveThread;

public class Server {
	public static void main(String[] argv) {
		try{
			int serverPort = 3888;
			Thread receiveThread = new Thread(new ReceiveThread(serverPort));
			receiveThread.start();
			System.out.println("文件接受端口：" + serverPort);
			String dirString = "server";
			File file = new File(dirString);
			if(!file.exists()) {
				file.mkdir();
			}
			receiveThread.join();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
