package Gui;

import manager.ProjectManager;
import manager.TodoManager;
import model.Todo;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 개별 할 일의 표시 및 관리를 위한 한 줄 패널 (Figma TodoRow / ProjectTodoRow)
 *
 * <p>동그라미 체크(완료 여부), 할 일 내용(18pt), 옆에 회색 날짜·시간(15pt) 표시</p>
 *
 * <p>완료한 할 일은 안쪽이 찬 동그라미와 회색 글자로 표시</p>
 *
 * <p>마우스를 올리면 오른쪽에 (i) 아이콘이 보이고, 아이콘에 올리면 상세 정보,
 * 아이콘 클릭 또는 줄 우클릭 시 수정·삭제 메뉴 표시</p>
 *
 * <p>Calendar 및 Project 화면에서 공통으로 사용 가능하며
 * 데이터 변경 시 콜백을 통해 외부 화면과 연동 가능</p>
 */
public class TodoItemPanel extends JPanel {
    /** Figma 할 일 줄 높이 */
    public static final int ROW_HEIGHT = 33;

    // 할 일 정보 및 공유 Manager 객체
    private final Todo todo;
    private final TodoManager todoManager;
    private final ProjectManager projectManager;

    // 화면별 표시 설정 및 갱신 콜백
    private final String requiredProject;
    private final boolean showDate;
    private final Runnable onDataChanged;

    // 할 일 표시 컴포넌트
    private final JLabel checkLabel;
    private final JLabel contentLabel;
    private final JLabel dateTimeLabel;
    private final JLabel infoLabel;

    /**
     * 개별 할 일의 정보를 표시하는 패널 생성
     *
     * @param todo            화면에 표시할 할 일 객체
     * @param todoManager     할 일 관리 객체
     * @param projectManager  프로젝트 관리 객체
     * @param requiredProject 필수 지정 프로젝트 (없으면 null)
     * @param showDate        true면 날짜(또는 Anytime) 표시, false면 시간·D-Day만 표시
     * @param onDataChanged   데이터 변경 시 실행할 콜백
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

        // 패널 기본 설정 (왼쪽 29px, 동그라미와 글자 사이 11px)
        setLayout(new BorderLayout(11, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 29, 0, 20));

        // 1. 동그라미 완료 체크
        checkLabel = new JLabel();
        checkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        checkLabel.setToolTipText("완료 표시");

        // 2. 할 일 내용과 회색 날짜·시간
        contentLabel = UiKit.label(todo.getContent(), Theme.plain(18), Color.BLACK);
        dateTimeLabel = UiKit.label(formatDateTime(), Theme.plain(15), Theme.TEXT_DATE);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.X_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(contentLabel);
        textPanel.add(Box.createHorizontalStrut(11));
        textPanel.add(dateTimeLabel);
        textPanel.add(Box.createHorizontalGlue());

        // 3. 마우스를 올렸을 때만 보이는 (i) 아이콘
        infoLabel = new JLabel();
        infoLabel.setPreferredSize(new Dimension(18, 18));
        infoLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        add(checkLabel, BorderLayout.WEST);
        add(textPanel, BorderLayout.CENTER);
        add(infoLabel, BorderLayout.EAST);

        Dimension size = new Dimension(Integer.MAX_VALUE, ROW_HEIGHT);
        setMaximumSize(size);
        setPreferredSize(new Dimension(700, ROW_HEIGHT));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        // 기존 완료 상태를 화면에 반영
        updateDoneStyle();

        // 동그라미 클릭 시 완료 상태 변경
        UiKit.onClick(checkLabel, () -> {
            todo.setDone(!todo.isDone());
            updateDoneStyle();
            dateTimeLabel.setText(formatDateTime());
            onDataChanged.run();
        });

        // (i) 클릭 시 수정·삭제 메뉴
        UiKit.onClick(infoLabel, () -> showMenu(infoLabel, 0, infoLabel.getHeight()));

        installHoverAndMenu();
    }

    /** 마우스 올림 표시와 우클릭 메뉴를 줄 전체에 연결 */
    private void installHoverAndMenu() {
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setInfoVisible(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), TodoItemPanel.this);
                if (!contains(p)) {
                    setInfoVisible(false);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowMenu(e);
            }

