package oop.search.infrastructure;

import oop.search.application.NewsProvider;
import oop.search.domain.NewsCategory;
import oop.search.domain.NewsResult;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class NaverNewsProvider extends AbstractHttpScraper{

    private static  final String NEWS_API_URL = "https://openapi.naver.com/v1/search/news.json";

    private final String clientId;
    private final String clientSecret;
    private final NewsCategory category;

    public NaverNewsProvider() {
        super(NEWS_API_URL);
        this.clientId = System.getenv("NAVER_CLIENT_ID");
        this.clientSecret = System.getenv("NAVER_CLIENT_SECRET");
        this.category = NewsCategory.valueOf(System.getenv("NEWS_CATEGORY"));
    }

    @Override
    public List<NewsResult> fetchNews(String searchQuery, int limit) {
        String url = endpoint + "?query="
                + URLEncoder.encode(searchQuery, StandardCharsets.UTF_8)
                + "&display=" + limit
                + "&sort=" + category.getQueryValue()
                + "&start=1";

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .header("X-Naver-Client-Id", clientId)
                .header("X-Naver-Client-Secret", clientSecret)
                .build();
        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );
            String body = response.body();
            System.out.println("body = " + body);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
    }

    public static void main(String[] args) {
        NewsProvider provider = new NaverNewsProvider();
        List<NewsResult> results = provider.fetchNews("팝업", 10);
    }
}
