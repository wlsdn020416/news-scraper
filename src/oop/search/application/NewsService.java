package oop.search.application;

import oop.search.domain.NewsResult;

import java.util.List;

public class NewsService {
    private final NewsProvider provider;
    private  final NewsPublisher publisher;
    public NewsService(NewsProvider provider, NewsPublisher publisher) {
        this.provider = provider;
        this.publisher = publisher;
    }

    public List<NewsResult> search(String searchQuery, int limit) {
        List<NewsResult> results = provider.fetchNews(searchQuery, limit);
        publisher.publish(searchQuery, results);
        return results;
    }
}
