package oop.search.infrastructure;

import oop.search.application.NewsProvider;
import oop.search.domain.NewsCategory;
import oop.search.domain.NewsResult;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//public class NaverNewsProvider extends AbstractHttpScraper {
public class NaverNewsProvider extends AbstractHttpClient implements NewsProvider {
    //    protected AbstractHttpScraper(String endpoint) {
//        this.endpoint = endpoint;
//    }
    // 생성자 레벨에서 사용할 상수는 static
    private static final String NEWS_API_URL = "https://openapi.naver.com/v1/search/news.json";
    private final String clientId;
    private final String clientSecret;
    private final NewsCategory category;

    // clientId, clientSecret, category
    public NaverNewsProvider() {
        super(NEWS_API_URL);
        this.clientId = requireEnv("NAVER_CLIENT_ID");
        this.clientSecret = requireEnv("NAVER_CLIENT_SECRET");
        this.category = resolveCategory(System.getenv("NEWS_CATEGORY"));
        // SIM, DATE -> 변환 (Enum - NewsCategory.SIM, NewsCategory.DATE)
        System.out.println("category = " + category);
    }

    @Override
    public List<NewsResult> fetchNews(String searchQuery, int limit) {
        if (searchQuery == null || searchQuery.isBlank()) {
            throw new IllegalArgumentException("검색어를 입력해주세요.");
        }
        if (limit <= 0 || limit > 100) {
            throw new IllegalArgumentException("검색 건수는 1부터 100 사이여야 합니다.");
        }
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

        List<NewsResult> results = new ArrayList<>();
        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );
            String body = response.body();
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Naver API 요청 실패: HTTP " + response.statusCode() + " - " + body);
            }
//            System.out.println("body = " + body);

            results.addAll(parseNewsResults(body));

        } catch (Exception e) {
            throw new IllegalStateException("뉴스 검색 중 오류가 발생했습니다.", e);
        }

//        return List.of();
        return results;
    }

    List<NewsResult> parseNewsResults(String body) {
        List<NewsResult> results = new ArrayList<>();
        for (String item : extractItemObjects(body)) {
            results.add(new NewsResult(
                    getJsonString(item, "title"),
                    getJsonString(item, "description"),
                    getJsonString(item, "link"),
                    getJsonString(item, "pubDate")
            ));
        }
        return results;
    }

    private List<String> extractItemObjects(String body) {
        int itemsKey = body.indexOf("\"items\"");
        if (itemsKey < 0) {
            return List.of();
        }
        int arrayStart = body.indexOf('[', itemsKey);
        if (arrayStart < 0) {
            return List.of();
        }

        List<String> items = new ArrayList<>();
        boolean inString = false;
        boolean escaped = false;
        int objectDepth = 0;
        int objectStart = -1;

        for (int i = arrayStart + 1; i < body.length(); i++) {
            char current = body.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (current == '"') {
                    inString = false;
                }
                continue;
            }
            if (current == '"') {
                inString = true;
            } else if (current == '{') {
                if (objectDepth == 0) {
                    objectStart = i;
                }
                objectDepth++;
            } else if (current == '}') {
                objectDepth--;
                if (objectDepth == 0 && objectStart >= 0) {
                    items.add(body.substring(objectStart, i + 1));
                    objectStart = -1;
                }
            } else if (current == ']' && objectDepth == 0) {
                break;
            }
        }
        return items;
    }

    private String getJsonString(String object, String key) {
        String keyToken = "\"" + key + "\"";
        int keyIndex = object.indexOf(keyToken);
        if (keyIndex < 0) {
            return "";
        }
        int colonIndex = object.indexOf(':', keyIndex + keyToken.length());
        if (colonIndex < 0) {
            return "";
        }
        int valueStart = object.indexOf('"', colonIndex + 1);
        if (valueStart < 0) {
            return "";
        }

        StringBuilder value = new StringBuilder();
        boolean escaped = false;
        for (int i = valueStart + 1; i < object.length(); i++) {
            char current = object.charAt(i);
            if (escaped) {
                value.append(unescape(current));
                escaped = false;
            } else if (current == '\\') {
                escaped = true;
            } else if (current == '"') {
                return value.toString();
            } else {
                value.append(current);
            }
        }
        return value.toString();
    }

    private char unescape(char current) {
        return switch (current) {
            case '"' -> '"';
            case '\\' -> '\\';
            case '/' -> '/';
            case 'b' -> '\b';
            case 'f' -> '\f';
            case 'n' -> '\n';
            case 'r' -> '\r';
            case 't' -> '\t';
            default -> current;
        };
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " 환경변수가 필요합니다.");
        }
        return value;
    }

    private static NewsCategory resolveCategory(String rawCategory) {
        if (rawCategory == null || rawCategory.isBlank()) {
            return NewsCategory.DATE;
        }
        return NewsCategory.valueOf(rawCategory.trim().toUpperCase(Locale.ROOT));
    }

    public static void main(String[] args) {
        NewsProvider provider = new NaverNewsProvider();
        List<NewsResult> results = provider.fetchNews("프리티걸", 10);
//        System.out.println("results = " + results);
        for (NewsResult newsItem : results) {
            System.out.println("newsItem = " + newsItem);
        }
    }
}
