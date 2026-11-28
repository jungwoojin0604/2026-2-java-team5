package Gui;

import manager.ProjectManager;
import manager.TodoManager;
import model.Project;
import model.Theme;
import model.Todo;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 할 일 추가 및 수정을 위한 공통 팝업 클래스
 *
 * <p>할 일 내용, 날짜, 시간 및 프로젝트 태그 입력 기능 제공</p>
 *
 * <p>DatePickerPanel을 활용한 날짜 선택 팝업과
 * Anytime 및 시간 미지정 기능 제공</p>
 *
 * <p>Calendar 및 Project 화면에서 공통으로 사용 가능하며
 * Project 화면에서 호출 시 해당 프로젝트 필수 지정 가능</p>
 *
 * <p>입력값의 유효성 검사 후 할 일 데이터를 저장하고
 * 콜백을 통해 외부 화면 갱신</p>
 */
public class TodoDialog {

    /**
     * 새로운 할 일 등록을 위한 추가 팝업 표시
     *
     * <p>기본 날짜 및 필수 프로젝트 설정을 전달하여
     * 공통 입력 팝업 호출</p>
     *
     * @param owner 팝업의 부모 창
     * @param todoManager 할 일 관리 객체
     * @param projectManager 프로젝트 관리 객체
     * @param defaultDate 기본 선택 날짜 (null이면 Anytime)
     * @param requiredProject 필수 지정 프로젝트 (없으면 null)
     * @param onDataChanged 데이터 변경 시 실행할 콜백
     */
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

    /**
     * 기존 할 일의 정보 변경을 위한 수정 팝업 표시
     *
     * <p>수정할 할 일 객체를 전달하여
     * 기존 내용, 날짜, 시간 및 프로젝트 태그가 반영된
     * 공통 입력 팝업 호출</p>
     *
     * @param owner 팝업의 부모 창
     * @param todoManager 할 일 관리 객체
     * @param projectManager 프로젝트 관리 객체
     * @param editingTodo 수정할 할 일 객체
     * @param requiredProject 필수 지정 프로젝트 (없으면 null)
     * @param onDataChanged 데이터 변경 시 실행할 콜백
     */
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

