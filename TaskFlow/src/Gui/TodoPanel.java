package Gui;

import manager.ProjectManager;
import manager.TodoManager;
import model.Project;
import model.Todo;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TodoPanel extends JPanel {
    // 기존 데이터 관리 객체 공유
    private final TodoManager todoManager;
    private final ProjectManager projectManager;
    // 현재 선택된 날짜
    private LocalDate currentDate = LocalDate.now();
    // 할 일 목록 및 날짜 표시 영역
    private JPanel todoListPanel;
    private JLabel dateLabel;
    // 할 일 검색 입력창
    private JTextField searchField;
    // Anytime 목록 조회 여부
    private boolean anytimeMode = false;
    // 프로젝트별 펼치기 상태 저장
    private final Map<String, Boolean> expandedStates = new LinkedHashMap<>();
    // 화면 전환 버튼
    private JToggleButton anytimeButton;
    // 다른 화면의 데이터 갱신을 위한 콜백
    private Runnable onDataChanged = () -> {};

    public TodoPanel(TodoManager todoManager, ProjectManager projectManager) {
        this.todoManager = todoManager;
        this.projectManager = projectManager;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 1. 상단 날짜 표시 영역
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        dateLabel = new JLabel(currentDate.toString());
        dateLabel.setFont(dateLabel.getFont().deriveFont(Font.BOLD, 18f));

        // 할 일 검색 입력창과 버튼
        searchField = new JTextField(12);
        JButton searchButton = new JButton("검색");

        // 검색 영역 생성
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // 할 일 추가 버튼
        JButton addTodoButton = new JButton("+");

        addTodoButton.addActionListener(e -> TodoDialog.showAddDialog(
                        SwingUtilities.getWindowAncestor(this),
                        todoManager,
                        projectManager,
                        anytimeMode ? null : currentDate,
                        null,
                        this::notifyDataChanged
                )
        );

        searchPanel.add(addTodoButton);

        // Anytime 목록 전환 버튼
        anytimeButton = new JToggleButton("Anytime");
        anytimeButton.addActionListener(e -> {
            anytimeMode = anytimeButton.isSelected();
            dateLabel.setText(
                    anytimeMode ? "Anytime" : currentDate.toString()
            );
            refreshTodoList();
        });
        searchPanel.add(anytimeButton);

        // 검색 버튼 및 Enter 입력 이벤트
        searchButton.addActionListener(e -> refreshTodoList());
        searchField.addActionListener(e -> refreshTodoList());

        // 상단 영역 배치
        headerPanel.add(dateLabel, BorderLayout.WEST);
        headerPanel.add(searchPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. 할 일 목록 영역
        todoListPanel = new JPanel();
        todoListPanel.setLayout(new BoxLayout(todoListPanel, BoxLayout.Y_AXIS));
        todoListPanel.setBackground(Color.WHITE);

        todoListPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 10, 15));

        // 3. 스크롤 기능
        JScrollPane scrollPane = new JScrollPane(todoListPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // 4. 할 일 목록 표시
        refreshTodoList();
    }

    // 현재 날짜의 할 일을 프로젝트별로 표시
    public void refreshTodoList() {
        todoListPanel.removeAll();
        String keyword = searchField.getText().trim();
        List<Todo> todos;

        // 현재 조회 모드에 따른 할 일 조회
        if (keyword.isEmpty()) {
            todos = anytimeMode
                    ? todoManager.getAnyTimeTodo()
                    : todoManager.getTodo(currentDate);
        } else {
            todos = new ArrayList<>(todoManager.getTodoByKeyword(keyword));
            todos.removeIf(todo ->
                    anytimeMode
                            ? todo.getDate() != null
                            : !currentDate.equals(todo.getDate())
            );
        }

        if (todos.isEmpty()) {
            if (!keyword.isEmpty()) {
                todoListPanel.add(
                        new JLabel("검색 결과가 없습니다.")
                );
            } else if (anytimeMode) {
                todoListPanel.add(
                        new JLabel("등록된 Anytime 할 일이 없습니다.")
                );
            } else {
                showRemainingProjects();
            }
        } else {
            Map<String, List<Todo>> groups = groupTodos(todos);
            for (String name : groups.keySet()) {
                JPanel groupPanel = createGroupPanel(name, groups.get(name));
                todoListPanel.add(groupPanel);
            }
        }
        todoListPanel.revalidate();
        todoListPanel.repaint();
    }

    // 선택된 날짜 변경 및 날짜별 목록 표시
    public void setCurrentDate(LocalDate date) {
        this.currentDate = date;
        anytimeMode = false;
        anytimeButton.setSelected(false);
        dateLabel.setText(date.toString());
        refreshTodoList();
    }

    // 할 일을 프로젝트별로 분류
    private Map<String, List<Todo>> groupTodos(List<Todo> todos) {
        Map<String, List<Todo>> groups = new LinkedHashMap<>();
        for (Todo todo : todos) {
            if (todo.getProjectTags().isEmpty()) {
                groups.computeIfAbsent(
                        "", key -> new ArrayList<>()
                ).add(todo);
            } else {
                for (String tag : todo.getProjectTags()) {
                    groups.computeIfAbsent(
                            tag, key -> new ArrayList<>()
                    ).add(todo);
                }
            }
        }
        return groups;
    }

    // 프로젝트별 할 일 그룹 생성
    private JPanel createGroupPanel(String name, List<Todo> todos) {
        JPanel groupPanel = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(
                        Integer.MAX_VALUE,
                        getPreferredSize().height
                );
            }
        };
        groupPanel.setBackground(Color.WHITE);

        groupPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0, 0, 1, 0, new Color(230, 230, 230)
                        ),
                        BorderFactory.createEmptyBorder(5, 0, 8, 0)
                )
        );

        // 기존 펼치기 상태 조회 (기본값은 펼침)
        boolean initiallyExpanded = expandedStates.getOrDefault(name, true);
        JButton expandButton = new JButton(initiallyExpanded ? "▼" : "▶");

        expandButton.setMargin(new Insets(2, 6, 2, 6));

        JLabel nameLabel = new JLabel((name.isEmpty() ? "프로젝트 미지정" : name)+ " (" + todos.size() + ")");

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(Color.WHITE);

        headerPanel.add(expandButton);
        headerPanel.add(nameLabel);

        // 프로젝트 마감일에 따른 D-Day 표시
        if (!name.isEmpty()) {
            JLabel dDayLabel = new JLabel(getProjectDDay(name));
            dDayLabel.setForeground(Color.GRAY);
            headerPanel.add(dDayLabel);
        }

        // 그룹에 연결된 할 일 목록
        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBackground(Color.WHITE);

        for (Todo todo : todos) {
            TodoItemPanel item = new TodoItemPanel(
                    todo,
                    todoManager,
                    projectManager,
                    null,
                    false,
                    this::notifyDataChanged
            );
            itemsPanel.add(item);
        }
        itemsPanel.setVisible(initiallyExpanded);

        // 펼치기 및 접기 이벤트
        expandButton.addActionListener(e -> {
            boolean expanded = !itemsPanel.isVisible();
            // 변경된 상태 저장
            expandedStates.put(name, expanded);
            itemsPanel.setVisible(expanded);
            expandButton.setText(expanded ? "▼" : "▶");
            groupPanel.revalidate();
            groupPanel.repaint();
        });
        groupPanel.add(headerPanel, BorderLayout.NORTH);
        groupPanel.add(itemsPanel, BorderLayout.CENTER);
        groupPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return groupPanel;
    }

    // 다른 화면을 갱신할 콜백 설정
    public void setOnDataChanged(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
    }

    // 할 일 변경 시 현재 화면과 연결된 화면 갱신
    private void notifyDataChanged() {
        refreshTodoList();
        onDataChanged.run();
    }

    /**
     * 실제 오늘 날짜를 기준으로 프로젝트 마감일까지
     * 남은 날짜를 계산한다.
     *
     * @param name 조회할 프로젝트 이름
     * @return D-Day 표시 문자열
     */
    private String getProjectDDay(String name) {
        Project project = projectManager.findProject(name);
        if (project == null) {
            return "";
        }

        if (project.getDeadLine() == null) {
            return "미설정";
        }

        long days = ChronoUnit.DAYS.between(
                LocalDate.now(),
                project.getDeadLine().toLocalDate()
        );

        if (days > 0) {
            return "D-" + days;
        } else if (days == 0) {
            return "D-Day";
        } else {
            return "D+" + (-days);
        }
    }

    /**
     * 선택한 날짜에 할 일이 없는 경우
     * 마감되지 않은 프로젝트 목록을 표시한다.
     *
     * <p>남은 프로젝트도 없으면 안내 문구를 표시한다.</p>
     */
    private void showRemainingProjects() {
        boolean hasProject = false;
        for (Project project : projectManager.getAllProjects()) {
            // 마감일이 없거나 이미 마감된 프로젝트는 제외
            if (project.getDeadLine() == null || project.getDeadLine().toLocalDate().isBefore(LocalDate.now())) {
                continue;
            }

            // 펼치기 버튼 없이 프로젝트 이름과 D-Day 표시
            JPanel projectPanel = new JPanel(
                    new FlowLayout(FlowLayout.LEFT)
            );
            projectPanel.setBackground(Color.WHITE);

            JLabel nameLabel = new JLabel(project.getName());
            JLabel dDayLabel = new JLabel(getProjectDDay(project.getName()));
            dDayLabel.setForeground(Color.GRAY);

            projectPanel.add(nameLabel);
            projectPanel.add(dDayLabel);

            projectPanel.setMaximumSize(
                    new Dimension(
                            Integer.MAX_VALUE,
                            projectPanel.getPreferredSize().height
                    )
            );
            projectPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            todoListPanel.add(projectPanel);
            hasProject = true;
        }
        if (!hasProject) {
            todoListPanel.add(new JLabel("표시할 일정이 없습니다."));
        }
    }
}