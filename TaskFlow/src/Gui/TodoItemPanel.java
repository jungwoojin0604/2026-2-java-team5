package Gui;

import manager.ProjectManager;
import manager.TodoManager;
import model.Todo;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

public class TodoItemPanel extends JPanel {

    // 할 일 정보 및 공유 Manager 객체
    private final Todo todo;
    private final TodoManager todoManager;
    private final ProjectManager projectManager;

    // 화면별 표시 설정 및 갱신 콜백
    private final String requiredProject;
    private final boolean showDate;
    private final Runnable onDataChanged;

    // 할 일 표시 컴포넌트
    private final JCheckBox doneCheckBox;
    private final JLabel contentLabel;
    private final JLabel dateTimeLabel;
    private final JButton menuButton;

    // 할 일 한 개를 표시하는 패널 생성
    public TodoItemPanel(Todo todo,
                         TodoManager todoManager,
                         ProjectManager projectManager,
                         String requiredProject,
                         boolean showDate,
                         Runnable onDataChanged) {

        this.todo = todo;
        this.todoManager = todoManager;
        this.projectManager = projectManager;
        this.requiredProject = requiredProject;
        this.showDate = showDate;
        this.onDataChanged = onDataChanged;

        // 패널 기본 설정
        setLayout(new BorderLayout(10, 5));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 15));

        // 1. 원형 완료 체크박스
        doneCheckBox = new JCheckBox();

        doneCheckBox.setIcon(new CircleCheckIcon(false));
        doneCheckBox.setSelectedIcon(new CircleCheckIcon(true));

        doneCheckBox.setSelected(todo.isDone());
        doneCheckBox.setOpaque(false);
        doneCheckBox.setFocusPainted(false);
        doneCheckBox.setBorderPainted(false);
        doneCheckBox.setContentAreaFilled(false);

        doneCheckBox.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        // 2. 할 일 내용
        contentLabel = new JLabel(todo.getContent());

        // 3. 날짜 및 시간
        dateTimeLabel = new JLabel(formatDateTime());
        dateTimeLabel.setForeground(Color.GRAY);

        // 할 일 내용과 시간을 양쪽으로 배치
        JPanel contentPanel = new JPanel(new BorderLayout(10, 0));
        contentPanel.setOpaque(false);

        contentPanel.add(contentLabel, BorderLayout.CENTER);
        contentPanel.add(dateTimeLabel, BorderLayout.EAST);

        contentLabel.setFont(
                contentLabel.getFont().deriveFont(Font.PLAIN, 14f)
        );

        dateTimeLabel.setFont(
                dateTimeLabel.getFont().deriveFont(Font.PLAIN, 12f)
        );

        // 4. 수정 및 삭제 메뉴 버튼
        menuButton = new JButton("···");
        menuButton.setBorderPainted(false);
        menuButton.setContentAreaFilled(false);
        menuButton.setFocusPainted(false);

        // 할 일 관리 메뉴
        JPopupMenu popupMenu = new JPopupMenu();

        // 수정 메뉴
        JMenuItem editItem = new JMenuItem("수정");

        editItem.addActionListener(e ->
                TodoDialog.showEditDialog(
                        SwingUtilities.getWindowAncestor(this),
                        todoManager,
                        projectManager,
                        todo,
                        requiredProject,
                        onDataChanged
                )
        );

        popupMenu.add(editItem);

        // 삭제 메뉴
        JMenuItem deleteItem = new JMenuItem("삭제");

        // 삭제 메뉴 클릭 이벤트
        deleteItem.addActionListener(e -> {

            int result = JOptionPane.showConfirmDialog(
                    this,
                    "'" + todo.getContent() + "' 할 일을 삭제하시겠습니까?",
                    "할 일 삭제",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            // 사용자가 삭제에 동의한 경우
            if (result == JOptionPane.YES_OPTION) {

                if (todoManager.deleteTodo(todo)) {
                    onDataChanged.run();
                }
            }
        });

        popupMenu.add(deleteItem);

        // 메뉴 버튼 클릭 시 팝업 메뉴 표시
        menuButton.addActionListener(e ->
                popupMenu.show(menuButton, 0, menuButton.getHeight())
        );

        // 5. 컴포넌트 배치
        add(doneCheckBox, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
        add(menuButton, BorderLayout.EAST);

        setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        getPreferredSize().height
                )
        );

        setAlignmentX(Component.LEFT_ALIGNMENT);

        // 기존 완료 상태를 화면에 반영
        updateDoneStyle();

        // 체크박스 클릭 시 완료 상태 변경
        doneCheckBox.addActionListener(e -> {
            todo.setDone(doneCheckBox.isSelected());

            updateDoneStyle();

            onDataChanged.run();
        });
    }

    // 화면에 표시할 날짜 및 시간 문자열 반환
    private String formatDateTime() {
        String result = "";

        if (showDate) {
            if (todo.getDate() == null) {
                result = "Anytime";
            } else {
                result = todo.getDate().toString();
            }
        }

        if (todo.getTime() != null) {
            result += " " + todo.getTime();
        }

        return result.trim();
    }

    /**
     * 완료 상태에 따라 취소선과 글자 색상을 변경한다.
     */
    private void updateDoneStyle() {
        Map<TextAttribute, Object> attributes = new HashMap<>();
        attributes.putAll(contentLabel.getFont().getAttributes());

        if (todo.isDone()) {
            attributes.put(
                    TextAttribute.STRIKETHROUGH,
                    TextAttribute.STRIKETHROUGH_ON
            );
            contentLabel.setForeground(Color.GRAY);
        } else {
            attributes.remove(TextAttribute.STRIKETHROUGH, false);
            contentLabel.setForeground(Color.BLACK);
        }

        contentLabel.setFont(
                contentLabel.getFont().deriveFont(attributes)
        );
    }

    // 완료 상태에 따른 원형 체크박스 아이콘
    private static class CircleCheckIcon implements Icon {

        private final boolean selected;

        public CircleCheckIcon(boolean selected) {
            this.selected = selected;
        }

        @Override
        public int getIconWidth() {
            return 20;
        }

        @Override
        public int getIconHeight() {
            return 20;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {

            Graphics2D g2 = (Graphics2D) g.create();

            // 원의 테두리를 부드럽게 표시
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setStroke(new BasicStroke(1.5f));

            if (selected) {
                // 완료 상태: 회색 원
                g2.setColor(new Color(176, 181, 189));
                g2.fillOval(x + 2, y + 2, 16, 16);

                // 흰색 체크 표시
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(
                        2f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                ));

                g2.drawLine(x + 6, y + 10, x + 9, y + 13);
                g2.drawLine(x + 9, y + 13, x + 14, y + 7);

            } else {
                // 미완료 상태: 빈 원
                g2.setColor(new Color(170, 170, 170));
                g2.drawOval(x + 2, y + 2, 16, 16);
            }

            g2.dispose();
        }
    }
}