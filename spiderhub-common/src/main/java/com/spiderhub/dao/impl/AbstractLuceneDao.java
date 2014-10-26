package com.spiderhub.commons.dao.impl;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.ControlledRealTimeReopenThread;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ReferenceManager;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import com.spiderhub.commons.dao.IBaseIndex;
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
	
	/**索引目录*/
	protected Directory directory;
	
	/**版本*/
	public static Version matchVersion = Version.LUCENE_4_9;
	
	/**查询管理者*/
	protected SearcherManager searchManager = null;
	
	/**索引的版本号*/
	private long maxGen = -1;
	
	/**索引写对象*/
	IndexWriter writer;
	
	/**封装索引写对象,该对象的每一个操作都会返回索引当前的版本号*/
	private TrackingIndexWriter genWriter;
	
	/**NRT NRTManager被该类所替换*/
	protected ControlledRealTimeReopenThread<IndexSearcher> realTimeThread = null;
	
//	private final ThreadLocal<Long> lastGens = new ThreadLocal<Long>();
	
	protected static Logger logger = Logger.getLogger(LuceneDaoImpl.class);
	
	public AbstractLuceneDao(String indexPath, Analyzer analyzer){
		try {
			this.analyzer = analyzer;
			this.directory = FSDirectory.open(new File(indexPath));
			this.writer = getIndexWriter(matchVersion, this.analyzer, this.directory);
			this.searchManager = new SearcherManager(writer, false , new SearcherFactory());
			Random random = new Random();
			final double minReopenSec = 0.01 + 0.05 * random.nextDouble();
			final double maxReopenSec = minReopenSec * (1.0 + 10 * random.nextDouble());
			this.genWriter = new TrackingIndexWriter(writer);
			this.realTimeThread = new ControlledRealTimeReopenThread<IndexSearcher>(
					this.genWriter, this.searchManager, maxReopenSec, minReopenSec);
			this.realTimeThread.setName("NRT Reopen Thread");
			this.realTimeThread.setPriority(Math.min(Thread.currentThread().getPriority()+2, Thread.MAX_PRIORITY));
			this.realTimeThread.start();
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
	
	
	@Override
	public IndexWriter getIndexWriter() throws IOException {
		return writer;
	}
	
	private IndexWriter getIndexWriter(Version matchVersion, Analyzer analyzer, 
			Directory directory) throws IOException {
		IndexWriterConfig iwConfig = new IndexWriterConfig(matchVersion, 
				analyzer);
		iwConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
		return new IndexWriter(directory, iwConfig);
	}
	
	@Override
	public void add(Map doc) {
		try {
			long gen = genWriter.addDocument(createDocument(doc));
			addMaxGen(gen);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new IllegalStateException(e);
		} 
	}
	
	@Override
	public void batchAdd(List<Map> docs) {
		try {
			List<Document> d = new ArrayList<Document>(docs.size());
		    for (Map doc : docs) {  
		    	d.add(createDocument(doc));
		    }  
		    long gen = genWriter.addDocuments(d);
		    addMaxGen(gen);
		}catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new IllegalStateException(e);
		} 
	}

	@Override
	public void delete(String fieldName, String fieldVaule) {
		try {
			Query q = new TermQuery(new Term(fieldName, fieldVaule));
			 long gen = genWriter.deleteDocuments(q);
			 addMaxGen(gen);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void batchDelete(Map<String, String> fieldMap) {
		try {
			for (String fieldName : fieldMap.keySet()) {
				Query q = new TermQuery(new Term(fieldName, fieldMap.get(fieldName)));
				long gen = genWriter.deleteDocuments(q);
				addMaxGen(gen);
			}
		}catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void update(String fieldName, String fieldVaule,  Map udoc) {
		try {
			IndexSearcher search = getFinalSearcher();
			QueryParser qp = new QueryParser(matchVersion, fieldName, analyzer);
			org.apache.lucene.search.Query query = qp.parse(fieldVaule);
			System.out.println(query.toString());
			TopDocs topDocs = search.search(query, 10);
			if(topDocs.scoreDocs.length == 0)
				throw new IllegalArgumentException("索引中没有匹配的结果");
			if(topDocs.scoreDocs.length > 1) {
				throw new IllegalArgumentException("匹配到的结果超过一个");
			}
			Document doc = search.doc(topDocs.scoreDocs[0].doc);
			for(IndexableField field : createDocument(udoc).getFields()) {
				if(doc.get(field.name()) != null) {
					doc.removeFields(field.name());//可能是多值域
					doc.add(field);
				}else {
					doc.add(field);
				}
			}
			long gen = genWriter.updateDocument(new Term(fieldName, fieldVaule), doc);
			addMaxGen(gen);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(e);
			throw new IllegalStateException(e);
		}
	}

	@Override
	public List<Document> search(String searchField, String searchKeyStr, int topCount) {
		IndexSearcher search = null;
		try {
			search = getFinalSearcher();
			QueryParser qp = new QueryParser(matchVersion, searchField, this.analyzer);
			Query query = qp.parse(searchKeyStr);
			logger.info(query.toString(searchField));
			TopDocs topDocs = search.search(query, topCount);
			List<Document> docs = new ArrayList<Document>(topDocs.scoreDocs.length);
			for (int i = 0; i < topDocs.scoreDocs.length; i++)
				docs.add(search.doc(topDocs.scoreDocs[i].doc));
			return docs;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(e);
			throw new IllegalStateException(e);
		}finally {
			release(search);
		}
	}

	@Override
	public List<String> searchToHighlighter(String fieldName, String fieldValue, int topCount) {
		IndexSearcher search = null;
		try {
			QueryParser parser = new QueryParser(matchVersion, fieldName, analyzer);
			Query query = parser.parse(fieldValue);
			search = getFinalSearcher();
			TopDocs topDocs = search.search(query, null, topCount);// 查找
			QueryScorer scorer = new QueryScorer(query);
			Highlighter highlight = new Highlighter(scorer);
			highlight.setTextFragmenter(new SimpleSpanFragmenter(scorer));
			Document document;
			TokenStream tokenStream;
			List<String> lightStrs = new ArrayList<String>(topDocs.scoreDocs.length);
			for (int i = 0; i < topDocs.scoreDocs.length; i++) {// 获取查找的文档的属性数据
				int docID = topDocs.scoreDocs[i].doc;
				document = search.doc(docID);
				String str = "";
				String value = document.get(fieldName);
				if (value != null) {
					tokenStream = analyzer.tokenStream(fieldName, new StringReader(value));
					str = str + highlight.getBestFragment(tokenStream, value);
				}
				lightStrs.add(str);
			}
			return lightStrs;
		}catch(Exception e) {
			e.printStackTrace();
			logger.info(e);
			throw new IllegalStateException(e);
		}finally {
			release(search);
		}
	}

	@Override
	public void forceMerge(int maxSegments, boolean await)  {
		try {
			writer.forceMerge(maxSegments, await);
		}catch(Exception e) {
			e.printStackTrace();
			logger.info(e);
			throw new IllegalStateException(e);
		}finally {
			close(writer);
		}
	}
	
	@Override
	public void doClose() {
		this.realTimeThread.close();
		close(writer);
	}
	
	private void release(IndexSearcher search) {
		try {
			searchManager.release(search);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private IndexSearcher getFinalSearcher() throws IOException, InterruptedException {
		this.realTimeThread.waitForGeneration(maxGen);
		return this.searchManager.acquire();
	}
	
//	protected void doAfterIndexingThreadDone() {
//	    Long gen = lastGens.get();
//	    if (gen != null) {
//	      addMaxGen(gen);
//	    }
//	}
	 
	private synchronized void addMaxGen(long gen) {
	    maxGen = Math.max(gen, maxGen);
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
