package Generator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xiaoleilu.hutool.http.HttpException;
import com.xiaoleilu.hutool.http.HttpRequest;
import com.xiaoleilu.hutool.http.HttpResponse;
import com.xiaoleilu.hutool.io.FileUtil;
import com.xiaoleilu.hutool.io.IoUtil;
import com.xiaoleilu.hutool.util.RandomUtil;
import com.xiaoleilu.hutool.util.ThreadUtil;


public class DownFile {
	String cookie="bcookie=\"v=2&a7755219-b19f-4140-8f78-51c4a47fb127\"; bscookie=\"v=1&20171007114819269e3446-831e-42a5-8ec9-eaca93013854AQFIVQMe_4pWVIJcF3C0D01XZyf2AWeH\"; visit=\"v=1&M\"; u_tz=GMT+0800; _ga=GA1.2.1895440075.1508481122; lidc=\"b=SB76:g=21:u=5:i=1508818530:t=1508904888:s=AQHeMHedZiFaDyidn1x6xNcKlwKvwIrJ\"; _lipt=CwEAAAFfUQ9JRmtzH2Y67YyjXTAFgAd363LgeLKILLDEP7Ar07loOgq6uzZ2fil7P_ayCSTz9-u1a77-3KhuPrxkOD-zt2JxeBoYoWoDaHbXKtiDpPOyrC3VfyMQSpy5NUIxUYCQOftY_OCFdXeofXlCrnvoPO0FD8VdV7JTlz-v66Z9KptARmLuFccpbDiMxmhmVQ; liap=true; li_at=AQEDASO-FpAFqWxJAAABX1EPdOwAAAFfdRv47FEAPNd0NGdAJQt_BLxNIN9qP7TjYtYr7U4Bn8rkY4YTH2azGHf5-4wdJVGaHSKKpEp_AWLXMMttUqU32Lr0ctOYG2IOTrlf-T4UHpYL3gBpvElXrNsg; __utmt=1; cap_session_id=\"1773375671:1\"; li_a=AQJ2PTEmY2FwX3NlYXQ9MTU1OTkxNDM2JmNhcF9hZG1pbj1mYWxzZSZjYXBfa249MjU4MjUyODM2ipS7DdCwMp4MTzGLZRyARjwj_7g; JSESSIONID=\"ajax:4884456514025656675\"; RT=s=1508893497796&r=https%3A%2F%2Fwww.linkedin.com%2Fcap%2Fdashboard%2Fhome%2F%3Fsession_redirect%3Dhttps%253A%252F%252Fwww.linkedin.com%252Frecruiter%252Fhiring-dashboard%26destURL%3Dhttps%253A%252F%252Fwww.linkedin.com%252Frecruiter%252Fhiring-dashboard; __utma=70075158.1895440075.1508481122.1508832580.1508893493.16; __utmb=70075158.2.10.1508893493; __utmc=70075158; __utmz=70075158.1508736896.8.2.utmcsr=mail.qq.com|utmccn=(referral)|utmcmd=referral|utmcct=/; sdsc=32%3A1%2C1508893495252%7ECAOR%2C0%7ECAST%2C-609NU%2BJsz50RGqe%2B9eFg2B1O9B7XjY%3D; lang=\"v=2&lang=zh-cn\"";
	
	public static void main(String[] args) throws IOException {
		DownFile downfile=new DownFile();
		downfile.downFiles("1048244301","1北京大学社会学");
		//downfile.downFiles();
	}
	
	public List<Project> getProject(){
		String url="https://www.linkedin.com/recruiter/api/projects/searchWithStatuses?q=&view=sharedWithMe&start=0&state=A&count=100";
		String result=HttpRequest.get(url).cookie(cookie).execute().body();
		JSONArray ja=JSON.parseObject(result).getJSONObject("result").getJSONObject("projects").getJSONArray("data");
		List<Project> projects=new ArrayList<Project>();
		String filterName="0北京大学环境工程,";
		for(int i=0;i<ja.size();i++){
			JSONObject project=ja.getJSONObject(i);
			String id=project.getString("projectId");
			String name=project.getString("name");
			if(!filterName.contains(name)&&!name.contains("清华大学")){
				projects.add(new Project(id, i+name));
			}
		}
		return projects;
	}
	public void downFiles(){
		List<Project> projects=getProject();
		for(Project project:projects){
			JSONArray ja=getProspectIds(project);
			getFile(project,ja);
		}
		
	}
	public void downFiles(String projectid,String projectname){
		Project project =new Project(projectid,projectname);
		JSONArray ja=getProspectIds(project);
		getFile(project,ja);
	}
	
