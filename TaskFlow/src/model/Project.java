package model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Project {
    private String name; //프로젝트 이름
    private LocalDateTime deadLine; //마감일 LocalDateTime 자료구조가 날짜 계산에 유리하다고 해서 지정함.

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

    //프로젝트 이름을 기준으로 두 Project 객체의 중복 여부 판단
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj instanceof Project p) {
            return Objects.equals(this.name, p.name);
        }

        return false;
    }

    //이름이 같은 프로젝트는 같은 해시값을 갖도록 설정
    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
