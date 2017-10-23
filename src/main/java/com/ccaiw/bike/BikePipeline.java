package com.ccaiw.bike;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ccaiw.bike.model.Biker;
import com.jfinal.plugin.activerecord.Db;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

public class BikePipeline implements Pipeline {
	private Logger logger=LoggerFactory.getLogger(BikePipeline.class);
	@Override
	public void process(ResultItems resultItems, Task task) {
		String res=resultItems.get("res");
		//logger.info(res);
		JSONObject  data=JSON.parseObject(res);
		logger.info(data.get("msg").getClass().toString());
		JSONArray ja=(JSONArray)data.get("msg");
		List<Biker> bikes=new ArrayList<>(ja.size());
		for(int i=0;i<ja.size();i++){
			JSONObject m=(JSONObject)ja.get(i);
			Biker biker=new Biker();
			biker.put(m);
			biker.set("bikeid", biker.get("id")).remove("id").set("url", resultItems.getRequest().getUrl());
			bikes.add(biker);
		}
		Db.batchSave(bikes, 20);
	}
}
