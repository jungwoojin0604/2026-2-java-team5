package Gui;

import manager.ProjectManager;
import manager.TodoManager;
import model.Project;
import model.Todo;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TodoDialog {

    // 할 일 추가 팝업
    public static void showAddDialog(
            Window owner,
            TodoManager todoManager,
            ProjectManager projectManager,
            LocalDate defaultDate,
            String requiredProject,
            Runnable onDataChanged) {

        showDialog(owner, todoManager, projectManager,
                defaultDate, requiredProject, null, onDataChanged);
    }

    // 할 일 수정 팝업
    public static void showEditDialog(
            Window owner,
            TodoManager todoManager,
            ProjectManager projectManager,
            Todo editingTodo,
            String requiredProject,
            Runnable onDataChanged) {

        showDialog(owner, todoManager, projectManager,
                editingTodo.getDate(), requiredProject,
                editingTodo, onDataChanged);
    }

    // 추가 및 수정 공통 팝업
    private static void showDialog(
            Window owner,
            TodoManager todoManager,
            ProjectManager projectManager,
            LocalDate defaultDate,
            String requiredProject,
            Todo editingTodo,
            Runnable onDataChanged) {

        boolean isEdit = editingTodo != null;

        // 할 일 추가 창 생성
        JDialog dialog = new JDialog(
                owner,
                isEdit ? "할 일 수정" : "할 일 추가",
                Dialog.ModalityType.APPLICATION_MODAL
        );

        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        // 입력 영역 생성
        JPanel formPanel = new JPanel();
        formPanel.setLayout(
                new BoxLayout(formPanel, BoxLayout.Y_AXIS)
        );
        formPanel.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        );
        formPanel.setBackground(Color.WHITE);

        // 할 일 내용 입력
        JLabel contentLabel = new JLabel("할 일 내용");
        JTextField contentField = new JTextField(25);

        contentField.setPreferredSize(new Dimension(300, 30));
        contentField.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 30)
        );

        // 수정 시 기존 할 일 내용 표시
        if (isEdit) {
            contentField.setText(editingTodo.getContent());
        }

        contentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentField.setAlignmentX(Component.LEFT_ALIGNMENT);

        formPanel.add(contentLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(contentField);

        // 날짜 선택 영역
        JLabel dateLabel = new JLabel("날짜");
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        DatePickerPanel datePicker = new DatePickerPanel(defaultDate);
        datePicker.setPreferredSize(new Dimension(300, 230));
        datePicker.setMaximumSize(new Dimension(300, 230));
        datePicker.setAlignmentX(Component.LEFT_ALIGNMENT);

        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(dateLabel);
        formPanel.add(datePicker);

        // 날짜 미지정 체크박스
        JCheckBox anytimeCheckBox = new JCheckBox("Anytime");

        anytimeCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        anytimeCheckBox.setOpaque(false);

        // 추가 및 수정 시 날짜 미지정 상태 반영
        anytimeCheckBox.setSelected(defaultDate == null);

        datePicker.setEnabled(!anytimeCheckBox.isSelected());

        // Anytime 선택 시 날짜 선택 비활성화
        anytimeCheckBox.addActionListener(e ->
                datePicker.setEnabled(!anytimeCheckBox.isSelected())
        );

        formPanel.add(anytimeCheckBox);

        // 시간 지정 여부
        JCheckBox timeCheckBox = new JCheckBox("시간 지정");
        timeCheckBox.setOpaque(false);
        timeCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 기존 시간 또는 기본 시간
        LocalTime initialTime =
                isEdit && editingTodo.getTime() != null
                        ? editingTodo.getTime()
                        : LocalTime.of(12, 0);

        // 시, 분 선택 컴포넌트
        JSpinner hourSpinner = new JSpinner(
                new SpinnerNumberModel(
                        initialTime.getHour(), 0, 23, 1
                )
        );

        JSpinner minuteSpinner = new JSpinner(
                new SpinnerNumberModel(
                        initialTime.getMinute(), 0, 59, 1
                )
        );

        // 시간 표시 형식
        hourSpinner.setEditor(
                new JSpinner.NumberEditor(hourSpinner, "00")
        );

        minuteSpinner.setEditor(
                new JSpinner.NumberEditor(minuteSpinner, "00")
        );

        // 시간 선택 영역
        JPanel timePanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT)
        );

        timePanel.setOpaque(false);
        timePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        timePanel.add(hourSpinner);
        timePanel.add(new JLabel(":"));
        timePanel.add(minuteSpinner);

        // 기존 시간 지정 여부 반영
        timeCheckBox.setSelected(
                isEdit && editingTodo.getTime() != null
        );

        hourSpinner.setEnabled(timeCheckBox.isSelected());
        minuteSpinner.setEnabled(timeCheckBox.isSelected());

        // 시간 지정 체크박스 이벤트
        timeCheckBox.addActionListener(e -> {
            hourSpinner.setEnabled(timeCheckBox.isSelected());
            minuteSpinner.setEnabled(timeCheckBox.isSelected());
        });

        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(timeCheckBox);
        formPanel.add(timePanel);

        // 프로젝트 선택 영역
        JLabel projectLabel = new JLabel("프로젝트");
        projectLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel projectPanel = new JPanel();
        projectPanel.setLayout(
                new BoxLayout(projectPanel, BoxLayout.Y_AXIS)
        );
        projectPanel.setOpaque(false);

        // 프로젝트 이름과 체크박스 연결
        Map<String, JCheckBox> projectCheckBoxes =
                new LinkedHashMap<>();

        // 기존 프로젝트 목록 조회
        for (Project project : projectManager.getAllProjects()) {

            String name = project.getName();

            JCheckBox checkBox = new JCheckBox(name);
            checkBox.setOpaque(false);

            // 수정 시 기존 프로젝트 태그 반영
            if (isEdit && editingTodo.getProjectTags().contains(name)) {
                checkBox.setSelected(true);
            }

            // Project 화면에서 호출한 경우 필수 프로젝트 설정
            if (name.equals(requiredProject)) {
                checkBox.setSelected(true);
                checkBox.setEnabled(false);
            }

            projectCheckBoxes.put(name, checkBox);
            projectPanel.add(checkBox);
        }

        if (projectCheckBoxes.isEmpty()) {
            projectPanel.add(
                    new JLabel("등록된 프로젝트가 없습니다.")
            );
        }

        // 프로젝트 목록 스크롤 영역
        JScrollPane projectScrollPane = new JScrollPane(projectPanel);
        projectScrollPane.setPreferredSize(new Dimension(300, 90));
        projectScrollPane.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 90)
        );
        projectScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(projectLabel);
        formPanel.add(projectScrollPane);

        // 입력 오류 메시지 표시
        JLabel errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        formPanel.add(errorLabel);

        // 추가 버튼
        JButton addButton = new JButton(
                isEdit ? "저장" : "추가"
        );

        // 하단 버튼 영역
        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton cancelButton = new JButton("취소");
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(addButton);

        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(buttonPanel);

        // 추가 버튼 클릭 시 할 일 등록
        addButton.addActionListener(e -> {

            String content = contentField.getText().trim();

            // 할 일 내용 유효성 검사
            if (content.isEmpty()) {
                errorLabel.setText("할 일 내용을 입력해주세요.");
                return;
            }

            if (content.contains("|")) {
                errorLabel.setText("| 문자는 사용할 수 없습니다.");
                return;
            }

            // Anytime 여부에 따라 날짜 설정
            LocalDate date = anytimeCheckBox.isSelected()
                    ? null : datePicker.getSelectedDate();

            // 시간 지정 여부에 따라 시간 설정
            LocalTime time = null;

            if (timeCheckBox.isSelected()) {
                try {
                    hourSpinner.commitEdit();
                    minuteSpinner.commitEdit();

                    time = LocalTime.of(
                            (Integer) hourSpinner.getValue(),
                            (Integer) minuteSpinner.getValue()
                    );

                } catch (java.text.ParseException ex) {
                    errorLabel.setText("올바른 시간을 입력해주세요.");
                    return;
                }
            }

            // 선택된 프로젝트 태그 저장
            List<String> projectTags = new ArrayList<>();

            for (String name : projectCheckBoxes.keySet()) {

                if (projectCheckBoxes.get(name).isSelected()) {
                    projectTags.add(name);
                }
            }

            // 기존 프로젝트 목록에 없는 태그도 수정 시 유지
            if (isEdit) {
                for (String tag : editingTodo.getProjectTags()) {
                    if (!projectCheckBoxes.containsKey(tag)) {
                        projectTags.add(tag);
                    }
                }
            }

            if (isEdit) {
                editingTodo.setContent(content);
                editingTodo.setDate(date);
                editingTodo.setTime(time);
                editingTodo.setProjectTags(projectTags);

            } else {
                todoManager.addTodo(
                        content,
                        date,
                        time,
                        projectTags,
                        false
                );
            }

            // 화면 갱신 및 팝업 닫기
            onDataChanged.run();
            dialog.dispose();
        });

        // 팝업 화면 설정
        dialog.setContentPane(formPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }
}