package manager;



import model.Todo;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TodoManager {
    Scanner scanner;

    public TodoManager(Scanner scanner) {
        this.scanner = scanner; //scanner 작업은 이걸로 통일
        List<Todo> todoList = new ArrayList<>(); //todo 보관 List
    }
}
