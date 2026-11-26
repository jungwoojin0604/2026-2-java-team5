package Gui;

import manager.ProjectManager;
import manager.TodoManager;
import model.Project;
import model.Todo;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 날짜별 할 일의 조회 및 관리를 위한 오른쪽 패널 (Figma Calendar 탭 panel2)
 *
 * <p>상단 바: 선택한 날짜(2027.11.23) 또는 Anytime, + 버튼(할 일 추가), 검색칸</p>
 *
 * <p>목록: 프로젝트 없는 할 일을 먼저 보여 주고, 그 아래 프로젝트별로 묶어 표시.
 * 프로젝트 이름 오른쪽 화살표로 접고 펼칠 수 있음</p>
 *
 * <p>선택한 날짜에 할 일이 없으면 마감 전 프로젝트와 D-Day 표시</p>
 *
 * <p>데이터 변경 시 콜백을 통해 외부 화면(달력 점, Project 탭)과 연동 가능</p>
 */
public class TodoPanel extends JPanel {
    // 기존 데이터 관리 객체 공유
    private final TodoManager todoManager;
    private final ProjectManager projectManager;
    // 현재 선택된 날짜
    private LocalDate currentDate = LocalDate.now();
    // 상단 바와 할 일 목록 영역
    private final RightHeader header;
    private final JPanel todoListPanel;
    // Anytime 목록 조회 여부
    private boolean anytimeMode = false;
    // 프로젝트별 펼치기 상태 저장
    private final Map<String, Boolean> expandedStates = new LinkedHashMap<>();
    // 다른 화면의 데이터 갱신을 위한 콜백
    private Runnable onDataChanged = () -> {
    };

