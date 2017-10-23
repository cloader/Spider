package com.ccaiw.ctrip.landchina;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.jfinal.kit.LogKit;

public class LanchinaJob implements Job{
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Thread thread =new Thread(new  Runnable() {
			public void run() {
				LandchinaPageProcessor.runDetail();
			}
		});
		thread.setDaemon(true);
		thread.start();
		LogKit.info("LanchinaJob  run....");
	}

}
