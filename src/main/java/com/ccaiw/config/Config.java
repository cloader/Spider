/**   
* @Title: Config.java 
* @Package com.yba.graphshow 
* @Description: TODO
* @author SirChen
* @date 2017年3月30日 下午4:30:45 
* @version V1.0   
*/
package com.ccaiw.config;

import com.ccaiw.bike.model.Biker;
import com.ccaiw.ctrip.hotel.model.Hotel;
import com.ccaiw.ctrip.hotel.model.HotelCity;
import com.ccaiw.ctrip.hotel.model.Task;
import com.ccaiw.ctrip.landchina.LandchinaPageProcessor;
import com.ccaiw.ctrip.landchina.model.Landchina;
import com.ccaiw.quartz.QuartzPlugin;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.core.JFinal;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.template.Engine;

/**
 * @ClassName: Config
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author SirChen
 * @date 2017年3月30日 下午4:30:45
 * 
 */
public class Config extends JFinalConfig {

	@Override
	public void configConstant(Constants cons) {
		cons.setDevMode(true);
	}

	@Override
	public void configEngine(Engine engine) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configHandler(Handlers handlers) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configInterceptor(Interceptors interceptors) {
		// TODO Auto-generated method stub
	}

	@Override
	public void configPlugin(Plugins plugins) {
		// TODO Auto-generated method stub
		PropKit.use("jdbc.properties");
		DruidPlugin druidPlugin=new DruidPlugin(PropKit.get("db_url"), PropKit.get("db_user"), PropKit.get("db_pwd"));
		ActiveRecordPlugin  activeRecordPlugin=new ActiveRecordPlugin(druidPlugin) ;
		activeRecordPlugin.addMapping("biker2", Biker.class);
		activeRecordPlugin.addMapping("ctrip_hotel", Hotel.class);
		activeRecordPlugin.addMapping("ctrip_hotel_city", HotelCity.class);
		activeRecordPlugin.addMapping("taskinfo", Task.class);
		activeRecordPlugin.addMapping("landchina", Landchina.class);
		plugins.add(druidPlugin);
		plugins.add(activeRecordPlugin);
		plugins.add(new QuartzPlugin());
	}

	@Override
	public void configRoute(Routes routes) {
			
	}
	
	@Override
	public void afterJFinalStart() {
		super.afterJFinalStart();
	}
	
	public static void main(String[] args) {
		JFinal.start("src/main/webapp", 80, "/", 5); 
	}
}
