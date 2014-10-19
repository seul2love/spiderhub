package com.spiderhub.dao.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.spiderhub.dao.HibernateDao;
import com.spiderhub.vo.PageInfo;

public class HibernateDaoImpl implements HibernateDao{
	protected Logger log = Logger.getLogger(HibernateDaoImpl.class);
	
	private Session session;
	
	public HibernateDaoImpl(Session session) {
		this.session = session;
	}

	public Session getSession() {
		return session;
	}
	
	public void delete(final Object obj) {
		session.delete(obj);
	}

	public void delete(final Collection objs) {
		if(objs!=null){
		Iterator iter = objs.iterator();
		while(iter.hasNext()){
			this.delete(iter.next());
		}
		}
	}

	public void delete(Class refClass, Serializable[] key) {
		if(key != null){
			for(int i=0;i<key.length;i++){
				Object obj = this.get(refClass, key[i]);
				this.delete(obj);
			}
		}
	}
	public Object get(Class refClass, Serializable key) {
		return session.get(refClass, key);
	}

	public Object load(Class refClass, Serializable key) {
		return session.load(refClass, key);
	}

	public void refresh(final Object obj) {
		session.refresh(obj);
	}

	public Serializable save(Object obj) {
		return this.session.save(obj);
	}

	public int save(Collection objs) {
		if(objs!=null){
			Iterator iter = objs.iterator();
			while(iter.hasNext()){
				this.save(iter.next());
			}
		}
		return objs.size();
	}

	public void update(Object obj) {
		session.update(obj);
	}

	public void update(Collection objs) {
		if(objs!=null){
			Iterator iter = objs.iterator();
			while(iter.hasNext()){
				this.update(iter.next());
			}
		}
	}

	private Query getQuery(String hql) {
		return session.createQuery(hql);
	}

	private Query getQuery(String hql, Serializable param) {
		Query query = getQuery(hql);
		query.setParameter(1, param);
		return query;
	}

	private Query getQuery(String hql, Serializable[] params) {
			Session s = null;
	        Query q = getQuery(hql, s);
	        if (params != null) {
	            for (int i = 0; i < params.length; i++) {
	                q.setParameter(i + 1, params[i]);
	            }
	        }
	        return q;
	}

	private Query getQuery(String hql, Map params) {
		
		if(params != null) {
			Query q =  getQuery(hql);
			Set set = params.keySet();
			Iterator iter = set.iterator();
			while(iter.hasNext()){
				String key = (String)iter.next();
				String value = (String) params.get(key);
				q.setParameter(key, value);
			}
			return q;
		} else
		return getQuery(hql);
	}

	public List queryForList(String queryStr) {
		return queryForList(queryStr,null);
	}

	public List queryForList(String queryStr, Map params) {
		try {
			Query q = getQuery(queryStr, params);
			return q.list();
		} catch (HibernateException e) {
			log.debug(queryStr);
			e.printStackTrace();
			throw e;
		}
	}

	public List queryForSQLList(String queryStr) {
		try {
			Query q = session.createSQLQuery(queryStr);
			return q.list();
		} catch (HibernateException e) {
			log.debug(queryStr);
			e.printStackTrace();
			throw e;
		}
	}
	
	public List queryForSQLList(String queryStr, Map params) {
		Query q = session.createSQLQuery(queryStr);
		if(params != null) {
		Set set = params.keySet();
		Iterator iter = set.iterator();
		while(iter.hasNext()){
			String key = (String)iter.next();
			String value = (String) params.get(key);
			q.setParameter(key, value);
		}
		return q.list();
		} else {
			return queryForSQLList(queryStr);
		}
	}
	
	public PageInfo queryForPage(String hql, Map params) {
		PageInfo pageInfo = new PageInfo();
		return queryForPage(hql,params,pageInfo);
	}


	public PageInfo queryForPage(String hql, Map params, PageInfo pageInfo) {
		return queryForPage(hql,params,pageInfo.getPageIndex(),pageInfo.getPageIndex(),
				pageInfo.getSortField(),pageInfo.getSortDirect());
	}

	public PageInfo queryForPage(String hql, Map params ,
			int pageSize, int pageIndex, String sortField, String sortDirect) {
		PageInfo pageInfo = new PageInfo();
		Query query = null;
		if(pageSize != 0) {
			pageInfo.setPageSize(pageSize);
		}
		if(sortDirect != null) {
			pageInfo.setSortDirect(sortDirect);
		}
		if(sortField != null) {
			pageInfo.setSortField(sortField);
		}
		if(sortField != null) {
			hql += " order by "+sortField +" "+pageInfo.getSortDirect();
		}
		System.out.println(hql);
		if(params != null) {
			query = getQuery(hql, params)
			.setFirstResult((pageInfo.getPageIndex()-1)*pageInfo.getPageSize())
			.setMaxResults(pageInfo.getPageSize());
		} else {
			query = getQuery(hql)
			.setFirstResult((pageInfo.getPageIndex()-1)*pageInfo.getPageSize())
			.setMaxResults(pageInfo.getPageSize());
		}
		pageInfo.setDatas(query.list());
		return pageInfo;
	}

	
//	public PageInfo queryForPage(String hql, Map params) {
//		PageInfo pageInfo = new PageInfo();
//		return queryForPage(hql,params,pageInfo.getTotalCount(),pageInfo.getPageSize(),
//				pageInfo.getPageIndex(),pageInfo.getSortField(),pageInfo.getSortDirect());
//	}
//
//	public PageInfo queryForPage(String hql, Map params,PageInfo pageInfo) {
//		return queryForPage(hql,params,pageInfo.getTotalCount(),pageInfo.getPageSize(),
//				pageInfo.getPageIndex(),pageInfo.getSortField(),pageInfo.getSortDirect());
//	}
//
//	public PageInfo queryForPage(String hql, Map params, int totalCount,
//			int pageSize, int pageIndex, String sortField, String sortDirect) {
//		PageInfo pageInfo = new PageInfo();
//		
//		if(pageSize!=0)
//			pageInfo.setPageSize(pageSize);
//		if(pageIndex!=0)
//			pageInfo.setPageIndex(pageIndex);
//		pageInfo.setSortField(sortField);
//		pageInfo.setSortDirect(sortDirect);
//		//pageInfo.setTotalCount(totalCount);
//		
//		Query query = getQuery(hql, params)
//		.setFirstResult((pageInfo.getPageIndex()-1)*pageInfo.getPageSize())
//		.setMaxResults(pageInfo.getPageSize());
//		pageInfo.setDataList(query.list());
//		
//		return pageInfo;
//	}



}
