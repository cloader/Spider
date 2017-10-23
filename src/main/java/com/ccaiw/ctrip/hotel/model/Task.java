/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.ccaiw.ctrip.hotel.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

/**
 * 定时任务
 *
 * @author Jieven
 * @date 2014-9-10
 */
public class Task extends Model<Task> {

	private static final long serialVersionUID = 4254060861819273244L;

	public static final Task dao = new Task();

	/** 暂停 **/
	public static final int STATE_STOP = 0;
	/** 运行 **/
	public static final int STATE_START = 1;
    
	public List<Task> findAll() {
		return this.find("select * from taskinfo");
	}

	public List<Task> findAll(int page, int pagesize) {
		return this.find("select * from taskinfo limit ?,?", (page - 1) * pagesize, pagesize);
	}

	public List<Task> findByStart() {
		return this.find("select * from taskinfo where state = 2");
	}

	public int updateState(int id, int state) {
		return Db.use("opersysdb").update("update taskinfo set state = ? where id = ?", state, id);
	}

	// 修改任务执行时间
	public int updateTask(int id, String exp, int state, String name, String info) {
		return Db.use("opersysdb").update("update taskinfo set exp = ?,state=?,name=?,info=? where id = ?", exp, state,
				name, info, id);
	}

	// count
	public int Count() {
		Task model = Task.dao.findFirst("select count(*) as count from taskinfo");
		int count = model.getLong("count").intValue();

		return count;
	}
}