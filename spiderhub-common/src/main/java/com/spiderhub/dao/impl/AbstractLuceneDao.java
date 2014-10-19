package com.spiderhub.dao.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.spiderhub.dao.IBaseIndex;
/**
 * 
 * @author chenzm
 *
 */
public abstract class AbstractLuceneDao implements IBaseIndex{
	/**分词器*/
	protected Analyzer analyzer;
	/**索引路径*/
	protected String indexPath;
	/**批量提交数*/
	protected int commitSize = 5000;
	
	protected Directory directory;
	
	protected Version matchVersion = Version.LUCENE_4_9;
	
	protected static Logger logger = Logger.getLogger(LuceneDaoImpl.class);
	
	public AbstractLuceneDao(String indexPath, Analyzer analyzer){
		try {
			this.analyzer = analyzer;
			this.directory = FSDirectory.open(new File(indexPath));
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
			throw new IllegalArgumentException(e);
		}
	}
	
	/**
	 * 创建文档
	 * @param doc
	 * @return
	 */
	protected abstract Document createDocument(Map doc);
	
	/**
	 * 获取IndexWrite实例
	 * 
	 * @param analyzer
	 * @param indexPath
	 * @return
	 * @throws IOException
	 */
	public IndexWriter getIndexWriter() throws IOException {
		IndexWriterConfig iwConfig = new IndexWriterConfig(this.matchVersion,
				this.analyzer);
		iwConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
		return new IndexWriter(this.directory, iwConfig);
	}
	
	public void add(Map doc) {
		IndexWriter writer = null;
		try {
			writer = getIndexWriter();
			writer.addDocument(createDocument(doc));
			writer.commit();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new IllegalStateException(e);
		} finally {
			close(writer);
		}
	}
	
	public void batchAdd(List<Map> docs) {
		IndexWriter writer = null;
		try {
			writer = getIndexWriter();  
			int index = 0;
		    for (Map doc : docs) {  
		    	writer.addDocument(createDocument(doc));
		    	if(++index % commitSize == 0) {
		    		writer.commit();
		    	}
		    }  
		    if(index % commitSize != 0)
		    	writer.commit();
		}catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new IllegalStateException(e);
		} finally {
			close(writer);
		}
	}

	/***
	 * 
	 * 删除方法
	 * 
	 * */
	public void delete(String fieldName, String fieldVaule) {
		IndexWriter writer = null;
		try {
			writer = getIndexWriter();
			Query q = new TermQuery(new Term(fieldName, fieldVaule));
			writer.deleteDocuments(q);
			writer.commit();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new IllegalStateException(e);
		} finally {
			close(writer);
		}
	}

	/**
	 * 批量删除
	 * 
	 * @param fieldMap
	 */
	public void batchDelete(Map<String, String> fieldMap) {
		IndexWriter writer = null; 
		try {
			writer = getIndexWriter();
			for (String fieldName : fieldMap.keySet()) {
				Query q = new TermQuery(new Term(fieldName, fieldMap.get(fieldName)));
				writer.deleteDocuments(q);
			}
			writer.commit();
		}catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new IllegalStateException(e);
		}finally {
			close(writer);
		}
	}

	/**
	 * 
	 * @param fieldName
	 * @param fieldVaule
	 * @param stuff
	 * @throws Exception
	 */
	public void update(String fieldName, String fieldVaule,  Map udoc) {
		IndexWriter writer = null;
		try {
			writer = getIndexWriter();
			IndexSearcher isearcher = new IndexSearcher(DirectoryReader.open(directory));
			QueryParser qp = new QueryParser(matchVersion, fieldName, analyzer);
			org.apache.lucene.search.Query query = qp.parse(fieldVaule);
			System.out.println(query.toString());
			TopDocs topDocs = isearcher.search(query, 10);
			if(topDocs.scoreDocs.length == 0)
				throw new IllegalArgumentException("索引中没有匹配的结果");
			if(topDocs.scoreDocs.length > 1) {
				throw new IllegalArgumentException("匹配到的结果超过一个");
			}
			Document doc = isearcher.doc(topDocs.scoreDocs[0].doc);
			for(IndexableField field : createDocument(udoc).getFields()) {
				if(doc.get(field.name()) != null) {
					doc.removeFields(field.name());//可能是多值域
					doc.add(field);
				}else {
					doc.add(field);
				}
			}
			writer.updateDocument(new Term(fieldName, fieldVaule), doc);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(e);
			throw new IllegalStateException(e);
		}finally {
			close(writer);
		}
	}
	
	public List<Document> search(String searchField, String searchKeyStr,
			int topCount) {
		try {
			IndexSearcher isearcher = new IndexSearcher(DirectoryReader.open(this.directory));
			QueryParser qp = new QueryParser(this.matchVersion, searchField, this.analyzer);
//			qp.setDefaultOperator(QueryParser.AND_OPERATOR);
			Query query = qp.parse(searchKeyStr);
			logger.info(query.toString(searchField));
			TopDocs topDocs = isearcher.search(query, topCount);
			List<Document> docs = new ArrayList<Document>(
					topDocs.scoreDocs.length);
			for (int i = 0; i < topDocs.scoreDocs.length; i++)
				docs.add(isearcher.doc(topDocs.scoreDocs[i].doc));
			return docs;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(e);
			throw new IllegalStateException(e);
		}
	}

	public void forceMerge(int maxSegments, boolean await)  {
		IndexWriter writer = null;
		try {
			writer = getIndexWriter();
			writer.forceMerge(maxSegments, await);
		}catch(Exception e) {
			e.printStackTrace();
			logger.info(e);
			throw new IllegalStateException(e);
		}finally {
			close(writer);
		}
	}

	protected void close(IndexWriter writer) {
		try {
			if (writer != null)
				writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Analyzer getAnalyzer() {
		return analyzer;
	}
	
	public void setAnalyzer(Analyzer analyzer) {
		this.analyzer = analyzer;
	}
	
	public String getIndexPath() {
		return indexPath;
	}
	
	public int getCommitSize() {
		return commitSize;
	}

	public void setCommitSize(int commitSize) {
		this.commitSize = commitSize;
	}
}
