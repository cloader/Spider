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
	String cookie="bcookie=\"v=2&a7755219-b19f-4140-8f78-51c4a47fb127\"; bscookie=\"v=1&20171007114819269e3446-831e-42a5-8ec9-eaca93013854AQFIVQMe_4pWVIJcF3C0D01XZyf2AWeH\"; visit=\"v=1&M\"; _ga=GA1.2.1895440075.1508481122; u_tz=GMT+0800; lidc=\"b=SB76:g=21:u=5:i=1508729230:t=1508815614:s=AQEQ6OBkANjAkUzLqnBkNN1iWvlhvmgo\"; _lipt=CwEAAAFfR5MHbvYEkT19cPqcbTSgdYaW5x6XsWo4x2PBk9XKmzevRTmvkVR6QfgJSMiGFrqj_paD-CBI6Pvxc04mLgw-CpPWuJUEk3SlkOhQCnPHk1lJ9z4stJ6lpobNkppugH_50pXTpPLh37THFaTpzvpJcZs6Pw1LxlqSdUnv5qavitsxCGBkWa4qV0s1V4LYFw; liap=true; li_at=AQEDASO-FpAFkVePAAABX0eTHTgAAAFfa5-hOFEABP68_ym2C-fniKNVfY2Pt-0BiC5wq0DY3tRqpS4gfzJtyAJ4ImI0JAuKSXS5JviX3tyBcfa_ZgdGgP9gttBBrf_Og_f5OYZzpKWfrdwtNVTy9NN7; cap_session_id=\"1770674021:1\"; li_a=AQJ2PTEmY2FwX3NlYXQ9MTU1OTkxNDM2JmNhcF9hZG1pbj1mYWxzZSZjYXBfa249MjU4MjUyODM2ub8mi8Q39zL8HAqI4wgbbDGveLQ; JSESSIONID=\"ajax:5885183157678961609\"; RT=s=1508734351427&r=https%3A%2F%2Fwww.linkedin.com%2Fuas%2Flogin-cap%3Fsession_redirect%3Dhttps%253A%252F%252Fwww%252Elinkedin%252Ecom%252Fcap%252Fdashboard%252Fhome%253FrecruiterEntryPoint%253Dtrue%2526trk%253Dnav_account_sub_nav_cap%26source_app%3Dcap%26trk%3Dcap_signin; __utmt=1; __utma=70075158.1895440075.1508481122.1508729238.1508734353.7; __utmb=70075158.1.10.1508734353; __utmc=70075158; __utmz=70075158.1508481122.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); sdsc=32%3A1%2C1508734348739%7ECAOR%2C0%7ECAST%2C-5926p9Tu3nrgNN9Uq%2F2nPshGXKEue4%3D; lang=\"v=2&lang=zh-cn\"";
	public static void main(String[] args) throws IOException {
		DownFile downfile=new DownFile();
		downfile.downFiles();
	}
	
	public List<Project> getProject(){
		String url="https://www.linkedin.com/recruiter/api/projects/searchWithStatuses?q=&view=sharedWithMe&start=0&state=A&count=20";
		String result=HttpRequest.get(url).cookie(cookie).execute().body();
		JSONArray ja=JSON.parseObject(result).getJSONObject("result").getJSONObject("projects").getJSONArray("data");
		List<Project> projects=new ArrayList<Project>();
		String filterName="清华大学机械工程,清华大学法学,清华大学会计,清华大学药学,清华大学艺术鉴赏,清华大学艺术,清华大学招聘项目";
		for(int i=0;i<ja.size();i++){
			JSONObject project=ja.getJSONObject(i);
			String id=project.getString("projectId");
			String name=project.getString("name");
			if(!filterName.contains(name)){
				projects.add(new Project(id, i+name));
			}
		}
		System.out.println(projects.get(1).getName());
		return projects;
	}
	public void downFiles(){
		List<Project> projects=getProject();
		for(Project project:projects){
			JSONArray ja=getProspectIds(project);
			getFile(project,ja);
		}
		
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
		for(int i=0;i<ja.size();i++){
			String prospectid=ja.getString(i);
			System.out.println(String.format("开始抓取项目-%s(%s)第%s个%s项目", project.getName(),project.getId(),i+1,prospectid));
			String url2="https://www.linkedin.com/cap/people/profileExportPdf/_LinkedIn_20171022.pdf?memberIds="+prospectid+",PTS,PTS&origin=profile";
			String result=HttpRequest.get(url2).cookie(cookie).execute().body();
			System.out.println(result);
			String id=JSON.parseObject(result).getString("id");
			String url3="https://www.linkedin.com/cap/people/printStateAjax?id="+id;
			String isready=HttpRequest.get(url3).cookie(cookie).execute().body();
			/*while(!isready.contains("Not Ready")){
				System.out.println(isready);
				System.out.println("Not Ready 休息5s");
				ThreadUtil.sleep(5000);
				isready=HttpRequest.get(url3).cookie(cookie).execute().body();
			}*/
			System.out.println(isready);
			try{
				downFile(project,prospectid,id);
			}catch(HttpException e){
				System.err.println(e.getMessage());
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
			System.out.println("休息5s");
			ThreadUtil.sleep(5000);
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