            private void maybeShowMenu(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), TodoItemPanel.this);
                    showMenu(TodoItemPanel.this, p.x, p.y);
                }
            }
        };
        addMouseListener(adapter);
        for (Component child : new Component[]{checkLabel, contentLabel, dateTimeLabel, infoLabel}) {
            child.addMouseListener(adapter);
        }
    }

    /** (i) 아이콘 보이기/숨기기 (자리는 그대로 두어 줄이 흔들리지 않음) */
    private void setInfoVisible(boolean visible) {
        infoLabel.setIcon(visible ? Icons.info() : null);
        infoLabel.setToolTipText(visible ? buildDetailText() : null);
    }

    /** 수정·삭제 메뉴 표시 */
    private void showMenu(Component invoker, int x, int y) {
        JPopupMenu menu = UiKit.contextMenu(
                new String[]{"수정", "삭제"},
                new Runnable[]{this::editTodo, this::deleteTodo});
        menu.show(invoker, x, y);
    }

    /** 할 일 수정 창 열기 */
    private void editTodo() {
        TodoDialog.showEditDialog(this, todoManager, projectManager,
                todo, requiredProject, onDataChanged);
    }

    /** 확인 후 할 일 삭제 */
    private void deleteTodo() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "'" + todo.getContent() + "' 할 일을 삭제하시겠습니까?",
                "할 일 삭제",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        // 사용자가 삭제에 동의한 경우
        if (result == JOptionPane.YES_OPTION && todoManager.deleteTodo(todo)) {
            onDataChanged.run();
        }
    }

    /**
     * 할 일 옆에 회색으로 표시할 문자열 생성
     *
     * <p>Project 화면(showDate): 날짜(2026.11.25) 또는 Anytime, 시간이 있으면 시간 추가</p>
     *
     * <p>Calendar 화면: 시간과 미완료 할 일의 D-Day</p>
     */
    private String formatDateTime() {
        StringBuilder result = new StringBuilder();
        if (showDate) {
            result.append(todo.getDate() == null ? "Anytime" : todo.getDate().format(Theme.DOT_DATE));
        }
        if (todo.getTime() != null) {
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(todo.getTime().format(Theme.TIME));
        }
        if (!showDate && !todo.isDone() && todo.getDate() != null) {
            if (!result.isEmpty()) {
                result.append(" · ");
            }
            result.append(Theme.dDay(todo.getDate()));
        }
        return result.toString();
    }

    /** (i) 아이콘에 올렸을 때 보이는 상세 정보 */
    private String buildDetailText() {
        String date = todo.getDate() == null ? "Anytime" : todo.getDate().format(Theme.DOT_DATE)
                + " (" + Theme.dDay(todo.getDate()) + ")";
        String time = todo.getTime() == null ? "미지정" : todo.getTime().format(Theme.TIME);
        String projects = todo.getProjectTags().isEmpty() ? "없음" : String.join(", ", todo.getProjectTags());
        return "<html>날짜: " + escape(date)
                + "<br>시간: " + time
                + "<br>프로젝트: " + escape(projects)
                + "<br>상태: " + (todo.isDone() ? "완료" : "미완료")
                + "<br><font color='#868686'>클릭: 수정·삭제</font></html>";
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /**
     * 완료 상태에 따른 모양 변경 (Figma: 찬 동그라미 + 회색 글자)
     */
    private void updateDoneStyle() {
        checkLabel.setIcon(Icons.check(Icons.CheckType.TODO, todo.isDone(), false));
        contentLabel.setForeground(todo.isDone() ? Theme.TEXT_GRAY : Color.BLACK);
    }
}
