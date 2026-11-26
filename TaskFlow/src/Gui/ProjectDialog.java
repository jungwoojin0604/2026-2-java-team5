package Gui;

import manager.ProjectManager;
import model.Project;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

// TodoDialog와 같은 DatePickerPanel로 프로젝트 마감일을 선택하는 창
public class ProjectDialog {

    // 새 프로젝트 등록
    public static void showAddDialog(Component parent, ProjectManager pm, Runnable onSaved) {
        showDialog(parent, pm, null, onSaved);
    }

    // 기존 프로젝트의 마감일 변경
    public static void showDeadlineDialog(Component parent, ProjectManager pm,
                                          Project project, Runnable onSaved) {
        showDialog(parent, pm, project, onSaved);
    }

    // 추가와 수정이 함께 사용하는 입력창
    private static void showDialog(Component parent, ProjectManager pm,
                                   Project editing, Runnable onSaved) {
        boolean isEdit = editing != null;
        LocalDateTime oldDeadline = isEdit ? editing.getDeadLine() : null;
        LocalDate initialDate = oldDeadline == null ? LocalDate.now() : oldDeadline.toLocalDate();
        LocalTime initialTime = oldDeadline == null ? LocalTime.of(23, 59) : oldDeadline.toLocalTime();

        JTextField nameField = new JTextField(isEdit ? editing.getName() : "", 18);
        nameField.setEnabled(!isEdit);

        // TodoDialog와 동일한 날짜 버튼 + 달력 팝업
        DatePickerPanel datePicker = new DatePickerPanel(initialDate);
        datePicker.setPreferredSize(new Dimension(300, 230));
        JButton dateButton = new JButton(datePicker.getSelectedDate() + " ▼");
        JPopupMenu datePopup = new JPopupMenu();
        datePopup.add(datePicker);
        dateButton.addActionListener(e ->
                datePopup.show(dateButton, 0, dateButton.getHeight()));
        datePicker.setOnDateSelected(() -> {
            dateButton.setText(datePicker.getSelectedDate() + " ▼");
            datePopup.setVisible(false);
        });

        // TodoDialog처럼 시와 분을 따로 선택
        JSpinner hour = new JSpinner(new SpinnerNumberModel(initialTime.getHour(), 0, 23, 1));
        JSpinner minute = new JSpinner(new SpinnerNumberModel(initialTime.getMinute(), 0, 59, 1));
        hour.setEditor(new JSpinner.NumberEditor(hour, "00"));
        minute.setEditor(new JSpinner.NumberEditor(minute, "00"));
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        timePanel.add(hour);
        timePanel.add(new JLabel(":"));
        timePanel.add(minute);

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 10));
        form.add(new JLabel("프로젝트 이름"));
        form.add(nameField);
        form.add(new JLabel("마감일"));
        form.add(dateButton);
        form.add(new JLabel("시간"));
        form.add(timePanel);

        // 취소할 때까지, 또는 유효한 값을 입력할 때까지 반복
        while (true) {
            int result = JOptionPane.showConfirmDialog(parent, form,
                    isEdit ? "마감일 수정" : "프로젝트 추가", JOptionPane.OK_CANCEL_OPTION);
            if (result != JOptionPane.OK_OPTION) return;

            String name = nameField.getText().trim();
            if (name.isEmpty() || name.contains("|") || name.contains(",") ||
                    (!isEdit && pm.findProject(name) != null)) {
                JOptionPane.showMessageDialog(parent, "프로젝트 이름을 확인해주세요.");
                continue;
            }

            try {
                hour.commitEdit();
                minute.commitEdit();
                LocalDate date = datePicker.getSelectedDate();
                if (date.isBefore(LocalDate.now())) {
                    JOptionPane.showMessageDialog(parent, "오늘 이전 날짜는 선택할 수 없습니다.");
                    continue;
                }
                LocalDateTime deadline = date.atTime((int) hour.getValue(), (int) minute.getValue());
                if (isEdit) pm.updateDeadline(editing.getName(), deadline);
                else pm.addProject(new Project(name, deadline));

                onSaved.run();
                return;
            } catch (java.text.ParseException ex) {
                JOptionPane.showMessageDialog(parent, "시간은 00~23시, 00~59분으로 입력해주세요.");
            }
        }
    }
}
