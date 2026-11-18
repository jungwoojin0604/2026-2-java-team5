package manager;

import model.Project;
import model.Todo;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

/**
 * 할 일의 등록, 조회, 수정, 삭제, 완료 상태 관리하는 클래스 (TodoManager)
 *
 * <p>전체 할 일은 todoList에 저장 후
 * currentDate 기준으로 날짜별 조회와 필터링을 진행한다. </p>
 *
 * <p>날짜가 지정되지 않은 할 일은 Anytime으로 구분하여 별도의 목록으로 조회하게 한다</p>
 *
 * <p>할 일의 수정, 삭제, 완료 상태 변경은
 * 현재 선택된 날짜 또는 Anytime의 목록 번호를 기준으로 처리한다.</p>
 */
public class TodoManager {
    private final Scanner scanner;
    private final List<Todo> todoList; //todo 보관 list
    private ProjectManager projectManager;
    private int nextId;
    private LocalDate currentDate;

    /**
     * TodoManager 객체 생성
     *
     * <p>
     * - 할 일 목록 초기화
     * - 다음 할 일 id 1로 설정
     * - 조회 기준 날짜를 현재 날짜로 설정
     * - 빈번한 index 기반 접근으로 인해 ArrayList 사용 유지 </p>
     *
     * @param scanner 사용자 입력에 사용할 Scanner 객체
     */
    public TodoManager(Scanner scanner) {
        this.scanner = scanner; //scanner 작업은 이걸로 통일
        this.todoList = new ArrayList<>();
        this.nextId = 1;
        this.currentDate = LocalDate.now();
    }

    /**
     * 프로젝트 목록 조회를 위해 ProjectManager 연결
     *
     * @param projectManager 연결할 ProjectManager 객체
     */
    public void setProjectManager(ProjectManager projectManager) {
        this.projectManager = projectManager;
    }

