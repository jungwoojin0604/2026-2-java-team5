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

// ProjectPanel만 실행해 보는 테스트 프로그램 (저장 기능 없음).
public class ProjectGuiTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Scanner scanner = new Scanner(System.in);
            TodoManager todoManager = new TodoManager(scanner);
            ProjectManager projectManager = new ProjectManager(scanner, todoManager);
            todoManager.setProjectManager(projectManager);

            projectManager.addProject(new Project("JAVA 프로젝트", LocalDateTime.now().plusDays(7)));
            projectManager.addProject(new Project("선형대수 과제", LocalDateTime.now().plusDays(14)));
            todoManager.addTodo("발표 준비", LocalDate.now().plusDays(3), null,
                    List.of("JAVA 프로젝트"), false);
            todoManager.addTodo("자료 조사", LocalDate.now().plusDays(2), null,
                    List.of("JAVA 프로젝트", "선형대수 과제"), true);

            JFrame frame = new JFrame("TaskFlow");
            ProjectPanel panel = new ProjectPanel(projectManager, todoManager);
            panel.setPreferredSize(new Dimension(1200, 750));
            frame.setContentPane(panel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
