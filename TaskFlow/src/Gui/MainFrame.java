package Gui;

import manager.ProjectManager;
import manager.ThemeManager;
import manager.TodoManager;

import javax.swing.*;
import java.awt.*;
import java.util.Scanner;
import model.Project;
import model.Theme;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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

    // 테마 관리 객체
    private final ThemeManager themeManager;

    // 상단 메뉴 및 화면 전환 버튼
    private final JPanel menuPanel;
    private final JToggleButton calendarButton;
    private final JToggleButton projectButton;

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
        themeManager = new ThemeManager();
        // 1. 메인 창 설정
        setTitle("TaskFlow");
        // 종료 전에 파일을 저장하기 위해 자동 종료 방지
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());

        // 2. Calendar 및 Project 화면 생성
        calendarPanel = new CalendarPanel(todoManager, projectManager);

        // Project에서 데이터를 변경하면 Calendar도 갱신
        projectPanel = new ProjectPanel(
                projectManager,
                todoManager,
                calendarPanel::refreshCalendarPanel
        );

        // Calendar에서 데이터를 변경하면 Project도 갱신
        calendarPanel.setOnDataChanged(projectPanel::refreshData);

        // 3. 화면 전환 영역 생성
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        cardPanel.add(calendarPanel, "Calendar");
        cardPanel.add(projectPanel, "Project");

        // 4. 화면 전환 버튼
        calendarButton = new JToggleButton("Calendar");
        projectButton = new JToggleButton("Project");

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(calendarButton);
        buttonGroup.add(projectButton);

        calendarButton.setSelected(true);

        // 5. 화면 전환 이벤트
        calendarButton.addActionListener(e -> {
            calendarPanel.refreshCalendarPanel();
            cardLayout.show(cardPanel, "Calendar");
        });

        projectButton.addActionListener(e -> {
            // 프로젝트 목록과 오른쪽 상세 화면 모두 갱신
            projectPanel.refreshData();
            cardLayout.show(cardPanel, "Project");
        });

        // 6. 상단 메뉴 영역
        menuPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
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
        themeManager.setCurrentTheme("쿠");
        applyTheme();

        // 프로그램 종료 시 프로젝트와 Todo 저장
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {

                // 기존 Manager의 파일 저장 형식 재사용
                String projects = projectManager.toString();
                String todos = todoManager.toString();

                // 프로젝트와 Todo 사이에 줄바꿈 추가
                String data = projects
                        + (projects.isEmpty() || todos.isEmpty() ? "" : "\n")
                        + todos;

                try {
                    Files.writeString(
                            Path.of("taskflow-gui.txt"),
                            data,
                            StandardCharsets.UTF_8
                    );

                    // 저장에 성공한 경우에만 종료
                    dispose();

                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(
                            MainFrame.this,
                            "파일 저장 실패: " + ex.getMessage()
                    );
                }
            }
        });

    }

    /**
     * 현재 선택된 테마를 메인 화면에 적용
     *
     * <p>전체 배경 및 상단 메뉴 영역의 색상,
     * 화면 전환 버튼의 색상과 폰트 변경</p>
     */
    public void applyTheme() {
        Theme theme = themeManager.getCurrentTheme();

        // 전체 배경색
        getContentPane().setBackground(theme.getBackgroundColor());
        cardPanel.setBackground(theme.getBackgroundColor());

        // 상단 메뉴 영역
        menuPanel.setBackground(theme.getBackgroundColor());

        // 화면 전환 버튼
        calendarButton.setBackground(theme.getButtonColor());
        calendarButton.setForeground(theme.getTextColor());
        calendarButton.setFont(theme.getFont());
        projectButton.setBackground(theme.getButtonColor());
        projectButton.setForeground(theme.getTextColor());
        projectButton.setFont(theme.getFont());

        // Calendar 화면 테마 적용
        calendarPanel.applyTheme(theme);

        // Project 화면 테마 적용
        projectPanel.applyTheme(theme);

        // 화면 갱신
        revalidate();
        repaint();
    }

    /**
     * 프로그램의 테마 관리 객체 반환
     *
     * @return 테마 관리 객체
     */
    public ThemeManager getThemeManager() {
        return themeManager;
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

        // 저장 데이터를 먼저 불러온 후 GUI 생성
            if (!loadData(todoManager, projectManager)) {
                return;
            }

            MainFrame frame = new MainFrame(todoManager, projectManager);
            frame.setVisible(true);
        });
    }

    // 기존 TaskFlow 저장 형식으로 프로젝트와 Todo 불러오기
    private static boolean loadData(TodoManager todoManager,
                                    ProjectManager projectManager) {

        Path file = Path.of("taskflow-gui.txt");

        // 저장 파일이 없다면 새 프로그램으로 시작
        if (Files.notExists(file)) {
            return true;
        }

        try {
            for (String line : Files.readAllLines(
                    file, StandardCharsets.UTF_8)) {

                line = line.replace("\uFEFF", "").trim();
                if (line.isEmpty()) continue;

                // | 기호로 저장된 데이터 구분
                String[] info = line.split("\\|", -1);

                if (info.length == 3 && info[0].equals("P")) {

                    // 프로젝트 데이터 복원
                    LocalDateTime deadline = info[2].isBlank()
                            || info[2].equals("null")
                            ? null : LocalDateTime.parse(info[2]);

                    projectManager.addProject(
                            new Project(info[1], deadline)
                    );

                } else if (info.length == 6 && info[0].equals("T")) {

                    // 날짜와 시간 복원
                    LocalDate date = info[2].isBlank()
                            || info[2].equals("null")
                            ? null : LocalDate.parse(info[2]);

                    LocalTime time = info[3].isBlank()
                            ? null : LocalTime.parse(info[3]);

                    // 프로젝트 태그 복원
                    List<String> tags = new ArrayList<>();

                    for (String tag : info[4].split(",")) {
                        if (!tag.isBlank()) {
                            tags.add(tag.trim());
                        }
                    }

                    // 완료 상태 복원
                    if (!info[5].equals("true")
                            && !info[5].equals("false")) {
                        throw new IllegalArgumentException("완료 상태 오류");
                    }

                    todoManager.addTodo(
                            info[1],
                            date,
                            time,
                            tags,
                            Boolean.parseBoolean(info[5])
                    );

                } else {
                    throw new IllegalArgumentException(
                            "저장 파일 형식이 올바르지 않습니다."
                    );
                }
            }

            return true;

        } catch (IOException | RuntimeException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "데이터 불러오기 실패: " + e.getMessage()
            );
            return false;
        }
    }
}