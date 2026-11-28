
package Gui;

import manager.ProjectManager;
import manager.TodoManager;
import model.Project;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;

public class MainFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            // Calendar와 Project가 사용할 Manager를 한 번만 생성
            Scanner scanner = new Scanner(System.in);
            TodoManager todoManager = new TodoManager(scanner);
            ProjectManager projectManager =
                    new ProjectManager(scanner, todoManager);
            todoManager.setProjectManager(projectManager);

            // 저장된 프로젝트와 Todo를 불러온 후 GUI 생성
            if (!loadData(todoManager, projectManager)) return;

            // 기존 Calendar와 Project 화면 생성
            CalendarPanel calendarPanel =
                    new CalendarPanel(todoManager, projectManager);


            // Project 데이터가 변경되면 Calendar도 갱신
            ProjectPanel projectPanel = new ProjectPanel(
                    projectManager,
                    todoManager,
                    () -> {
                        for (Component c : calendarPanel.getComponents()) {
                            if (c instanceof TodoPanel todo) {
                                todo.refreshTodoList();
                            }
                        }
                    }
            );

            // Calendar에서 Todo를 변경하면 Project도 갱신
            for (Component c : calendarPanel.getComponents()) {
                if (c instanceof TodoPanel todo) {
                    todo.setOnDataChanged(projectPanel::refreshData);
                }
            }


            // 두 화면을 같은 위치에 겹쳐 놓고 선택한 화면만 표시
            CardLayout cardLayout = new CardLayout();
            JPanel screens = new JPanel(cardLayout);

            screens.add(calendarPanel, "Calendar");
            screens.add(projectPanel, "Project");

            // Figma처럼 왼쪽 상단에 전환 버튼 배치
            JButton calendarButton = new JButton("Calendar");
            JButton projectButton = new JButton("Project");

            JPanel tabs = new JPanel(new GridLayout(1, 2));
            tabs.setPreferredSize(new Dimension(420, 40));
            tabs.add(calendarButton);
            tabs.add(projectButton);
            // 처음 실행할 때 Calendar 탭을 선택 상태로 표시
            updateTabButtons(calendarButton, projectButton, true);

            // Calendar 버튼 클릭
            calendarButton.addActionListener(e -> {

                // Calendar의 Todo 목록을 최신 데이터로 갱신
                for (Component c : calendarPanel.getComponents()) {
                    if (c instanceof TodoPanel todo) {
                        todo.refreshTodoList();
                    }
                }

                cardLayout.show(screens, "Calendar");
                // Calendar 버튼을 파란색으로 표시
                updateTabButtons(calendarButton, projectButton, true);
            });

            // Project 버튼 클릭
            projectButton.addActionListener(e -> {
                projectPanel.refreshData();
                cardLayout.show(screens, "Project");
                // Project 버튼을 파란색으로 표시
                updateTabButtons(calendarButton, projectButton, false);
            });

            // 전체 창 구성
            JPanel root = new JPanel(new BorderLayout());
            JPanel header = new JPanel(new BorderLayout());

            header.add(tabs, BorderLayout.WEST);
            root.add(header, BorderLayout.NORTH);
            root.add(screens, BorderLayout.CENTER);

            JFrame frame = new JFrame("TaskFlow");
            frame.setContentPane(root);
            frame.setSize(1200, 790);
            // 저장에 성공한 경우에만 프로그램을 종료하도록 설정
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.setLocationRelativeTo(null);

            // 창을 닫을 때 프로젝트와 Todo를 파일에 저장
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {

                    // 기존 TaskFlow 저장 형식에 맞춰 프로젝트 저장
                    StringBuilder data = new StringBuilder();

                    for (Project project : projectManager.getAllProjects()) {
                        data.append("P|")
                                .append(project.getName()).append("|")
                                .append(project.getDeadLine()).append("\n");
                    }

                    // TodoManager의 기존 저장 형식을 재사용
                    data.append(todoManager.toString());

                    try {
                        Files.writeString(
                                Path.of("taskflow-gui.txt"),
                                data.toString(),
                                StandardCharsets.UTF_8
                        );

                        // 저장이 성공하면 창 닫기
                        frame.dispose();

                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(
                                frame,
                                "저장 실패: " + ex.getMessage()
                        );
                    }
                }
            });

            frame.setVisible(true);
        });
    }

    // 현재 선택한 탭은 파란색, 나머지는 흰색으로 표시
    private static void updateTabButtons(
            JButton calendarButton,
            JButton projectButton,
            boolean calendarSelected) {

        Color blue = new Color(37, 99, 235);

        // Calendar 버튼 색상
        calendarButton.setBackground(
                calendarSelected ? blue : Color.WHITE);
        calendarButton.setForeground(
                calendarSelected ? Color.WHITE : Color.BLACK);

        // Project 버튼 색상
        projectButton.setBackground(
                calendarSelected ? Color.WHITE : blue);
        projectButton.setForeground(
                calendarSelected ? Color.BLACK : Color.WHITE);

        // 버튼에 배경색 적용
        calendarButton.setOpaque(true);
        projectButton.setOpaque(true);
    }

    // 저장 파일에서 프로젝트와 Todo를 불러온다.
    private static boolean loadData(
            TodoManager todoManager,
            ProjectManager projectManager) {

        Path file = Path.of("taskflow-gui.txt");

        // 처음 실행하여 저장 파일이 없으면 빈 상태로 시작
        if (Files.notExists(file)) return true;

        try {
            for (String line : Files.readAllLines(
                    file, StandardCharsets.UTF_8)) {

                line = line.replace("\uFEFF", "").trim();
                if (line.isEmpty()) continue;

                // 빈 항목까지 유지하여 날짜·시간·태그 구분
                String[] info = line.split("\\|", -1);

                if (info.length == 3 && info[0].equals("P")) {

                    // 프로젝트 이름과 마감일 복원
                    projectManager.addProject(new Project(
                            info[1],
                            LocalDateTime.parse(info[2])
                    ));

                } else if (info.length == 6 && info[0].equals("T")) {

                    // 날짜 또는 시간이 비어 있으면 null
                    LocalDate date = info[2].isEmpty()
                            ? null : LocalDate.parse(info[2]);

                    LocalTime time = info[3].isEmpty()
                            ? null : LocalTime.parse(info[3]);

                    // 쉼표로 구분된 프로젝트 태그 복원
                    List<String> tags = new ArrayList<>();

                    for (String tag : info[4].split(",")) {
                        if (!tag.isBlank()) {
                            tags.add(tag.trim());
                        }
                    }

                    // 완료 상태도 함께 복원
                    if (!info[5].equals("true")
                            && !info[5].equals("false")) {
                        throw new IllegalArgumentException("완료 상태 오류");
                    }

                    todoManager.addTodo(
                            info[1], date, time, tags,
                            Boolean.parseBoolean(info[5])
                    );

                } else {
                    throw new IllegalArgumentException("저장 형식 오류");
                }
            }

            return true;

        } catch (IOException | RuntimeException e) {

            // 불러오기 실패 시 실행을 중단해 저장 파일 덮어쓰기 방지
            JOptionPane.showMessageDialog(
                    null,
                    "데이터 불러오기 실패: " + e.getMessage()
            );
            return false;
        }
    }

}

