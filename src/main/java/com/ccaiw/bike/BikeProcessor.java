package com.ccaiw.bike;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ccaiw.ctrip.hotel.HotelPageProcessor;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;


public class BikeProcessor implements PageProcessor {
	
	private Logger logger=LoggerFactory.getLogger(HotelPageProcessor.class);
	
	private Site site = Site.me().setRetryTimes(3).setSleepTime(500).setRetrySleepTime(10000)
			.setUserAgent( "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
	public BikeProcessor() {
	}
	public Site getSite() {
		// TODO site-generated method stub
		return site;
	}

	public void process(Page page) {
		page.putField("res", page.getRawText());
	}
}
