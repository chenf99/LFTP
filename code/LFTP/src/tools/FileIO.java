/// 因为JVM堆内存的限制，大概跑了一下只能读写300-MB的文件

package tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class FileIO {
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
            // 获得流文件需要分开的数量
            streamNum = (int)Math.floor(streamTotal/MAX_BYTE);
            // 获得分开成多个登长流文件后，最后剩余的流大小
            if(streamNum > 0)
            	leave = (int)streamTotal/streamNum;
            else leave = (int)streamTotal;
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

    public static void byte2file(String path,List<byte[]> datas) {
        try {
            FileOutputStream outputStream  =new FileOutputStream(new File(path));
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
    
    public static int getFileSize(String path) {
    	return 0;
    }
    
    public static int getBufferLength(String path) {
    	return 0;
    }
    

    public static void main(String[] args) {
        List<byte[]> datas=file2byte("test.txt");   
        System.out.println("readFile succeed!");
        byte2file("output.txt",datas);
        System.out.println("saveFile succeed!");
        
    }
}