	public JSONArray getProspectIds(Project project){
		String url="https://www.linkedin.com/recruiter/api/projects/"+project.getId()+"/profiles?count=25&start=0&fetchAllIds=true";
		String result=HttpRequest.get(url).cookie(cookie).execute().body();
		System.out.println(result);
		JSONArray ja=JSON.parseObject(result).getJSONObject("result").getJSONArray("prospectIds");
		System.out.println(ja);
		return ja;
	}
	
	public void getFile(Project project,JSONArray ja){
		int trytimes=3;//失败后重试3次 然后跳过
		for(int i=169;i<ja.size();i++){
			String prospectid=ja.getString(i);
			System.out.println(String.format("开始抓取项目-%s(%s)第%s/%s个%s简历", project.getName(),project.getId(),i+1,ja.size(),prospectid));
			String url2="https://www.linkedin.com/cap/people/profileExportPdf/_LinkedIn_20171022.pdf?memberIds="+prospectid+",PTS,PTS&origin=profile";
			String result=HttpRequest.get(url2).cookie(cookie).execute().body();
			System.out.println(result);
			String id=JSON.parseObject(result).getString("id");
			String url3="https://www.linkedin.com/cap/people/printStateAjax?id="+id;
			
			try{
				String isready=HttpRequest.get(url3).cookie(cookie).execute().body();
				System.out.println(isready);
				downFile(project,prospectid,id);
			}catch(HttpException e){
				e.printStackTrace();
				String fileUrl = "https://www.linkedin.com/cap/people/streamPdf/"+id;
				System.err.println("抓取失败...请手动抓取:"+prospectid+":"+fileUrl);
				logfail(project,prospectid,fileUrl);
			}catch(Exception ex) {
				ex.printStackTrace();
				if(trytimes>0){
					i--;
					trytimes--;
					System.out.println("抓取失败重试...");
					ThreadUtil.sleep(4000);
				}else{
					trytimes=3;
					System.out.println("抓取失败,不再重试...连接写入文件");
					String fileUrl = "https://www.linkedin.com/cap/people/streamPdf/"+id;
					logfail(project,prospectid,fileUrl);
				}
			}
			int sleeptime=5000+RandomUtil.randomInt(10,1000);
			System.out.println("休息:"+sleeptime+"ms");
			ThreadUtil.sleep(sleeptime);
		}
	}
	
	public void logfail(Project project,String prospectid,String url){
		String log=project.getName()+","+prospectid+","+url;
		FileUtil.appendLines(Collections.singleton(log), "e://resume//fail.log", "utf-8");
	}

	private void downFile(Project project,String prospectid,String id) {
		String fileUrl = "https://www.linkedin.com/cap/people/streamPdf/"+id;
		HttpResponse response=HttpRequest.get(fileUrl).cookie(cookie).execute();
		String filename=response.header("Content-Disposition").split("=")[1].replaceAll("\"", "");
		filename=prospectid+"."+filename.split("\\.")[0]+"."+RandomUtil.randomInt(2000)+".pdf";
		System.out.println(filename);
		InputStream  in=HttpRequest.get(fileUrl).cookie(cookie).execute().bodyStream();
		try{
			String path="e:\\resume\\"+project.getName();
			System.out.println("Download size: " + in.available());
			Files.createDirectories(Paths.get(path));
			FileOutputStream file=new FileOutputStream(path+"\\"+filename);
			IoUtil.copyByNIO(in, file, IoUtil.DEFAULT_BUFFER_SIZE, null);
			file.close();
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
		
	}
}

class Project{
	private String id;
	private String name;
	Project(String id,String name){
		this.id=id;this.name=name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) { 
		this.name = name;
	}
	
}
