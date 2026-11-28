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

/**
 * 날짜별 할 일의 조회 및 관리를 위한 패널 클래스
 *
 * <p>현재 선택된 날짜를 기준으로 할 일을 조회하고
 * 연결된 프로젝트별로 분류하여 표시</p>
 *
 * <p>할 일 검색 및 추가, Anytime 목록 조회,
 * 프로젝트별 목록 펼치기 및 접기 기능 제공</p>
 *
 * <p>프로젝트별 D-Day를 표시하고 선택한 날짜에
 * 할 일이 없는 경우 마감되지 않은 프로젝트 목록 표시</p>
 *
 * <p>데이터 변경 시 콜백을 통해 외부 화면과 연동 가능</p>
 */
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

    // D-Day 목록 조회 여부
    private boolean dDayMode = false;

    // 프로젝트별 펼치기 상태 저장
    private final Map<String, Boolean> expandedStates = new LinkedHashMap<>();

    // 화면 전환 버튼
    private JToggleButton anytimeButton;
    private JToggleButton dDayButton;

    // 완료 상태별 조회 메뉴
    private JComboBox<String> statusComboBox;

    // 다른 화면의 데이터 갱신을 위한 콜백
    private Runnable onDataChanged = () -> {};

    /**
     * 날짜별 할 일 조회 및 관리 패널 생성
     *
     * <p>날짜 표시 영역, 검색 입력창, 할 일 추가 버튼,
     * Anytime 전환 버튼 및 스크롤 가능한 할 일 목록 배치</p>
     *
     * <p>현재 날짜를 기준으로 초기 할 일 목록 표시 및
     * 각 버튼과 검색 입력 이벤트 설정</p>
     *
     * @param todoManager 할 일 관리 객체
     * @param projectManager 프로젝트 관리 객체
     */
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
                anytimeMode ? null :
                        (dDayMode || statusComboBox.getSelectedIndex() != 0 ? LocalDate.now() : currentDate),
                        null,
                        this::notifyDataChanged
                )
        );

        searchPanel.add(addTodoButton);

        // Anytime 목록 전환 버튼
        anytimeButton = new JToggleButton("Anytime");
        anytimeButton.addActionListener(e -> {
            boolean selected = anytimeButton.isSelected();
            // 완료 상태별 조회 해제
            statusComboBox.setSelectedIndex(0);
            anytimeMode = selected;
            anytimeButton.setSelected(selected);
            // D-Day 조회 모드 해제
            dDayMode = false;
            dDayButton.setSelected(false);
            dateLabel.setText(anytimeMode ? "Anytime" : currentDate.toString());
            refreshTodoList();
        });
        searchPanel.add(anytimeButton);

        // D-Day 목록 전환 버튼
        dDayButton = new JToggleButton("D-Day");
        dDayButton.addActionListener(e -> {
            boolean selected = dDayButton.isSelected();
            // 완료 상태별 조회 해제
            statusComboBox.setSelectedIndex(0);
            dDayMode = selected;
            dDayButton.setSelected(selected);
            // Anytime 조회 모드 해제
            anytimeMode = false;
            anytimeButton.setSelected(false);
            dateLabel.setText(dDayMode ? "D-Day" : currentDate.toString());
            refreshTodoList();
        });
        searchPanel.add(dDayButton);

        // 완료 상태별 조회 메뉴
        String[] statusOptions = {"날짜별 조회", "완료 할 일 조회", "미완료 할 일 조회"};
        statusComboBox = new JComboBox<>(statusOptions);
        statusComboBox.addActionListener(e -> {
            // 완료 상태별 조회 시 다른 조회 모드 해제
            anytimeMode = false;
            dDayMode = false;
            anytimeButton.setSelected(false);
            dDayButton.setSelected(false);
            int selectedIndex = statusComboBox.getSelectedIndex();
            if (selectedIndex == 1) {
                dateLabel.setText("완료된 할 일");
            } else if (selectedIndex == 2) {
                dateLabel.setText("미완료 할 일");
            } else {
                dateLabel.setText(currentDate.toString());
            }
            refreshTodoList();
        });
        searchPanel.add(statusComboBox);

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

    /**
     * 현재 조회 모드 및 검색 조건에 따른 할 일 목록 갱신
     *
     * <p>선택된 날짜, Anytime 또는 D-Day 목록을 기준으로 할 일 조회</p>
     *
     * <p>검색어가 입력된 경우 해당 검색 조건에 맞는 할 일만 표시</p>
     *
     * <p>D-Day 목록은 날짜 오름차순으로 표시하고
     * 날짜별 및 Anytime 목록은 프로젝트별로 분류하여 표시</p>
     */
    public void refreshTodoList() {
        todoListPanel.removeAll();
        String keyword = searchField.getText().trim();
        List<Todo> todos;
        // 현재 선택된 완료 상태 조회
        int statusIndex = statusComboBox.getSelectedIndex();
        // 현재 조회 모드에 따른 할 일 조회
        if (statusIndex == 1) {
            todos = todoManager.getTodoByStatus(true);
        } else if (statusIndex == 2) {
            todos = todoManager.getTodoByStatus(false);
        } else if (dDayMode) {
            todos = todoManager.getDDayTodo();
        } else if (anytimeMode) {
            todos = todoManager.getAnyTimeTodo();
        } else {
            todos = todoManager.getTodo(currentDate);
        }
        // 검색 조건 적용
        if (!keyword.isEmpty()) {
            todos = new ArrayList<>(todos);
            todos.retainAll(todoManager.getTodoByKeyword(keyword));
        }
        // 조회 결과가 없는 경우
        if (todos.isEmpty()) {
            if (!keyword.isEmpty()) {
                todoListPanel.add(
                        new JLabel("검색 결과가 없습니다.")
                );
            } else if (statusIndex == 1) {
                todoListPanel.add(
                        new JLabel("완료된 할 일이 없습니다.")
                );
            } else if (statusIndex == 2) {
                todoListPanel.add(
                        new JLabel("미완료 할 일이 없습니다.")
                );
            } else if (dDayMode) {
                todoListPanel.add(
                        new JLabel("예정된 미완료 할 일이 없습니다.")
                );
            } else if (anytimeMode) {
                todoListPanel.add(
                        new JLabel("등록된 Anytime 할 일이 없습니다.")
                );
            } else {
                showRemainingProjects();
            }
            // D-Day 및 완료 상태별 목록 표시
        } else if (dDayMode || statusIndex != 0) {
            for (Todo todo : todos) {
                TodoItemPanel item = new TodoItemPanel(
                        todo,
                        todoManager,
                        projectManager,
                        null,
                        true,
                        this::notifyDataChanged
                );
                todoListPanel.add(item);
            }
            // 기존 프로젝트별 목록 표시
        } else {
            Map<String, List<Todo>> groups = groupTodos(todos);
            for (String name : groups.keySet()) {
                JPanel groupPanel = createGroupPanel(
                        name, groups.get(name)
                );
                todoListPanel.add(groupPanel);
            }
        }
        todoListPanel.revalidate();
        todoListPanel.repaint();
    }

    /**
     * 현재 선택된 날짜 변경 및 할 일 목록 갱신
     *
     * <p>Anytime, D-Day 및 완료 상태별 조회 모드를 해제하고
     * 변경된 날짜를 기준으로 할 일 목록 표시</p>
     *
     * @param date 변경할 날짜
     */
    public void setCurrentDate(LocalDate date) {
        this.currentDate = date;
        // 조회 모드 초기화
        anytimeMode = false;
        dDayMode = false;
        anytimeButton.setSelected(false);
        dDayButton.setSelected(false);
        statusComboBox.setSelectedIndex(0);
        dateLabel.setText(date.toString());
        refreshTodoList();
    }

    /**
     * 할 일 목록을 연결된 프로젝트별로 분류
     *
     * <p>프로젝트 태그가 없는 할 일은 빈 문자열을 키로 사용하여 분류하고
     * 여러 프로젝트에 연결된 할 일은 각 프로젝트 그룹에 포함</p>
     *
     * <p>LinkedHashMap을 사용하여 그룹 생성 순서 유지</p>
     *
     * @param todos 프로젝트별로 분류할 할 일 목록
     * @return 프로젝트 이름을 키로 하고 할 일 목록을 값으로 하는 Map
     */
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

    /**
     * 프로젝트별 할 일 목록을 표시하는 그룹 패널 생성
     *
     * <p>프로젝트 이름, 할 일 개수, 프로젝트 D-Day 및
     * 펼치기 버튼을 포함하는 상단 영역 구성</p>
     *
     * <p>TodoItemPanel을 사용하여 그룹에 연결된 개별 할 일을 표시하고
     * 프로젝트별 펼치기 및 접기 상태 저장</p>
     *
     * <p>프로젝트 태그가 없는 그룹은 프로젝트 미지정으로 표시</p>
     *
     * @param name 프로젝트 이름 (프로젝트 미지정이면 빈 문자열)
     * @param todos 해당 그룹에 포함된 할 일 목록
     * @return 프로젝트별 할 일 그룹 패널
     */
    private JPanel createGroupPanel(String name, List<Todo> todos) {
        JPanel groupPanel = new JPanel(new BorderLayout()) {

            /**
             * 그룹 패널의 최대 크기 반환
             *
             * <p>가로 방향 확장을 허용하고
             * 세로 크기는 패널의 기본 권장 높이로 제한</p>
             *
             * @return 그룹 패널의 최대 크기
             */
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

    /**
     * 데이터 변경 시 외부 화면을 갱신할 콜백 설정
     *
     * <p>Calendar와 Project 화면 간 데이터 변경 사항 연동 가능</p>
     *
     * @param onDataChanged 데이터 변경 시 실행할 콜백
     */
    public void setOnDataChanged(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
    }

    /**
     * 할 일 데이터 변경에 따른 화면 갱신
     *
     * <p>현재 할 일 목록을 갱신하고
     * 등록된 콜백을 실행하여 연결된 외부 화면 갱신</p>
     */
    private void notifyDataChanged() {
        refreshTodoList();
        onDataChanged.run();
    }

    /**
     * 실제 오늘 날짜를 기준으로 프로젝트 마감일까지의 D-Day 계산
     *
     * <p>미래 마감일은 D-n, 오늘은 D-Day,
     * 지난 마감일은 D+n 형식으로 표시</p>
     *
     * @param name 조회할 프로젝트 이름
     * @return D-Day 표시 문자열 (프로젝트가 존재하지 않으면 빈 문자열, 마감일이 지정되지 않았으면 미설정 반환)
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
     * 선택된 날짜에 할 일이 없는 경우 남은 프로젝트 목록 표시
     *
     * <p>실제 오늘 날짜를 기준으로 마감일이 지정되지 않았거나
     * 마감일이 지난 프로젝트는 제외</p>
     *
     * <p>마감되지 않은 프로젝트의 이름과 D-Day를 표시하고
     * 할 일 목록 펼치기 버튼은 생략</p>
     *
     * <p>표시할 프로젝트가 없는 경우 일정 없음 안내 문구 표시</p>
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