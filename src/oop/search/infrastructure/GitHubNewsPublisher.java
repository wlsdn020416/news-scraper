package oop.search.infrastructure;

import oop.search.application.NewsPublisher;
import oop.search.domain.NewsResult;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public class GitHubNewsPublisher extends AbstractHttpClient implements NewsPublisher {

    private static final String GITHUB_API_URL = "https://api.github.com/repos/%s/issues";
    // %s : GITHUB_REPOSITORY <- GitHub Actions가 주는 것을 그대로 쓸 예정
    private final String token; // 환경변수로 주어질 것. GitHub Actions가 주는 것을 그대로 쓸 예정

    public GitHubNewsPublisher() {
        super(GITHUB_API_URL
                .formatted(requireEnv("GITHUB_REPOSITORY"))
        );
        this.token = requireEnv("GITHUB_TOKEN");
    }

    @Override
    public void publish(String topic, List<NewsResult> newsResults) {
//        httpClient
        String url = endpoint;
        String payload = buildPayload(topic, newsResults);
        HttpRequest request = HttpRequest.newBuilder()
//                .GET()
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .uri(URI.create(url))
//                .header("X-Naver-Client-Id", clientId)
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("GitHub issue 생성 실패: HTTP " + response.statusCode() + " - " + response.body());
            }

        } catch (Exception e) {
            throw new IllegalStateException("뉴스 발행 중 오류가 발생했습니다.", e);
        }
    }

    String buildPayload(String topic, List<NewsResult> newsResults) {
        String title = "%s (%s)".formatted(topic, ZonedDateTime.now(ZoneId.of("Asia/Seoul")));
        String body = buildIssueBody(newsResults);
        return """
                {
                  "title": "%s",
                  "body": "%s"
                }
                """.formatted(escapeJson(title), escapeJson(body)).trim();
    }

    private String buildIssueBody(List<NewsResult> newsResults) {
        StringBuilder body = new StringBuilder();
        for (NewsResult newsResult : newsResults) {
            body.append("## ").append(newsResult.title()).append('\n')
                    .append("- 링크: ").append(newsResult.url()).append('\n')
                    .append("- 발행일자: ").append(newsResult.pubDate()).append('\n')
                    .append("- 설명: ").append(newsResult.description()).append("\n\n");
        }
        return body.toString().trim();
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " 환경변수가 필요합니다.");
        }
        return value;
    }
}
