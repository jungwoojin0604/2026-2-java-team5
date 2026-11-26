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

        JPanel header = new JPanel(new BorderLayout());
        header.add(new JLabel("  Project"), BorderLayout.WEST);
        JButton addButton = new JButton("+");
        header.add(addButton, BorderLayout.EAST);
        left.add(header, BorderLayout.NORTH);
        addButton.addActionListener(e ->
                ProjectDialog.showAddDialog(this, projectManager, this::dataChanged));

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
        for (Project project : projectManager.getAllProjects()) {
            List<Todo> todos = todoManager.getAllTodoByTag(project.getName());
            int completed = 0;
            for (Todo todo : todos) if (todo.isDone()) completed++;
            int progress = todos.isEmpty() ? 0 : completed * 100 / todos.size();

            projectList.add(new ProjectItemPanel(
                    project, progress, project == selectedProject,
                    () -> { selectedProject = project; refreshData(); },
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
            JPanel top = new JPanel(new BorderLayout());
            top.add(new JLabel("  " + project.getName()), BorderLayout.CENTER);
            JButton addTodoButton = new JButton("+ Todo");
            top.add(addTodoButton, BorderLayout.EAST);
            addTodoButton.addActionListener(e -> addTodo(project));
            detailPanel.add(top, BorderLayout.NORTH);

            JPanel todoList = new JPanel();
            todoList.setLayout(new BoxLayout(todoList, BoxLayout.Y_AXIS));
            for (Todo todo : projectManager.getProjectTodos(project.getName())) {
                String date = todo.getDate() == null ? "Anytime" : todo.getDate().toString();
                JCheckBox box = new JCheckBox(todo.getContent() + "   " + date, todo.isDone());
                box.setAlignmentX(Component.LEFT_ALIGNMENT);
                box.addActionListener(e -> {
                    todo.setDone(box.isSelected());
                    dataChanged();
                });
                todoList.add(box);
            }
            detailPanel.add(new JScrollPane(todoList), BorderLayout.CENTER);
        }
        detailPanel.revalidate();
        detailPanel.repaint();
    }

    // 팀원 TodoDialog 연결 전에는 내용만 입력하는 임시 추가 기능을 사용한다.
    private void addTodo(Project project) {
        String content = JOptionPane.showInputDialog(this, "할 일 내용");
        if (content == null) return;
        content = content.trim();
        if (content.isEmpty() || content.contains("|")) {
            JOptionPane.showMessageDialog(this, "내용을 확인해주세요. (| 사용 불가)");
            return;
        }
        todoManager.addTodo(content, LocalDate.now(), null,
                List.of(project.getName()), false);
        dataChanged();
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
