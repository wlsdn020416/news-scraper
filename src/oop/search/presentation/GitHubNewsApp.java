package oop.search.presentation;

import oop.search.application.NewsService;
import oop.search.infrastructure.GitHubNewsPublisher;
import oop.search.infrastructure.NaverNewsProvider;

public class GitHubNewsApp {
    private final NewsService newsService;

    public GitHubNewsApp(NewsService newsService) {
        this.newsService = newsService;
    }

    public void run(String keyword, int limit) {
        newsService.search(keyword, limit);
    }

    public static void main(String[] args) {
        NewsService newsService = new NewsService(
                new NaverNewsProvider(), // 그대로 두고
//                new ConsoleNewsPublisher()
                new GitHubNewsPublisher()
        );
        GitHubNewsApp app = new GitHubNewsApp(newsService);
        String keyword = args.length > 0 ? args[0] : System.getenv().getOrDefault("NEWS_QUERY", "AI");
        int limit = args.length > 1 ? Integer.parseInt(args[1]) : Integer.parseInt(System.getenv().getOrDefault("NEWS_LIMIT", "10"));
        app.run(keyword, limit);
    }
}
