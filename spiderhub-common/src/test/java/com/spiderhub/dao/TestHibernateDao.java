package com.spiderhub.dao;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.spiderhub.dao.impl.HibernateDaoImpl;
import com.spiderhub.orm.User;
import com.spiderhub.utils.HibernateUtils;
import com.spiderhub.vo.PageInfo;

public class TestHibernateDao {
	private Logger log = Logger.getLogger(HibernateDaoImpl.class);
	
	private Session session = null;
	private HibernateDao dao = null;
	private Transaction tc = null;
	@Before
	public void init() {

		
		session = HibernateUtils.getSession();
		tc = session.beginTransaction();
		dao = new HibernateDaoImpl(session);
	}
	@Test
	public void testSave() {
		log.info("testSave");
			try{
				User user = new User();
				user.setName("王五1");
				user.setPassword("213456");
				user.setCreateTime(new Date());
				user.setExpireTime(new Date());
				dao.save(user);
				tc.commit();
			}catch(Exception e){
				e.printStackTrace();
				tc.rollback();
			}
	}
	@Test
	public void testBatchAdd() {
		log.info("testBatchAdd");
		List<User> list = new ArrayList<User>();
		for(int i=0;i<20;i++) {
			User user = new User();
			user.setName("李少军"+i);
			user.setPassword(i+"");
			user.setCreateTime(new Date());
			user.setExpireTime(new Date());
			list.add(user);
		}
		dao.save(list);
		tc.commit();
	}

	@Test
	public void testLoad() {
		log.info("testLoad");
		try{
			User user = (User)dao.getSession().load(User.class, 1);
			assertEquals(user.getName(),"王五1");
		}catch(Exception e){
			e.printStackTrace();
			tc.rollback();
		}
	}
	@Test
	public void testGet() {
		log.info("testGet");
		try{
			User user = (User)dao.getSession().load(User.class, 1);
			assertEquals(user.getName(),"王五1");
		}catch(Exception e){
			e.printStackTrace();
			tc.rollback();
		}
	}
	@Test
	public void testUpdate() {
		log.info("testUpdate");
		try{
			User user = new User();
			user.setName("王五2");
			user.setPassword("213456");
			user.setCreateTime(new Date());
			user.setExpireTime(new Date());
			dao.save(user);
			tc.commit();
		}catch(Exception e){
			e.printStackTrace();
			tc.rollback();
		}
	}
	@Test
	public void testDel() {
		log.info("testDel");
		User user = (User)dao.getSession().load(User.class, 6);
		dao.delete(user);
		tc.commit();
	}
	@Test
	public void testArrDel() {
		log.info("testArrDel");
		Serializable[] keys = {2,5};
		dao.delete(User.class, keys);
		tc.commit();
	}
	@Test
	public void testBatchDel() {
		log.info("testBatchDel");
		List<User> list = new ArrayList<User>();
		for(int i=0;i<10;i++) {
			User user = new User();
			user.setName("李少军"+i);
			user.setPassword(i+"");
			user.setCreateTime(new Date());
			user.setExpireTime(new Date());
			list.add(user);
		}
		dao.delete(list);
		tc.commit();
	}
	@Test
	public void testQueryForList() {
		log.info("testQueryForList");
		String hql = "from User";
		List list = dao.queryForList(hql);
		for(Iterator iter = list.iterator();iter.hasNext();) {
			User user = (User)iter.next();
			System.out.println(user.getName()+"\t"+user.getPassword());
		}
	}
	@Test
	public void testQueryForParamList() {
		log.info("testQueryForParamList");
		String hql = "from User where name = :name";
		Map params = new HashMap();
		params.put("name", "李少军11");
		List list = dao.queryForList(hql,params);
		for(Iterator iter = list.iterator();iter.hasNext();) {
			User user = (User)iter.next();
			System.out.println(user.getName()+"\t"+user.getPassword());
		}
	}
	
	@Test
	public void testQueryForSQLList() {
		log.info("testQueryForSQLList");
		String hql = "select * from user2";
		List list = dao.queryForSQLList(hql);
		for(Iterator iter = list.iterator();iter.hasNext();) {
			Object[] obj = (Object[])iter.next();
			System.out.println(obj[0] + ", " + obj[1]);
		}
	}
	@Test
	public void testQueryForParamSQLList() {
		log.info("testQueryForParamSQLList");
		String hql = "from User where name = :name";
		Map params = new HashMap();
		params.put("name", "李少军11");
		List list = dao.queryForList(hql,params);
		for(Iterator iter = list.iterator();iter.hasNext();) {
			User user = (User)iter.next();
			System.out.println(user.getName()+"\t"+user.getPassword());
		}
	}
	@Test
	public void testQueryForPage() {
		log.info("testQueryForPage");
		String hql = "from User";
		PageInfo pageInfo = dao.queryForPage(hql, null, 10, 5, null, "asc");
		List list = pageInfo.getDatas();
		for(Iterator iter = list.iterator();iter.hasNext();) {
			User user = (User)iter.next();
			System.out.println(user.getName()+"\t"+user.getPassword());
		}
		
	}
	@Test
	public void testQueryParamsForPage() {
		log.info("testQueryParamsForPage");
		String hql = "from User";
		Map params = new HashMap();
		params.put("name", "李少军11");
		PageInfo pageInfo = dao.queryForPage(hql, null, 10, 5, null, "asc");
		List list = pageInfo.getDatas();
		for(Iterator iter = list.iterator();iter.hasNext();) {
			User user = (User)iter.next();
			System.out.println(user.getName()+"\t"+user.getPassword());
		}
		
	}
	@Test
	public void testQueryForPageDirect() {
		log.info("testQueryForPage");
		String hql = "from User";
		PageInfo pageInfo = dao.queryForPage(hql, null, 10, 5, "name", "asc");
		List list = pageInfo.getDatas();
		for(Iterator iter = list.iterator();iter.hasNext();) {
			User user = (User)iter.next();
			System.out.println(user.getName()+"\t"+user.getPassword());
		}
		
	}
	@Test
	public void testQueryForPageClass() {
		log.info("testQueryForPageClass");
		String hql = "from User";
		PageInfo pi = new PageInfo();
		PageInfo pageInfo = dao.queryForPage(hql,null, pi);
		List list = pageInfo.getDatas();
		for(Iterator iter = list.iterator();iter.hasNext();) {
			User user = (User)iter.next();
			System.out.println(user.getName()+"\t"+user.getPassword());
		}
		
	}
	@Test
	public void testQueryForPageNoClass() {
		log.info("testQueryForPageNoClass");
		String hql = "from User";
		PageInfo pageInfo = dao.queryForPage(hql,null);
		List list = pageInfo.getDatas();
		for(Iterator iter = list.iterator();iter.hasNext();) {
			User user = (User)iter.next();
			System.out.println(user.getName()+"\t"+user.getPassword());
		}
		
	}
	@After
	public void after() {
		HibernateUtils.closeSession(session);
	}
}
