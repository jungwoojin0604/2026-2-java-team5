
package Gui;

import manager.ProjectManager;
import manager.TodoManager;
import model.Project;
import model.Todo;

import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class ProjectPanel extends JPanel {
    // 기존 데이터 관리 객체를 공유하기 위한 필드
    private final ProjectManager projectManager;
    private final TodoManager todoManager;

    // 프로젝트 목록을 다시 그릴 때 사용하는 패널
    private JPanel projectListPanel;
    // 선택한 프로젝트의 상세 정보를 표시하는 패널
    private JPanel rightPanel;
    // Project 화면의 기본 구조 생성
    // 기존 Manager를 전달받아 Project 화면 생성
    public ProjectPanel(ProjectManager projectManager,
                        TodoManager todoManager) {

        this.projectManager = projectManager;
        this.todoManager = todoManager;

        // 화면을 왼쪽(WEST)과 오른쪽(CENTER)으로 구분
        setLayout(new BorderLayout());

        // 왼쪽 프로젝트 목록 영역
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(420, 750));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));


        // 프로젝트 목록 상단: 제목과 추가 버튼
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel projectLabel = new JLabel("Projects");
        JButton addProjectButton = new JButton("+");

        // + 버튼 클릭 시 프로젝트 추가 창 열기
        addProjectButton.addActionListener(e ->
                ProjectDialog.showAddDialog(
                        this,
                        projectManager,
                        this::refreshProjectList
                )
        );

        headerPanel.add(projectLabel, BorderLayout.WEST);
        headerPanel.add(addProjectButton, BorderLayout.EAST);

        leftPanel.add(headerPanel, BorderLayout.NORTH);

        // 프로젝트들이 표시될 목록 영역
        projectListPanel = new JPanel();
        projectListPanel.setLayout(
                new BoxLayout(projectListPanel, BoxLayout.Y_AXIS)
        );
        projectListPanel.setBackground(Color.WHITE);





        // 프로젝트가 많아지면 스크롤 가능하도록 설정
        JScrollPane scrollPane = new JScrollPane(projectListPanel);
        scrollPane.setBorder(null);

        leftPanel.add(scrollPane, BorderLayout.CENTER);


        // 오른쪽 프로젝트 상세 영역
        rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JLabel detailLabel = new JLabel("프로젝트를 선택하세요");
        rightPanel.add(detailLabel, BorderLayout.NORTH);

        // 두 패널을 ProjectPanel에 배치
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
        refreshProjectList();
    }

    // Manager에 저장된 프로젝트와 Todo 정보를 화면에 다시 표시
    public void refreshProjectList() {

        // 기존 목록을 모두 지워 중복 표시 방지
        projectListPanel.removeAll();

        // 등록된 프로젝트가 없으면 안내 문구 출력
        if (projectManager.getAllProjects().isEmpty()) {
            projectListPanel.add(
                    new JLabel("등록된 프로젝트가 없습니다.")
            );
        } else {

            // 등록된 모든 프로젝트를 하나씩 확인
            for (Project project : projectManager.getAllProjects()) {

                // 해당 프로젝트에 연결된 전체 할 일 조회
                List<Todo> todos =
                        todoManager.getAllTodoByTag(project.getName());

                // 완료된 할 일 개수 계산
                int completed = 0;

                for (Todo todo : todos) {
                    if (todo.isDone()) {
                        completed++;
                    }
                }

                // 프로젝트 이름을 클릭하면 오른쪽에 상세 정보를 표시
                ProjectItemPanel item = new ProjectItemPanel(
                        project,
                        completed,
                        todos.size(),
                        () -> showProjectDetails(project)
                );

                // 목록에 추가
                projectListPanel.add(item);
            }
        }

        // 변경된 컴포넌트 배치와 화면 다시 표시
        projectListPanel.revalidate();
        projectListPanel.repaint();
    }

    // 선택한 프로젝트의 정보를 오른쪽 영역에 표시
    private void showProjectDetails(Project project) {

        // 이전 상세 화면 제거
        rightPanel.removeAll();

        // 선택한 프로젝트의 전체 할 일 조회
        List<Todo> todos =
                todoManager.getAllTodoByTag(project.getName());

        // 완료한 할 일 개수 계산
        int completed = 0;
        for (Todo todo : todos) {
            if (todo.isDone()) {
                completed++;
            }
        }

        // 완료 비율 계산
        int progress = todos.isEmpty()
                ? 0 : completed * 100 / todos.size();

        // 상세 정보를 세로로 배치할 패널
        JPanel detailPanel = new JPanel();
        detailPanel.setLayout(
                new BoxLayout(detailPanel, BoxLayout.Y_AXIS)
        );
        detailPanel.setBackground(Color.WHITE);
        detailPanel.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        );

        // 프로젝트 이름
        JLabel nameLabel =
                new JLabel("프로젝트: " + project.getName());

        // 프로젝트 마감일
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        String deadline = project.getDeadLine() == null
                ? "미설정"
                : project.getDeadLine().format(formatter);

        JLabel deadlineLabel =
                new JLabel("마감일: " + deadline);

        // 할 일 완료 현황
        JLabel todoLabel = new JLabel(
                "할 일: " + completed + " / " + todos.size() + "개 완료"
        );

        // 진행률 표시
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(progress);
        progressBar.setStringPainted(true);

        // 상세 화면에 컴포넌트 추가
        detailPanel.add(nameLabel);
        detailPanel.add(Box.createVerticalStrut(15));
        detailPanel.add(deadlineLabel);
        detailPanel.add(Box.createVerticalStrut(15));
        detailPanel.add(todoLabel);
        detailPanel.add(Box.createVerticalStrut(15));
        detailPanel.add(progressBar);

        // 오른쪽 영역에 상세 화면 표시
        rightPanel.add(detailPanel, BorderLayout.NORTH);

        // 변경된 화면 다시 그리기
        rightPanel.revalidate();
        rightPanel.repaint();
    }

}
