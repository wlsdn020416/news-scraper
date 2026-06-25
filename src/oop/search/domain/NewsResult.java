package oop.search.domain;


//value object
//Immutable => 수정 불가능
//기본 자바 객체는 불변객체로 만드는 과정을 거쳐야 한다 => Record 는 원래부터 immutable 하다
//불러오는 API 바탕으로 ***

public record NewsResult(
        String title,
        String description,
        String url,
        String pupDate
) {
    public static void main(String[] args) {
        NewsResult result = new NewsResult("환율","현재데이터","http://naver.com",
                "2026.06.25");
        System.out.println("result = " + result);
        System.out.println("result.title = " + result.title);
    }
}
