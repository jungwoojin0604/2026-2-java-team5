import manager.ProjectManager;
import manager.TodoManager;
import model.Project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.io.File;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class TaskFlow {
    private File file;
    private String userName;
    private Scanner scanner2;
    private Scanner scanner = new Scanner(System.in);
    private TodoManager todoManager;
    private ProjectManager projectManager;

    public TaskFlow(String userName, String file) {
        this.userName = userName;
        this.file = new File(file);
        try {
            scanner2 = new Scanner(this.file, "UTF-8");
        } catch (Exception e) {
            System.out.println("파일을 찾을 수 없습니다.");
        }
        System.out.println(userName+"님의 TaskFlow을 실행합니다.");
        todoManager = new TodoManager(scanner);
        // ProjectManager에게 todoManager를 넘겨주어 프로젝트별 할 일을 조회할 수 있게 함
        projectManager = new ProjectManager(scanner, todoManager);
        // TodoManager에서 기존 프로젝트 목록 조회할 수 있게 함
        todoManager.setProjectManager(projectManager);
        loadTask();
        showMenu();
    }

    private void showMenu() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("강제 종료합니다. 현재 데이터를 저장합니다.");
            saveTask();
        }));
        int choice;
        while(true){
            try{
                saveTask();
                System.out.println("-".repeat(10)+"MENU"+"-".repeat(10));
                System.out.println("[1. 할 일 관리]\n[2. 프로젝트 관리]\n[0. 종료]");
                System.out.println("-".repeat(10)+"MENU"+"-".repeat(10));
                System.out.print("선택 : ");
                choice = scanner.nextInt();
                scanner.nextLine();
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
            System.out.println("저장에 실패했습니다: " + e.getMessage());

        }
    }

    /**
     * 파일에서 프로젝트와 할 일을 불러온다.
     *
     * <p>저장 형식 (구분자 |, 날짜·시간은 ISO-8601)</p>
     * <pre>
     * P|이름|2026-11-13T04:20
     * T|내용|2026-11-13|18:30|태그1,태그2|false   (시간 없으면 빈칸, Anytime이면 날짜 빈칸, 마지막 칸은 완료 여부)
     * </pre>
     */
    private void loadTask() {
        if(scanner2 == null) {
            System.out.println("저장된 데이터가 없어 새로 시작합니다.");
            return;
        }
        while (scanner2.hasNextLine()) {
            String line = scanner2.nextLine().replace("\uFEFF", "");
            if (line.trim().isEmpty()) continue;
            try {
                // -1: 뒤쪽 빈칸(시간·태그 없음)도 버리지 않고 칸 수를 유지
                String[] info = line.split("\\|", -1);
                String type = info[0].trim();
                if (type.equals("P")) {
                    String name = info[1].trim();
                    LocalDateTime deadLine = LocalDateTime.parse(info[2].trim());
                    projectManager.addProject(new Project(name, deadLine));
                } else if (type.equals("T")) {
                    String name = info[1].trim();
                    String dateText = info[2].trim();
                    String timeText = info[3].trim();
                    LocalDate date = (dateText.isEmpty() || dateText.equals("null")) ? null : LocalDate.parse(dateText);
                    LocalTime time = timeText.isEmpty() ? null : LocalTime.parse(timeText);
                    List<String> projectTags = new ArrayList<>();
                    for (String tag : info[4].split(",")) {
                        if (!tag.trim().isEmpty()) {
                            projectTags.add(tag.trim());
                        }
                    }
                    // 완료 여부 칸이 없는 옛 형식 파일은 미완료(false)로 읽음
                    boolean isDone = info.length > 5 && Boolean.parseBoolean(info[5].trim());
                    todoManager.addTodo(name, date, time, projectTags, isDone);
                } else {
                    System.out.println("알 수 없는 줄을 건너뜁니다: " + line);
                }
            }catch (RuntimeException e) {
            System.out.println("읽을 수 없는 줄을 건너뜁니다: " + line);
        }
        }
    }
}
