package gui;

import manager.ProjectManager;
import manager.TodoManager;
import model.Project;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * 새 화면 미리보기 실행용 창 (GuiPreview)
 *
 * <p>Figma 프레임(1200 x 750)의 틀을 그대로 두고, 이번에 만든 화면을 붙여 확인한다.</p>
 * <ul>
 *   <li>왼쪽(420px): Calendar / Project 탭 + CardLayout. Project 탭이 {@link ProjectListPanel}.</li>
 *   <li>오른쪽(780px) 위 + 버튼: Calendar 탭에서 쓰는 {@link TodoFormDialog#forCalendar} 창.</li>
 * </ul>
 *
 * <p>Calendar 카드와 오른쪽 할 일 목록은 기존 Figma 디자인 영역이라 자리만 표시한다.
 * 예시 데이터로 시작하며 파일에 저장하지 않는다. (콘솔 버전 res 파일을 건드리지 않음)</p>
 *
 * <p>실행: TaskFlow/src 를 컴파일한 뒤 {@code java gui.GuiPreview}</p>
 */
public class GuiPreview extends JFrame {
    private static final String CARD_CALENDAR = "Calendar";
    private static final String CARD_PROJECT = "Project";

    private final TodoManager todoManager;
    private final ProjectManager projectManager;
    private final CardLayout cards = new CardLayout();
    private final JPanel cardPanel = new JPanel(cards);
    private final TabButton calendarTab = new TabButton("Calendar");
    private final TabButton projectTab = new TabButton("Project");
    private final LocalDate selectedDate = LocalDate.now();

    public GuiPreview(TodoManager todoManager, ProjectManager projectManager) {
        super("TaskFlow");
        this.todoManager = todoManager;
        this.projectManager = projectManager;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.add(buildLeft(), BorderLayout.WEST);
        root.add(buildRight(), BorderLayout.CENTER);
        setContentPane(root);
        root.setPreferredSize(new Dimension(1200, 750));
        pack();
        setLocationRelativeTo(null);
    }

    // ----- 왼쪽: 탭 + 카드 -----
    private JComponent buildLeft() {
        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(GuiTheme.LEFT_BG);
        left.setBorder(new LineBorder(GuiTheme.LINE, 1));
        left.setPreferredSize(new Dimension(420, 750));

        JPanel tabs = new JPanel(new GridLayout(1, 2));   // Figma 메모: GridLayout (꽉차게)
        tabs.setBackground(GuiTheme.TAB_BAR_BG);
        tabs.setPreferredSize(new Dimension(420, 50));
        calendarTab.setBorder(new MatteBorder(0, 0, 1, 1, GuiTheme.LINE));
        projectTab.setBorder(new MatteBorder(0, 0, 1, 0, GuiTheme.LINE));
        calendarTab.addActionListener(e -> showCard(CARD_CALENDAR));
        projectTab.addActionListener(e -> showCard(CARD_PROJECT));
        tabs.add(calendarTab);
        tabs.add(projectTab);

        ProjectListPanel projectPanel = new ProjectListPanel(todoManager, projectManager);
        cardPanel.add(buildCalendarPlaceholder(), CARD_CALENDAR);
        cardPanel.add(projectPanel, CARD_PROJECT);

        left.add(tabs, BorderLayout.NORTH);
        left.add(cardPanel, BorderLayout.CENTER);
        showCard(CARD_PROJECT);
        return left;
    }

    private void showCard(String name) {
        cards.show(cardPanel, name);
        calendarTab.setActive(CARD_CALENDAR.equals(name));
        projectTab.setActive(CARD_PROJECT.equals(name));
    }

    /** 기존 Calendar 화면 자리 (Figma 머리글 "< 2027.11 >" 모양만 표시) */
    private JComponent buildCalendarPlaceholder() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(GuiTheme.LEFT_BG);
        YearMonth ym = YearMonth.from(selectedDate);
        JLabel head = new JLabel("<      " + ym.getYear() + "." + String.format("%02d", ym.getMonthValue()) + "      >",
                SwingConstants.CENTER);
        head.setFont(GuiTheme.bold(30));
        head.setBorder(new EmptyBorder(30, 0, 0, 0));
        JLabel note = new JLabel("<html><center>Calendar 화면 (기존 디자인 영역)<br>이번 작업 범위 밖</center></html>",
                SwingConstants.CENTER);
        note.setFont(GuiTheme.regular(16));
        note.setForeground(GuiTheme.TEXT_HINT);
        p.add(head, BorderLayout.NORTH);
        p.add(note, BorderLayout.CENTER);
        return p;
    }

    // ----- 오른쪽: 날짜 + [+] + [검색] -----
    private JComponent buildRight() {
        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(Color.WHITE);
        right.setBorder(new MatteBorder(1, 0, 1, 1, GuiTheme.LINE));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(GuiTheme.HEADER_BG);
        header.setPreferredSize(new Dimension(780, 50));
        header.setBorder(new EmptyBorder(0, 17, 0, 8));

        JLabel date = new JLabel(selectedDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
        date.setFont(GuiTheme.bold(32));

        JButton add = GuiTheme.grayButton("+", 28);
        add.setPreferredSize(new Dimension(35, 35));
        add.setToolTipText("할 일 추가");
        add.addActionListener(e ->
                TodoFormDialog.forCalendar(this, todoManager, projectManager, selectedDate, this::refreshAll).showBeside(add));

        // 검색칸은 기존 디자인 영역이라 모양만 둔다.
        JLabel search = new JLabel("검색", new SearchIcon(), SwingConstants.LEFT);
        search.setFont(GuiTheme.regular(15));
        search.setForeground(GuiTheme.TEXT_HINT);
        search.setIconTextGap(8);
        search.setOpaque(true);
        search.setBackground(GuiTheme.BUTTON_BG);
        search.setBorder(new CompoundBorder(GuiTheme.line(), new EmptyBorder(0, 6, 0, 6)));
        search.setPreferredSize(new Dimension(167, 35));
        search.setToolTipText("검색 (기존 디자인 영역)");

        JPanel tools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 11, 7));
        tools.setOpaque(false);
        tools.add(add);
        tools.add(search);

        header.add(date, BorderLayout.WEST);
        header.add(tools, BorderLayout.EAST);

        JLabel note = new JLabel("<html><center>할 일 목록 화면 (기존 디자인 영역)<br>이번 작업 범위 밖</center></html>",
                SwingConstants.CENTER);
        note.setFont(GuiTheme.regular(16));
        note.setForeground(GuiTheme.TEXT_HINT);

        right.add(header, BorderLayout.NORTH);
        right.add(note, BorderLayout.CENTER);
        return right;
    }

    private void refreshAll() {
        for (Component c : cardPanel.getComponents()) {
            if (c instanceof ProjectListPanel) ((ProjectListPanel) c).refresh();
        }
    }

    /** 왼쪽 위 탭 버튼. 선택되면 #9FD7F8, 아니면 #D9D9D9. */
    private static class TabButton extends JButton {
        TabButton(String text) {
            super(text);
            setFont(GuiTheme.bold(24));
            setForeground(GuiTheme.TEXT);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setActive(false);
        }

        void setActive(boolean active) {
            setBackground(active ? GuiTheme.TAB_ACTIVE : GuiTheme.TAB_INACTIVE);
        }
    }

    /** 돋보기 아이콘 (18px) */
    private static class SearchIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = GuiTheme.smooth(g);
            g2.setColor(GuiTheme.TEXT);
            g2.setStroke(new BasicStroke(1.6f));
            g2.drawOval(x + 1, y + 1, 12, 12);
            g2.drawLine(x + 11, y + 11, x + 16, y + 16);
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return 18; }

        @Override
        public int getIconHeight() { return 18; }
    }

    /** docx 예시와 같은 데모 데이터 */
    static void loadSample(TodoManager tm, ProjectManager pm) {
        LocalDate today = LocalDate.now();
        pm.addProject(new Project("JAVA 프로젝트", today.plusDays(7).atTime(23, 59)));
        pm.addProject(new Project("선형대수학 prj1", today.plusDays(14).atTime(23, 59)));
        pm.addProject(new Project("자료구조 과제", today.plusDays(3).atTime(18, 0)));

        tm.addTodo("발표자료 제작", today.minusDays(1), null, List.of("JAVA 프로젝트"), true);
        tm.addTodo("프로젝트 코드 오류 수정", today, LocalTime.of(14, 0), List.of("JAVA 프로젝트"), true);
        tm.addTodo("발표 시연 영상 촬영", today.plusDays(3), LocalTime.of(18, 0), List.of("JAVA 프로젝트"), false);
        tm.addTodo("과제물 제출", today.plusDays(7), null, List.of("JAVA 프로젝트"), false);
        tm.addTodo("3장 연습문제 풀기", null, null, List.of("선형대수학 prj1"), false);
        tm.addTodo("공부하기", today, null, List.of(), false);
    }

    public static void main(String[] args) {
        TodoManager tm = new TodoManager(new Scanner(System.in));
        ProjectManager pm = new ProjectManager(new Scanner(System.in), tm);
        tm.setProjectManager(pm);
        loadSample(tm, pm);
        SwingUtilities.invokeLater(() -> new GuiPreview(tm, pm).setVisible(true));
    }
}
