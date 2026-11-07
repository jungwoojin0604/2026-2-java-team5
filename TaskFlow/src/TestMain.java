import manager.TodoManager;
import model.Todo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class TestMain {
    static void main() {
//        TaskFlow taskFlow = new TaskFlow("우진");

        Scanner scanner = new Scanner(System.in);
        TodoManager todoManager = new TodoManager(scanner);

        todoManager.addTodo(
                "자바 프로젝트",
                LocalDateTime.of(2026, 11, 7, 17, 0),
                List.of("JAVA", "팀플")
        );

        todoManager.addTodo(
                "선대 과제",
                LocalDateTime.of(2026, 11, 27, 16, 0),
                List.of("선대")
        );

        todoManager.changeStatus(); // 검색해서 할 일 하나 완료 처리

        System.out.println("----- 완료된 할 일 -----");

        List<Todo> completed = todoManager.getTodoByStatus(true);

        for (Todo todo : completed) {
            System.out.println(todo.getContent()
                    + " | " + todo.getDate()
                    + " | " + (todo.isDone() ? "완료" : "미완료"));
        }
    }
}
