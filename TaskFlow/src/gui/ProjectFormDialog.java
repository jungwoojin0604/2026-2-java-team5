package gui;

import manager.ProjectManager;
import model.Project;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * "프로젝트 추가" / "프로젝트 수정" 떠 있는 창 (ProjectFormDialog)
 *
 * <p>Projects 탭 오른쪽 위 + 버튼(추가)과 프로젝트 ⋯ 메뉴의 "수정"이 이 창을 쓴다.
 * 프로젝트 이름과 마감일(날짜 + 시간)을 입력받는다.</p>
 *
 * <p>검사 규칙은 콘솔 버전(ProjectManager.inputProject / updateProjectName / inputUpdateDeadline)과 같다.</p>
 * <ul>
 *   <li>이름이 비었거나 | , 문자가 있으면 거부</li>
 *   <li>같은 이름의 프로젝트가 있으면 거부</li>
 *   <li>마감일이 오늘보다 이전이면 거부 (수정할 때는 마감일을 바꾼 경우에만 검사)</li>
 * </ul>
 */
public class ProjectFormDialog extends FloatingDialog {
    private static final LocalTime DEFAULT_TIME = LocalTime.of(23, 59);

    private final ProjectManager projectManager;
    private final Project editing;
    private final Runnable onSaved;

    private final JTextField nameField;
    private final DateField dateField;
    private final TimeField timeField;
    private final JLabel errorLabel = FormKit.errorLabel();

    /** 새 프로젝트 추가 창 */
    public static ProjectFormDialog forAdd(Window owner, ProjectManager pm, Runnable onSaved) {
        return new ProjectFormDialog(owner, pm, null, onSaved);
    }

    /** 기존 프로젝트 수정 창 (이름·마감일이 채워진 채로 열림) */
    public static ProjectFormDialog forEdit(Window owner, ProjectManager pm, Project project, Runnable onSaved) {
        return new ProjectFormDialog(owner, pm, project, onSaved);
    }

    private ProjectFormDialog(Window owner, ProjectManager pm, Project editing, Runnable onSaved) {
        super(owner);
        this.projectManager = pm;
        this.editing = editing;
        this.onSaved = onSaved;
        boolean edit = editing != null;

        LocalDateTime deadline = edit ? editing.getDeadLine() : null;

        JPanel body = FormKit.column();
        body.setBorder(new EmptyBorder(16, 20, 16, 20));

        body.add(FormKit.header(edit ? "프로젝트 수정" : "프로젝트 추가", this::dispose));
        body.add(FormKit.gap(14));

        body.add(FormKit.fieldLabel("프로젝트 이름 *"));
        nameField = FormKit.textField(edit ? editing.getName() : "");
        body.add(nameField);
        body.add(FormKit.gap(16));

        body.add(FormKit.sectionRow("마감일", null));
        body.add(FormKit.gap(8));
        dateField = new DateField(deadline == null ? LocalDate.now() : deadline.toLocalDate());
        body.add(FormKit.fullWidth(dateField));
        body.add(FormKit.gap(10));

        timeField = new TimeField(deadline == null ? DEFAULT_TIME : deadline.toLocalTime());
        JPanel timeWrap = new JPanel(new BorderLayout());
        timeWrap.setOpaque(false);
        timeWrap.add(timeField, BorderLayout.WEST);
        body.add(FormKit.fullWidth(timeWrap));
        body.add(FormKit.gap(14));

        body.add(errorLabel);
        body.add(FormKit.gap(6));

        JButton save = FormKit.primaryButton(edit ? "저장" : "추가");
        save.addActionListener(e -> save());
        body.add(FormKit.footer(save));
        getRootPane().setDefaultButton(save);

        setBody(FormKit.fixedWidth(body, FormKit.WIDTH + 40));

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                nameField.requestFocusInWindow();
            }
        });
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        if (isShowing()) {
            Point loc = getLocation();
            pack();
            setLocation(loc);
        }
    }

    private void save() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showError("프로젝트 이름을 입력해주세요.");
            return;
        }
        if (name.contains("|") || name.contains(",")) {
            showError("프로젝트 이름에 | 또는 , 를 사용할 수 없습니다.");
            return;
        }
        boolean renamed = editing == null || !editing.getName().equals(name);
        if (renamed && projectManager.findProject(name) != null) {
            showError("이미 존재하는 프로젝트 이름입니다.");
            return;
        }

        LocalDate date;
        try {
            date = dateField.getDate();
        } catch (DateTimeException e) {
            showError("날짜 형식이 잘못되었습니다. (예: 2026-12-01)");
            return;
        }
        if (date == null) {
            showError("마감일을 입력해주세요.");
            return;
        }
        LocalTime time;
        try {
            time = timeField.getTime();
        } catch (DateTimeException e) {
            showError("시간 형식이 잘못되었습니다. (시 0~23, 분 0~59)");
            return;
        }
        LocalDateTime deadline = LocalDateTime.of(date, time);

        boolean deadlineChanged = editing == null || !deadline.equals(editing.getDeadLine());
        if (deadlineChanged && date.isBefore(LocalDate.now())) {
            showError("오늘보다 이전 날짜는 마감일로 정할 수 없습니다.");
            return;
        }

        if (editing == null) {
            projectManager.addProject(new Project(name, deadline));
        } else {
            if (renamed) {
                // 이름을 바꾸면 연결된 할 일의 태그도 함께 바뀐다 (ProjectManager 기존 로직)
                projectManager.updateProjectName(editing.getName(), name);
            }
            if (deadlineChanged) {
                projectManager.updateDeadline(name, deadline);
            }
        }
        dispose();
        if (onSaved != null) onSaved.run();
    }
}
