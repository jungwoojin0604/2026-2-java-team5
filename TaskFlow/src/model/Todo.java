package model;

import java.time.LocalDateTime;
import java.util.List;

public class Todo {
    String content; //할 일 내용
    LocalDateTime date; //날짜 LocalDateTime 자료구조가 날짜 계산에 유리하다고 해서 지정함.
    boolean isDone; //완료 여부
    List<String> projectTags; //프로젝트 태그

    public Todo(String content, LocalDateTime date, List<String> projectTags) {
        this.content = content;
        this.date = date;
        this.isDone = false;
        this.projectTags = projectTags;
    }
}
