import manager.ProjectManager;
import manager.TodoManager;

import java.util.InputMismatchException;
import java.util.Scanner;

public class TaskFlow {
    String userName;
    Scanner scanner = new Scanner(System.in);

    public TaskFlow(String userName) {
        this.userName = userName;
        System.out.println(userName+"님의 TaskFlow을 실행합니다.");
        TodoManager todoManager = new TodoManager(scanner);
        // ProjectManager에게 todoManager를 넘겨주어 프로젝트별 할 일을 조회할 수 있게 함
        ProjectManager projectManager = new ProjectManager(scanner, todoManager);

        showMenu();
    }

    private void showMenu() {
        int choice=0;
        while(true){
            try{
                System.out.println("-".repeat(10)+"MENU"+"-".repeat(10));
                System.out.println("[1. 할 일(To-Do) 관리]\n[2. 프로젝트 관리]\n[0. 종료]");
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


}
