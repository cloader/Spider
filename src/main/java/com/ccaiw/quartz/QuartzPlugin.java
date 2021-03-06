package com.ccaiw.quartz;

import java.util.List;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.ccaiw.ctrip.hotel.model.Task;
import com.jfinal.kit.LogKit;
import com.jfinal.plugin.IPlugin;

public class QuartzPlugin implements IPlugin {

	private SchedulerFactory sf = null;

	public static Scheduler scheduler = null;

	/**
	 * 启动Quartz
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean start() {
		sf = new StdSchedulerFactory();

		try {
			scheduler = sf.getScheduler();

			List<Task> tasks = Task.dao.findAll();
			for (Task task : tasks) {
				String jobClassName = task.getStr("clazz");
				String jobCronExp = task.getStr("exp");
				int state = task.getInt("state");
				int runNow=task.getInt("isrunnow");
				Class clazz;
				try {
					clazz = Class.forName(jobClassName);
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}

				JobDetail job = JobBuilder.newJob(clazz).withIdentity(jobClassName, jobClassName).build();
				CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(jobClassName, jobClassName)
						.withSchedule(CronScheduleBuilder.cronSchedule(jobCronExp)).build();

				try {
					scheduler.scheduleJob(job, trigger);
					if (state == Task.STATE_STOP) {
						scheduler.pauseTrigger(trigger.getKey());
					}
					if(runNow==1){
						JobDetail jobnow = JobBuilder.newJob(clazz).withIdentity(jobClassName+":now", jobClassName).build();
						Trigger nowTrgger=TriggerBuilder.newTrigger().startNow().build();
						scheduler.scheduleJob(jobnow, nowTrgger);
					}
				} catch (SchedulerException e) {
					e.printStackTrace();
				}

				LogKit.info(job.getKey() + " loading and exp: " + trigger.getCronExpression());
			}

			scheduler.start();

		} catch (SchedulerException e) {
			new RuntimeException(e);
		}

		return true;
	}

	/**
	 * 停止Quartz
	 */
	@Override
	public boolean stop() {
		try {
			scheduler.shutdown();
		} catch (SchedulerException e) {
			LogKit.error("shutdown error", e);
			return false;
		}
		return true;
	}

}