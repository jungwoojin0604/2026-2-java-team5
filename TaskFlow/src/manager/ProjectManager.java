package manager;

import model.Project;
import model.Todo;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProjectManager {
    Scanner scanner;
    TodoManager todoManager;
    List<Project> projectList = new ArrayList<>(); //project 보관 List

    public ProjectManager(Scanner scanner, TodoManager todoManager) {
        this.scanner = scanner; //scanner 작업은 이걸로 통일
        this.todoManager = todoManager; //todoManager와 ProjectManager 연결
    }

    public void showMenu() {
        int choice;
        while(true){
            try{
                System.out.println("-".repeat(10)+"MENU"+"-".repeat(10));
                System.out.println("[1. 할 일 추가]\n[2. 프로젝트 관리]\n[0. 종료]");
                System.out.println("-".repeat(10)+"MENU"+"-".repeat(10));
                System.out.print("선택 : ");
                choice = scanner.nextInt();
                switch (choice){
                    case 1 -> System.out.println("[1. 할 일(To-Do) 관리]\n");
                    case 2 -> System.out.println("[2. 프로젝트 관리]\n");
                    case 0 -> {
                        System.out.println("TaskFlow를 종료합니다.");
                        return;
                    }
                    default -> System.out.println("다시 입력해주세요.\n");
                }
            }catch(InputMismatchException e){
                System.out.println("다시 입력해주세요.\n");
                scanner.nextLine();
            }
        }
    }

    //프로젝트 등록
    public void addProject(Project project) {
        if (project == null || project.getName() == null
                || project.getName().trim().isEmpty()) {
            System.out.println("올바른 프로젝트 이름을 입력해주세요.");
            return;
        }
        String name = project.getName().trim();
        project.setName(name);
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

    // projectList에서 이름이 같은 프로젝트 찾기
    public Project findProject(String name) {
        if (name == null) {
            return null;
        }
        name = name.trim();
        for(Project project:projectList){
            if(project!=null&& project.getName().equals(name)){
                return project;
            }
        }
        return null;
    }
    //프로젝트 삭제
    public void deleteProject(String name) {
        if (name == null) {
            System.out.println("프로젝트 이름을 입력해주세요.");
            return;
        }
        name = name.trim();
        Project project = findProject(name);
        if (project == null) {
            System.out.println("존재하지 않는 프로젝트입니다.");
            return;
        }
        List<Todo> todos = todoManager.getAllTodoByTag(name);
        for(Todo todo:todos){
            List<String> tags = new ArrayList<>(todo.getProjectTags());
            tags.remove(name);
            todo.setProjectTags(tags);
        }
        if(projectList.remove(project)){
            System.out.println("프로젝트가 삭제되었습니다.");
        }
    }
    //프로젝트 날짜 수정
    public void updateDeadline(String name, LocalDateTime newDeadline) {
        if (newDeadline == null) {
            System.out.println("올바른 마감일을 입력해주세요.");
            return;
        }
        Project project = findProject(name);
        if(project==null){
            System.out.println("프로젝트 이름을 다시 확인해주세요.");
            return;
        }
        project.setDeadLine(newDeadline);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        System.out.println("'" + project.getName() + "'의 마감일이 "
                + newDeadline.format(formatter) + "으로 변경되었습니다.");
    }
    //프로젝트 이름 수정
    public void updateProjectName(String oldName, String newName) {
        if (oldName == null||newName == null) {
            System.out.println("프로젝트 이름을 입력해주세요.");
            return;
        }
        oldName = oldName.trim();
        newName = newName.trim();
        if (newName.isEmpty()) {
            System.out.println("새로운 프로젝트 이름을 입력해주세요.");
            return;
        }
        Project project = findProject(oldName);
        if (project==null) {
            System.out.println("프로젝트가 존재하지 않습니다.");
            return;
        }
        if (oldName.equals(newName)) {
            System.out.println("기존 프로젝트 이름과 동일합니다.");
            return;
        }
        if (findProject(newName)!=null) {
            System.out.println("이미 사용 중인 프로젝트 이름입니다.");
            return;
        }
        List<Todo> todos = todoManager.getAllTodoByTag(oldName);
        for (Todo todo : todos) {
            List<String> tags = new ArrayList<>(todo.getProjectTags());
            int index = tags.indexOf(oldName);
            if (index >= 0) {
                tags.set(index,newName);
                todo.setProjectTags(tags);
            }
        }
        project.setName(newName);
    }

}
