package com.ccaiw.ctrip.landchina;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.kit.HttpKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.xiaoleilu.hutool.util.ObjectUtil;
import com.xiaoleilu.hutool.util.ReUtil;
import com.xiaoleilu.hutool.util.ThreadUtil;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.model.HttpRequestBody;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.SimpleProxyProvider;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.utils.HttpConstant;

public class LandchinaPageProcessor implements PageProcessor {
	private static Log logger=Log.getLog(LandchinaPageProcessor.class);
	private static String host="http://www.landchina.com";
	private static String url="http://www.landchina.com/default.aspx?tabid=263&ComName=default";
	private Site site = Site.me().setRetryTimes(1).setCycleRetryTimes(1).setSleepTime(10000).setRetrySleepTime(3000).setCharset("gb2312").setTimeOut(10000).setDomain(host);
	@Override
	public void process(Page page) {
		Map heanders=page.getHeaders();
		Map extras_=page.getRequest().getExtras();
		Map extras=new HashMap();
		extras.putAll(extras_);
		Html  html=page.getHtml();
		String result=html.get();
		//System.out.println(result);
		if(result.contains("self.location")){
			logger.info("安全狗防护跳转");
			//跳转了4次
			if((int)extras.get("times")>2){
				logger.info("安全狗防护跳转超过3次  休眠五分钟");
				ThreadUtil.sleep(5*60000);
				page.setSkip(true);
				return;
			}
			List<String> rs=ReUtil.findAll("self.location=\"(.*?)\";", result, 1);
			if(!rs.isEmpty()){
				String url=host+rs.get(0);
				Request request = new Request(url);
				//request.addHeader("Proxy-Authorization", authHeader());
				if( (int)extras.get("times")==0){
					extras.put("request",page.getRequest());
				}
				extras.put("times", (int)extras.get("times")+1);
				System.out.println(extras);
				request.setExtras(extras);
				page.addTargetRequest(request);
				logger.info("跳转链接:"+host+rs.get(0));
				try {
					Thread.sleep(new Random().nextInt(5000));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			page.setSkip(true);
			//page.setDownloadSuccess(false);
			
		}else{
			if(page.getRequest().getUrl().contains("tabid=263")){
				List<String> nums= ReUtil.findAll("共(\\d*?)页", result, 1);
				int pageNum=(int)extras.get("TAB_QuerySubmitPagerData");
				String dateStr=(String) extras.get("dateStr");
				List<String>  links=html.xpath("//*[@id=\"TAB_contentTable\"]/tbody/tr/td[3]/a/@href").all();
				for(String link:links){
					Record record=new Record();
					record.set("url", link);
					record.set("datestr", dateStr);
					record.set("pagenum", pageNum);
					Db.save("landchinaurl", record);
					/*logger.info("link:"+link);
					Long num=Db.queryLong("select count(*) from landchina where url=?",host+"/"+link);
					if(num==0){
						page.addTargetRequest(link);
					}else{
						logger.info("已抓取过。忽略");
					}*/
				}
				if(nums.size()>0){
					int sumNum=Integer.valueOf(nums.get(0));
					logger.info("当前页数:"+pageNum+",总页数:"+sumNum+",日期:"+dateStr);
					if(pageNum==1){
						while(pageNum<sumNum){
							page.addTargetRequest(getRequest(dateStr, ++pageNum));
						}
					}
				}else if(links.size()==0){
					logger.info("抓取页码错误... 下一页失败,当前页数:"+pageNum);
					//page.setDownloadSuccess(false);
					try {
						Thread.sleep(30000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				page.setSkip(true);
			}else{
				List<String> infos=page.getHtml().xpath("//*[@id=\"p1\"]/table/tbody/tr/td/allText()").all();
				//String source=page.getHtml().xpath("//*[@id=\"mainModuleContainer_1855_1856_ctl00_ctl00_p1_f1_r2_c4\"]/span").get();
				//System.out.println(source);
				if(infos.size()==50){
					Map<String,String> data=new HashMap<String,String>();
					data.put("行政区", infos.get(6));
					data.put("电子监管号", infos.get(8));
					data.put("项目名称", infos.get(10));
					data.put("项目位置", infos.get(12));
					data.put("面积(公顷)", infos.get(14));
					if(infos.get(16).equals(infos.get(14))){
						data.put("土地来源", "现有建设用地");
					}else if(infos.get(16).equals("")){
						data.put("土地来源", "新增建设用地(来自存量库)");
					}else if(Float.valueOf(infos.get(16))==0){
						data.put("土地来源", "新增建设用地");
					}else{
						data.put("土地来源", "新增建设用地(来自存量库)");
					}
					data.put("土地用途", infos.get(18));
					data.put("供地方式", infos.get(20));
					data.put("土地使用年限", infos.get(22));
					data.put("行业分类", infos.get(24));
					data.put("土地级别", infos.get(26));
					data.put("成交价格(万元)", infos.get(28));
					data.put("分期支付约定", infos.get(30));
					data.put("土地使用权人", infos.get(32));
					data.put("约定容积率", infos.get(35));
					data.put("约定交地时间", infos.get(37));
					data.put("约定开工时间", infos.get(39));
					data.put("约定竣工时间", infos.get(41));
					data.put("实际开工时间", infos.get(43));
					data.put("实际竣工时间", infos.get(45));
					data.put("批准单位", infos.get(47));
					data.put("合同签订日期", infos.get(49));
					data.put("url", page.getRequest().getUrl());
					page.putField("data", data);
				}else{
					logger.info("数据异常:"+page.getRequest().getUrl()+":");
					String body=page.getHtml().xpath("/html/body/allText()").get();
					logger.info(body);
					if(body.contains("请求过于频繁")){
						ThreadUtil.sleep(5*60*1000);
					}
					page.setSkip(true);
				}
				List<Record> rs=Db.find("select * from landchinaurl where isspider=0 order by id  limit 2");
				if(rs.size()==2){
					page.addTargetRequest(LandchinaPageProcessor.getRequest(rs.get(1)));
				}
			}
		}
		
	}

	@Override
	public Site getSite() {
		return site.setUserAgent(UserAgentKit.getRamdonUserAgent());
	}
	
	
	public static  Request getRequest(String dateStr,int page){
		Request request = new Request(url);
		HashMap<String, Object> params = new HashMap<String, Object>();
	//	params.put("__VIEWSTATE", "/wEPDwUJNjkzNzgyNTU4D2QWAmYPZBYIZg9kFgICAQ9kFgJmDxYCHgdWaXNpYmxlaGQCAQ9kFgICAQ8WAh4Fc3R5bGUFIEJBQ0tHUk9VTkQtQ09MT1I6I2YzZjVmNztDT0xPUjo7ZAICD2QWAgIBD2QWAmYPZBYCZg9kFgJmD2QWBGYPZBYCZg9kFgJmD2QWAmYPZBYCZg9kFgJmDxYEHwEFIENPTE9SOiNEM0QzRDM7QkFDS0dST1VORC1DT0xPUjo7HwBoFgJmD2QWAgIBD2QWAmYPDxYCHgRUZXh0ZWRkAgEPZBYCZg9kFgJmD2QWAmYPZBYEZg9kFgJmDxYEHwEFhwFDT0xPUjojRDNEM0QzO0JBQ0tHUk9VTkQtQ09MT1I6O0JBQ0tHUk9VTkQtSU1BR0U6dXJsKGh0dHA6Ly93d3cubGFuZGNoaW5hLmNvbS9Vc2VyL2RlZmF1bHQvVXBsb2FkL3N5c0ZyYW1lSW1nL3hfdGRzY3dfc3lfamhnZ18wMDAuZ2lmKTseBmhlaWdodAUBMxYCZg9kFgICAQ9kFgJmDw8WAh8CZWRkAgIPZBYCZg9kFgJmD2QWAmYPZBYCZg9kFgJmD2QWAmYPZBYEZg9kFgJmDxYEHwEFIENPTE9SOiNEM0QzRDM7QkFDS0dST1VORC1DT0xPUjo7HwBoFgJmD2QWAgIBD2QWAmYPDxYCHwJlZGQCAg9kFgJmD2QWBGYPZBYCZg9kFgJmD2QWAmYPZBYCZg9kFgJmD2QWAmYPFgQfAQUgQ09MT1I6I0QzRDNEMztCQUNLR1JPVU5ELUNPTE9SOjsfAGgWAmYPZBYCAgEPZBYCZg8PFgIfAmVkZAICD2QWBGYPZBYCZg9kFgJmD2QWAmYPZBYCAgEPZBYCZg8WBB8BBYYBQ09MT1I6I0QzRDNEMztCQUNLR1JPVU5ELUNPTE9SOjtCQUNLR1JPVU5ELUlNQUdFOnVybChodHRwOi8vd3d3LmxhbmRjaGluYS5jb20vVXNlci9kZWZhdWx0L1VwbG9hZC9zeXNGcmFtZUltZy94X3Rkc2N3X3p5X2pnZ2dfMDEuZ2lmKTsfAwUCNDYWAmYPZBYCAgEPZBYCZg8PFgIfAmVkZAIBD2QWAmYPZBYCZg9kFgJmD2QWAgIBD2QWAmYPFgQfAQUgQ09MT1I6I0QzRDNEMztCQUNLR1JPVU5ELUNPTE9SOjsfAGgWAmYPZBYCAgEPZBYCZg8PFgIfAmVkZAIDD2QWAgIDDxYEHglpbm5lcmh0bWwFgwc8cCBhbGlnbj0iY2VudGVyIj48c3BhbiBzdHlsZT0iZm9udC1zaXplOiB4LXNtYWxsIj4mbmJzcDs8YnIgLz4NCiZuYnNwOzxhIHRhcmdldD0iX3NlbGYiIGhyZWY9Imh0dHA6Ly93d3cubGFuZGNoaW5hLmNvbS8iPjxpbWcgYm9yZGVyPSIwIiBhbHQ9IiIgd2lkdGg9IjI2MCIgaGVpZ2h0PSI2MSIgc3JjPSIvVXNlci9kZWZhdWx0L1VwbG9hZC9mY2svaW1hZ2UvdGRzY3dfbG9nZS5wbmciIC8+PC9hPiZuYnNwOzxiciAvPg0KJm5ic3A7PHNwYW4gc3R5bGU9ImNvbG9yOiAjZmZmZmZmIj5Db3B5cmlnaHQgMjAwOC0yMDE0IERSQ25ldC4gQWxsIFJpZ2h0cyBSZXNlcnZlZCZuYnNwOyZuYnNwOyZuYnNwOyA8c2NyaXB0IHR5cGU9InRleHQvamF2YXNjcmlwdCI+DQp2YXIgX2JkaG1Qcm90b2NvbCA9ICgoImh0dHBzOiIgPT0gZG9jdW1lbnQubG9jYXRpb24ucHJvdG9jb2wpID8gIiBodHRwczovLyIgOiAiIGh0dHA6Ly8iKTsNCmRvY3VtZW50LndyaXRlKHVuZXNjYXBlKCIlM0NzY3JpcHQgc3JjPSciICsgX2JkaG1Qcm90b2NvbCArICJobS5iYWlkdS5jb20vaC5qcyUzRjgzODUzODU5YzcyNDdjNWIwM2I1Mjc4OTQ2MjJkM2ZhJyB0eXBlPSd0ZXh0L2phdmFzY3JpcHQnJTNFJTNDL3NjcmlwdCUzRSIpKTsNCjwvc2NyaXB0PiZuYnNwOzxiciAvPg0K54mI5p2D5omA5pyJJm5ic3A7IOS4reWbveWcn+WcsOW4guWcuue9kSZuYnNwOyZuYnNwO+aKgOacr+aUr+aMgTrmtZnmsZ/oh7vlloTnp5HmioDogqHku73mnInpmZDlhazlj7gmbmJzcDvkupHlnLDnvZE8YnIgLz4NCuWkh+ahiOWPtzog5LqsSUNQ5aSHMDkwNzQ5OTLlj7cg5Lqs5YWs572R5a6J5aSHMTEwMTAyMDAwNjY2KDIpJm5ic3A7PGJyIC8+DQo8L3NwYW4+Jm5ic3A7Jm5ic3A7Jm5ic3A7PGJyIC8+DQombmJzcDs8L3NwYW4+PC9wPh8BBWRCQUNLR1JPVU5ELUlNQUdFOnVybChodHRwOi8vd3d3LmxhbmRjaGluYS5jb20vVXNlci9kZWZhdWx0L1VwbG9hZC9zeXNGcmFtZUltZy94X3Rkc2N3MjAxM195d18xLmpwZyk7ZGSly46A4u9ucNxm4u+v3GDdH3XFZj+oB0Ph+q2P7e61KA==%2BPC9hPiZuYnNwOzxiciAvPg0KJm5ic3A7PHNwYW4gc3R5bGU9ImNvbG9yOiAjZmZmZmZmIj5Db3B5cmlnaHQgMjAwOC0yMDE0IERSQ25ldC4gQWxsIFJpZ2h0cyBSZXNlcnZlZCZuYnNwOyZuYnNwOyZuYnNwOyA8c2NyaXB0IHR5cGU9InRleHQvamF2YXNjcmlwdCI%2BDQp2YXIgX2JkaG1Qcm90b2NvbCA9ICgoImh0dHBzOiIgPT0gZG9jdW1lbnQubG9jYXRpb24ucHJvdG9jb2wpID8gIiBodHRwczovLyIgOiAiIGh0dHA6Ly8iKTsNCmRvY3VtZW50LndyaXRlKHVuZXNjYXBlKCIlM0NzY3JpcHQgc3JjPSciICsgX2JkaG1Qcm90b2NvbCArICJobS5iYWlkdS5jb20vaC5qcyUzRjgzODUzODU5YzcyNDdjNWIwM2I1Mjc4OTQ2MjJkM2ZhJyB0eXBlPSd0ZXh0L2phdmFzY3JpcHQnJTNFJTNDL3NjcmlwdCUzRSIpKTsNCjwvc2NyaXB0PiZuYnNwOzxiciAvPg0K54mI5p2D5omA5pyJJm5ic3A7IOS4reWbveWcn%2BWcsOW4guWcuue9kSZuYnNwOyZuYnNwO%2BaKgOacr%2BaUr%2BaMgTrmtZnmsZ%2Foh7vlloTnp5HmioDogqHku73mnInpmZDlhazlj7gmbmJzcDvkupHlnLDnvZE8YnIgLz4NCuWkh%2BahiOWPtzog5LqsSUNQ5aSHMDkwNzQ5OTLlj7cg5Lqs5YWs572R5a6J5aSHMTEwMTAyMDAwNjY2KDIpJm5ic3A7PGJyIC8%2BDQo8L3NwYW4%2BJm5ic3A7Jm5ic3A7Jm5ic3A7PGJyIC8%2BDQombmJzcDs8L3NwYW4%2BPC9wPh8BBWRCQUNLR1JPVU5ELUlNQUdFOnVybChodHRwOi8vd3d3LmxhbmRjaGluYS5jb20vVXNlci9kZWZhdWx0L1VwbG9hZC9zeXNGcmFtZUltZy94X3Rkc2N3MjAxM195d18xLmpwZyk7ZGSly46A4u9ucNxm4u%2Bv3GDdH3XFZj%2BoB0Ph%2Bq2P7e61KA%3D%3D");
		//params.put("__EVENTVALIDATION", "/wEWAgLo5s+WDwLN3cj/BFqcpY6UDfCXVMNF3olzp/yG8yZfXoc6t9Dy0wgxOwkR");
		params.put("hidComName", "default");
		params.put("TAB_QuerySortItemList", "");
		params.put("TAB_QueryConditionItem", "9f2c3acd-0256-4da2-a659-6949c4671a2a");
		params.put("TAB_QuerySubmitConditionData", "9f2c3acd-0256-4da2-a659-6949c4671a2a:"+dateStr+"~"+dateStr);
		params.put("TAB_QuerySubmitOrderData", "");
		params.put("TAB_QuerySubmitPagerData", page);
		params.put("TAB_QuerySubmitSortData", "");
		params.put("dateStr", dateStr);
		request.addHeader("Content-Type", "application/x-www-form-urlencoded");
		//request.addHeader("Proxy-Authorization", authHeader());
		Map extras=(Map) params.clone();
		extras.put("dateStr", dateStr);
		extras.put("times", 0);
		request.setExtras(extras);
		try {
			request.setRequestBody(HttpRequestBody.form(params,"utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			
		}
		request.setMethod(HttpConstant.Method.POST);
		return request;
	}
	
	public static Request getRequest(Record record){
		Request request = new Request(host+"/"+record.getStr("url"));
		Map extras=new HashMap();
		extras.put("record", record);
		extras.put("times", 0);
		//request.addHeader("Proxy-Authorization", authHeader());
		request.setExtras(extras);
		return request;
	}
	
	//代理签名头
	 public static String authHeader(){
		 String orderno="ZF20179140510mEZX1J";
		 String secret="754f94a420074091ac3b127d5c636c26";
		   int timestamp = (int) (new Date().getTime()/1000);
	        //拼装签名字符串
	        String planText = String.format("orderno=%s,secret=%s,timestamp=%d", orderno, secret, timestamp);
	        //计算签名
	        String sign = org.apache.commons.codec.digest.DigestUtils.md5Hex(planText).toUpperCase();
	        //拼装请求头Proxy-Authorization的值
	        String authHeader = String.format("sign=%s&orderno=%s&timestamp=%d", sign, orderno, timestamp);
	        return authHeader;
	 }
	 
	 

	 
	public static Request getNext(Request request){
		Map<String, Object> params=request.getExtras();
		int page=(int)params.get("TAB_QuerySubmitPagerData");
		params.put("TAB_QuerySubmitPagerData", page+1);
		request.addHeader("Content-Type", "application/x-www-form-urlencoded");
		//request.addHeader("Proxy-Authorization", authHeader());
		request.setExtras(params);
		try {
			request.setRequestBody(HttpRequestBody.form(params, "utf-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		request.setMethod(HttpConstant.Method.POST);
		return request;
	}

	
	public static void main(String[] args) {
		DateTimeFormatter df=DateTimeFormatter.ofPattern("yyyy-M-d");
		HttpClientDownloader downloader=new HttpClientDownloader();
		Proxy proxy=new Proxy("forward.xdaili.cn",80);
		List<Proxy> proxys=new ArrayList<>();
		//proxys.add(new Proxy("183.158.11.11", 38078));
		//proxys.add(new Proxy("122.241.204.240", 43457));
		//proxys.add(new Proxy("117.88.246.72", 24343));
		//proxys.add(new Proxy("114.239.249.208", 20841));
		//proxys.add(new Proxy("115.202.246.230", 30561));
		proxys.add(new Proxy("180.102.188.16", 38085));
		//proxys.add(new Proxy("123.55.177.211", 42283));
		//proxys.add(new Proxy("114.239.148.210", 22512));
		//proxys.add(new Proxy("114.99.73.176", 48791));
		SimpleProxyProvider  proxyProvider=new SimpleProxyProvider(Collections.singletonList(proxy));
		downloader.setProxyProvider(proxyProvider);
		Spider spider = Spider.create(new LandchinaPageProcessor()).setDownloader(downloader);
		spider.addPipeline(new LandChinaPipline());
		/*for(int i=6;i<=15;i++){
			spider.addRequest(LandchinaPageProcessor.getRequest("2017-8-17", i));
		}
		
		spider.run();*/
		LocalDate ld1=LocalDate.of(2017, 8, 12);
		while(true){
			String dateStr=ld1.format(df);
			spider.addRequest(LandchinaPageProcessor.getRequest(dateStr, 1));
			spider.run();
			ld1=ld1.minusDays(1);
		}
			
	}
	
	public static void runDetail(){
		List<Record> urls=Db.find("select * from landchinaurl where isspider=0 order by id limit 1");
		//Proxy proxy=new Proxy("forward.xdaili.cn",80);
		while(!urls.isEmpty()){
			HttpClientDownloader dowanloader=new HttpClientDownloader();
			//SimpleProxyProvider  proxyProvider=new SimpleProxyProvider(Collections.singletonList(getProxy()));
			//dowanloader.setProxyProvider(proxyProvider);
			Spider spider = Spider.create(new LandchinaPageProcessor()).setDownloader(dowanloader);
			spider.addPipeline(new LandChinaPipline());
			for(Record record:urls){
				spider.addRequest(LandchinaPageProcessor.getRequest(record));
			}
			spider.run();
			urls=Db.find("select * from landchinaurl where isspider=0 order by id limit 1");
		}
		
	}
	
	public static void stopDetail(){
	}
	
	public static Proxy getProxy(){
		String url="http://api.xdaili.cn/xdaili-api//greatRecharge/getGreatIp?spiderId=93b89bedbf5a4e0799e558f66a99e7bc&orderno=YZ20179130822wa5jHc&returnType=2&count=1";
		String res=HttpKit.get(url);
		JSONObject jo;
		try{
			jo=JSON.parseObject(res).getJSONArray("RESULT").getJSONObject(0);
			String port=jo.getString("port");
			String ip=jo.getString("ip");
			logger.info("proxy:ip:"+ip+",port:"+port);
			return new Proxy(ip, Integer.parseInt(port));
		}catch(Exception e){
			e.printStackTrace();
			try {
				Thread.sleep(5000);
				return getProxy();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return null;
		
	}
}
