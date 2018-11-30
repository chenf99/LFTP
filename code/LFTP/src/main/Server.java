package main;

import service.ReceiveThread;

public class Server {
	public static void main(String[] argv) {
		Thread receiveThread = new Thread(new ReceiveThread(3888));
		receiveThread.start();
		
	}
}
