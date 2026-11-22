package gui;

import manager.ProjectManager;
import manager.TodoManager;
import model.Project;
import model.Todo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * "할 일 추가" / "할 일 수정" 떠 있는 창 (TodoFormDialog)
 *
 * <p>Calendar 탭, Projects 탭, 수정 메뉴가 모두 이 창 하나를 쓰고,
 * 어디서 열었는지에 따라 기본값만 다르다. (docx "기타 버튼, 데모 구현 방향")</p>
 * <ul>
 *   <li>{@link #forCalendar}: 선택한 날짜가 미리 들어가고, 프로젝트는 고르지 않아도 된다.</li>
 *   <li>{@link #forProject}: 현재 프로젝트가 체크된 채 잠기고, 다른 프로젝트는 더 고를 수 있다.</li>
 *   <li>{@link #forEdit}: 수정 전 내용이 채워진 채로 열린다.</li>
 * </ul>
 *
 * <p>내용이 비었거나 날짜·시간 형식이 잘못되면 등록하지 않고 창 아래에 안내 문구를 띄운다.
 * X 버튼(또는 Esc)은 저장 없이 닫고, "추가" 버튼은 할 일을 반영한 뒤 창을 닫는다.</p>
 */
public class TodoFormDialog extends FloatingDialog {
    private final TodoManager todoManager;
    private final ProjectManager projectManager;
    private final Todo editing;          // 수정 중인 할 일 (추가면 null)
    private final String lockedProject;  // 현재 프로젝트 이름 (없으면 null)
    private final Runnable onSaved;

    private final JTextField contentField;
    private final CircleCheck anytimeCheck;
    private final DateField dateField;
    private final JPanel dateArea;
    private final CircleCheck timeCheck;
    private final TimeField timeField;
    private final Map<String, CircleCheck> projectChecks = new LinkedHashMap<>();
    private final List<String> hiddenTags = new ArrayList<>(); // 목록에 없는 프로젝트 태그 (수정 시 보존)
    private final JLabel errorLabel = FormKit.errorLabel();

    /** Calendar 탭 오른쪽 + 버튼: 선택한 날짜가 미리 지정된 창 */
    public static TodoFormDialog forCalendar(Window owner, TodoManager tm, ProjectManager pm,
                                             LocalDate selectedDate, Runnable onSaved) {
        return new TodoFormDialog(owner, tm, pm, null, null,
                selectedDate == null ? LocalDate.now() : selectedDate, onSaved);
    }

    /** Projects 탭 "+ 할 일 추가": 현재 프로젝트가 잠긴 채 체크된 창 */
    public static TodoFormDialog forProject(Window owner, TodoManager tm, ProjectManager pm,
                                            Project current, Runnable onSaved) {
        return new TodoFormDialog(owner, tm, pm, null, current.getName(), LocalDate.now(), onSaved);
    }

    /** 할 일 수정: 수정 전 내용이 채워진 창 */
    public static TodoFormDialog forEdit(Window owner, TodoManager tm, ProjectManager pm,
                                         Todo todo, Runnable onSaved) {
        return new TodoFormDialog(owner, tm, pm, todo, null, todo.getDate(), onSaved);
    }

    private TodoFormDialog(Window owner, TodoManager tm, ProjectManager pm, Todo editing,
                           String lockedProject, LocalDate initialDate, Runnable onSaved) {
        super(owner);
        this.todoManager = tm;
        this.projectManager = pm;
        this.editing = editing;
        this.lockedProject = lockedProject;
        this.onSaved = onSaved;
        boolean edit = editing != null;

        JPanel body = FormKit.column();
        body.setBorder(new EmptyBorder(16, 20, 16, 20));

        // 제목
        body.add(FormKit.header(edit ? "할 일 수정" : "할 일 추가", this::dispose));
        body.add(FormKit.gap(14));

        // 할 일 내용
        body.add(FormKit.fieldLabel("할 일 내용 *"));
        contentField = FormKit.textField(edit ? editing.getContent() : "");
        body.add(contentField);
        body.add(FormKit.gap(16));

        // 일정 설정 + Anytime
        boolean anytime = edit && editing.getDate() == null;
        anytimeCheck = new CircleCheck(CircleCheck.Style.CHECK, 20, anytime);
        body.add(FormKit.sectionRow("일정 설정", FormKit.checkRow(anytimeCheck, "Anytime", GuiTheme.TEXT)));
        body.add(FormKit.gap(8));

        dateField = new DateField(initialDate == null ? LocalDate.now() : initialDate);
        JLabel anytimeNote = new JLabel("날짜가 지정되지 않은 할 일로 등록합니다.");
        anytimeNote.setFont(GuiTheme.regular(14));
        anytimeNote.setForeground(GuiTheme.TEXT_DONE);
        anytimeNote.setBorder(new EmptyBorder(8, 4, 8, 0));
        dateArea = new JPanel(new CardLayout());
        dateArea.setOpaque(false);
        dateArea.add(dateField, "date");
        dateArea.add(anytimeNote, "anytime");
        body.add(FormKit.fullWidth(dateArea));
        anytimeCheck.addChangeListener(on -> showDateCard());
        showDateCard();
        body.add(FormKit.gap(16));

        // 시간 지정 + 사용
        LocalTime initialTime = edit ? editing.getTime() : null;
        timeCheck = new CircleCheck(CircleCheck.Style.CHECK, 20, initialTime != null);
        body.add(FormKit.sectionRow("시간 지정", FormKit.checkRow(timeCheck, "사용", GuiTheme.TEXT)));
        timeField = new TimeField(initialTime);
        JPanel timeWrap = new JPanel(new BorderLayout());
        timeWrap.setOpaque(false);
        timeWrap.setBorder(new EmptyBorder(8, 0, 0, 0));
        timeWrap.add(timeField, BorderLayout.WEST);
        body.add(FormKit.fullWidth(timeWrap));
        timeWrap.setVisible(timeCheck.isSelected());
        timeCheck.addChangeListener(on -> {
            timeWrap.setVisible(on);
            repack();
        });
        body.add(FormKit.gap(16));
        body.add(FormKit.divider());
        body.add(FormKit.gap(14));

        // 연결된 프로젝트
        body.add(FormKit.sectionRow("연결된 프로젝트", null));
        body.add(FormKit.gap(8));
        body.add(buildProjectList());
        body.add(FormKit.gap(10));

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
                contentField.requestFocusInWindow();
            }
        });
    }

    private JComponent buildProjectList() {
        JPanel list = FormKit.column();
        List<String> tags = editing == null ? new ArrayList<>() : new ArrayList<>(editing.getProjectTags());

        if (projectManager != null) {
            for (Project p : projectManager.getAllProjects()) {
                String name = p.getName();
                boolean isCurrent = name.equals(lockedProject);
                CircleCheck c = new CircleCheck(CircleCheck.Style.CHECK, 20, isCurrent || tags.contains(name));
                c.setLocked(isCurrent);
                projectChecks.put(name, c);
                JPanel row = FormKit.checkRow(c, isCurrent ? name + " (현재 프로젝트)" : name,
                        isCurrent ? GuiTheme.TEXT_DONE : GuiTheme.TEXT);
                row.setBorder(new EmptyBorder(3, 0, 3, 0));
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                list.add(row);
                tags.remove(name);
            }
        }
        hiddenTags.addAll(tags);

        if (projectChecks.isEmpty()) {
            JLabel none = new JLabel("등록된 프로젝트가 없습니다.");
            none.setFont(GuiTheme.regular(14));
            none.setForeground(GuiTheme.TEXT_DONE);
            list.add(none);
        }

        // 프로젝트가 많으면 5줄 높이까지만 보이고 스크롤
        if (projectChecks.size() > 5) {
            JScrollPane sp = new JScrollPane(list);
            sp.setBorder(null);
            sp.setOpaque(false);
            sp.getViewport().setOpaque(false);
            sp.getVerticalScrollBar().setUnitIncrement(14);
            sp.setPreferredSize(new Dimension(FormKit.WIDTH, 5 * 28));
            return FormKit.fullWidth(sp);
        }
        return FormKit.fullWidth(list);
    }

    private void showDateCard() {
        ((CardLayout) dateArea.getLayout()).show(dateArea, anytimeCheck.isSelected() ? "anytime" : "date");
    }

    private void repack() {
        if (isShowing()) {
            Point loc = getLocation();
            pack();
            setLocation(loc);
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        repack();
    }

    /** 입력을 검사하고, 통과하면 할 일을 추가(또는 수정)하고 창을 닫는다. */
    private void save() {
        String content = contentField.getText().trim();
        if (content.isEmpty()) {
            showError("할 일 내용을 입력해주세요.");
            contentField.requestFocusInWindow();
            return;
        }
        // 저장 파일 구분 문자 (콘솔 버전과 같은 규칙)
        if (content.contains("|")) {
            showError("'|' 문자는 사용할 수 없습니다.");
            contentField.requestFocusInWindow();
            return;
        }

        LocalDate date = null;
        if (!anytimeCheck.isSelected()) {
            try {
                date = dateField.getDate();
            } catch (DateTimeException e) {
                showError("날짜 형식이 잘못되었습니다. (예: 2026-12-01)");
                return;
            }
            if (date == null) {
                showError("날짜를 입력하거나 Anytime을 선택해주세요.");
                return;
            }
        }

        LocalTime time = null;
        if (timeCheck.isSelected()) {
            try {
                time = timeField.getTime();
            } catch (DateTimeException e) {
                showError("시간 형식이 잘못되었습니다. (시 0~23, 분 0~59)");
                return;
            }
        }

        List<String> tags = new ArrayList<>();
        for (Map.Entry<String, CircleCheck> en : projectChecks.entrySet()) {
            if (en.getValue().isSelected()) tags.add(en.getKey());
        }
        tags.addAll(hiddenTags);

        if (editing == null) {
            todoManager.addTodo(content, date, time, tags, false);
        } else {
            editing.setContent(content);
            editing.setDate(date);
            editing.setTime(time);
            editing.setProjectTags(tags);
        }
        dispose();
        if (onSaved != null) onSaved.run();
    }
}
