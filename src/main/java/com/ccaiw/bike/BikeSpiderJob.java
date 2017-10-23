package com.ccaiw.bike;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;

public class BikeSpiderJob implements Job{
	public static void main(String[] args) {
		double step=0.0005;
		double min_lat=30.626;
		double min_lng=104.0278;
		double max_lat=30.7009;
		double max_lng=104.1198;
		int k=0;
		double lat=min_lat;
		double lng=min_lng;
		for(;lat<=max_lat;){
			lng=min_lng;
			for(;lng<=max_lng;){
				k++;
				System.out.println(lat);
				lng=sum(lng,step);
			}
			lat=sum(lat,step);
		}
		System.out.println(k);
		System.out.println(lat);
		System.out.println(lng);
		/*String token="0009511";
		String url="http://www.dancheditu.com:3000/bikes?lat=30.663284&lng=104.065648&cityid=75&token=0009511";;
		Spider spider1 = Spider.create(new BikeProcessor());
		spider1.addUrl(url).start();*/
	}
	
	public static String getUrl(Double lat,Double lng){
		return "http://www.dancheditu.com:3000/bikes?lat="+lat.toString()+"&lng="+lng.toString()+"&token=0009511&cityid=75";
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		/*double step=0.00020;
		double min_lat=30.626;   //成都二环范围内
		double min_lng=104.027991;
		double max_lat=30.701299;
		double max_lng=104.122277;*/
		double step=0.001;
		double min_lat=30.626;
		double min_lng=104.0278;
		double max_lat=30.7009;
		double max_lng=104.1198;
		int k=0;
		double lat=min_lat;
		double lng=min_lng;
		List<Request> requests=new ArrayList<>(500);
		Spider spider = Spider.create(new BikeProcessor());
		spider.addPipeline(new BikePipeline());
		for(;lat<=max_lat;){
			lng=min_lng;
			for(;lng<=max_lng;){
				spider.addUrl(getUrl(lat,lng));
				lng=sum(lng,step);
			}
			lat=sum(lat,step);
		}
		spider.thread(2).start();
		
	}
	
	public static double sum(double d1,double d2){
		BigDecimal  bd1=new BigDecimal(Double.toString(d1));
		BigDecimal  bd2=new BigDecimal(Double.toString(d2));
		BigDecimal sum=bd1.add(bd2);
		sum.setScale(6, BigDecimal.ROUND_HALF_DOWN);
		return sum.doubleValue();
	}
}
