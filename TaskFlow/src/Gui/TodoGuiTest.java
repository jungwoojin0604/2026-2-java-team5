package Gui;

import manager.ProjectManager;
import manager.TodoManager;
import model.Project;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Scanner;

public class TodoGuiTest {
    public static void main(String[] args) {

        // Swing 화면을 안전하게 생성
        SwingUtilities.invokeLater(() -> {

            // 테스트용 프로그램 창 생성
            JFrame frame = new JFrame("TaskFlow - Todo Test");

            // 기존 Manager 객체 생성 및 연결
            Scanner scanner = new Scanner(System.in);

            TodoManager todoManager = new TodoManager(scanner);

            ProjectManager projectManager =
                    new ProjectManager(scanner, todoManager);

            todoManager.setProjectManager(projectManager);

            // 테스트용 프로젝트 생성
            projectManager.addProject(new Project(
                    "JAVA 프로젝트",
                    LocalDateTime.now().plusDays(7)
            ));

            projectManager.addProject(new Project(
                    "선형대수 과제",
                    LocalDateTime.now().plusDays(14)
            ));

            // 테스트용 일반 할 일
            todoManager.addTodo(
                    "공부하기",
                    LocalDate.now(),
                    null,
                    List.of(),
                    false
            );

            // 테스트용 프로젝트 연결 할 일
            todoManager.addTodo(
                    "발표 준비",
                    LocalDate.now(),
                    LocalTime.of(18, 0),
                    List.of("JAVA 프로젝트"),
                    false
            );

            // 테스트용 완료된 할 일
            todoManager.addTodo(
                    "자료 조사",
                    LocalDate.now(),
                    null,
                    List.of("JAVA 프로젝트"),
                    true
            );

            // 테스트용 Anytime 할 일
            todoManager.addTodo(
                    "아이디어 정리",
                    null,
                    null,
                    List.of(),
                    false
            );

            // 두 Manager를 공유하는 TodoPanel 생성
            TodoPanel mainPanel = new TodoPanel(todoManager, projectManager);
            mainPanel.setPreferredSize(new Dimension(800, 750));

            // 창 설정
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.setContentPane(mainPanel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}