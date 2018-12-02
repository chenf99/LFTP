package tools;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Random;

public class Percentage {
	
	
	// fileSize为文件大小(kb)，也即要传送的数据包的最大seq(1kb一个数据包)
	// date为整个文件传输开始的Date时间，也即发送方发送seq=0的时间
	// ackNum为当前传输已经完成的数据包seq
	public void showPercentage(int fileSize, Date date, int ackNum) {
		// TODO:展示进度百分比
		float percentage = (float)ackNum * 100 / (float)fileSize;
		// 保留百分比进度为两位小数
		DecimalFormat decimalFormat = new DecimalFormat("0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
		String p = decimalFormat.format(percentage);//format 返回的是字符串
		System.out.print("\r");
		System.out.print(p + "%\t");
		
		// TODO：展示进度动画
		StringBuilder percentageAnimationMessage = new StringBuilder();
		percentageAnimationMessage.append("[");
		for(int j = 0; j < 100; j++) {
			if(j < (int)percentage)
				percentageAnimationMessage.append("=");
			if(j == (int)percentage)
				percentageAnimationMessage.append(">");
			else if(j > (int)percentage)
				percentageAnimationMessage.append(" ");
		}
		percentageAnimationMessage.append("]\t");
		System.out.print(percentageAnimationMessage.toString());
		
		// TODO：展示平均速度
		int speed = getAverageSpeed(fileSize, date, ackNum);
		String speedMessage = speed + "kb/s";
		while(speedMessage.length()<16) speedMessage+=" ";//补全对齐
		System.out.print(speedMessage);
		
		// TODO： 展示剩余时间
		String remainTime = timeTransform(getRemainTime(fileSize, date, ackNum));
		System.out.print(remainTime);
	}
	
	// 按照平均速度计算剩余需要的时间
	public int getRemainTime(int fileSize, Date date, int ackNum) {
		int speed = getAverageSpeed(fileSize, date, ackNum);
		return (fileSize-ackNum)/speed;
	}
	
	// 获得从文件传输开始到当下的平均速度
	public int getAverageSpeed(int fileSize, Date date, int ackNum) {
		long startTime = date.getTime();
		long nowTime = new Date().getTime();
		long after = nowTime - startTime;
		return (int)(ackNum * 1000 / after);
	}
	
	// 时间转换，将秒数(int)转换成字符串表达
	public String timeTransform(int secondNum) {
		if (secondNum > 24*60*60) return "More than A day";
		int hour = secondNum / (60*60);
		int min = secondNum / 60 - hour * 60;
		int second = secondNum % 60;
		String resultString;
		if(min == 0) resultString = second+"s";
		else if(hour == 0) resultString =  min+"m"+second+"s";
		else resultString = hour+"h"+min+"m"+second+"s";
		while (resultString.length() < 9) resultString += " ";//补全对齐
		return resultString;
		
	}
	
	
}
