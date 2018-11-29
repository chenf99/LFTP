package tools;

import java.io.Serializable;

public class Packet implements Serializable {
	private int ack;		//确认号
	private int seq;		//序列号
	private boolean ACK;	//ACK标志位
	private boolean FIN;	//FIN标志位
	private int rwwd;		//接收窗口(流量控制)
	private byte[] data;	//数据
	
	public void setAck(int ack) {
		this.ack = ack;
		this.ACK = true;
	}
	public int getAck() {
		return ack;
	}
	
	public void setSeq(int seq) {
		this.seq = seq;
	}
	public int getSeq() {
		return seq;
	}
	
	public void setACK(boolean aCK) {
		ACK = aCK;
	}
	public boolean isACK() {
		return ACK;
	}
	
	public void setFIN(boolean fIN) {
		FIN = fIN;
	}
	public boolean isFIN() {
		return FIN;
	}
	
	public void setRwwd(int rwwd) {
		this.rwwd = rwwd;
	}
	public int getRwwd() {
		return rwwd;
	}
	
	public void setData(byte[] data) {
		this.data = data;
	}
	public byte[] getData() {
		return data;
	}
}
