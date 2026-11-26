package Gui;

import manager.ProjectManager;
import manager.TodoManager;
import model.Project;
import model.Todo;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 할 일 추가 및 수정을 위한 공통 입력 창 (Figma "Popover/할 일 추가")
 *
 * <p>할 일 내용, 날짜(Anytime), 시간, 연결된 프로젝트 입력 기능 제공</p>
 *
 * <p>+ 버튼 바로 아래에 말풍선처럼 뜨고, X 버튼이나 Esc로 저장하지 않고 닫음</p>
 *
 * <p>Calendar 및 Project 화면에서 공통으로 사용 가능하며
 * Project 화면에서 호출 시 해당 프로젝트가 잠긴 채 체크됨</p>
 *
 * <p>입력값의 유효성 검사 후 할 일 데이터를 저장하고
 * 콜백을 통해 외부 화면 갱신</p>
 */
public class TodoDialog {

    private static final int WIDTH = 380;

    /**
     * 새로운 할 일 등록을 위한 추가 창 표시
     *
     * @param anchor          창을 띄울 기준 (+ 버튼, 창을 넘기면 화면 가운데)
     * @param todoManager     할 일 관리 객체
     * @param projectManager  프로젝트 관리 객체
     * @param defaultDate     기본 선택 날짜 (null이면 Anytime)
     * @param requiredProject 필수 지정 프로젝트 (없으면 null)
     * @param onDataChanged   데이터 변경 시 실행할 콜백
     */
    public static void showAddDialog(
            Component anchor,
            TodoManager todoManager,
            ProjectManager projectManager,
            LocalDate defaultDate,
            String requiredProject,
            Runnable onDataChanged) {

        showDialog(anchor, todoManager, projectManager,
                defaultDate, requiredProject, null, onDataChanged);
    }

    /**
     * 기존 할 일의 정보 변경을 위한 수정 창 표시
     *
     * @param anchor          창을 띄울 기준 컴포넌트
     * @param todoManager     할 일 관리 객체
     * @param projectManager  프로젝트 관리 객체
     * @param editingTodo     수정할 할 일 객체
     * @param requiredProject 필수 지정 프로젝트 (없으면 null)
     * @param onDataChanged   데이터 변경 시 실행할 콜백
     */
    public static void showEditDialog(
            Component anchor,
            TodoManager todoManager,
            ProjectManager projectManager,
            Todo editingTodo,
            String requiredProject,
            Runnable onDataChanged) {

        showDialog(anchor, todoManager, projectManager,
                editingTodo.getDate(), requiredProject,
                editingTodo, onDataChanged);
    }

