package model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 할 일 정보 저장용 클래스 (Todo)
 *
 * <p>할 일의 고유 ID, 내용, 날짜 및 시간,
 * 완료 여부, 프로젝트 태그를 관리한다.</p>
 *
 * <p>할 일 생성 시 완료 상태를 함께 받는다. (새로 입력한 할 일은 false, 파일에서 불러온 할 일은 저장된 값)</p>
 *
 * <p>시간이 지정되지 않은 경우 time은 null로 저장한다.</p>
 */

public class Todo {
    private int id;
    private String content;
    private LocalDate date;
    private LocalTime time;
    private boolean isDone;
    private List<String> projectTags;

    /**
     * 새로운 할 일 생성
     *
     * <p>프로젝트 태그 null이면 빈 리스트로 초기화, 태그 있을 때는 목록을 별도의 리스트로 복사해 저장</p>
     *
     * @param id 할 일의 고유 번호 (동일한 이름의 할 일 구분용)
     * @param content 할 일 내용
     * @param date 할 일의 날짜
     * @param time 할 일의 시간
     * @param projectTags 할 일에 연결된 태그
     * @param isDone 할 일 완료 여부 (완료면 true, 미완료면 false)
     */
    public Todo(int id, String content, LocalDate date, LocalTime time, List<String> projectTags, boolean isDone) {
        this.id = id;
        this.content = content;
        this.date = date;
        this.time = time;
        this.isDone = isDone;
        this.projectTags = (projectTags == null) ? new ArrayList<>() : new ArrayList<>(projectTags);
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
     * 할 일의 날짜 반환
     * @return 할 일 날짜
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * 할 일의 날짜 변경
     * @param date 변경할 할 일 날짜
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /**
     * 할 일의 시간 반환
     * @return 할 일 시간
     */
    public LocalTime getTime() {
        return time;
    }

    /**
     * 할 일의 시간 변경
     * @param time 변경할 할 일 시간
     */
    public void setTime(LocalTime time) {
        this.time = time;
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
     * @param projectTags 변경할 할 일의 프로젝트 태그
     */
    public void setProjectTags(List<String> projectTags) {
        this.projectTags = (projectTags == null) ? new ArrayList<>() : new ArrayList<>(projectTags);
    }
}