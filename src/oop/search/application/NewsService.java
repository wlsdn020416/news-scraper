package oop.search.application;

import oop.search.domain.NewsResult;

import java.util.List;
import java.util.Objects;

/// publisher와 provider를 중개해서 함께 사용 (합성)
public class NewsService {
    private final NewsProvider newsProvider;
    private final NewsPublisher newsPublisher;

    public NewsService(NewsProvider newsProvider, NewsPublisher newsPublisher) {
        this.newsProvider = Objects.requireNonNull(newsProvider, "newsProvider must not be null");
        this.newsPublisher = Objects.requireNonNull(newsPublisher, "newsPublisher must not be null");
    }

    public List<NewsResult> search(String searchQuery, int limit) {
        if (searchQuery == null || searchQuery.isBlank()) {
            throw new IllegalArgumentException("검색어를 입력해주세요.");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("검색 건수는 양의 정수여야 합니다.");
        }
        List<NewsResult> results = newsProvider.fetchNews(searchQuery, limit);
        newsPublisher.publish(searchQuery, results); // 외부에 publish할 수도 있음.
        return results; // search를 통해서 자체적으로 결과값을 사용할 수도 있고.
    }

}
