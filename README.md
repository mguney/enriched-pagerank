enriched-pagerank
=================

-solr zero-bust a göre pagerank hesaplıyor
o değer sıfır olursa tfidf 

tfidf 0
tfidf + pagerank 1



Parent Folder'da 
mvn clean install

Integration Folder'da
mvn exec:java -Dexec.mainClass="com.gun3y.pagerank.MainApp" -Dexec.args="-op=Semantic -thread=10 -data='C:\Users\Mustafa\Desktop\PR-DATA\data_prdm'"

	
