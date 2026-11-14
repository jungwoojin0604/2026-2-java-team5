package manager;

import model.Project;
import model.Todo;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ProjectManager {
    private final Scanner scanner;
    private final TodoManager todoManager;
    // 중복 방지와 등록 순서 유지를 위해 LinkedHashSet 사용
    private final Set<Project> projectList = new LinkedHashSet<>();

    public ProjectManager(Scanner scanner, TodoManager todoManager) {
        this.scanner = scanner; // scanner 작업은 이걸로 통일
        this.todoManager = todoManager; // todoManager와 ProjectManager 연결
    }

    /**
     * 프로젝트 마감일까지 남은 날짜를 계산한다.
     * 마감 전은 D-N일, 당일은 D-Day, 지난 날짜는 D+N일로 반환한다.
     */
    private String getDDay(Project project) {
        if (project.getDeadLine() == null) {
            return "미설정";
        }
        LocalDate today = LocalDate.now();
        LocalDate deadline = project.getDeadLine().toLocalDate();

        long days = ChronoUnit.DAYS.between(today, deadline);

        if (days > 0) {
            return "D-" + days + "일";
        } else if (days == 0) {
            return "D-Day";
        } else {
            return "D+" + (-days) + "일";
        }
    }

    public void showMenu() {
        while (true) {
            // 현재 등록된 프로젝트 목록 출력
            System.out.println("\n----- 프로젝트 목록 -----");
            if (projectList.isEmpty()) {
                System.out.println("등록된 프로젝트가 없습니다.");
            } else {
                // LinkedHashSet에 저장된 프로젝트를 등록 순서대로 출력
                // 프로젝트 이름 옆에 마감일까지 남은 일수를 표시
                int i = 1;
                for (Project project : projectList) {
                    System.out.println(i + ". " + project.getName()
                            + " | " + getDDay(project));
                    i++;
                }
            }

            // 프로젝트 관리 메뉴 출력
            System.out.println("\n---------- MENU ----------");
            System.out.println("1. 프로젝트 추가");
            System.out.println("2. 프로젝트 이름 수정");
            System.out.println("3. 프로젝트 마감일 수정");
            System.out.println("4. 프로젝트 삭제");
            System.out.println("5. 프로젝트별 할 일 조회");
            System.out.println("0. 돌아가기");
            System.out.println("--------------------------");
            System.out.print("선택 : ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> inputProject();
                case "2" -> inputUpdateProjectName();
                case "3" -> inputUpdateDeadline();
                case "4" -> inputDeleteProject();
                case "5" -> selectProjectAndShowTodos();
                case "0" -> { return; }
                default -> System.out.println("다시 입력해주세요.");
            }
        }
    }

    // 1. 프로젝트 등록 메뉴
    public void inputProject() {
        System.out.print("프로젝트 이름을 입력하세요: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("프로젝트 이름을 입력해주세요.");
            return;
        }

        System.out.print("프로젝트 마감일을 입력하세요 (yyyy-MM-dd HH:mm): ");
        String inputDate = scanner.nextLine().trim();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        try {
            LocalDateTime deadline = LocalDateTime.parse(inputDate, formatter);
            Project project = new Project(name, deadline);
            addProject(project);
        } catch (DateTimeException e) {
            System.out.println("날짜 형식이 잘못되었습니다.");
        }
    }

    // 2. 프로젝트 이름 수정 메뉴
    public void inputUpdateProjectName() {
        System.out.print("수정할 프로젝트 이름: ");
        String oldName = scanner.nextLine().trim();
        System.out.print("새로운 프로젝트 이름: ");
        String newName = scanner.nextLine().trim();
        updateProjectName(oldName, newName);
    }

    // 3. 프로젝트 마감일 수정 메뉴
    public void inputUpdateDeadline() {
        System.out.print("마감일을 수정할 프로젝트 이름: ");
        String name = scanner.nextLine().trim();
        if (findProject(name) == null) {
            System.out.println("존재하지 않는 프로젝트입니다.");
            return;
        }

        System.out.print("새로운 마감일 (yyyy-MM-dd HH:mm): ");
        String inputDate = scanner.nextLine().trim();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        try {
            LocalDateTime newDeadline = LocalDateTime.parse(inputDate, formatter);
            updateDeadline(name, newDeadline);
        } catch (DateTimeException e) {
            System.out.println("날짜 형식이 잘못되었습니다.");
        }
    }

    // 4. 프로젝트 삭제 메뉴
    public void inputDeleteProject() {
        System.out.print("삭제할 프로젝트 이름: ");
        String name = scanner.nextLine().trim();
        deleteProject(name);
    }


    /**
     * 프로젝트를 Set에 등록한다.
     * 프로젝트 이름이 같으면 중복으로 판단하여 등록하지 않는다.
     */
    public void addProject(Project project) {
        if (project == null || project.getName() == null
                || project.getName().trim().isEmpty()) {
            System.out.println("올바른 프로젝트 이름을 입력해주세요.");
            return;
        }

        String name = project.getName().trim();
        project.setName(name);

        // Set.add()는 추가 성공 시 true, 중복 시 false 반환
        if (projectList.add(project)) {
            System.out.println("프로젝트가 등록되었습니다.");
        } else {
            System.out.println("이미 존재하는 프로젝트입니다.");
        }
    }

    /**
     * 프로젝트 목록을 Set 형태로 반환한다.
     * 외부에서 Set에 직접 추가하거나 삭제하지 못하도록 보호한다.
     */
    public Set<Project> getAllProjects() {
        return Collections.unmodifiableSet(projectList);
    }

    // projectList에서 이름이 같은 프로젝트 찾기
    public Project findProject(String name) {
        if (name == null) {
            return null;
        }
        name = name.trim();
        for (Project project : projectList) {
            if (project != null && project.getName().equals(name)) {
                return project;
            }
        }
        return null;
    }

    // 프로젝트 삭제
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
        for (Todo todo : todos) {
            List<String> tags = new ArrayList<>(todo.getProjectTags());
            tags.remove(name);
            todo.setProjectTags(tags);
        }
        if (projectList.remove(project)) {
            System.out.println("프로젝트가 삭제되었습니다.");
        }
    }

    // 프로젝트 날짜 수정
    public void updateDeadline(String name, LocalDateTime newDeadline) {
        if (newDeadline == null) {
            System.out.println("올바른 마감일을 입력해주세요.");
            return;
        }
        Project project = findProject(name);
        if (project == null) {
            System.out.println("프로젝트 이름을 다시 확인해주세요.");
            return;
        }
        project.setDeadLine(newDeadline);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        System.out.println("'" + project.getName() + "'의 마감일이 "
                + newDeadline.format(formatter) + "으로 변경되었습니다.");
    }

    // 프로젝트 이름 수정
    public void updateProjectName(String oldName, String newName) {
        if (oldName == null || newName == null) {
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
        if (project == null) {
            System.out.println("프로젝트가 존재하지 않습니다.");
            return;
        }
        if (oldName.equals(newName)) {
            System.out.println("기존 프로젝트 이름과 동일합니다.");
            return;
        }
        if (findProject(newName) != null) {
            System.out.println("이미 사용 중인 프로젝트 이름입니다.");
            return;
        }
        List<Todo> todos = todoManager.getAllTodoByTag(oldName);
        for (Todo todo : todos) {
            List<String> tags = new ArrayList<>(todo.getProjectTags());
            int index = tags.indexOf(oldName);
            if (index >= 0) {
                tags.set(index, newName);
                todo.setProjectTags(tags);
            }
        }
        projectList.remove(project);  // 1. 기존 이름으로 Set에서 제거
        project.setName(newName);    // 2. 프로젝트 이름 변경
        projectList.add(project);     // 3. 변경된 이름으로 다시 등록
        System.out.println("프로젝트 이름이 변경되었습니다.");
    }

    //프로젝트별 전체 할 일 조회
    public List<Todo> getProjectTodos(String projectName) {
        Project project = findProject(projectName);
        if (project == null) {
            return new ArrayList<>();
        }
        List<Todo> todos = todoManager.getAllTodoByTag(project.getName());
        todos.sort(Comparator.comparing(Todo::getDate));
        return todos;
    }

    public void showProjectTodos(String projectName) {
        Project project = findProject(projectName);
        if (project == null) {
            System.out.println("존재하지 않는 프로젝트입니다.");
            return;
        }
        List<Todo> todos = getProjectTodos(project.getName());
        System.out.println("----- " + project.getName() + "의 전체 할 일 -----");
        if (todos.isEmpty()) {
            System.out.println("이 프로젝트에 할 일이 없습니다.");
            return;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (int i = 0; i < todos.size(); i++) {
            Todo todo = todos.get(i);
            System.out.println((i + 1) + ". " + todo.getContent() + " | "
                    + todo.getDate().format(formatter) + " | "
                    + (todo.isDone() ? "완료" : "미완료"));
        }
    }

    /**
     * 등록된 프로젝트에 번호를 붙여 출력하고,
     * 사용자가 선택한 프로젝트의 할 일 목록을 조회한다.
     */
    public void selectProjectAndShowTodos() {
        if (projectList.isEmpty()) {
            System.out.println("등록된 프로젝트가 없습니다.");
            return;
        }

        // 1. Set에 저장된 프로젝트를 순서대로 출력
        System.out.println("----- 프로젝트 목록 -----");

        // 번호 선택 화면에서도 프로젝트별 D-day 표시
        int i = 1;

        for (Project project : projectList) {
            System.out.println(i + ". " + project.getName()
                    + " | " + getDDay(project));
            i++;
        }

        // 2. 사용자에게 프로젝트 번호 입력받기
        System.out.print("조회할 프로젝트 번호 (취소: 0) : ");
        String input = scanner.nextLine().trim();

        int selected;
        try {
            selected = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("숫자를 입력해주세요.");
            return;
        }

        if (selected == 0) {
            return;
        }

        if (selected < 1 || selected > projectList.size()) {
            System.out.println("존재하지 않는 프로젝트 번호입니다.");
            return;
        }

        // 3. 선택한 번호에 해당하는 프로젝트 찾기
        i = 1;
        for (Project project : projectList) {
            if (i == selected) {
                showProjectTodos(project.getName());
                return;
            }
            i++;
        }
    }

    @Override
    public String toString() {
        String[] str = new String[projectList.size()];
        int i = 0;
        for (Project project : projectList) {
            str[i++] = "P|" + project.getName() + "|" + project.getDeadLine();
        }
        return String.join("\n", str);
    }
}