    /**
     * 날짜별 할 일 조회 및 관리 패널 생성
     *
     * @param todoManager    할 일 관리 객체
     * @param projectManager 프로젝트 관리 객체
     */
    public TodoPanel(TodoManager todoManager, ProjectManager projectManager) {
        this.todoManager = todoManager;
        this.projectManager = projectManager;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 1. 상단 바
        header = new RightHeader();
        header.getAddButton().addActionListener(e -> TodoDialog.showAddDialog(
                header.getAddButton(),
                todoManager,
                projectManager,
                anytimeMode ? null : currentDate,
                null,
                this::notifyDataChanged
        ));
        // 검색칸: 글자를 입력할 때마다 바로 걸러서 표시
        header.getSearchBox().getField().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshTodoList();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshTodoList();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshTodoList();
            }
        });
        add(header, BorderLayout.NORTH);

        // 2. 할 일 목록 영역 (스크롤 가능)
        todoListPanel = new JPanel();
        todoListPanel.setLayout(new BoxLayout(todoListPanel, BoxLayout.Y_AXIS));
        todoListPanel.setBackground(Color.WHITE);
        todoListPanel.setBorder(BorderFactory.createEmptyBorder(6, 0, 20, 0));

        JScrollPane scrollPane = new JScrollPane(todoListPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        refreshTodoList();
    }

    /**
     * 현재 조회 모드 및 검색 조건에 따른 할 일 목록 갱신
     *
     * <p>선택된 날짜 또는 Anytime 목록을 기준으로 조회하고
     * 검색어가 있으면 그 안에서 내용에 검색어가 들어간 할 일만 표시</p>
     */
    public void refreshTodoList() {
        updateHeaderTitle();
        todoListPanel.removeAll();
        String keyword = header.getSearchBox().getText();
        List<Todo> todos;

        // 현재 조회 모드에 따른 할 일 조회
        if (keyword.isEmpty()) {
            todos = anytimeMode
                    ? todoManager.getAnyTimeTodo()
                    : todoManager.getTodo(currentDate);
        } else {
            todos = new ArrayList<>(todoManager.getTodoByKeyword(keyword));
            todos.removeIf(todo -> anytimeMode
                    ? todo.getDate() != null
                    : !currentDate.equals(todo.getDate()));
        }

        if (todos.isEmpty()) {
            if (!keyword.isEmpty()) {
                todoListPanel.add(message("검색 결과가 없습니다."));
            } else if (anytimeMode) {
                todoListPanel.add(message("등록된 Anytime 할 일이 없습니다."));
            } else {
                showRemainingProjects();
            }
        } else {
            Map<String, List<Todo>> groups = groupTodos(todos);
            // 프로젝트 없는 할 일은 제목 없이 맨 위에
            List<Todo> noProject = groups.remove("");
            if (noProject != null) {
                for (Todo todo : noProject) {
                    todoListPanel.add(createItem(todo));
                }
            }
            for (Map.Entry<String, List<Todo>> entry : groups.entrySet()) {
                todoListPanel.add(createGroupPanel(entry.getKey(), entry.getValue()));
            }
        }
        todoListPanel.add(Box.createVerticalGlue());
        todoListPanel.revalidate();
        todoListPanel.repaint();
    }

    /**
     * 현재 선택된 날짜 변경 및 할 일 목록 갱신 (Anytime 조회는 해제)
     *
     * @param date 변경할 날짜
     */
    public void setCurrentDate(LocalDate date) {
        this.currentDate = date;
        anytimeMode = false;
        refreshTodoList();
    }

    /**
     * Anytime(날짜 없는 할 일) 목록 보기 켜기·끄기
     *
     * @param anytime true면 Anytime 목록
     */
    public void setAnytimeMode(boolean anytime) {
        this.anytimeMode = anytime;
        refreshTodoList();
    }

    public boolean isAnytimeMode() {
        return anytimeMode;
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    /** 상단 바 제목: 날짜(오늘이면 "오늘" 표시) 또는 Anytime */
    private void updateHeaderTitle() {
        if (anytimeMode) {
            header.setTitle("Anytime", "날짜 없는 할 일");
        } else {
            header.setTitle(currentDate.format(Theme.DOT_DATE),
                    currentDate.equals(LocalDate.now()) ? "오늘" : "");
        }
    }

    /**
     * 할 일 목록을 연결된 프로젝트별로 분류
     *
     * <p>프로젝트 태그가 없는 할 일은 빈 문자열을 키로 사용하고
     * 여러 프로젝트에 연결된 할 일은 각 프로젝트 그룹에 포함</p>
     *
     * @param todos 프로젝트별로 분류할 할 일 목록
     * @return 프로젝트 이름을 키로 하고 할 일 목록을 값으로 하는 Map (생성 순서 유지)
     */
    private Map<String, List<Todo>> groupTodos(List<Todo> todos) {
        Map<String, List<Todo>> groups = new LinkedHashMap<>();
        for (Todo todo : todos) {
            if (todo.getProjectTags().isEmpty()) {
                groups.computeIfAbsent("", key -> new ArrayList<>()).add(todo);
            } else {
                for (String tag : todo.getProjectTags()) {
                    groups.computeIfAbsent(tag, key -> new ArrayList<>()).add(todo);
                }
            }
        }
        return groups;
    }

    /** 할 일 한 줄 */
    private TodoItemPanel createItem(Todo todo) {
        return new TodoItemPanel(todo, todoManager, projectManager,
                null, false, this::notifyDataChanged);
    }

    /**
     * 프로젝트별 할 일 묶음 (Figma Project01: 굵은 이름 24pt + 오른쪽 접기 화살표)
     *
     * @param name  프로젝트 이름
     * @param todos 해당 그룹에 포함된 할 일 목록
     * @return 프로젝트별 할 일 그룹 패널
     */
    private JPanel createGroupPanel(String name, List<Todo> todos) {
        JPanel groupPanel = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        groupPanel.setOpaque(false);
        groupPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 기존 펼치기 상태 조회 (기본값은 펼침)
        boolean initiallyExpanded = expandedStates.getOrDefault(name, true);

        // 머리줄: 이름 + 회색 D-Day ........ 화살표
        JLabel nameLabel = UiKit.label(name, Theme.bold(24), Color.BLACK);
        JLabel dDayLabel = UiKit.label(getProjectDDay(name), Theme.plain(16), Theme.TEXT_GRAY);
        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
        namePanel.setOpaque(false);
        namePanel.add(nameLabel);
        namePanel.add(Box.createHorizontalStrut(10));
        namePanel.add(dDayLabel);

        JLabel arrow = new JLabel(Icons.chevron(initiallyExpanded));
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(8, 19, 4, 20));
        headerPanel.add(namePanel, BorderLayout.WEST);
        headerPanel.add(arrow, BorderLayout.EAST);

        // 그룹에 연결된 할 일 목록
        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setOpaque(false);
        for (Todo todo : todos) {
            itemsPanel.add(createItem(todo));
        }
        itemsPanel.setVisible(initiallyExpanded);

        // 머리줄 아무 곳이나 누르면 접기·펼치기
        UiKit.onClick(headerPanel, () -> {
            boolean expanded = !itemsPanel.isVisible();
            expandedStates.put(name, expanded);
            itemsPanel.setVisible(expanded);
            arrow.setIcon(Icons.chevron(expanded));
            groupPanel.revalidate();
            groupPanel.repaint();
        });

        groupPanel.add(headerPanel, BorderLayout.NORTH);
        groupPanel.add(itemsPanel, BorderLayout.CENTER);
        return groupPanel;
    }

    /**
     * 데이터 변경 시 외부 화면을 갱신할 콜백 설정
     *
     * @param onDataChanged 데이터 변경 시 실행할 콜백
     */
    public void setOnDataChanged(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
    }

    /** 할 일 데이터 변경에 따른 화면 갱신 + 외부 콜백 실행 */
    private void notifyDataChanged() {
        refreshTodoList();
        onDataChanged.run();
    }

    /**
     * 프로젝트 마감일까지의 D-Day
     *
     * @param name 조회할 프로젝트 이름
     * @return D-Day 문자열 (프로젝트가 없으면 빈 문자열, 마감일이 없으면 미설정)
     */
    private String getProjectDDay(String name) {
        Project project = projectManager.findProject(name);
        if (project == null) {
            return "";
        }
        if (project.getDeadLine() == null) {
            return "미설정";
        }
        return Theme.dDay(project.getDeadLine().toLocalDate());
    }

    /**
     * 선택된 날짜에 할 일이 없는 경우 마감 전 프로젝트 목록 표시
     *
     * <p>마감일이 없거나 이미 지난 프로젝트는 제외</p>
     */
    private void showRemainingProjects() {
        todoListPanel.add(message("이 날짜에 등록된 할 일이 없습니다."));
        boolean hasProject = false;
        for (Project project : projectManager.getAllProjects()) {
            // 마감일이 없거나 이미 마감된 프로젝트는 제외
            if (project.getDeadLine() == null
                    || project.getDeadLine().toLocalDate().isBefore(LocalDate.now())) {
                continue;
            }
            if (!hasProject) {
                JLabel title = UiKit.label("진행 중인 프로젝트", Theme.bold(16), Color.BLACK);
                title.setBorder(BorderFactory.createEmptyBorder(18, 19, 6, 0));
                title.setAlignmentX(Component.LEFT_ALIGNMENT);
                todoListPanel.add(title);
                hasProject = true;
            }
            JPanel row = new JPanel();
            row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
            row.setOpaque(false);
            row.setBorder(BorderFactory.createEmptyBorder(4, 29, 4, 0));
            row.add(UiKit.label(project.getName(), Theme.plain(18), Color.BLACK));
            row.add(Box.createHorizontalStrut(11));
            row.add(UiKit.label(Theme.dDay(project.getDeadLine().toLocalDate()),
                    Theme.plain(15), Theme.TEXT_DATE));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
            todoListPanel.add(row);
        }
    }

    /** 회색 안내 문구 한 줄 */
    private static JLabel message(String text) {
        JLabel label = UiKit.label(text, Theme.plain(16), Theme.TEXT_GRAY);
        label.setBorder(BorderFactory.createEmptyBorder(10, 29, 0, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
}
