package lucenex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.Codec;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfos;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.tests.analysis.TokenStreamToDot;
import org.junit.Test;

/**
 * Trivial tests for indexing and search in Lucene
 */
public class IndexerTxt {
	
	public static String titoloOContenuto;
	public static String searchTerm;
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		boolean end = false;
		
		Path path = Paths.get("target/idx0");
		Path path1 = Paths.get("target/idx1");
		while(end!=true) {
			boolean primoInput = false;
			while(primoInput!=true) {
				System.out.println("Ciao utente vuoi cercare per titolo o per contenuto?\n\n"
						+ "[t] per titolo\n\n"
						+ "[c] per contenuto\n\n");
		
				BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
				titoloOContenuto = bufferRead.readLine();
        
				if(!(titoloOContenuto.equals("t") | titoloOContenuto.equals("c"))){
					System.out.println("Hai sbagliato");
				}
				else {
					primoInput = true;
				}
			}
        
		
			System.out.println("Cosa vuoi cercare?");
		
			BufferedReader bufferRead1 = new BufferedReader(new InputStreamReader(System.in));
			searchTerm = bufferRead1.readLine();
		
			if(titoloOContenuto.equals("t")) {
				/*String parts[] = searchTerm.split("[.]");
				for(String part: parts) {
					System.out.println(part);
				}
				BooleanQuery.Builder builder = new BooleanQuery.Builder();
				
				for(String part: parts) {
					TermQuery termQuery = new TermQuery(new Term("titolo", part));
					builder.add(new BooleanClause(termQuery, BooleanClause.Occur.SHOULD));
				}
		        BooleanQuery booleanQuery = builder.build();*/
				
				PhraseQuery phraseQuery = new PhraseQuery.Builder()
		                .add(new Term("titolo", searchTerm))
		                .build();
				try (Directory directory = FSDirectory.open(path)) {
					indexDocs(directory, new SimpleTextCodec());
					try (IndexReader reader = DirectoryReader.open(directory)) {
						IndexSearcher searcher = new IndexSearcher(reader);
						runQuery(searcher, phraseQuery);
					} finally {
						directory.close();
					}
				}
				System.out.println("Vuoi terminare la ricerca?\n\n"
						+"[y] per terminare\n\n"
						+"qualsiasi altra cosa per continuare\n\n");
				
				BufferedReader bufferRead2 = new BufferedReader(new InputStreamReader(System.in));
				String continueOrNot = bufferRead2.readLine();
				
				if(continueOrNot.equals("y")) {
					end = true;
					System.out.println("Ciao!");
				}
				
			}
			
			else if(titoloOContenuto.equals("c")){
				String parts[] = searchTerm.split(" ");
				
				BooleanQuery.Builder builder = new BooleanQuery.Builder();
				
				for(String part: parts) {
					TermQuery termQuery = new TermQuery(new Term("contenuto", part));
					builder.add(new BooleanClause(termQuery, BooleanClause.Occur.SHOULD));
				}
		        BooleanQuery booleanQuery = builder.build();

				try (Directory directory = FSDirectory.open(path1)) {	
					indexDocs(directory, new SimpleTextCodec());
					try (IndexReader reader = DirectoryReader.open(directory)) {
						IndexSearcher searcher = new IndexSearcher(reader);
						runQuery(searcher, booleanQuery);
					} finally {
						directory.close();
					}
				}
				System.out.println("Vuoi terminare la ricerca?\n\n"
						+"[y] per terminare\n\n"
						+"qualsiasi altra cosa per continuare\n\n");
				
				BufferedReader bufferRead2 = new BufferedReader(new InputStreamReader(System.in));
				String continueOrNot = bufferRead2.readLine();
				
				if(continueOrNot.equals("y")) {
					end = true;
					System.out.println("Ciao!");
				}
			}
		}
	}
	


    @Test
    public void testIndexStatistics() throws Exception {
        Path path = Paths.get("target/idx0");

        try (Directory directory = FSDirectory.open(path)) {
            indexDocs(directory, new SimpleTextCodec());
            try (IndexReader reader = DirectoryReader.open(directory)) {
                IndexSearcher searcher = new IndexSearcher(reader);
                Collection<String> indexedFields = FieldInfos.getIndexedFields(reader);
                for (String field : indexedFields) {
                    System.out.println(searcher.collectionStatistics(field));
                }
            } finally {
                directory.close();
            }

        }
    }


    @Test
    public void testIndexingAndSearchAll() throws Exception {
        Path path = Paths.get("target/idx3");

        Query query = new MatchAllDocsQuery();

        try (Directory directory = FSDirectory.open(path)) {
            indexDocs(directory, new SimpleTextCodec());
            try (IndexReader reader = DirectoryReader.open(directory)) {
                IndexSearcher searcher = new IndexSearcher(reader);
                runQuery(searcher, query);
            } finally {
                directory.close();
            }

        }
    }

    @Test
    public void testIndexingAndSearchTQ() throws Exception {
        Path path = Paths.get("target/idx2");

        Query query = new TermQuery(new Term("titolo", "michele.txt"));

        try (Directory directory = FSDirectory.open(path)) {
            indexDocs(directory, new SimpleTextCodec());
            try (IndexReader reader = DirectoryReader.open(directory)) {
                IndexSearcher searcher = new IndexSearcher(reader);
                runQuery(searcher, query);
            } finally {
                directory.close();
            }

        }
    }

    @Test
    public void testIndexingAndSearchPQ() throws Exception {
        Path path = Paths.get("target/idx4");

        PhraseQuery query = new PhraseQuery.Builder()
                .add(new Term("contenuto", "nome"))
                .add(new Term("contenuto", "michele"))
                .build();

        try (Directory directory = FSDirectory.open(path)) {
            indexDocs(directory, null);
            try (IndexReader reader = DirectoryReader.open(directory)) {
                IndexSearcher searcher = new IndexSearcher(reader);
                runQuery(searcher, query);
            } finally {
                directory.close();
            }

        }
    }

    @Test
    public void testIndexingAndSearchBQ() throws Exception {
        Path path = Paths.get("target/idx5");

        PhraseQuery phraseQuery = new PhraseQuery.Builder()
                .add(new Term("contenuto", "maurizio"))
                .build();

        TermQuery termQuery = new TermQuery(new Term("titolo", "maurizio"));

        BooleanQuery query = new BooleanQuery.Builder()
                .add(new BooleanClause(termQuery, BooleanClause.Occur.SHOULD))
                .add(new BooleanClause(phraseQuery, BooleanClause.Occur.SHOULD))
                .build();

        try (Directory directory = FSDirectory.open(path)) {
            indexDocs(directory, null);
            try (IndexReader reader = DirectoryReader.open(directory)) {
                IndexSearcher searcher = new IndexSearcher(reader);
                runQuery(searcher, query);
            } finally {
                directory.close();
            }

        }
    }

    @Test
    public void testIndexingAndSearchQP() throws Exception {
        Path path = Paths.get("target/idx1");

        QueryParser parser = new QueryParser("contenuto", new WhitespaceAnalyzer());
        Query query = parser.parse("+ingegneria dei +dati");

        try (Directory directory = FSDirectory.open(path)) {
            indexDocs(directory, null);
            try (IndexReader reader = DirectoryReader.open(directory)) {
                IndexSearcher searcher = new IndexSearcher(reader);
                runQuery(searcher, query);
            } finally {
                directory.close();
            }

        }
    }

    @Test
    public void testRankingWithDifferentSimilarities() throws Exception {
        Path path = Paths.get(Files.createTempDirectory("target").toUri());
        Directory directory = FSDirectory.open(path);

        QueryParser parser = new MultiFieldQueryParser(new String[] {"contenuto", "titolo"}, new WhitespaceAnalyzer());
        Query query = parser.parse("ingegneria dati data scientist");
        try {
            indexDocs(directory, null);
            Collection<Similarity> similarities = Arrays.asList(new ClassicSimilarity(), new BM25Similarity(2.5f, 0.2f),
                    new LMJelinekMercerSimilarity(0.1f));
            for (Similarity similarity : similarities) {
                try (IndexReader reader = DirectoryReader.open(directory)) {
                    IndexSearcher searcher = new IndexSearcher(reader);
                    searcher.setSimilarity(similarity);
                    System.err.println("Using "+ similarity);
                    runQuery(searcher, query, true);
                }
            }

        } finally {
            directory.close();
        }
    }

    @Test
    public void testIndexingAndSearchAllWithCodec() throws Exception {
        Path path = Paths.get("target/idx6");

        Query query = new MatchAllDocsQuery();

        try (Directory directory = FSDirectory.open(path)) {
            indexDocs(directory, new SimpleTextCodec());
            try (IndexReader reader = DirectoryReader.open(directory)) {
                IndexSearcher searcher = new IndexSearcher(reader);
                runQuery(searcher, query);
            } finally {
                directory.close();
            }

        }
    }

    private static void runQuery(IndexSearcher searcher, Query query) throws IOException {
        runQuery(searcher, query, false);
    }

    private static void runQuery(IndexSearcher searcher, Query query, boolean explain) throws IOException {
        TopDocs hits = searcher.search(query, 10);
        for (int i = 0; i < hits.scoreDocs.length; i++) {
            ScoreDoc scoreDoc = hits.scoreDocs[i];
            Document doc = searcher.doc(scoreDoc.doc);
            System.out.println("doc"+scoreDoc.doc + ":"+ doc.get("titolo") + " (" + scoreDoc.score +")");
            if (explain) {
                Explanation explanation = searcher.explain(query, scoreDoc.doc);
                System.out.println(explanation);
            }
        }
    }

    private static void indexDocs(Directory directory, Codec codec) throws IOException {
        Analyzer defaultAnalyzer = new StandardAnalyzer();
        CharArraySet stopWords = new CharArraySet(Arrays.asList("[.]"), true);
        Map<String, Analyzer> perFieldAnalyzers = new HashMap<>();
        perFieldAnalyzers.put("contenuto", new StandardAnalyzer());
        perFieldAnalyzers.put("titolo", new StandardAnalyzer(stopWords));

        Analyzer analyzer = new PerFieldAnalyzerWrapper(defaultAnalyzer, perFieldAnalyzers);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        if (codec != null) {
            config.setCodec(codec);
        }
        IndexWriter writer = new IndexWriter(directory, config);
        writer.deleteAll();
        
        addFileToIndex(writer);
    }
    
    public static void addFileToIndex(IndexWriter writer) throws IOException {
    	
    	Path path1 = Paths.get("files/maurizio.txt");
    	Path path2 = Paths.get("files/michele.txt");
    	Path path3 = Paths.get("files/brini.txt");
    	File file1 = path1.toFile();
    	File file2 = path2.toFile();
    	File file3 = path3.toFile();
        Document doc1 = new Document();

        FileReader fileReader1 = new FileReader(file1);
        doc1.add(
          new TextField("contenuto", fileReader1));
        doc1.add(
          new StringField("titolo", file1.getName(), Field.Store.YES));
        
        Document doc2 = new Document();

        FileReader fileReader2 = new FileReader(file2);
        doc2.add(
          new TextField("contenuto", fileReader2));
        doc2.add(
          new StringField("titolo", file2.getName(), Field.Store.YES));
        
        Document doc3 = new Document();

        FileReader fileReader3 = new FileReader(file3);
        doc3.add(
          new TextField("contenuto", fileReader3));
        doc3.add(
          new StringField("titolo", file3.getName(), Field.Store.YES));
        
        writer.addDocument(doc1);
        writer.addDocument(doc2);
        writer.addDocument(doc3);
        writer.commit();
        writer.close();
    }
    

    @Test
    public void testAnalyzer() throws Exception {
        CharArraySet stopWords = new CharArraySet(Arrays.asList("mi", "di", "a", "da", "dei", "il", "la"), true);
        Analyzer a = new StandardAnalyzer(stopWords);
        TokenStream ts = a.tokenStream(null, "Ciao, mi chiamo Maurizio");
        StringWriter w = new StringWriter();
        new TokenStreamToDot(null, ts, new PrintWriter(w)).toDot();
        System.out.println(w);
    }

}