package manager;

import model.Project;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProjectManager {
    Scanner scanner;
    TodoManager todoManager;
    List<Project> projectList = new ArrayList<>(); //todo 보관 List

    public ProjectManager(Scanner scanner, TodoManager todoManager) {
        this.scanner = scanner; //scanner 작업은 이걸로 통일
        this.todoManager = todoManager; //todoManager와 ProjectManager 연결
    }
    //프로젝트 등록
    public void addProject(Project project) {
        if (findProject(project.getName())!=null) {
            System.out.println("이미 존재하는 프로젝트입니다.");
            return;
        }
        projectList.add(project);
        System.out.println("프로젝트가 등록되었습니다.");
    }
    //프로젝트 리스트 반환
    public List<Project> getAllProjects() {
        return projectList;
    }
    // projectList에서 이름이 같은 프로젝트 반환
    public Project findProject(String name) {
        for(Project project:projectList){
            if(project!=null&& project.getName().equals(name)){
                return project;
            }
        }
        return null;
    }

}
