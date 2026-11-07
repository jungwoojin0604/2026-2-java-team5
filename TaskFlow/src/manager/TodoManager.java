package manager;

import model.Todo;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TodoManager {
    private Scanner scanner;
    private List<Todo> todoList; //todo 보관 list
    private int nextId;
    private LocalDate currentDate;

    public TodoManager(Scanner scanner) {
        this.scanner = scanner; //scanner 작업은 이걸로 통일
        this.todoList = new ArrayList<>();
        this.nextId = 1;
        this.currentDate = LocalDate.now();
    }

    public void addTodo(String content, LocalDateTime date, List<String> projectTags) {
        Todo todo = new Todo(nextId, content, date, projectTags);
        todoList.add(todo);
        nextId++;

        //테스트용 출력
        System.out.println("등록된 ID: " + todo.getId());
        System.out.println("할 일: " + todo.getContent());
        System.out.println("날짜: " + todo.getDate());
        System.out.println("태그: " + todo.getProjectTags());
        System.out.println("목록 크기: " + todoList.size());
    }

    public void inputTodo() {
        String content;
        while (true) {
            System.out.print("할 일을 입력하세요: ");
            content = scanner.nextLine().trim();

            if (!content.isEmpty()) {
                break;
            }

            System.out.println("할 일이 입력되지 않았습니다");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime date;

        while (true) {
            System.out.print("배정할 날짜를 형식과 같이 입력하세요 (yyyy-MM-dd HH:mm): ");
            String allocatedDate = scanner.nextLine();

            try {
                date = LocalDateTime.parse(allocatedDate, formatter);
                break;
            } catch (DateTimeException e) {
                System.out.println("날짜 형식이 잘못되었습니다.");
            }
        }

        System.out.print("태그를 입력하세요: ");
        String inputTag = scanner.nextLine();

        List<String> projectTags = new ArrayList<>();

        for (String tag : inputTag.split(",")) {
            if (!tag.trim().isEmpty()) {
                projectTags.add(tag.trim());
            }
        }

        addTodo(content, date, projectTags);
    }

    public List<Todo> getTodo(LocalDate date) {
        List<Todo> result = new ArrayList<>();

        for (Todo todo : todoList) {
            if (todo.getDate().toLocalDate().equals(date)) {
                result.add(todo);
            }
        }
        return result;
    }

    public void showTodo() {
        List<Todo> todobydate = getTodo(currentDate);
        System.out.println("----- "+currentDate+" 할 일 목록 -----");
        if (todobydate.isEmpty()) {
            System.out.println("할 일이 없습니다.");
            return;
        }

        for (Todo todo : todobydate) {
            System.out.println(todo.getId()+"."+todo.getContent()+" | "+todo.getDate().toLocalTime()+" | "+(todo.isDone() ? "완료" : "미완료")+" | "+todo.getProjectTags());
        }
    }

    public List<Todo> searchTodo(String word) {
        List<Todo> result = new ArrayList<>();

        for (Todo todo : todoList) {
            if (todo.getContent().contains(word.trim())) {
                result.add(todo);
            }
        }
        return result;
    }

    public void changeStatus() {
        System.out.print("변경할 할 일 이름을 입력하세요: ");
        String word = scanner.nextLine().trim();

        if (word.isEmpty()) {
            System.out.println("검색어를 입력하세요.");
            return;
        }

        List<Todo> result = searchTodo(word);

        if (result.isEmpty()) {
            System.out.println("검색된 할 일이 없습니다.");
            return;
        }

        for (int i=0; i<result.size(); i++) {
            Todo todo = result.get(i);
            System.out.println((i+1)+"."+todo.getContent()+" | "+todo.getDate()+" | "+(todo.isDone() ? "완료" : "미완료"));
        }

        System.out.print("상태 변경할 할 일 번호: ");

        int number;

        try {
            number = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.print("숫자를 입력하세요");
            return;
        }

        if (number < 1 || number > result.size()) {
            System.out.println("잘못된 번호입니다");
            return;
        }

        Todo selectedTodo = result.get(number-1);
        selectedTodo.setDone(!selectedTodo.isDone());
        System.out.println("할 일 상태 변경이 완료되었습니다");
        System.out.println("변경 내용");
        System.out.println(selectedTodo.getContent()+" | "+selectedTodo.getDate()+" | "+(selectedTodo.isDone() ? "완료" : "미완료"));
    }

    public void moveDate() {
        System.out.println("----- 날짜 이동 -----");
        System.out.println("1) 어제 2) 내일 3) 날짜 직접 입력");
        System.out.print("숫자 선택: ");

        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                currentDate = currentDate.minusDays(1);
                break;
            case "2":
                currentDate = currentDate.plusDays(1);
                break;
            case "3":
                System.out.print("이동할 날짜를 아래와 같은 형식으로 입력하세요 (yyyy-MM-dd): ");
                String inputDate = scanner.nextLine();

                try {
                    currentDate = LocalDate.parse(inputDate);
                } catch (DateTimeException e) {
                    System.out.println("날짜 형식이 잘못되었습니다");
                    return;
                }
                break;
            default:
                System.out.println("잘못된 선택입니다");
                return;
        }

        showTodo();
    }

    public List<Todo> getTodoByTag(String tag) {
        List<Todo> result = new ArrayList<>();

        for (Todo todo : todoList) {
            if (todo.getProjectTags().contains(tag.trim())) {
                result.add(todo);
            }
        }
        return result;
    }

    public List<Todo> getTodoByStatus(boolean done) {
        List<Todo> result = new ArrayList<>();

        for (Todo todo : todoList) {
            if (todo.isDone() == done) {
                result.add(todo);
            }
        }
        return result;
    }

    public void deleteTodo() {
        System.out.print("삭제할 할 일 이름을 입력하세요: ");
        String word = scanner.nextLine().trim();

        if (word.isEmpty()) {
            System.out.println("검색어를 입력하세요.");
            return;
        }

        List<Todo> result = searchTodo(word);

        if (result.isEmpty()) {
            System.out.println("검색된 할 일이 없습니다.");
            return;
        }

        for (int i=0; i<result.size(); i++) {
            Todo todo = result.get(i);
            System.out.println((i+1)+"."+todo.getContent()+" | "+todo.getDate()+" | "+(todo.isDone() ? "완료" : "미완료"));
        }

        System.out.print("삭제할 할 일 번호: ");

        int number;

        try {
            number = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.print("숫자를 입력하세요");
            return;
        }

        if (number < 1 || number > result.size()) {
            System.out.println("잘못된 번호입니다");
            return;
        }

        Todo selectedTodo = result.get(number-1);
        todoList.remove(selectedTodo);
        System.out.println("할 일 삭제가 완료되었습니다");
    }

    public void updateTodo() {
        System.out.print("수정할 할 일 이름을 입력하세요: ");
        String word = scanner.nextLine().trim();

        if (word.isEmpty()) {
            System.out.println("검색어를 입력하세요.");
            return;
        }

        List<Todo> result = searchTodo(word);

        if (result.isEmpty()) {
            System.out.println("검색된 할 일이 없습니다.");
            return;
        }

        for (int i=0; i<result.size(); i++) {
            Todo todo = result.get(i);
            System.out.println((i+1)+"."+todo.getContent()+" | "+todo.getDate()+" | "+(todo.isDone() ? "완료" : "미완료"));
        }

        System.out.print("수정할 할 일 번호: ");

        int number;

        try {
            number = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.print("숫자를 입력하세요");
            return;
        }

        if (number < 1 || number > result.size()) {
            System.out.println("잘못된 번호입니다");
            return;
        }

        Todo selectedTodo = result.get(number-1);
        System.out.println("----- 수정할 항목 선택 -----");
        System.out.println("1) 할 일 내용 2) 날짜 및 시간 3) 태그 4) 수정 취소");
        System.out.print("선택 : ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                System.out.print("수정할 내용을 입력하세요: ");
                String newContent = scanner.nextLine().trim();
                if (newContent.isEmpty()) {
                    System.out.println("할 일 내용을 입력하세요");
                    return;
                }
                selectedTodo.setContent(newContent);
                break;

            case "2":
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                System.out.print("수정할 날짜 및 시간을 아래 형식과 같이 입력하세요 (yyyy-MM-dd HH:mm) : ");
                String inputDate = scanner.nextLine().trim();
                try {
                    LocalDateTime newDate = LocalDateTime.parse(inputDate, formatter);
                    selectedTodo.setDate(newDate);
                } catch (DateTimeException e) {
                    System.out.println("날짜 형식이 잘못되었습니다");
                    return;
                }
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
        System.out.println(selectedTodo.getContent()
                + " | " + selectedTodo.getDate()
                + " | " + selectedTodo.getProjectTags()
                + " | " + (selectedTodo.isDone() ? "완료" : "미완료"));
    }
}