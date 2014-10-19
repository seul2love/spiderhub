package com.spiderhub.dao;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.Query;

/**
 * lucene基础操作接口
 * @author chenzm
 *
 */
public interface IBaseIndex {
	
	/**
	 * 获取索引写对象
	 * @return
	 */
	public IndexWriter getIndexWriter() throws IOException;
	
	/**
	 * 添加索引
	 * @param doc
	 */
	public void add(Map doc);
	
	/**
	 * 批量添加索引
	 * @param docs
	 */
	public void batchAdd(List<Map> docs);
	
	/**
	 * 更新索引
	 * @param fieldName
	 * @param fieldVaule
	 * @param doc
	 */
	 public void update(String fieldName, String fieldVaule, Map doc);
	 
	/**
	 * 删除单个域匹配到的文档
	 * @param doc
	 */
	public void delete(String fieldName, String fieldVaule);
	
	/**
	 * 删除多个域匹配到的文档
	 * @param doc
	*/
	public void batchDelete(Map<String, String> fieldMap) ;
	
	/**
	 * 索引优化
	 * @param maxSegments 
	 * @param await
	 */
	public void forceMerge(int maxSegments, boolean await);
	
	/**
	 * 查询接口
	 * @param searchField 域
	 * @param keyWord 关键字
	 * @param topCount 最多返回的条数
	 * @return
	 */
	public List<Document> search(String searchField, String keyWord, int topCount);
	
}
