/// 因为JVM堆内存的限制，大概跑了一下只能读写300-MB的文件

package tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class FileIO {
	
	// 读取整个文件成List<byte[]>
	public static List<byte[]> file2byte(String path) {
        try {
            FileInputStream inStream =new FileInputStream(new File(path));
            List<byte[]> datas = new ArrayList<>();
            final int MAX_BYTE = 1024;	//每个byte[]的容量,当前1Kb
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
		try {
            FileInputStream inStream =new FileInputStream(new File(path));
            List<byte[]> datas = new ArrayList<>();	//返回的区块信息
            final int MAX_BYTE = 1024;	//流划分byte[]的大小
            final int BLOCK_SIZE = 1024*1024*10;	//流划分区块的大小
            long streamTotal = 0;	//接受流的容量
            int bytesTotal = 0;  	//流分成满1kb的byte[]的数量
            int blockTotal = 0;	//流分成满10Mb区块block的数量
            int leave = 0;  //文件剩下的字符数
            // 获得文件输入流的总量
            streamTotal = inStream.available();
            // 获得流文件需要分开的满1kb byte[]的数量
            bytesTotal = (int)Math.floor(streamTotal/MAX_BYTE);
            // 获得流文件分开满10Mb区块的数量
            blockTotal = (int)Math.floor(streamTotal/BLOCK_SIZE);
            // 获得分开成多个登长流文件后，最后剩余的流大小
            leave = (int)streamTotal%MAX_BYTE;
            
            // 请求区块号不匹配
            if(blockNum < 0 || blockNum > blockTotal) return null;
            
            // 请求区块号是满10Mb的
            for(int i = 0; i < blockTotal; i++) {
            		for(int j = 0; j < BLOCK_SIZE/MAX_BYTE; j++) {
            			byte[] data = new byte[MAX_BYTE];
            			inStream.read(data, 0, MAX_BYTE);
            			if(i == blockNum) datas.add(data);
            		}
            }
            // 请求区块号是不满10Mb的
            if(blockNum == blockTotal) {
            	for(int i = 0; i < bytesTotal - (BLOCK_SIZE/MAX_BYTE) * blockTotal; i++) {
            		byte[] data = new byte[MAX_BYTE];
            		inStream.read(data, 0, MAX_BYTE);
            		datas.add(data);
            	}
            	byte[] data = new byte[leave];
                inStream.read(data, 0, leave);
                datas.add(data);
            }
            inStream.close();
            System.out.println("读取区块" + blockNum + "完毕! 区块大小------" + datas.size());
            return datas;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
    
    public static int getBufferLength(String path) {
    	try{
	    	FileInputStream inStream =new FileInputStream(new File(path));
	        List<byte[]> datas = new ArrayList<>();
	        final int MAX_BYTE = 1024;	//每个byte[]的容量,当前1Kb
	        long streamTotal = 0;  //接受流的容量
	        int streamNum = 0;  //流需要分开的数量
	        int leave = 0;  //文件剩下的字符数
	        // 获得文件输入流的总量
	        streamTotal = inStream.available();
	        // 获得流文件需要分开的满1kb的流的数量
	        streamNum = (int)Math.floor(streamTotal/MAX_BYTE);
	        inStream.close();
	        return streamNum+1;
    	} catch (Exception e) {
    		e.printStackTrace();
    		return 0;
    	}
    }
    
    public static int getBlockLength(String path) {
    	try{
	    	FileInputStream inStream =new FileInputStream(new File(path));
	        List<byte[]> datas = new ArrayList<>();
	        final int BLOCK_SIZE = 1024*1024*10;	//每个block的最大容量,当前10MB
	        long streamTotal = 0;  //接受流的容量
	        int blockNum = 0;  //流需要分开的区块数量
	        // 获得文件输入流的总量
	        streamTotal = inStream.available();
	        // 获得流文件需要分开的满1kb的流的数量
	        blockNum = (int)Math.floor(streamTotal/BLOCK_SIZE);
	        inStream.close();
	        return blockNum+1;
    	} catch (Exception e) {
    		e.printStackTrace();
    		return 0;
    	}
    }
    
    

    public static void main(String[] args) {
        List<byte[]> datas=file2byte("test.mp4");   
        System.out.println("readFile succeed!");
        System.out.println("Total: " + datas.size());
        
        System.out.println("Total: " + getBufferLength("test.mp4") + "kb.");
        System.out.println("BlockNum: " + getBlockLength("test.mp4"));
        for(int i = 0; i < getBlockLength("test.mp4"); i++) {
        	datas = file2bList("test.mp4", i);
        }
        //byte2file("output.rmvb",datas);
        //System.out.println("saveFile succeed!");
        //System.out.println("Total: " + getBufferLength("output.mp3") + "kb.");
        
        
    }
}
