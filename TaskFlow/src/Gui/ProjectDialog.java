
package Gui;

import manager.ProjectManager;
import model.Project;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class ProjectDialog {

    // 프로젝트 마감일을 엄격하게 검사하는 날짜 형식
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm")
                    .withResolverStyle(ResolverStyle.STRICT);

    // 프로젝트 추가 창을 열고 입력받은 프로젝트를 등록
    public static void showAddDialog(
            Component parent,
            ProjectManager projectManager,
            Runnable onSaved) {

        // 프로젝트 이름과 마감일 입력칸
        JTextField nameField = new JTextField(20);
        JTextField deadlineField = new JTextField(20);

        // 입력칸을 2행 2열로 배치
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));

        formPanel.add(new JLabel("프로젝트 이름"));
        formPanel.add(nameField);

        formPanel.add(new JLabel("마감일 (yyyy-MM-dd HH:mm)"));
        formPanel.add(deadlineField);

        // 잘못 입력하면 창을 다시 열어 수정할 수 있도록 반복
        while (true) {

            int result = JOptionPane.showConfirmDialog(
                    parent,
                    formPanel,
                    "프로젝트 추가",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            // 취소하거나 창을 닫으면 등록하지 않음
            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            String name = nameField.getText().trim();
            String deadlineText = deadlineField.getText().trim();

            // 프로젝트 이름 유효성 검사
            if (name.isEmpty() || name.contains("|") || name.contains(",")) {
                JOptionPane.showMessageDialog(
                        parent,
                        "프로젝트 이름을 확인해주세요.\n| 또는 , 는 사용할 수 없습니다."
                );
                continue;
            }

            // 이미 등록된 프로젝트 이름인지 확인
            if (projectManager.findProject(name) != null) {
                JOptionPane.showMessageDialog(
                        parent,
                        "이미 존재하는 프로젝트 이름입니다."
                );
                continue;
            }

            LocalDateTime deadline;

            // 마감일 형식과 실제 날짜 검사
            try {
                deadline = LocalDateTime.parse(deadlineText, FORMATTER);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(
                        parent,
                        "날짜를 yyyy-MM-dd HH:mm 형식으로 입력해주세요."
                );
                continue;
            }

            // 과거 날짜 등록 방지
            if (deadline.toLocalDate().isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(
                        parent,
                        "오늘보다 이전 날짜는 등록할 수 없습니다."
                );
                continue;
            }

            // 기존 ProjectManager에 프로젝트 등록
            projectManager.addProject(new Project(name, deadline));

            // 등록된 프로젝트를 화면에 즉시 반영
            onSaved.run();

            return;
        }
    }
}

