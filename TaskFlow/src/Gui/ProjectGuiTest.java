package Gui;

import manager.ProjectManager;
import manager.TodoManager;
import model.Project;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import javax.swing.*;
import java.awt.*;

public class ProjectGuiTest {
    public static void main(String[] args) {

        // Swing 화면을 안전하게 생성하는 실행 방식
        SwingUtilities.invokeLater(() -> {

            // 테스트용 프로그램 창 생성
            JFrame frame = new JFrame("TaskFlow");

            // 창 내부 콘텐츠 크기 지정

            // 기존 콘솔에서 사용하던 Manager 객체 생성
            Scanner scanner = new Scanner(System.in);

            TodoManager todoManager = new TodoManager(scanner);

            ProjectManager projectManager =
                    new ProjectManager(scanner, todoManager);

            // TodoManager에서도 프로젝트 목록을 조회할 수 있게 연결
            todoManager.setProjectManager(projectManager);

            // GUI 화면 확인용 임시 프로젝트 2개
            projectManager.addProject(new Project(
                    "JAVA 프로젝트",
                    LocalDateTime.now().plusDays(7)
            ));

            projectManager.addProject(new Project(
                    "선형대수 과제",
                    LocalDateTime.now().plusDays(14)
            ));

            // JAVA 프로젝트에 연결된 할 일 2개
            todoManager.addTodo(
                    "발표 준비",
                    LocalDate.now().plusDays(3),
                    null,
                    List.of("JAVA 프로젝트"),
                    false
            );

            todoManager.addTodo(
                    "자료 조사",
                    LocalDate.now().plusDays(2),
                    null,
                    List.of("JAVA 프로젝트", "선형대수 과제"),
                    true
            );

            // 두 Manager를 공유하는 ProjectPanel 생성
            ProjectPanel mainPanel =
                    new ProjectPanel(projectManager, todoManager);

            mainPanel.setPreferredSize(new Dimension(1200, 750));

            // 창 종료 설정
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // 창 크기 변경 불가능
            frame.setResizable(false);

            // 메인 패널 등록
            frame.setContentPane(mainPanel);

            // 내부 크기에 맞게 창 크기 설정
            frame.pack();

            // 화면 중앙에 표시
            frame.setLocationRelativeTo(null);

            // 창 표시
            frame.setVisible(true);
        });
    }
}

