package com.ccaiw.ctrip.hotel;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ccaiw.ctrip.hotel.model.HotelCity;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.model.HttpRequestBody;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.utils.HttpConstant;


public class HotelPageProcessor implements PageProcessor {
	
	private Logger logger=LoggerFactory.getLogger(HotelPageProcessor.class);
	private String StartTime=LocalDate.now().plusDays(1).toString();//
	private String DepTime=LocalDate.now().plusDays(2).toString();//
	private static String url="http://hotels.ctrip.com/Domestic/Tool/AjaxHotelList.aspx";;
	private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setRetrySleepTime(10000)
			.setUserAgent( "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
	public HotelPageProcessor(String StartTime,String DepTime){
		this.StartTime=StartTime;
		this.DepTime=DepTime;
	}
	public HotelPageProcessor() {
	}
	public Site getSite() {
		// TODO site-generated method stub
		return site;
	}
	
	public Request getFirstRequest(HotelCity city){
		return getRequest(city, 1);
	}
	
	public  Request getRequest(HotelCity city,int page){
		Request request = new Request(url);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("cityName",city.get("city_name"));
		params.put("cityId",city.get("city_id"));
		params.put("cityPY", city.get("pinyin"));
		params.put("StartTime",StartTime);
		params.put("DepTime", DepTime);
		params.put("page", page);
		
		params.put("__VIEWSTATEGENERATOR", "DB1FBB6D");
		params.put("txtkeyword", "");
		params.put("Resource", "");
		params.put("Room", "");
		params.put("Paymentterm", "");
		params.put("BRev", "");
		params.put("Minstate", "");
		params.put("PromoteType", "");
		params.put("PromoteDate", "");
		params.put("operationtype", "NEWHOTELORDER");
		params.put("PromoteStartDate", "");
		params.put("PromoteEndDate", "");
		params.put("OrderID", "");
		params.put("RoomNum", "");
		params.put("IsOnlyAirHotel", "F");
//		params.put("cityCode", "1853");
//		params.put("cityLat", "22.1946");
//		params.put("cityLng", "113.549");
		params.put("positionArea", "");
		params.put("positionId", "");
		params.put("keyword", "");
		params.put("hotelId", "");
		params.put("htlPageView", "0");
		params.put("hotelType", "F");
		params.put("hasPKGHotel", "F");
		params.put("requestTravelMoney", "F");
		params.put("isusergiftcard", "F");
		params.put("useFG", "F");
		params.put("HotelEquipment", "");
		params.put("priceRange", "-2");
		params.put("hotelBrandId", "");
		params.put("promotion", "F");
		params.put("prepay", "F");
		params.put("IsCanReserve", "F");
		params.put("OrderBy", "99");
		params.put("OrderType", "");
		params.put("k1", "");
		params.put("k2", "");
		params.put("CorpPayType", "");
		params.put("viewType", "");
//		params.put("checkIn", "2016-11-24");
//		params.put("checkOut", "2016-11-25");
		params.put("DealSale", "");
		params.put("ulogin", "");
		params.put("hidTestLat", "0%7C0");
//		params.put("AllHotelIds", "436450%2C371379%2C396332%2C419374%2C345805%2C436553%2C425997%2C436486%2C436478%2C344977%2C5605870%2C344983%2C371396%2C344979%2C2572033%2C699384%2C425795%2C419823%2C2010726%2C5772619%2C1181591%2C2005951%2C345811%2C371381%2C371377");// TODO
		params.put("psid", "");
		params.put("HideIsNoneLogin", "T");
		params.put("isfromlist", "T");
		params.put("ubt_price_key", "htl_search_result_promotion");
		params.put("showwindow", "");
		params.put("defaultcoupon", "");
		params.put("isHuaZhu", "False");
		params.put("hotelPriceLow", "");
		params.put("htlFrom", "hotellist");
		params.put("unBookHotelTraceCode", "");
		params.put("showTipFlg", "");
//		params.put("hotelIds", "436450_1_1,371379_2_1,396332_3_1,419374_4_1,345805_5_1,436553_6_1,425997_7_1,436486_8_1,436478_9_1,344977_10_1,5605870_11_1,344983_12_1,371396_13_1,344979_14_1,2572033_15_1,699384_16_1,425795_17_1,419823_18_1,2010726_19_1,5772619_20_1,1181591_21_1,2005951_22_1,345811_23_1,371381_24_1,371377_25_1");// TODO
		params.put("markType", "1");
		params.put("zone", "");
		params.put("location", "");
		params.put("type", "");
		params.put("brand", "");
		params.put("group", "");
		params.put("feature", "");
		params.put("equip", "");
		params.put("star", "");
		params.put("sl", "");
		params.put("s", "");
		params.put("l", "");
		params.put("price", "");
		params.put("a", "0");
		params.put("keywordLat", "");
		params.put("keywordLon", "");
		params.put("contrast", "0");
		params.put("contyped", "0");
		params.put("productcode", "");
		
		request.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
		try {
			request.setRequestBody(HttpRequestBody.form(params,"utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			
		}
		request.setMethod(HttpConstant.Method.POST);
		Map<String,  Object> extras=new HashMap<String, Object>();
		extras.put("city", city);
		extras.put("page", page);
		extras.put("StartTime", StartTime);
		extras.put("DepTime", DepTime);
		request.setExtras(extras);
		return request;
	}

	public void process(Page page) {
		Map<String,  Object> extras=page.getRequest().getExtras();
		JSONObject hotelResultObj;
		try{
		 hotelResultObj = JSONObject.parseObject(page.getRawText());
		}catch (Exception e){
			e.printStackTrace();
			logger.error("解析错误/n:"+page.getRawText());
			page.setDownloadSuccess(false); //
			return;
		}
		if(!hotelResultObj.containsKey("hotelAmount")){
			logger.error("解析错误/n:"+page.getRawText());
			page.setDownloadSuccess(false); //
			return;
		}
		if("1".equals(String.valueOf(extras.get("page")))){  //��ץȡ��ҳʱ������ҳ�����ץȡ����
			int hotelAmount=Integer.parseInt(page.getJson().jsonPath("hotelAmount").get());
			int pageAmount = hotelAmount%25 == 0 ? hotelAmount/25 : hotelAmount/25 + 1;
			for(int i=2;i<pageAmount + 1;i++){  //���ʱ��ӵڶ�ҳ��ʼ
				page.addTargetRequest(getRequest((HotelCity) page.getRequest().getExtras().get("city"),i));
			}
		}
		/*System.out.println();
		Stream<Hotel> hotels=page.getJson().jsonPath("hotelPositionJSON").nodes().stream().map(a->a.jsonPrase(Hotel.class));
		hotels.forEach(c->System.out.println(c.getName()));
		*/
		
		List<Map> pageHotels = JSON.parseArray(hotelResultObj.getString("hotelPositionJSON"), Map.class);
		// ���Ӽ۸�����
		JSONArray hotelsPrice = hotelResultObj.getJSONObject("HotelMaiDianData").getJSONObject("value").getJSONArray("htllist");
		if (hotelsPrice != null && !hotelsPrice.isEmpty()) {
			for (int j = 0; j < pageHotels.size(); j++) {
				JSONObject priceObj = hotelsPrice.getJSONObject(j);
				if (priceObj != null && !priceObj.isEmpty()) {
					Map hotel = pageHotels.get(j);
					String hotelId = priceObj.getString("hotelid");
					double price = 0;
					try {
						price = priceObj.getDoubleValue("amount");
					} catch (Exception e) { 
						e.printStackTrace();
					}
					if (hotel.get("id").equals(hotelId)) {
						hotel.put("price", price);
					}
				}
			}
		}
		
		//��������
		for(Map hotel:pageHotels){
			hotel.put("hotel_id", hotel.get("id"));
			hotel.put("id",null);//ʹ����������
			hotel.put("city_id",((HotelCity)extras.get("city")).get("city_id"));
			hotel.put("city_name",((HotelCity)extras.get("city")).get("city_name"));
			hotel.put("StartTime",extras.get("StartTime"));
			hotel.put("DepTime",extras.get("DepTime"));
		}
		page.putField("hotels", pageHotels);
	}
}
