package main;

<<<<<<< HEAD
import java.util.ArrayList;
import java.util.List;

import javax.xml.crypto.Data;

import tools.FileIO;



public class Client {
	
	public static void main(String[] args) {
    	String dir = "test.txt";
    	List<byte[]> byteList = FileIO.file2byte(dir);
    	List<Package> packageList = new ArrayList<>();
    	Package data;
    	for(int i = 0; i < byteList.size(); i++) {
    		//data = new Package();
    	}
    	
	}
    
    public void run() {
    	
    }
}
