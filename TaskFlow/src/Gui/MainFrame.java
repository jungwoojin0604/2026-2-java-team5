package Gui;

import manager.ProjectManager;
import manager.TodoManager;

import javax.swing.*;
import java.awt.*;
import java.util.Scanner;

/**
 * TaskFlow 프로그램의 메인 화면 클래스
 *
 * <p>Calendar 및 Project 화면을 하나의 창에서 관리하고
 * CardLayout을 활용한 화면 전환 기능 제공</p>
 *
 * <p>두 화면에서 동일한 TodoManager와 ProjectManager 객체 공유</p>
 */
public class MainFrame extends JFrame {

    private final CalendarPanel calendarPanel;
    private final ProjectPanel projectPanel;

    // 화면 전환 영역
    private final CardLayout cardLayout;
    private final JPanel cardPanel;

    /**
     * TaskFlow 메인 화면 생성
     *
     * @param todoManager 할 일 관리 객체
     * @param projectManager 프로젝트 관리 객체
     */
    public MainFrame(TodoManager todoManager, ProjectManager projectManager) {

        // 1. 메인 창 설정
        setTitle("TaskFlow");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 2. Calendar 및 Project 화면 생성
        calendarPanel = new CalendarPanel(todoManager, projectManager);

        projectPanel = new ProjectPanel(projectManager, todoManager);

        // 3. 화면 전환 영역 생성
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        cardPanel.add(calendarPanel, "Calendar");
        cardPanel.add(projectPanel, "Project");

        // 4. 화면 전환 버튼
        JToggleButton calendarButton = new JToggleButton("Calendar");
        JToggleButton projectButton = new JToggleButton("Project");

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(calendarButton);
        buttonGroup.add(projectButton);

        calendarButton.setSelected(true);

        // 5. 화면 전환 이벤트
        calendarButton.addActionListener(e ->
                cardLayout.show(cardPanel, "Calendar")
        );

        projectButton.addActionListener(e ->
                cardLayout.show(cardPanel, "Project")
        );

        // 6. 상단 메뉴 영역
        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        menuPanel.setBackground(Color.WHITE);

        calendarButton.setPreferredSize(new Dimension(210, 40));
        projectButton.setPreferredSize(new Dimension(210, 40));

        menuPanel.add(calendarButton);
        menuPanel.add(projectButton);

        // 7. 전체 화면 배치
        add(menuPanel, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);

        // 8. 초기 화면 설정
        cardLayout.show(cardPanel, "Calendar");

        setSize(1100, 700);
        setLocationRelativeTo(null);
    }

    /**
     * TaskFlow 메인 화면 실행
     *
     * @param args 명령행 실행 인자
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Scanner scanner = new Scanner(System.in);
            TodoManager todoManager = new TodoManager(scanner);
            ProjectManager projectManager = new ProjectManager(scanner, todoManager);
            todoManager.setProjectManager(projectManager);
            MainFrame frame = new MainFrame(todoManager, projectManager);
            frame.setVisible(true);
        });
    }
}