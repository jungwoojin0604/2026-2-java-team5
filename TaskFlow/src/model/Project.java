package model;

import java.time.LocalDateTime;

public class Project {
    String name; //프로젝트 이름
    LocalDateTime deadLine; //마감일 LocalDateTime 자료구조가 날짜 계산에 유리하다고 해서 지정함.

    public Project(String name, LocalDateTime deadLine) {
        this.name = name;
        this.deadLine = deadLine;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getDeadLine() {
        return deadLine;
    }

    public void setDeadLine(LocalDateTime deadLine) {
        this.deadLine = deadLine;
    }
}
