package model;

import java.time.LocalDateTime;
import java.util.List;

public class Todo {
    private int id;
    private String content;
    private LocalDateTime date;
    private boolean isDone;
    private List<String> projectTags;

    public Todo(int id, String content, LocalDateTime date, List<String> projectTags) {
        this.id = id;
        this.content = content;
        this.date = date;
        this.isDone = false;
        this.projectTags = projectTags;
    }

    public int getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public List<String> getProjectTags() {
        return projectTags;
    }

    public void setProjectTags(List<String> projectTags) {
        this.projectTags = projectTags;
    }
}

/**
 * 할 일 등록 클래스 (Todo)
 * - 할 일 내용 : String content
 * - 날짜 : LocalDateTime date
 * - 태그(프로젝트명) : List<String> projectTags
 * - 할 일 완료 / 미완 상태 표시 : boolean isDone
 * - 할 일별 고유번호(동일 이름 할 일 구분용) : int id
*/