    /**
     * 전체 메뉴 보여주는 메서드
     */
    public void showMenu() {
        int choice;
        while(true){
            try{
                showTodo();
                System.out.println("-".repeat(10)+"MENU"+"-".repeat(10));
                System.out.println("[1. 할 일 추가]");
                System.out.println("[2. 할 일 삭제]");
                System.out.println("[3. 할 일 수정]");
                System.out.println("[4. 완료 상태 변경]");
                System.out.println("[5. 날짜 이동]");
                System.out.println("[6. 태그별 할 일 조회]");
                System.out.println("[7. 완료 상태별 할 일 조회]");
                System.out.println("[8. 키워드별 할 일 검색]");
                System.out.println("[9. AnyTime 목록 조회]");
                System.out.println("[10. D-Day 목록 조회]");
                System.out.println("[0. 이전 메뉴]");
                System.out.println("-".repeat(10)+"MENU"+"-".repeat(10));
                System.out.print("선택 : ");
                choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice){
                    case 1 -> inputTodo();
                    case 2 -> deleteTodo();
                    case 3 -> updateTodo();
                    case 4 -> changeStatus();
                    case 5 -> moveDate();
                    case 6 -> {
                        String tag;
                        while(true) {
                            System.out.print("조회할 태그를 입력하세요 : ");
                            tag = scanner.nextLine().trim();
                            if(!tag.isEmpty()) {
                                break;
                            }
                            System.out.println("태그를 입력해주세요");
                        }

                        List<Todo> result = getAllTodoByTag(tag);

                        if (result.isEmpty()) {
                            System.out.println("해당 태그의 할 일이 없습니다");
                        } else {
                            System.out.println("-".repeat(10)+"태그별 할 일"+"-".repeat(10));
                            for (Todo todo : result) {
                                String output = todo.getContent() + " | " + formatTodoDate(todo) + formatTodoTime(todo) + " | " + (todo.isDone() ? "완료" : "미완료");
                                if (!todo.getProjectTags().isEmpty()) {
                                    output += " | " + todo.getProjectTags();
                                }
                                System.out.println(output);
                            }
                        }
                    }
                    case 7 -> {
                        int status;
                        while (true) {
                            System.out.println("조회할 할 일 목록의 상태를 선택하세요");
                            System.out.print("1) 완료 2) 미완료 : ");
                            String input = scanner.nextLine().trim();
                            if (input.equals("1") || input.equals("2")) {
                                status = Integer.parseInt(input);
                                break;
                            }
                            System.out.println("유효하지 않은 번호입니다. 1 또는 2를 입력해주세요");
                        }

                        if (status == 1 || status == 2) {
                            boolean done = (status == 1);
                            List<Todo> result = getTodoByStatus(done);
                            if (result.isEmpty()) {
                                System.out.println("해당 상태의 할 일이 없습니다");
                            } else {
                                System.out.println("-".repeat(10)+"상태별 할 일"+"-".repeat(10));
                                for (Todo todo : result) {
                                    String output = todo.getContent() + " | " + formatTodoDate(todo) + formatTodoTime(todo) + " | " + (todo.isDone() ? "완료" : "미완료");
                                    if (!todo.getProjectTags().isEmpty()) {
                                        output += " | " + todo.getProjectTags();
                                    }
                                    System.out.println(output);
                                }
                            }
                        }
                    }
                    case 8 -> {
                        while (true) {
                            System.out.print("검색할 키워드를 입력하세요 (취소는 0 입력): ");
                            String keyword = scanner.nextLine().trim();
                            if (keyword.equals("0")) {
                                System.out.println("검색을 취소합니다.");
                                break;
                            }
                            if (keyword.isEmpty()) {
                                System.out.println("키워드를 입력해주세요.");
                                continue;
                            }
                            List<Todo> result = getTodoByKeyword(keyword);
                            if (result.isEmpty()) {
                                System.out.println("해당 키워드가 포함된 할 일이 없습니다.");
                                continue;
                            }
                            System.out.println("---------- 키워드 검색 결과 ----------");
                            for (Todo todo : result) {
                                String output = todo.getContent() + " | " + formatTodoDate(todo) + formatTodoTime(todo) + " | " + (todo.isDone() ? "완료" : "미완료");
                                if (!todo.getProjectTags().isEmpty()) {
                                    output += " | " + todo.getProjectTags();
                                }
                                System.out.println(output);
                            }
                            break;
                        }
                    }
                    case 9 -> showAnyTimeTodo();
                    case 10 -> showDDayTodo();
                    case 0 -> {
                        System.out.println("이전 메뉴로 돌아갑니다");
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

    /**
     * 새로운 할 일 생성 후 전체 할 일 목록에 등록하는 메드
     *
     * <p>할 일에 nextId를 고유 id로 부여, 등록 후 nextId 1 증가</p>
     *
     * @param content 등록할 할 일 내용
     * @param date 등록할 할 일의 날짜 (Anytime일 떄는 null)
     * @param time 등록할 할 일의 시간 (미지정시 null로 저장)
     * @param projectTags 등록할 할 일의 태그 목록
     * @param isDone 등록할 할 일의 완료 여부 (새로 입력하면 false, 파일에서 불러오면 저장된 값)
     */
    public void addTodo(String content, LocalDate date, LocalTime time, List<String> projectTags, boolean isDone) {
        Todo todo = new Todo(nextId, content, date, time, projectTags, isDone);
        todoList.add(todo);
        nextId++;
    }

    /**
     * 사용자로부터 할 일 정보 입력받아 등록하는 메서드
     *
     * <p>할 일 내용, 날짜 및 시간, 프로젝트 태그 순서대로 입력받기</p>
     *
     * <p>할 일 내용 비어있거나 날짜 입력이 유효하지 않으면 다시 입력받고, 태그는 쉼표로 구분</p>
     */
    public void inputTodo() {
        String content;
        while (true) {
            System.out.print("할 일을 입력하세요: ");
            content = scanner.nextLine().trim();

            if (content.isEmpty()) {
                System.out.println("할 일이 입력되지 않았습니다. 다시 입력해주세요");
            } else if (content.contains("|")) {
                System.out.println("'|' 문자는 사용할 수 없습니다. 다시 입력해주세요.");
            } else {
                break;
            }
        }

       LocalDate date;

        while (true) {
            System.out.println("현재 날짜 (" + currentDate + ")에 바로 등록이 가능합니다");
            System.out.print("1) 현재 날짜에 등록 2) 날짜 직접 입력 3) Anytime >> ");

            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                date = currentDate;
                break;
            } else if (choice.equals("2")) {
                while (true) {
                    System.out.print("등록할 날짜를 입력하세요 (yyyy-MM-dd): ");
                    String inputDate = scanner.nextLine().trim();
                    try {
                        date = LocalDate.parse(inputDate, DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(java.time.format.ResolverStyle.STRICT));
                        break;
                    } catch (DateTimeException e) {
                        System.out.println("날짜 형식이 잘못되었습니다.");
                    }
                }
                break;
            } else if (choice.equals("3")) {
                date = null;
                break;
            } else {
                System.out.println("1, 2, 3 중에서 입력해주세요");
            }
        }

        LocalTime time = null;

        while (true) {
            System.out.print("시간을 입력하세요 (HH:mm, 미입력 시 시간 미지정) >> ");
            String inputTime = scanner.nextLine().trim();
            if (inputTime.isEmpty()) {
                break;
            }
            try {
                time = LocalTime.parse(inputTime);
                break;
            } catch (DateTimeException e) {
                System.out.println("시간 형식이 잘못되었습니다.");
            }
        }

        System.out.println("----- 기존 프로젝트 목록 -----");

        if (projectManager == null || projectManager.getAllProjects().isEmpty()) {
            System.out.println("등록된 프로젝트가 없습니다.");
        } else {
            for (Project project : projectManager.getAllProjects()) {
                System.out.println("- " + project.getName());
            }
        }
        System.out.println("태그 여러 개 입력시 컴마로 구분하여 입력해주세요. 태그가 없으면 Enter를 누르세요");
        System.out.print("태그를 입력하세요: ");
        String inputTag = scanner.nextLine();
        List<String> projectTags = new ArrayList<>();
        for (String tag : inputTag.split(",")) {
            if (!tag.trim().isEmpty()) {
                projectTags.add(tag.trim());
            }
        }
        addTodo(content, date, time, projectTags, false);
    }

    /**
     * 지정한 날짜에 등록한 할 일 목록 조회하는 메서드
     *
     * @param date 조회할 날짜
     * @return 해당 날짜의 할 일 목록
     */
    public List<Todo> getTodo(LocalDate date) {
        List<Todo> result = new ArrayList<>();

        for (Todo todo : todoList) {
            if (todo.getDate() != null && todo.getDate().equals(date)) {
                result.add(todo);
            }
        }
        return result;
    }

    /**
     * 할 일의 날짜를 문자열로 반환하는 메서드
     *
     * @param todo 날짜 정보를 확인할 할 일
     * @return 날짜가 지정되지 않았으면 AnyTime, 지정되었으면 날짜
     */
    private String formatTodoDate(Todo todo) {
        if (todo.getDate() == null) {
            return "AnyTime";
        }
        return todo.getDate().toString();
    }

    /**
     * 할 일의 시간 정해져 있으면 시간 문자열로 반환
     *
     * @param todo 시간 정보 확인할 할 일
     * @return 시간이 있는 경우 " | HH:mm 형식, 없으면 빈 문자열
     */
    private String formatTodoTime(Todo todo) {
        if (todo.getTime() == null) {
            return "";
        }
        return " | "+todo.getTime().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    /**
     * 현재 선택된 날짜의 할 일 목록 출력하는 메서드
     *
     * <p>할 일의 목록 번호, 내용, 시간, 완료 여부, 프로젝트 태그 출력</p>
     *
     * <p>목록 번호 1부터 시작 (할 일의 고유 id와는 별개)</p>
     *
     * <p>프로젝트 태그 있는 경우 태그 함께 출력</p>
     */
    public void showTodo() {
        List<Todo> todobydate = getTodo(currentDate);
        System.out.println("----- " + currentDate + " 할 일 목록 -----");
        if (todobydate.isEmpty()) {
            System.out.println("할 일이 없습니다.");
            return;
        }
        for (int i = 0; i < todobydate.size(); i++) {
            Todo todo = todobydate.get(i);
            String result = (i + 1) + "." + todo.getContent() + formatTodoTime(todo) + " | " + (todo.isDone() ? "완료" : "미완료");
            if (!todo.getProjectTags().isEmpty()) {
                result += " | " + todo.getProjectTags();
            }
            System.out.println(result);
        }
    }

    /**
     * Anytime 목록 출력하는 메서드
     */
    public void showAnyTimeTodo() {
        List<Todo> result = getAnyTimeTodo();
        System.out.println("----- Anytime 할 일 목록 -----");
        if (result.isEmpty()) {
            System.out.println("등록된 Anytime 할 일이 없습니다");
            return;
        }
        for (int i = 0; i< result.size(); i++) {
            Todo todo = result.get(i);
            String output = (i+1)+". "+todo.getContent()+formatTodoTime(todo)+" | "+(todo.isDone() ? "완료": "미완료");
            if (!todo.getProjectTags().isEmpty()) {
                output += " | "+todo.getProjectTags();
            }
            System.out.println(output);
        }
    }

    /**
     * 이번주와 다음주 미완료 할 일을 D-Day 순으로 출력하는 메서드
     *
     * <p>오늘부터 다음주 일요일까지의 할 일 조회</p>
     *
     * <p>날짜 있고 미완료 상태인 todo만 조회해서 남은 날짜 적은 순서대로 정렬</p>
     */
    public void showDDayTodo() {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.with(DayOfWeek.SUNDAY).plusWeeks(1);
        List<Todo> result = new ArrayList<>();

        for (Todo todo : todoList) {
            LocalDate date = todo.getDate();
            if (date != null && !todo.isDone() && !date.isBefore(today) && !date.isAfter(endDate)) {
                result.add(todo);
            }
        }

        for (int i = 0; i < result.size() - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < result.size(); j++) {
                if (result.get(j).getDate().isBefore(result.get(minIndex).getDate())) {
                    minIndex = j;
                }
            }
            Todo temp = result.get(i);
            result.set(i, result.get(minIndex));
            result.set(minIndex, temp);
        }

        System.out.println("---------- D-Day 목록 ----------");

        if (result.isEmpty()) {
            System.out.println("예정된 미완료 할 일이 없습니다.");
            return;
        }

        for (Todo todo : result) {
            long days = ChronoUnit.DAYS.between(today, todo.getDate());
            String dDay = (days == 0) ? "D-Day" : "D-" + days;
            String output = dDay + " | " + todo.getContent() + formatTodoTime(todo);
            if (!todo.getProjectTags().isEmpty()) {
                output += " | " + todo.getProjectTags();
            }
            System.out.println(output);
        }
    }

    /**
     * 할 일 조회할 때 기준이 되는 날짜 변경하는 메서드
     *
     * <p>어제, 내일, 직접 입력한 날짜로 currentDate 변경</p>
     *
     * <p>날짜 직접 입력시 존재하지 않는 날짜는 ResolverStyle.STRICT을 통해 검증</p>
     *
     * <p>잘못된 메뉴 번호 입력 시 오류 메시지를 출력하고 다시 입력받음</p>
     *
     * <p>날짜 변경 후 할 일 목록은 showTodo() 별도로 호출해야함 (날짜 변경 후 할 일 목록 자동 출력 X)</p>
     */
    public void moveDate() {
        while (true) {
            System.out.println("----- 날짜 이동 -----");
            System.out.println("1) 어제 2) 내일 3) 날짜 직접 입력");
            System.out.print("숫자 선택: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    currentDate = currentDate.minusDays(1);
                    return;
                case "2":
                    currentDate = currentDate.plusDays(1);
                    return;
                case "3":
                    while (true) {
                        System.out.print("이동할 날짜를 아래와 같은 형식으로 입력하세요 (yyyy-MM-dd): ");
                        String inputDate = scanner.nextLine().trim();

                        try {
                            currentDate = LocalDate.parse(inputDate, DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(java.time.format.ResolverStyle.STRICT));
                            break;
                        } catch (DateTimeException e) {
                            System.out.println("날짜 형식이 잘못되었습니다. 다시 입력해주세요");
                        }
                    }
                    return;
                default:
                    System.out.println("잘못된 선택입니다");
            }
        }
    }

    /**
     * 현재 선택된 날짜에서 지정한 태그 가진 할 일 조회하는 메서드
     *
     * <p>currentDate 기준으로 검색</p>
     *
     * @param tag 검색할 태그
     * @return 현재 날짜에서 해당 태그를 가진 할 일 목록
     */
    public List<Todo> getTodoByTag(String tag) {
        List<Todo> result = new ArrayList<>();

        for (Todo todo : getTodo(currentDate)) {
            if (todo.getProjectTags().contains(tag.trim())) {
                result.add(todo);
            }
        }
        return result;
    }

    /**
     * 실제 오늘 날짜부터 이후의 할 일을 완료 상태에 따라 조회할 수 있는 메서드
     *
     * <p>조회 결과는 날짜 오름차순으로 정렬 + Anytime은 마지막에 배치</p>
     *
     * @param done true면 완료, false면 미완료
     * @return 오늘 이후 및 Anytime 중 각 상태에 해당하는 할 일 목록
     */
    public List<Todo> getTodoByStatus(boolean done) {
        List<Todo> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (Todo todo : todoList) {
            LocalDate date = todo.getDate();
            if ((date == null || !date.isBefore(today)) && todo.isDone() == done) {
                result.add(todo);
            }
        }
        for (int i = 0; i < result.size() - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < result.size(); j++) {
                LocalDate date1 = result.get(j).getDate();
                LocalDate date2 = result.get(minIndex).getDate();
                if (date1 != null && (date2 == null || date1.isBefore(date2))) {
                    minIndex = j;
                }
            }
            Todo temp = result.get(i);
            result.set(i, result.get(minIndex));
            result.set(minIndex, temp);
        }
        return result;
    }

    /**
     * 전체 할 일에서 키워드가 포함된 할 일 검색하는 메서드 (조건 검색 메서드)
     *
     * @param keyword 검색할 키워드
     * @return 키워드가 포함된 할 일 목록
     */
    public List<Todo> getTodoByKeyword(String keyword) {
        List<Todo> result = new ArrayList<>();
        for (Todo todo : todoList) {
            if (todo.getContent().contains(keyword)) {
                result.add(todo);
            }
        }
        return result;
    }

    /**
     * 전체 날짜에서 지정한 태그 가진 할 일 조회하는 메서드
     * getTodoByTag()는 현재 날짜 기준 할 일 필터링, getTodoByStatus()는 실제 오늘 이후 기준, getAllTodoByTag()는 전체 날짜 기준 (Project에서 사용)
     *
     * @param tag 검색할 태그
     * @return 전체 날짜에서 해당 태그를 가진 할 일 목록
     */
    public List<Todo> getAllTodoByTag(String tag) {
        List<Todo> result = new ArrayList<>();

        for (Todo todo : todoList) {
            if (todo.getProjectTags().contains(tag.trim())) {
                result.add(todo);
            }
        }

        return result;
    }

    /**
     * Anytime에 해당하는 할 일 조회하는 메서드
     *
     * @return Anytime에 해당하는 할 일 목록
     */
    public List<Todo> getAnyTimeTodo() {
        List<Todo> result = new ArrayList<>();

        for (Todo todo : todoList) {
            if (todo.getDate() == null) {
                result.add(todo);
            }
        }
        return result;
    }

    /**
     * 사용자가 선택한 할 일의 완료 상태 변경하는 메서드
     *
     * <p>현재 날짜 또는 Anytime의 목록 번호로 할 일 선택 후 완료 상태를 현재의 반대로 전환</p>
     *
     * <p>선택 취소 또는 할 일 없으면 변경 X</p>
     */
    public void changeStatus() {
        Todo selectedTodo = selectTodo("상태 변경할 ");

        if (selectedTodo == null) {
            return;
        }

        selectedTodo.setDone(!selectedTodo.isDone());

        System.out.println("할 일 상태 변경이 완료되었습니다");
        System.out.println("변경 내용");
        System.out.println(selectedTodo.getContent()+" | "+formatTodoDate(selectedTodo)+formatTodoTime(selectedTodo)+" | "+(selectedTodo.isDone() ? "완료" : "미완료"));
    }

    /**
     * 사용자가 선택한 할 일 삭제하는 메서드
     *
     * <p>현재 날짜 또는 Anytime의 목록 번호로 할 일 선택 후 전체 할 일 목록(todoList)에서 해당 객체 제거</p>
     *
     * <p>선택을 취소하거나 할 일이 없으면 삭제 X</p>
     */
    public void deleteTodo() {
        Todo selectedTodo = selectTodo("삭제할 ");

        if (selectedTodo == null) {
            return;
        }

        todoList.remove(selectedTodo);
        System.out.println("할 일 삭제가 완료되었습니다");
        System.out.println("삭제 내용: " + selectedTodo.getContent());
    }

    /**
     * 사용자가 선택한 할 일의 정보 수정하는 메서드
     *
     * <p>현재 날짜 또는 Anytime의 목록 번호로 할 일 선택 후 내용, 날짜 및 시간, 태그 중 하나 수정 가능</p>
     *
     * <p>날짜 및 시간 선택시 날짜 지정 또는 Anytime 지정 가능. 선택 이후 시간 설정 또는 시간 미설정 상태로 지정 가능</p>
     *
     * <p>수정해도 할 일의 고유 id는 계속 유지</p>
     */
    public void updateTodo() {
        Todo selectedTodo = selectTodo("수정할 ");

        if (selectedTodo == null) {
            return;
        }

        String choice;

        while (true) {
            System.out.println("----- 수정할 항목 선택 -----");
            System.out.println("1) 할 일 내용 2) 날짜 및 시간 3) 태그 4) 수정 취소");
            System.out.print("선택 : ");
            choice = scanner.nextLine().trim();

            if (choice.equals("1") || choice.equals("2") || choice.equals("3") || choice.equals("4")) {
                break;
            }

            System.out.println("잘못된 선택입니다. 다시 입력해주세요.");
        }

        switch (choice) {
            case "1":
                while (true) {
                    System.out.print("수정할 내용을 입력하세요: ");
                    String newContent = scanner.nextLine().trim();
                    if (newContent.isEmpty()) {
                        System.out.println("할 일 내용을 입력하세요");
                    } else if (newContent.contains("|")) {
                        System.out.println("'|' 문자는 사용할 수 없습니다. 다시 입력해주세요.");
                    } else {
                        selectedTodo.setContent(newContent);
                        break;
                    }
                }
                break;

            case "2":
                LocalDate newDate = null;
                while (true) {
                    System.out.println("1) 날짜 지정 2) AnyTime");
                    System.out.print("선택 : ");
                    String dateChoice = scanner.nextLine().trim();
                    if (dateChoice.equals("1")) {
                        while (true) {
                            System.out.print("수정할 날짜를 입력하세요 (yyyy-MM-dd): ");
                            String inputDate = scanner.nextLine().trim();
                            try {
                                newDate = LocalDate.parse(inputDate,DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(java.time.format.ResolverStyle.STRICT));
                                break;
                            } catch (DateTimeException e) {
                                System.out.println("날짜 형식이 잘못되었습니다.");
                            }
                        }
                        break;
                    } else if (dateChoice.equals("2")) {
                        newDate = null;
                        break;
                    } else {
                        System.out.println("1 또는 2를 입력해주세요.");
                    }
                }
                LocalTime newTime = null;
                while (true) {
                    System.out.print("수정할 시간을 입력하세요 (HH:mm, 미입력 시 시간 미지정): ");
                    String inputTime = scanner.nextLine().trim();
                    if (inputTime.isEmpty()) {
                        break;
                    }
                    try {
                        newTime = LocalTime.parse(inputTime);
                        break;
                    } catch (DateTimeException e) {
                        System.out.println("시간 형식이 잘못되었습니다.");
                    }
                }
                selectedTodo.setDate(newDate);
                selectedTodo.setTime(newTime);
                break;

            case "3":
                System.out.println("태그 여러 개를 한 번에 수정이 가능합니다. 쉼표를 사용해 입력하세요");
                System.out.print("수정할 태그를 입력하세요: ");
                String inputTag = scanner.nextLine();
                List<String> newTag = new ArrayList<>();
                for(String tag : inputTag.split(",")) {
                    if (!tag.trim().isEmpty()) {
                        newTag.add(tag.trim());
                    }
                }
                selectedTodo.setProjectTags(newTag);
                break;

            case "4":
                System.out.println("수정을 취소합니다");
                return;

            default:
                System.out.println("잘못된 선택입니다");
                return;
        }
        System.out.println("할 일 수정이 완료되었습니다");
        System.out.println("수정 내용");
        System.out.println(selectedTodo.getContent()+" | "+formatTodoDate(selectedTodo)+formatTodoTime(selectedTodo)
                +" | "+selectedTodo.getProjectTags()+" | "+(selectedTodo.isDone() ? "완료" : "미완료"));
    }

    /**
     * 현재 날짜 또는 AnyTime 목록에서 할 일을 선택하는 메서드
     *
     * <p> 입력한 번호가 유효하지 않으면 다시 입력 받고, 0 입력하면 선택 취소</p>
     *
     * <p>할 일 선택에 필요한 공통 처리 담당 (수정, 삭제, 완료 상태 변경 메서드에서 이용)</p>
     *
     * @param message 번호 입력시 표시될 안내 문구
     * @return 선택된 Todo 객체, 취소하거나 할 일 없으면 null
     */
    private Todo selectTodo(String message) {
        List<Todo> result;
        while (true) {
            System.out.println("1) 현재 날짜 할 일");
            System.out.println("2) AnyTime 할 일");
            System.out.print("선택 : ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                result = getTodo(currentDate);
                break;
            } else if (choice.equals("2")) {
                result = getAnyTimeTodo();
                break;
            } else {
                System.out.println("잘못된 입력입니다. 다시 입력하세요");
            }
        }

        if (result.isEmpty()) {
            System.out.println("해당 목록에 할 일이 없습니다.");
            return null;
        }

        while (true) {
            System.out.print(message + "할 일 번호 (취소는 0 입력) : ");
            int number;
            try {
                number = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("올바른 숫자를 입력하세요.");
                continue;
            }

            if (number == 0) {
                System.out.println("선택을 취소했습니다.");
                return null;
            }

            if (number < 1 || number > result.size()) {
                System.out.println("잘못된 번호입니다. 다시 입력하세요.");
                continue;
            }

            return result.get(number - 1);
        }
    }

    @Override
    public String toString() {
        String[] str = new String[todoList.size()];
        int i = 0;
        for (Todo todo : todoList) {
            str[i++] = "T|" + todo.getContent() + "|" + (todo.getDate() == null ? "" : todo.getDate()) + "|" + (todo.getTime() == null ? "" : todo.getTime()) + "|" + String.join(",", todo.getProjectTags()) + "|" + todo.isDone();
        }
        return String.join("\n", str);
    }
}