package com.ccaiw.ctrip.landchina;

import java.util.Map;

import com.ccaiw.ctrip.landchina.model.Landchina;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

public class LandChinaPipline implements Pipeline {

	@Override
	public void process(ResultItems resultItems, Task task) {
		Map data=(Map)resultItems.get("data");
		//System.out.println(data);
		Landchina landchina=new Landchina();
		landchina.put(data);
		landchina.save();
		Record record=(Record)resultItems.getRequest().getExtra("record");
		record.set("isspider", 1);
		Db.update("landchinaurl", record);
	}

}
