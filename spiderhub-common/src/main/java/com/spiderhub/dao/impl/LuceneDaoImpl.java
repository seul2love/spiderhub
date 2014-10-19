package com.spiderhub.dao.impl;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
/**
 * 
 * @author chenzm
 *
 */
public class LuceneDaoImpl extends AbstractLuceneDao {
	public static interface DocCommand {
		public Document exec(Map doc);
	}
	
	/**FiledType构造器*/ 
	public static class FiledTypeBuilder{
		private boolean indexed = true;
	    private boolean stored = true;
	    private boolean tokenized = true;
		
		public FiledTypeBuilder indexed(boolean indexed) {
			this.indexed = indexed;
			return this;
		}
		
		public FiledTypeBuilder stored(boolean stored) {
			this.stored = stored;
			return this;
		}
		
		public FiledTypeBuilder tokenized(boolean tokenized) {
			this.tokenized = tokenized;
			return this;
		}
		
		public FiledType builder() {
			return new FiledType(this);
		}
	}
	
	/**用来描述 域 是否存储,索引,分词 */
	public static class FiledType  {
		private boolean indexed = true;
	    private boolean stored = true;
	    private boolean tokenized = true;
	    private FiledType(){};
	    
	    public FiledType(FiledTypeBuilder builder) {
	    	this.indexed = builder.indexed;
	    	this.stored = builder.stored;
	    	this.tokenized = builder.tokenized;
	    }
	    
		public boolean indexed() {
			return indexed;
		}

		public boolean stored() {
			return stored;
		}

		public boolean tokenized() {
			return tokenized;
		}
	}
	
	/**如果文档中的个别域要特殊存储，则用该类进行描述。DefaultCommand类会进行特殊处理*/ 
	public static class DocDescribe {
		Map<Object, FiledType> filedTypeMap = new HashMap<Object, FiledType>();
		public void addFieldType(Object fieldName, FiledType filedType) {
			filedTypeMap.put(fieldName, filedType);
		}
		public FiledType getFiledType(Object fieldName) {
			return this.filedTypeMap.get(fieldName);
		}
	}
	
	/** 默认的执行命令*/ 
	public static class DefaultCommand implements DocCommand{
		DocDescribe docDescribe;
		public DefaultCommand() {
		}
		public DefaultCommand(DocDescribe docDescribe) {
			this.docDescribe = docDescribe;
		}
		public Document exec(Map doc) {
			Document d = new Document();
			for(Object key : doc.keySet()) {
				if(doc.get(key) != null && doc.get(key) instanceof Reader) 
					d.add(new TextField(String.valueOf(key), (Reader)doc.get(key))); 
				else 
					if(docDescribe == null || docDescribe.getFiledType(key) == null)
						d.add(new TextField(String.valueOf(key), 
								String.valueOf(doc.get(key)), 
								Field.Store.YES));
					else {
						FiledType fieldType = docDescribe.getFiledType(key);
						//TODO 这边的实现还有问题
						d.add(new TextField(String.valueOf(key), 
								String.valueOf(doc.get(key)), 
								fieldType.stored ? Field.Store.YES : Field.Store.NO));
					}
			}
			return d;
		}
		public DocDescribe getDocDescribe() {
			return docDescribe;
		}
		public void setDocDescribe(DocDescribe docDescribe) {
			this.docDescribe = docDescribe;
		}
	};
	
	private DocCommand command;
	
	public static final DefaultCommand DEFAULT_COMMAND = new DefaultCommand();
	
	public LuceneDaoImpl(String indexPath, Analyzer analyzer, DocCommand command){
		super(indexPath, analyzer);
		this.command = command;
	}
	
	@Override
	protected Document createDocument(Map doc) {
		if(this.command == null) 
			throw new IllegalArgumentException("DocCommand类未实现,可以用默认实现类DefaultCommand.");
		return this.command.exec(doc);
	}
	
	public DocCommand getCommand() {
		return command;
	}

	public void setCommand(DocCommand command) {
		this.command = command;
	}
}
