import manager.ProjectManager;
import manager.TodoManager;
import model.Project;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class TaskFlow {
    File file;
    String userName;
    Scanner scanner2;
    Scanner scanner = new Scanner(System.in);
    TodoManager todoManager;
    ProjectManager projectManager;

    public TaskFlow(String userName, String file) {
        this.userName = userName;
        this.file = new File(file);
        try {
            scanner2 = new Scanner(this.file, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println(userName+"님의 TaskFlow을 실행합니다.");
        todoManager = new TodoManager(scanner);
        // ProjectManager에게 todoManager를 넘겨주어 프로젝트별 할 일을 조회할 수 있게 함
        projectManager = new ProjectManager(scanner, todoManager);
        loadTask();
        showMenu();
    }

    private void showMenu() {
        int choice;
        while(true){
            try{
                System.out.println("-".repeat(10)+"MENU"+"-".repeat(10));
                System.out.println("[1. 할 일 관리]\n[2. 프로젝트 관리]\n[0. 종료]");
                System.out.println("-".repeat(10)+"MENU"+"-".repeat(10));
                System.out.print("선택 : ");
                choice = scanner.nextInt();
                switch (choice){
                    case 1 -> todoManager.showMenu();
                    case 2 -> projectManager.showMenu();
                    case 0 -> {
                        System.out.println("TaskFlow를 종료합니다.");
                        saveTask();
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

    private void saveTask() {
        try {
            Files.writeString(file.toPath(), "");
            Files.writeString(file.toPath(), projectManager.toString());
            Files.writeString(file.toPath(), "\n"+todoManager.toString(), StandardOpenOption.APPEND);


        } catch (IOException e) {
            System.out.println("파일이 존재하지않습니다.");
            throw new RuntimeException(e);
        }
    }

    private void loadTask() {
        while(scanner2.hasNextLine()){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String[] info = scanner2.nextLine().split("\\|");
            if(info[0].trim().replace("\uFEFF", "").equals("P")) {

                String name = info[1].trim();
                LocalDateTime date = LocalDateTime.parse(info[2], formatter);
                projectManager.getAllProjects().add(new Project(name, date));
            }
            else if(info[0].trim().replace("\uFEFF", "").equals("T")) {
                String name = info[1].trim();
                LocalDateTime date = LocalDateTime.parse(info[2], formatter);
                List<String> projectTags = new ArrayList<>();
                for (String tag : info[3].split(",")) {
                    if (!tag.trim().isEmpty()) {
                        projectTags.add(tag.trim());
                    }
                }
                todoManager.addTodo(name, date, projectTags);
            }
            else System.out.println("!");
        }
    }


}