    /**
     * 할 일 추가 및 수정을 위한 공통 입력 팝업 생성
     *
     * <p>수정할 할 일 객체의 존재 여부에 따라
     * 추가 모드와 수정 모드 구분</p>
     *
     * <p>할 일 내용 입력창, 날짜 선택 팝업, Anytime 체크박스,
     * 시간 지정 영역 및 프로젝트 선택 영역 구성</p>
     *
     * <p>필수 지정 프로젝트가 존재하는 경우
     * 해당 프로젝트의 선택 상태 고정</p>
     *
     * <p>입력값의 유효성 검사 후 새로운 할 일을 등록하거나
     * 기존 할 일의 정보 변경</p>
     *
     * <p>데이터 저장 완료 시 콜백 실행 및 팝업 종료,
     * 취소 또는 창 닫기 시 데이터 변경 없이 종료</p>
     *
     * @param owner 팝업의 부모 창
     * @param todoManager 할 일 관리 객체
     * @param projectManager 프로젝트 관리 객체
     * @param defaultDate 기본 선택 날짜 (null이면 Anytime)
     * @param requiredProject 필수 지정 프로젝트 (없으면 null)
     * @param editingTodo 수정할 할 일 객체 (추가 모드이면 null)
     * @param onDataChanged 데이터 변경 시 실행할 콜백
     */
    private static void showDialog(
            Window owner,
            TodoManager todoManager,
            ProjectManager projectManager,
            LocalDate defaultDate,
            String requiredProject,
            Todo editingTodo,
            Runnable onDataChanged) {

        boolean isEdit = editingTodo != null;

        Theme theme = null;

        if (owner instanceof MainFrame frame) {
            theme = frame.getThemeManager().getCurrentTheme();
        }

        // 할 일 추가 및 수정 창 생성
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

        // 날짜 선택 버튼
        JButton dateButton = new JButton(
                datePicker.getSelectedDate().toString() + " ▼"
        );

        dateButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateButton.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 30)
        );

        // 날짜 선택 팝업
        JPopupMenu datePopup = new JPopupMenu();
        datePopup.add(datePicker);

        // 날짜 버튼 클릭 시 달력 표시
        dateButton.addActionListener(e ->
                datePopup.show(dateButton, 0, dateButton.getHeight())
        );

        // 날짜 선택 시 버튼의 날짜 변경 및 팝업 닫기
        datePicker.setOnDateSelected(() -> {
            dateButton.setText(
                    datePicker.getSelectedDate().toString() + " ▼"
            );
            datePopup.setVisible(false);
        });
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(dateLabel);
        formPanel.add(dateButton);

        // 날짜 미지정 체크박스
        JCheckBox anytimeCheckBox = new JCheckBox("Anytime");

        anytimeCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        anytimeCheckBox.setOpaque(false);

        // 추가 및 수정 시 날짜 미지정 상태 반영
        anytimeCheckBox.setSelected(defaultDate == null);

        // 초기 날짜 선택 상태 설정
        dateButton.setEnabled(!anytimeCheckBox.isSelected());
        if (anytimeCheckBox.isSelected()) {
            dateButton.setText("날짜 미지정");
        }
        // Anytime 선택 시 날짜 선택 비활성화
        anytimeCheckBox.addActionListener(e -> {
            boolean anytime = anytimeCheckBox.isSelected();
            dateButton.setEnabled(!anytime);
            datePopup.setVisible(false);
            dateButton.setText(anytime ? "날짜 미지정" : datePicker.getSelectedDate().toString() + " ▼");
        });

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

        // 추가 및 저장 버튼
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

        // 추가 및 저장 버튼 클릭 시 데이터 저장
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
        dialog.getRootPane().setDefaultButton(addButton);
        // 현재 테마 적용
        applyComponentTheme(formPanel, theme);
        // 날짜 선택 팝업 테마 적용
        if (theme != null) {
            datePicker.applyTheme(theme);
            datePopup.setBackground(theme.getBackgroundColor());
        }
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    /**
     * 입력 팝업의 컴포넌트에 지정된 테마 적용
     *
     * <p>컴포넌트 종류에 따른 배경색, 글자색 및 폰트 변경</p>
     *
     * <p>기존 폰트의 크기와 굵기 및 오류 메시지 색상 유지</p>
     *
     * @param component 테마를 적용할 컴포넌트
     * @param theme 적용할 테마 객체
     */
    static void applyComponentTheme(Component component, Theme theme) {
        // 테마가 지정되지 않은 경우 기존 디자인 유지
        if (theme == null) {
            return;
        }
        // 날짜 선택 패널은 자체 테마 적용 메서드 사용
        if (component instanceof DatePickerPanel datePicker) {
            datePicker.applyTheme(theme);
            return;
        }
        // 기존 폰트의 크기 및 굵기 유지
        Font oldFont = component.getFont();
        if (oldFont != null) {
            component.setFont(
                    theme.getFont().deriveFont(
                            oldFont.getStyle(),
                            oldFont.getSize2D()
                    )
            );
        }
        // 컴포넌트 종류에 따른 색상 적용
        if (component instanceof JCheckBox) {
            component.setForeground(theme.getTextColor());
        } else if (component instanceof AbstractButton) {
            component.setBackground(theme.getButtonColor());
            component.setForeground(theme.getTextColor());
        } else if (component instanceof JTextField textField) {
            textField.setBackground(theme.getPanelColor());
            textField.setForeground(theme.getTextColor());
            textField.setCaretColor(theme.getTextColor());
        } else if (component instanceof JLabel) {
            // 오류 메시지는 빨간색 유지
            if (!Color.RED.equals(component.getForeground())) {
                component.setForeground(theme.getTextColor());
            }
        } else if (component instanceof JPanel
                || component instanceof JSpinner
                || component instanceof JScrollPane
                || component instanceof JViewport) {
            component.setBackground(theme.getBackgroundColor());
        }
        // 하위 컴포넌트에도 테마 적용
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                applyComponentTheme(child, theme);
            }
        }
    }
}