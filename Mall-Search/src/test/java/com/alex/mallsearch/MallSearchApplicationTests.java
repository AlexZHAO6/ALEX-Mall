package com.alex.mallsearch;

import com.alex.mallsearch.config.ElasticSearchConfig;
import com.alibaba.fastjson.JSON;
import lombok.Data;
import net.minidev.json.JSONValue;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Map;

@SpringBootTest
class MallSearchApplicationTests {
	@Autowired
	private RestHighLevelClient client;

	@Test
	void contextLoads() {

		System.out.println(client);
	}


	@Test
	public void indexData() throws IOException {
		IndexRequest indexRequest = new IndexRequest ("users");
		indexRequest.id("1");
		User user = new User();
		user.setUserName("zhangsa");
		user.setAge(18);
		String jsonString = JSON.toJSONString(user);
		indexRequest.source(jsonString, XContentType.JSON);
		IndexResponse indexResponse = client.index(indexRequest, ElasticSearchConfig.COMMON_OPTIONS);

		System.out.println(indexResponse);
	}

	@Test
	public void searchData() throws IOException {
		//1. 创建检索请求
		SearchRequest searchRequest = new SearchRequest();

		//1.1）指定索引
		searchRequest.indices("bank");
		//1.2）构造检索条件
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.query(QueryBuilders.matchQuery("address","Mill"));

		//1.2.1)按照年龄分布进行聚合
		TermsAggregationBuilder ageAgg= AggregationBuilders.terms("ageAgg").field("age").size(10);
		sourceBuilder.aggregation(ageAgg);

		//1.2.2)计算平均年龄
		AvgAggregationBuilder ageAvg = AggregationBuilders.avg("ageAvg").field("age");
		sourceBuilder.aggregation(ageAvg);
		//1.2.3)计算平均薪资
		AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
		sourceBuilder.aggregation(balanceAvg);

		System.out.println("search query："+sourceBuilder);
		searchRequest.source(sourceBuilder);
		//2. 执行检索
		SearchResponse searchResponse = client.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
		System.out.println("search results："+searchResponse);

		//3. 将检索结果封装为Bean
		SearchHits hits = searchResponse.getHits();
		SearchHit[] searchHits = hits.getHits();
		for (SearchHit searchHit : searchHits) {
			String sourceAsString = searchHit.getSourceAsString();
			Account account = JSON.parseObject(sourceAsString, Account.class);
			System.out.println(account);
		}

		//4. 获取聚合信息
		Aggregations aggregations = searchResponse.getAggregations();

		Terms ageAgg1 = aggregations.get("ageAgg");

		for (Terms.Bucket bucket : ageAgg1.getBuckets()) {
			String keyAsString = bucket.getKeyAsString();
			System.out.println("age："+keyAsString+" ==> "+bucket.getDocCount());
		}
		Avg ageAvg1 = aggregations.get("ageAvg");
		System.out.println("avg age："+ageAvg1.getValue());

		Avg balanceAvg1 = aggregations.get("balanceAvg");
		System.out.println("avg balance："+balanceAvg1.getValue());
	}


}