    /**
     * 할 일 추가 및 수정을 위한 공통 입력 창 생성
     *
     * <p>수정할 할 일 객체의 존재 여부에 따라 추가 모드와 수정 모드 구분</p>
     */
    private static void showDialog(
            Component anchor,
            TodoManager todoManager,
            ProjectManager projectManager,
            LocalDate defaultDate,
            String requiredProject,
            Todo editingTodo,
            Runnable onDataChanged) {

        boolean isEdit = editingTodo != null;

        // 창 전체 (흰 바탕, 검은 테두리, 안쪽 여백 18/20/20/20)
        JPanel form = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(WIDTH, super.getPreferredSize().height);
            }
        };
        form.setLayout(new javax.swing.BoxLayout(form, javax.swing.BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                UiKit.blackLine(), BorderFactory.createEmptyBorder(18, 20, 20, 20)));

        // 1. 제목과 X 버튼
        JButton closeButton = UiKit.iconButton(Icons.close());
        closeButton.setToolTipText("닫기 (Esc)");
        form.add(UiKit.spread(
                UiKit.label(isEdit ? "할 일 수정" : "할 일 추가", Theme.bold(20), Color.BLACK),
                closeButton));
        form.add(UiKit.gap(14));

        // 2. 할 일 내용
        form.add(leftAligned(UiKit.label("할 일 내용 *", Theme.plain(14), Theme.TEXT_LABEL)));
        form.add(UiKit.gap(6));
        JTextField contentField = UiKit.textInput();
        if (isEdit) {
            contentField.setText(editingTodo.getContent());
        }
        form.add(contentField);
        form.add(UiKit.gap(18));

        // 3. 일정 설정 (Anytime 선택 시 날짜칸 대신 안내 문구)
        UiKit.CheckToggle anytimeToggle = new UiKit.CheckToggle("Anytime", defaultDate == null);
        form.add(UiKit.spread(UiKit.label("일정 설정", Theme.bold(16), Color.BLACK), anytimeToggle));
        form.add(UiKit.gap(8));

        UiKit.DateInput dateInput = new UiKit.DateInput(defaultDate);
        JLabel anytimeNote = UiKit.label("날짜가 지정되지 않은 할 일로 등록합니다.",
                Theme.plain(14), Theme.TEXT_GRAY);
        anytimeNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(dateInput);
        form.add(anytimeNote);
        form.add(UiKit.gap(18));

        // 4. 시간 지정 (사용 선택 시 시·분 입력칸)
        boolean hasTime = isEdit && editingTodo.getTime() != null;
        LocalTime initialTime = hasTime ? editingTodo.getTime() : LocalTime.of(12, 0);
        UiKit.CheckToggle timeToggle = new UiKit.CheckToggle("사용", hasTime);
        form.add(UiKit.spread(UiKit.label("시간 지정", Theme.bold(16), Color.BLACK), timeToggle));

        UiKit.TimeSpinner hourSpinner = new UiKit.TimeSpinner(initialTime.getHour(), 23);
        UiKit.TimeSpinner minuteSpinner = new UiKit.TimeSpinner(initialTime.getMinute(), 59);
        JPanel timePanel = timeRow(hourSpinner, minuteSpinner);
        form.add(timePanel);
        form.add(UiKit.gap(18));

        // 5. 구분선과 연결된 프로젝트
        form.add(divider());
        form.add(UiKit.gap(16));
        form.add(leftAligned(UiKit.label("연결된 프로젝트", Theme.bold(16), Color.BLACK)));
        form.add(UiKit.gap(8));

        JPanel projectPanel = UiKit.column();
        Map<String, UiKit.CheckToggle> projectChecks = new LinkedHashMap<>();
        for (Project project : projectManager.getAllProjects()) {
            String name = project.getName();
            boolean required = name.equals(requiredProject);
            UiKit.CheckToggle check = new UiKit.CheckToggle(
                    required ? name + " (현재 프로젝트)" : name,
                    isEdit && editingTodo.getProjectTags().contains(name));
            // Project 화면에서 호출한 경우 필수 프로젝트 잠금
            check.setLocked(required);
            check.setAlignmentX(Component.LEFT_ALIGNMENT);
            if (!projectChecks.isEmpty()) {
                projectPanel.add(UiKit.gap(8));
            }
            projectPanel.add(check);
            projectChecks.put(name, check);
        }
        if (projectChecks.isEmpty()) {
            projectPanel.add(UiKit.label("등록된 프로젝트가 없습니다.", Theme.plain(14), Theme.TEXT_GRAY));
        }
        form.add(projectList(projectPanel, projectChecks.size()));
        form.add(UiKit.gap(14));

        // 6. 오류 문구와 추가 버튼
        JLabel errorLabel = UiKit.label(" ", Theme.plain(13), Theme.RED);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        Component errorGap = UiKit.gap(8);
        form.add(errorLabel);
        form.add(errorGap);

        JButton addButton = UiKit.primaryButton(isEdit ? "저장" : "추가");
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        footer.add(addButton);
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        form.add(footer);

        // 선택 상태에 맞춰 보일 칸 정리
        Runnable syncVisibility = () -> {
            boolean anytime = anytimeToggle.isSelected();
            dateInput.setVisible(!anytime);
            anytimeNote.setVisible(anytime);
            timePanel.setVisible(timeToggle.isSelected());
            boolean hasError = !errorLabel.getText().isBlank();
            errorLabel.setVisible(hasError);
            errorGap.setVisible(hasError);
        };
        syncVisibility.run();

        Popover popover = Popover.show(anchor, form);
        Runnable refresh = () -> {
            syncVisibility.run();
            form.revalidate();
            popover.relayout();
        };
        anytimeToggle.setOnChange(refresh);
        timeToggle.setOnChange(refresh);
        closeButton.addActionListener(e -> popover.close());

        // 추가 및 저장 버튼 클릭 시 데이터 저장
        Runnable submit = () -> {
            String error = null;
            String content = contentField.getText().trim();

            // 할 일 내용 유효성 검사
            if (content.isEmpty()) {
                error = "할 일 내용을 입력해주세요.";
            } else if (content.contains("|")) {
                error = "| 문자는 사용할 수 없습니다.";
            }

            // Anytime 여부에 따라 날짜 설정
            LocalDate date = anytimeToggle.isSelected() ? null : dateInput.getDate();

            // 시간 지정 여부에 따라 시간 설정
            LocalTime time = null;
            if (error == null && timeToggle.isSelected()) {
                int hour = hourSpinner.getValue();
                int minute = minuteSpinner.getValue();
                if (hour < 0 || minute < 0) {
                    error = "올바른 시간을 입력해주세요. (시 0~23, 분 0~59)";
                } else {
                    time = LocalTime.of(hour, minute);
                }
            }

            if (error != null) {
                errorLabel.setText(error);
                refresh.run();
                return;
            }

            // 선택된 프로젝트 태그 저장
            List<String> projectTags = new ArrayList<>();
            for (Map.Entry<String, UiKit.CheckToggle> entry : projectChecks.entrySet()) {
                if (entry.getValue().isSelected()) {
                    projectTags.add(entry.getKey());
                }
            }

            // 기존 프로젝트 목록에 없는 태그도 수정 시 유지
            if (isEdit) {
                for (String tag : editingTodo.getProjectTags()) {
                    if (!projectChecks.containsKey(tag)) {
                        projectTags.add(tag);
                    }
                }
                editingTodo.setContent(content);
                editingTodo.setDate(date);
                editingTodo.setTime(time);
                editingTodo.setProjectTags(projectTags);
            } else {
                todoManager.addTodo(content, date, time, projectTags, false);
            }

            // 창 닫고 화면 갱신
            popover.close();
            onDataChanged.run();
        };
        addButton.addActionListener(e -> submit.run());
        contentField.addActionListener(e -> submit.run());

        SwingUtilities.invokeLater(contentField::requestFocusInWindow);
    }

    /** 시 : 분 입력 줄 (위 여백 8) */
    static JPanel timeRow(UiKit.TimeSpinner hourSpinner, UiKit.TimeSpinner minuteSpinner) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(8, -6, 0, 0));
        row.add(hourSpinner);
        row.add(UiKit.label("시", Theme.plain(14), Theme.TEXT_LABEL));
        row.add(UiKit.label(":", Theme.bold(20), Color.BLACK));
        row.add(minuteSpinner);
        row.add(UiKit.label("분", Theme.plain(14), Theme.TEXT_LABEL));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        return row;
    }

    /** 1px 회색 구분선 */
    private static JComponent divider() {
        JPanel line = new JPanel();
        line.setBackground(Theme.DIVIDER);
        line.setPreferredSize(new Dimension(WIDTH - 40, 1));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        return line;
    }

    /** 왼쪽 정렬된 한 줄 */
    static JComponent leftAligned(JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        return component;
    }

    /** 프로젝트가 6개를 넘으면 스크롤되는 목록 */
    private static JComponent projectList(JPanel projectPanel, int count) {
        projectPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (count <= 6) {
            return projectPanel;
        }
        JScrollPane scroll = new JScrollPane(projectPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        scroll.setPreferredSize(new Dimension(WIDTH - 40, 6 * 28));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6 * 28));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        return scroll;
    }
}
