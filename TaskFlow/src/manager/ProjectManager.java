package manager;

import model.Project;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ProjectManager {
    Scanner scanner;
    TodoManager todoManager;

    public ProjectManager(Scanner scanner, TodoManager todoManager) {
        this.scanner = scanner; //scanner 작업은 이걸로 통일
        this.todoManager = todoManager; //todoManager와 ProjectManager 연결
        List<Project> projectList = new ArrayList<>(); //todo 보관 List
    }
}
