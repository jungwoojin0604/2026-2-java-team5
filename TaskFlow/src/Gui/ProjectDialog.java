package Gui;

import manager.ProjectManager;
import model.Project;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 프로젝트 추가 및 수정을 위한 입력 창 (Figma "Popover/프로젝트 추가")
 *
 * <p>프로젝트 이름과 마감일(날짜 + 시·분)을 입력받음</p>
 *
 * <p>검사 규칙은 콘솔 버전과 같음: 빈 이름, | 또는 , 포함, 중복 이름,
 * 오늘보다 이전 마감일은 등록하지 않고 창 아래에 빨간 안내 표시</p>
 */
public class ProjectDialog {

    private static final int WIDTH = 380;

    /**
     * 프로젝트 추가 창을 열고 입력받은 프로젝트를 등록
     *
     * @param anchor         창을 띄울 기준 (+ 버튼)
     * @param projectManager 프로젝트 관리 객체
     * @param onSaved        등록 후 실행할 화면 갱신 동작
     */
    public static void showAddDialog(
            Component anchor,
            ProjectManager projectManager,
            Runnable onSaved) {
        showDialog(anchor, projectManager, null, onSaved);
    }

    /**
     * 프로젝트 수정 창 (이름·마감일이 채워진 채로 열림)
     *
     * @param anchor         창을 띄울 기준 (프로젝트 행)
     * @param projectManager 프로젝트 관리 객체
     * @param project        수정할 프로젝트
     * @param onSaved        수정 후 실행할 화면 갱신 동작
     */
    public static void showEditDialog(
            Component anchor,
            ProjectManager projectManager,
            Project project,
            Runnable onSaved) {
        showDialog(anchor, projectManager, project, onSaved);
    }

    private static void showDialog(
            Component anchor,
            ProjectManager projectManager,
            Project editing,
            Runnable onSaved) {

        boolean isEdit = editing != null;
        // 새 프로젝트의 기본 마감일: 일주일 뒤 23:59
        LocalDateTime initial = isEdit && editing.getDeadLine() != null
                ? editing.getDeadLine()
                : LocalDate.now().plusDays(7).atTime(23, 59);

        JPanel form = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(WIDTH, super.getPreferredSize().height);
            }
        };
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                UiKit.blackLine(), BorderFactory.createEmptyBorder(18, 20, 20, 20)));

        // 1. 제목과 X 버튼
        JButton closeButton = UiKit.iconButton(Icons.close());
        closeButton.setToolTipText("닫기 (Esc)");
        form.add(UiKit.spread(
                UiKit.label(isEdit ? "프로젝트 수정" : "프로젝트 추가", Theme.bold(20), Color.BLACK),
                closeButton));
        form.add(UiKit.gap(14));

        // 2. 프로젝트 이름
        form.add(TodoDialog.leftAligned(UiKit.label("프로젝트 이름 *", Theme.plain(14), Theme.TEXT_LABEL)));
        form.add(UiKit.gap(6));
        JTextField nameField = UiKit.textInput();
        if (isEdit) {
            nameField.setText(editing.getName());
        }
        form.add(nameField);
        form.add(UiKit.gap(18));

        // 3. 마감일 (날짜칸 + 시·분)
        form.add(TodoDialog.leftAligned(UiKit.label("마감일", Theme.bold(16), Color.BLACK)));
        form.add(UiKit.gap(8));
        UiKit.DateInput dateInput = new UiKit.DateInput(initial.toLocalDate());
        form.add(dateInput);
        form.add(UiKit.gap(2));
        UiKit.TimeSpinner hourSpinner = new UiKit.TimeSpinner(initial.getHour(), 23);
        UiKit.TimeSpinner minuteSpinner = new UiKit.TimeSpinner(initial.getMinute(), 59);
        form.add(TodoDialog.timeRow(hourSpinner, minuteSpinner));
        form.add(UiKit.gap(16));

        // 4. 오류 문구와 추가 버튼
        JLabel errorLabel = UiKit.label(" ", Theme.plain(13), Theme.RED);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorLabel.setVisible(false);
        Component errorGap = UiKit.gap(8);
        errorGap.setVisible(false);
        form.add(errorLabel);
        form.add(errorGap);

        JButton saveButton = UiKit.primaryButton(isEdit ? "저장" : "추가");
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        footer.add(saveButton);
        form.add(footer);

        Popover popover = Popover.show(anchor, form);
        closeButton.addActionListener(e -> popover.close());

        Runnable submit = () -> {
            String name = nameField.getText().trim();
            int hour = hourSpinner.getValue();
            int minute = minuteSpinner.getValue();
            String error = null;

            // 프로젝트 이름 유효성 검사
            if (name.isEmpty()) {
                error = "프로젝트 이름을 입력해주세요.";
            } else if (name.contains("|") || name.contains(",")) {
                error = "프로젝트 이름에 | 또는 , 를 사용할 수 없습니다.";
            } else if ((!isEdit || !name.equals(editing.getName()))
                    && projectManager.findProject(name) != null) {
                // 이미 등록된 프로젝트 이름인지 확인
                error = "이미 존재하는 프로젝트 이름입니다.";
            } else if (hour < 0 || minute < 0) {
                error = "올바른 시간을 입력해주세요. (시 0~23, 분 0~59)";
            }

            LocalDateTime deadline = null;
            if (error == null) {
                deadline = LocalDateTime.of(dateInput.getDate(), LocalTime.of(hour, minute));
                boolean deadlineChanged = !isEdit || !deadline.equals(editing.getDeadLine());
                // 과거 날짜 등록 방지 (수정 시에는 마감일을 바꾼 경우만 검사)
                if (deadlineChanged && deadline.toLocalDate().isBefore(LocalDate.now())) {
                    error = "오늘보다 이전 날짜는 등록할 수 없습니다.";
                }
            }

            if (error != null) {
                errorLabel.setText(error);
                errorLabel.setVisible(true);
                errorGap.setVisible(true);
                form.revalidate();
                popover.relayout();
                return;
            }

            if (isEdit) {
                // 이름이 바뀌면 할 일 태그까지 함께 바꾸는 기존 기능 사용
                if (!name.equals(editing.getName())) {
                    projectManager.updateProjectName(editing.getName(), name);
                }
                projectManager.updateDeadline(name, deadline);
            } else {
                // 기존 ProjectManager에 프로젝트 등록
                projectManager.addProject(new Project(name, deadline));
            }

            popover.close();
            onSaved.run();
        };
        saveButton.addActionListener(e -> submit.run());
        nameField.addActionListener(e -> submit.run());

        SwingUtilities.invokeLater(nameField::requestFocusInWindow);
    }
}
