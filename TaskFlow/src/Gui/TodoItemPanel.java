package Gui;

import manager.ProjectManager;
import manager.TodoManager;
import model.Todo;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * 개별 할 일의 표시 및 관리를 위한 패널 클래스
 *
 * <p>할 일의 내용, 완료 상태, 날짜, 시간 및 D-Day 표시</p>
 *
 * <p>체크박스를 통한 완료 상태 변경과
 * 팝업 메뉴를 통한 할 일 수정 및 삭제 기능 제공</p>
 *
 * <p>Calendar 및 Project 화면에서 공통으로 사용 가능하며
 * 데이터 변경 시 콜백을 통해 외부 화면과 연동 가능</p>
 */
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

    /**
     * 개별 할 일의 정보를 표시하는 패널 생성
     *
     * <p>원형 완료 체크박스, 할 일 내용, 날짜 및 시간,
     * D-Day와 수정 및 삭제 메뉴 배치</p>
     *
     * <p>할 일의 완료 상태 및 데이터 변경에 따른
     * 화면 갱신 이벤트 설정</p>
     *
     * @param todo 화면에 표시할 할 일 객체
     * @param todoManager 할 일 관리 객체
     * @param projectManager 프로젝트 관리 객체
     * @param requiredProject 필수 지정 프로젝트 (없으면 null)
     * @param showDate true면 날짜 표시, false면 날짜 생략
     * @param onDataChanged 데이터 변경 시 실행할 콜백
     */
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
        doneCheckBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

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
            dateTimeLabel.setText(formatDateTime());
            onDataChanged.run();
        });
    }

    /**
     * 화면에 표시할 날짜, 시간 및 D-Day 문자열 생성
     *
     * <p>showDate 설정에 따라 날짜 표시 여부 결정
     * (날짜 표시가 활성화된 상태에서 날짜 미지정 시 Anytime 표시)</p>
     *
     * <p>시간이 지정된 경우 시간 표시,
     * 날짜가 지정된 미완료 할 일의 경우 D-Day 추가 표시</p>
     *
     * @return 화면에 표시할 날짜, 시간 및 D-Day 문자열
     */
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
        // 미완료 할 일의 D-Day 표시
        String dDay = getTodoDDay();
        if (!dDay.isEmpty()) {
            if (!result.isBlank()) {
                result += " | ";
            }
            result += dDay;
        }
        return result.trim();
    }

    /**
     * 실제 오늘 날짜를 기준으로 할 일의 D-Day 계산
     *
     * <p>미래 날짜는 D-n, 오늘은 D-Day,
     * 지난 날짜는 D+n 형식으로 표시</p>
     *
     * @return D-Day 표시 문자열 (완료된 할 일이거나 날짜 미지정이면 빈 문자열 반환)
     */
    private String getTodoDDay() {
        if (todo.isDone() || todo.getDate() == null) {
            return "";
        }
        long days = ChronoUnit.DAYS.between(LocalDate.now(), todo.getDate());
        if (days > 0) {
            return "D-" + days;
        } else if (days == 0) {
            return "D-Day";
        } else {
            return "D+" + (-days);
        }
    }

    /**
     * 할 일의 완료 상태에 따른 글자 스타일 변경
     *
     * <p>완료 상태에서는 취소선 및 회색 글자 적용,
     * 미완료 상태에서는 취소선 해제 및 검은색 글자 적용</p>
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
            attributes.put(TextAttribute.STRIKETHROUGH, false);
            contentLabel.setForeground(Color.BLACK);
        }
        contentLabel.setFont(
                contentLabel.getFont().deriveFont(attributes)
        );
    }

    /**
     * 할 일의 완료 상태를 표시하는 원형 체크박스 아이콘 클래스
     *
     * <p>완료 상태는 흰색 체크 표시가 포함된 회색 원으로,
     * 미완료 상태는 내부가 비어 있는 원으로 표시</p>
     */
    private static class CircleCheckIcon implements Icon {
        private final boolean selected;

        /**
         * 완료 상태에 따른 원형 체크박스 아이콘 생성
         *
         * @param selected true면 완료, false면 미완료
         */
        public CircleCheckIcon(boolean selected) {
            this.selected = selected;
        }

        /**
         * 아이콘의 가로 크기 반환
         *
         * @return 아이콘 너비 (20px)
         */
        @Override
        public int getIconWidth() {
            return 20;
        }

        /**
         * 아이콘의 세로 크기 반환
         *
         * @return 아이콘 높이 (20px)
         */
        @Override
        public int getIconHeight() {
            return 20;
        }

        /**
         * 완료 상태에 따른 원형 체크박스 아이콘 그리기
         *
         * <p>ANTIALIASING을 적용하여 원의 테두리를 부드럽게 표시하고
         * 완료 여부에 따라 원 및 체크 표시 그리기</p>
         *
         * @param c 아이콘이 표시되는 컴포넌트
         * @param g 아이콘을 그리기 위한 Graphics 객체
         * @param x 아이콘의 시작 x좌표
         * @param y 아이콘의 시작 y좌표
         */
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