/// 因为JVM堆内存的限制，大概跑了一下只能读写300-MB的文件

package tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class FileIO {
	public static int MAX_BYTE = 1024;//每个byte[]的容量,当前1Kb
	public static int BLOCK_SIZE = 1024*1024*10;//流划分区块的大小,当前10MB
	public static int BYTES_IN_BLOCK = BLOCK_SIZE / MAX_BYTE;	// 一个区块byte[]的数目，目前10240
	
	// 读取整个文件成List<byte[]>
	public static List<byte[]> file2byte(String path) {
        try {
            FileInputStream inStream =new FileInputStream(new File(path));
            List<byte[]> datas = new ArrayList<>();
            long streamTotal = 0;  //接受流的容量
            int streamNum = 0;  //流需要分开的数量
            int leave = 0;  //文件剩下的字符数
            // 获得文件输入流的总量
            streamTotal = inStream.available();
            // 获得流文件需要分开的满1kb的流的数量
            streamNum = (int)Math.floor(streamTotal/MAX_BYTE);
            // 获得分开成多个登长流文件后，最后剩余的流大小
            leave = (int)streamTotal%MAX_BYTE;
            if(streamNum > 0) {
            	for(int i = 0; i < streamNum; i++) {
            		byte[] data;
            		data = new byte[MAX_BYTE];
            		inStream.read(data, 0, MAX_BYTE);
            		datas.add(data);
            	}
            }
            // 处理最后剩余的部分字符
            byte[] data = new byte[leave];
            inStream.read(data, 0, leave);
            datas.add(data);
            inStream.close();
            System.out.println("读取文件完毕,共 " + streamNum + "段");
            return datas;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
	
	// 按区块读取文件成List<byte[]>，(单个区块最大为10Mb)
	public static List<byte[]> file2bList(String path, int blockNum) {
        List<byte[]> datas = new ArrayList<>();	//返回的区块信息
		try {
            FileInputStream inStream =new FileInputStream(new File(path));
            long streamTotal = 0;	//接受流的容量
            int bytesTotal = 0;  	//流分成满1kb的byte[]的数量
            int blockTotal = 0;	//流分成满10Mb区块block的数量
            int leave = 0;  //文件剩下的字符数
            // 获得文件输入流的总量
            streamTotal = (new File(path)).length();
            // 获得流文件需要分开的满1kb byte[]的数量
            bytesTotal = getBufferLength(path);
            // 获得流文件分开满10Mb区块的数量
            blockTotal = getBlockLength(path);
            // 获得分开成多个登长流文件后，最后剩余的流大小
            leave = (int)(streamTotal%MAX_BYTE);
            
            // 请求区块号不匹配
            if(blockNum < 0 || blockNum > blockTotal) return datas;
            
            // 区块号匹配，分批次跳过区块前面的数据
            // jdk 9.0.1 skip源码有bug
            for(int i = 0; i < blockNum; i++) {
            	long skip = inStream.skip(BLOCK_SIZE);
            	if(skip != BLOCK_SIZE) return datas;
            }
            
            // 请求区块号是满10Mb的
            if(blockNum >= 0 && blockNum < blockTotal-1){
        		for(int j = 0; j < BLOCK_SIZE/MAX_BYTE; j++) {
        			byte[] data = new byte[MAX_BYTE];
        			inStream.read(data, 0, MAX_BYTE);
        			datas.add(data);
        		}
            }
            // 请求区块号是不满10Mb的
            else {
            	for(int i = 0; i < bytesTotal - (BLOCK_SIZE/MAX_BYTE) * (blockTotal-1); i++) {
            		byte[] data = new byte[MAX_BYTE];
            		inStream.read(data, 0, MAX_BYTE);
            		datas.add(data);
            	}
            	//剩余不满byte[MAX_BYTE]的
            	byte[] data = new byte[leave];
                inStream.read(data, 0, leave);
                datas.add(data);
            }
            inStream.close();
            System.out.println("读取区块" + blockNum + "完毕! 区块大小------" + datas.size() + "kb.");
            return datas;
        } catch (Exception e) {
            e.printStackTrace();
            return datas;
        }
	}
	
	// List<byte[]> 数据存入文件中(直接追加内容)
    public static void byte2file(String path,List<byte[]> datas) {
        try {
            FileOutputStream outputStream  =new FileOutputStream(new File(path), true);
            for(int i = 0; i < datas.size(); i++) {
            	outputStream.write(datas.get(i));
            	outputStream.flush();
        		//System.out.println("写入文件片段" + i);
            }
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // 获得文件byte[MAX_BYTE]的数量
    public static int getBufferLength(String path) {
    	try{
    		// 获取文件 的总字节数
    		File file = new File(path);
    		System.out.println(file.length());
    		long streamTotal =  file.length();
    		return (int)Math.floor(streamTotal/MAX_BYTE);
    	} catch (Exception e) {
    		e.printStackTrace();
    		return 0;
    	}
    }
    
    // 获得文件可以划分的区块数
    public static int getBlockLength(String path) {
    	try{
    		// 获取文件 的总字节数
    		File file = new File(path);
    		long streamTotal =  file.length();
	        // 获得流文件需要分开的满1kb的流的数量
	        return (int)Math.floor(streamTotal/BLOCK_SIZE) + 1;
    	} catch (Exception e) {
    		e.printStackTrace();
    		return 0;
    	}
    }
    

    

    public static void main(String[] args) {
    	
    	
    	String path = "C:/Users/chenbb/Desktop/test.zip";
        System.out.println("Total: " + getBufferLength(path) + "kb.");
        System.out.println("BlockNum: " + getBlockLength(path));
        String dirString = "download";
		File file = new File(dirString);
		if(!file.exists()) {
			file.mkdir();
		}
		
		for(int i = 0; i < getBlockLength(path); i++) {
        	List<byte[]> datas = file2bList(path, i);
        	byte2file("download/test.zip", datas);
        	System.out.println("区块" + i + "传输完毕！");
        }
        
        
        
    }
}
