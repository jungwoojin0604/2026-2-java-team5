package Gui;

import manager.ProjectManager;
import manager.TodoManager;
import model.Project;
import model.Todo;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

// 왼쪽은 프로젝트 목록, 오른쪽은 선택한 프로젝트의 할 일 목록이다.
public class ProjectPanel extends JPanel {
    private final ProjectManager projectManager;
    private final TodoManager todoManager;
    private final Runnable onDataChanged;
    private final JPanel projectList = new JPanel();
    private final JPanel detailPanel = new JPanel(new BorderLayout());
    private Project selectedProject;
    // 프로젝트 이름 검색 입력창
    private final JTextField projectSearchField = new JTextField(12);
    // 오른쪽 Todo 목록 검색 입력창
    private final JTextField todoSearchField = new JTextField(12);

    // 테스트할 때는 Manager 두 개만 전달하면 된다.
    public ProjectPanel(ProjectManager pm, TodoManager tm) {
        this(pm, tm, null);
    }

    // MainFrame에서는 공통 화면 갱신 콜백도 전달할 수 있다.
    public ProjectPanel(ProjectManager pm, TodoManager tm, Runnable onDataChanged) {
        super(new BorderLayout());
        this.projectManager = pm;
        this.todoManager = tm;
        this.onDataChanged = onDataChanged;

        JPanel left = new JPanel(new BorderLayout());
        left.setPreferredSize(new Dimension(420, 750));
        left.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));


        // 프로젝트 추가 및 검색 영역
        JPanel header = new JPanel(new BorderLayout());
        header.add(new JLabel("  Project"), BorderLayout.WEST);

        JButton addButton = new JButton("+");
        JButton searchButton = new JButton("검색");

        JPanel tools = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 5, 2)
        );

        tools.add(addButton);
        tools.add(projectSearchField);
        tools.add(searchButton);

        header.add(tools, BorderLayout.EAST);
        left.add(header, BorderLayout.NORTH);

        // 프로젝트 추가
        addButton.addActionListener(e ->
                ProjectDialog.showAddDialog(
                        this, projectManager, this::dataChanged));

        // 검색 버튼 또는 Enter 입력 시 목록 갱신
        searchButton.addActionListener(e -> refreshProjectList());
        projectSearchField.addActionListener(e -> refreshProjectList());


        projectList.setLayout(new BoxLayout(projectList, BoxLayout.Y_AXIS));
        left.add(new JScrollPane(projectList), BorderLayout.CENTER);
        add(left, BorderLayout.WEST);
        add(detailPanel, BorderLayout.CENTER);
        refreshData();
    }

    // Manager에서 최신 데이터를 가져와 두 화면을 다시 그린다.
    public void refreshData() {
        if (selectedProject == null ||
                projectManager.findProject(selectedProject.getName()) == null) {
            selectedProject = projectManager.getAllProjects().stream()
                    .findFirst().orElse(null);
        }
        refreshProjectList();
        showDetails();
    }

    // 프로젝트마다 Todo 완료 비율을 계산하고 왼쪽에 표시한다.
    public void refreshProjectList() {
        projectList.removeAll();

// 검색어를 소문자로 통일해 비교
        String keyword = projectSearchField.getText().trim().toLowerCase();

        for (Project project : projectManager.getAllProjects()) {

            // 검색어가 포함된 프로젝트만 표시
            if (!project.getName().toLowerCase().contains(keyword)) {
                continue;
            }
            List<Todo> todos = todoManager.getAllTodoByTag(project.getName());
            int completed = 0;
            for (Todo todo : todos) if (todo.isDone()) completed++;
            int progress = todos.isEmpty() ? 0 : completed * 100 / todos.size();

            projectList.add(new ProjectItemPanel(
                    project, progress, project == selectedProject,
                    // 다른 프로젝트 선택 시 Todo 검색 초기화
                    () -> {
                        selectedProject = project;
                        todoSearchField.setText("");
                        refreshData();
                    },
                    () -> renameProject(project),
                    () -> editDeadline(project),
                    () -> deleteProject(project)));
        }
        projectList.revalidate();
        projectList.repaint();
    }

    // 선택한 프로젝트의 Todo를 오른쪽에 체크박스로 표시한다.
    private void showDetails() {
        detailPanel.removeAll();
        if (selectedProject == null) {
            detailPanel.add(new JLabel("  프로젝트를 추가해주세요."), BorderLayout.NORTH);
        } else {

            Project project = selectedProject;

            // 선택한 프로젝트에 연결된 전체 Todo 조회
            List<Todo> allTodos =
                    projectManager.getProjectTodos(project.getName());

            // 완료된 Todo 개수 계산
            int completed = 0;

            for (Todo todo : allTodos) {
                if (todo.isDone()) {
                    completed++;
                }
            }

            // 전체 Todo가 0개이면 진행률은 0%
            int total = allTodos.size();
            int progress = total == 0
                    ? 0 : completed * 100 / total;

            // 오른쪽 상단 영역
            JPanel top = new JPanel(new BorderLayout());

            // 프로젝트 이름과 진행률을 세로로 표시
            JPanel titlePanel = new JPanel(new GridLayout(2, 1));

            JLabel nameLabel =
                    new JLabel("  " + project.getName());

            nameLabel.setFont(
                    nameLabel.getFont().deriveFont(Font.BOLD, 18f)
            );

            JLabel progressLabel = new JLabel(
                    "  진행률 " + progress + "% ("
                            + completed + "/" + total + " 완료)"
            );

            titlePanel.add(nameLabel);
            titlePanel.add(progressLabel);

            top.add(titlePanel, BorderLayout.CENTER);


            JButton addTodoButton = new JButton("+");
            JButton searchButton = new JButton("검색");

            JPanel tools = new JPanel(
                    new FlowLayout(FlowLayout.RIGHT, 5, 2)
            );

            tools.add(addTodoButton);
            tools.add(todoSearchField);
            tools.add(searchButton);

            top.add(tools, BorderLayout.EAST);

            // Todo 추가
            addTodoButton.addActionListener(e -> addTodo(project));

            // 검색 버튼 또는 Enter 입력 시 상세 목록 갱신
            searchButton.addActionListener(e -> showDetails());
            todoSearchField.addActionListener(e -> showDetails());

            detailPanel.add(top, BorderLayout.NORTH);


            JPanel todoList = new JPanel();
            todoList.setLayout(new BoxLayout(todoList, BoxLayout.Y_AXIS));


            // 검색어를 소문자로 바꿔 대소문자 구분 없이 검색
            String keyword = todoSearchField.getText()
                    .trim().toLowerCase();

            int count = 0;

            for (Todo todo : allTodos) {

                // 검색어가 포함되지 않은 Todo는 건너뛰기
                if (!todo.getContent().toLowerCase().contains(keyword)) {
                    continue;
                }

                TodoItemPanel item = new TodoItemPanel(
                        todo,
                        todoManager,
                        projectManager,
                        project.getName(),
                        true,
                        this::dataChanged
                );

                todoList.add(item);
                count++;
            }

            // 검색 결과가 없을 경우 안내 문구 표시
            if (count == 0) {
                todoList.add(new JLabel("검색 결과가 없습니다."));
            }


            detailPanel.add(new JScrollPane(todoList), BorderLayout.CENTER);
        }
        detailPanel.revalidate();
        detailPanel.repaint();
    }


    // 선택한 프로젝트에 Todo를 추가하는 공통 입력창 실행
    private void addTodo(Project project) {

        TodoDialog.showAddDialog(
                SwingUtilities.getWindowAncestor(this),
                todoManager,
                projectManager,
                LocalDate.now(),
                project.getName(),
                this::dataChanged
        );
    }


    // 프로젝트 이름을 변경하고 연결된 Todo 태그도 Manager에서 함께 변경한다.
    private void renameProject(Project project) {
        String name = JOptionPane.showInputDialog(this, "새 이름", project.getName());
        if (name == null) return;
        name = name.trim();
        if (name.isEmpty() || name.contains("|") || name.contains(",") ||
                projectManager.findProject(name) != null) {
            JOptionPane.showMessageDialog(this, "이름이 비었거나 중복되었거나 사용할 수 없는 문자가 있습니다.");
            return;
        }
        projectManager.updateProjectName(project.getName(), name);
        dataChanged();
    }

    // 프로젝트 마감일을 TodoDialog와 같은 달력 팝업에서 수정한다.
    private void editDeadline(Project project) {
        ProjectDialog.showDeadlineDialog(this, projectManager, project, this::dataChanged);
    }

    // 삭제 확인 후 ProjectManager가 프로젝트 태그만 제거한다.
    private void deleteProject(Project project) {
        int answer = JOptionPane.showConfirmDialog(this,
                project.getName() + " 프로젝트를 삭제할까요?", "삭제", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) return;
        projectManager.deleteProject(project.getName());
        selectedProject = null;
        dataChanged();
    }

    // 화면을 갱신하고 필요할 때 MainFrame에도 변경을 알린다.
    private void dataChanged() {
        refreshData();
        if (onDataChanged != null) onDataChanged.run();
    }
}
