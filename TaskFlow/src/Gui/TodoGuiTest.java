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

/**
 * Todo GUI 기능의 독립 실행 및 검증을 위한 테스트 클래스
 *
 * <p>TodoManager와 ProjectManager를 생성하고
 * 테스트용 프로젝트 및 할 일 데이터 등록</p>
 *
 * <p>TodoPanel을 독립적으로 실행하여 할 일 조회, 추가, 수정, 삭제,
 * 완료 상태 변경, 프로젝트별 분류 및 D-Day 표시 기능 검증</p>
 *
 * <p>Anytime 목록 및 날짜 선택 팝업 기능 테스트 가능</p>
 */
public class TodoGuiTest {

    /**
     * Todo GUI 테스트 화면 실행
     *
     * <p>SwingUtilities.invokeLater()를 사용하여
     * 이벤트 디스패치 스레드(EDT)에서 GUI 생성</p>
     *
     * <p>공유 Manager 객체 연결 및 테스트 데이터 등록 후
     * TodoPanel을 포함하는 JFrame 생성 및 표시</p>
     *
     * @param args 명령행 실행 인자 (사용하지 않음)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("TaskFlow - Todo Test");
            Scanner scanner = new Scanner(System.in);
            TodoManager todoManager = new TodoManager(scanner);
            ProjectManager projectManager = new ProjectManager(scanner, todoManager);
            todoManager.setProjectManager(projectManager);

            // 테스트용 프로젝트
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
            CalendarPanel mainPanel = new CalendarPanel(todoManager, projectManager);
            mainPanel.setPreferredSize(new Dimension(1220, 750));

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