package model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 할 일 정보 저장용 클래스 (Todo)
 *
 * <p>할 일의 고유 ID, 내용, 날짜 및 시간,
 * 완료 여부, 프로젝트 태그를 관리한다.</p>
 *
 * <p>할 일 생성 시 완료 상태는 미완료(false)로 초기화된다.</p>
 */

public class Todo {
    private int id;
    private String content;
    private LocalDateTime date;
    private boolean isDone;
    private List<String> projectTags;

    /**
     * 새로운 할 일 생성
     *
     * @param id 할 일의 고유 번호 (동일한 이름의 할 일 구분용)
     * @param content 할 일 내용
     * @param date 할 일의 날짜 및 시간
     * @param projectTags 할 일에 연결된 태그
     */
    public Todo(int id, String content, LocalDateTime date, List<String> projectTags) {
        this.id = id;
        this.content = content;
        this.date = date;
        this.isDone = false;
        this.projectTags = projectTags;
    }

    /**
     * 할 일의 고유 번호 반환
     * @return 할 일 고유 번호
     */
    public int getId() {
        return id;
    }

    /**
     * 할 일의 내용 반환
     * @return 할 일 내용
     */
    public String getContent() {
        return content;
    }

    /**
     * 할 일의 내용 변경
     * @param content 변경할 할 일 내용
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 할 일의 날짜 및 시간 반환
     * @return 할 일 날짜 및 시간
     */
    public LocalDateTime getDate() {
        return date;
    }

    /**
     * 할 일의 날짜 및 시간 변경
     * @param date 변경할 할 일 날짜 및 시간
     */
    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    /**
     * 할 일의 완료/미완료 여부 반환
     * @return 할 일 완료면 true, 미완료면 false
     */
    public boolean isDone() {
        return isDone;
    }

    /**
     * 할 일의 완료/미완료 상태 변경
     * @param done 변경할 할 일 완료 상태
     */
    public void setDone(boolean done) {
        isDone = done;
    }

    /**
     * 할 일의 태그 반환
     * @return 할 일 태그
     */
    public List<String> getProjectTags() {
        return projectTags;
    }

    /**
     * 변경할 할 일의 태그
     * @param projectTags 변경할 할 일 태그
     */
    public void setProjectTags(List<String> projectTags) {
        this.projectTags = projectTags;
    }
}