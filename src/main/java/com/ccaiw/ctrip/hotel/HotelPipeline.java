package com.ccaiw.ctrip.hotel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ccaiw.ctrip.hotel.model.Hotel;
import com.jfinal.plugin.activerecord.Db;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

public class HotelPipeline implements Pipeline {

	@Override
	public void process(ResultItems resultItems, Task task) {
		String newTime=LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		List<Map> hotels=(List<Map>)resultItems.get("hotels");
		if(hotels!=null){
			List<Hotel> hs=new ArrayList<Hotel>(hotels.size());
			hotels.forEach(hotel->{
				Hotel ht=new Hotel().put(hotel).remove("isSingleRec").set("newTime", newTime);
				double price=ht.getDouble("price");
				if(price<150){
					ht.set("pricetype", "150以下");
				}else if(price<300){
					ht.set("pricetype", "150-300");
				}else if(price<450){
					ht.set("pricetype", "300-450");
				}else if(price<600){
					ht.set("pricetype", "450-600");
				}else if(price<1000){
					ht.set("pricetype", "600-1000");
				}else{
					ht.set("pricetype", "1000以上");
				}
				
				hs.add(ht);
			});
			Db.batchSave(hs, 200);
		}
		
	}
}
