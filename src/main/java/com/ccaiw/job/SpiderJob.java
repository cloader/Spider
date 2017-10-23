package com.ccaiw.job;

import java.time.LocalDate;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.ccaiw.ctrip.hotel.HotelPageProcessor;
import com.ccaiw.ctrip.hotel.HotelPipeline;
import com.ccaiw.ctrip.hotel.model.HotelCity;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import us.codecraft.webmagic.Spider;

public class SpiderJob implements Job {

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		//爬取当天的酒店数据
		HotelPageProcessor hotelPageProcessor1=new HotelPageProcessor();
		//爬取一周以后的酒店数据
		HotelPageProcessor hotelPageProcessor2=new HotelPageProcessor(LocalDate.now().plusWeeks(1).toString(),LocalDate.now().plusWeeks(1).plusDays(1).toString());
		Spider spider1 = Spider.create(hotelPageProcessor1);
		Spider spider2 = Spider.create(hotelPageProcessor2);
		for(HotelCity city:HotelCity.dao.find("select * from ctrip_hotel_city")){
			spider1.addRequest(hotelPageProcessor1.getFirstRequest(city)).addPipeline(new HotelPipeline());
			spider2.addRequest(hotelPageProcessor2.getFirstRequest(city)).addPipeline(new HotelPipeline());
		}
		spider1.thread(2).run();
		spider2.thread(2).run();
	}

}
