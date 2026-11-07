import manager.TodoManager;
import model.Todo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class TestMain {
    static void main() {
//        TaskFlow taskFlow = new TaskFlow("우진");

        Scanner scanner = new Scanner(System.in);
        TodoManager todoManager = new TodoManager(scanner);

    }
}
