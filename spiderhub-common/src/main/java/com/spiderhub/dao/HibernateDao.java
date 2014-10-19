package com.spiderhub.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;

import com.spiderhub.vo.PageInfo;


/**
 * HibernateDao基础模块
 * @author lisj
 * @version 1.0
 *
 */
public interface HibernateDao {
	
	
	public Serializable save(final Object obj);
	
	public int save(final Collection objs);
	
	public void update(final Object obj);
	
	public void update(final Collection objs);
	
	public void delete(final Object obj);
	
	public void delete(final Collection objs);
	
	public void delete(Class refClass, Serializable[] key);
	
	public void refresh(Object obj);
	
	public Object get(Class refClass,Serializable key);
	
	public Object load(Class refClass,Serializable key);
	
	public List queryForList(String queryStr);
	
	public List queryForList(String queryStr, Map params);
	
	public List queryForSQLList(String queryStr);
	
	public List queryForSQLList(String queryStr, Map params);
	
	public PageInfo queryForPage(String hql, Map params);
	
	public PageInfo queryForPage(String hql, Map params,PageInfo pageInfo);
	
    public PageInfo queryForPage(String hql, Map params,  int pageSize, int pageIndex,String sortField,String sortDirect);

    public Session getSession();
}
