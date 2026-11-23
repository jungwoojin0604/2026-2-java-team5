
package Gui;

import manager.ProjectManager;
import manager.TodoManager;
import model.Project;
import model.Todo;

import java.util.List;
import javax.swing.*;
import java.awt.*;

public class ProjectPanel extends JPanel {
    // 기존 데이터 관리 객체를 공유하기 위한 필드
    private final ProjectManager projectManager;
    private final TodoManager todoManager;

    // 프로젝트 목록을 다시 그릴 때 사용하는 패널
    private JPanel projectListPanel;
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
        JPanel rightPanel = new JPanel(new BorderLayout());
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

                // 실제 데이터를 이용해 프로젝트 항목 생성
                ProjectItemPanel item = new ProjectItemPanel(
                        project, completed, todos.size()
                );

                // 목록에 추가
                projectListPanel.add(item);
            }
        }

        // 변경된 컴포넌트 배치와 화면 다시 표시
        projectListPanel.revalidate();
        projectListPanel.repaint();
    }

}
