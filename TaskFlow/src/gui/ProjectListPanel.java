package gui;

import manager.ProjectManager;
import manager.TodoManager;
import model.Project;
import model.Todo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 왼쪽 패널의 Project 탭 카드 (ProjectListPanel)
 *
 * <p>왼쪽 패널 CardLayout에 "Project" 카드로 넣어 쓴다. docx "Projects 화면" 요구사항:</p>
 * <ul>
 *   <li>각 줄에 프로젝트 이름, 디데이, 진행률 막대와 %</li>
 *   <li>이름 왼쪽 화살표를 누르면 그 프로젝트의 전체 할 일이 펼쳐지고, 다시 누르면 접힌다</li>
 *   <li>진행률 = 완료한 할 일 수 / 전체 할 일 수 × 100, 할 일이 없으면 0%</li>
 *   <li>할 일 동그라미를 누르면 완료/미완료가 바뀌고, 완료한 할 일은 회색 + 취소선, 진행률도 바로 바뀐다</li>
 *   <li>펼친 목록 맨 아래 "+ 할 일 추가"를 누르면 그 옆에 할 일 추가 창이 뜬다 (현재 프로젝트 자동 체크)</li>
 *   <li>오른쪽 위 + 버튼: 프로젝트 추가 창</li>
 *   <li>프로젝트 이름 옆 ⋯ 버튼 또는 우클릭: 수정 / 삭제 메뉴 (삭제해도 할 일은 남고 연결만 해제)</li>
 *   <li>할 일 우클릭 또는 ⋯ 버튼: 수정 메뉴 (할 일 추가 창과 같은 모양)</li>
 * </ul>
 */
public class ProjectListPanel extends JPanel {
    private static final int ROW_H = 44;
    private static final int TODO_H = 33;   // Figma 할 일 행 간격 33px
    private static final int BAR_W = 90;

    private final TodoManager todoManager;
    private final ProjectManager projectManager;
    private final JPanel list = new JPanel();
    private final JScrollPane scroll;
    private final Set<String> expanded = new HashSet<>();
    private Runnable onDataChanged;

    public ProjectListPanel(TodoManager todoManager, ProjectManager projectManager) {
        super(new BorderLayout());
        this.todoManager = todoManager;
        this.projectManager = projectManager;
        setBackground(GuiTheme.LEFT_BG);

        // 위쪽: 오른쪽에 + 버튼 (Figma TodoAddBtn 모양, 35x35)
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        bar.setOpaque(false);
        JButton add = GuiTheme.grayButton("+", 28);
        add.setPreferredSize(new Dimension(35, 35));
        add.setToolTipText("프로젝트 추가");
        add.addActionListener(e -> ProjectFormDialog.forAdd(window(), projectManager, this::changed).showBeside(add));
        bar.add(add);
        add(bar, BorderLayout.NORTH);

        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(GuiTheme.LEFT_BG);
        list.setBorder(new EmptyBorder(0, 0, 12, 0));
        JPanel holder = new JPanel(new BorderLayout());   // 목록을 위쪽에 붙이기
        holder.setBackground(GuiTheme.LEFT_BG);
        holder.add(list, BorderLayout.NORTH);
        scroll = new JScrollPane(holder);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        refresh();
    }

    /** 데이터가 바뀌었을 때(추가·수정·삭제·완료 전환) 호출할 함수. 다른 화면 갱신용. */
    public void setOnDataChanged(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
    }

    /** 목록을 다시 그린다. 펼침 상태와 스크롤 위치는 유지한다. */
    public void refresh() {
        int scrollY = scroll.getVerticalScrollBar().getValue();
        list.removeAll();

        Set<Project> projects = projectManager.getAllProjects();
        // 이름이 바뀌거나 삭제된 프로젝트의 펼침 상태 정리
        Set<String> names = new HashSet<>();
        for (Project p : projects) names.add(p.getName());
        expanded.retainAll(names);

        if (projects.isEmpty()) {
            JLabel empty = new JLabel("<html><center>등록된 프로젝트가 없습니다.<br>오른쪽 위 + 버튼으로 추가하세요.</center></html>",
                    SwingConstants.CENTER);
            empty.setFont(GuiTheme.regular(15));
            empty.setForeground(GuiTheme.TEXT_DONE);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            empty.setBorder(new EmptyBorder(60, 0, 0, 0));
            empty.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
            list.add(empty);
        }
        for (Project p : projects) {
            List<Todo> todos = projectManager.getProjectTodos(p.getName());
            list.add(new ProjectRow(p, todos));
            if (expanded.contains(p.getName())) {
                for (Todo t : todos) list.add(new TodoRow(t));
                list.add(new AddTodoRow(p));
            }
        }
        list.revalidate();
        list.repaint();
        SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(scrollY));
    }

    private void changed() {
        refresh();
        if (onDataChanged != null) onDataChanged.run();
    }

    private Window window() {
        return SwingUtilities.getWindowAncestor(this);
    }

    /** 진행률(%) = 완료 수 / 전체 수 × 100, 할 일이 없으면 0 */
    static int progress(List<Todo> todos) {
        if (todos.isEmpty()) return 0;
        int done = 0;
        for (Todo t : todos) if (t.isDone()) done++;
        return (int) Math.round(done * 100.0 / todos.size());
    }

    // ------------------------------------------------------------------
    // 줄 공통: 가로로 꽉 차고 높이 고정, 마우스를 올리면 배경이 옅게 바뀜
    // ------------------------------------------------------------------
    private abstract static class Row extends JPanel {
        Row(int height) {
            super(new GridBagLayout());
            setBackground(GuiTheme.LEFT_BG);
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
            setPreferredSize(new Dimension(100, height));
        }

        /** 줄 전체와 그 안의 모든 컴포넌트에 마우스 반응을 단다. */
        void installHover(MouseAdapter extra) {
            MouseAdapter hover = new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { setBackground(GuiTheme.ROW_HOVER); }

                @Override
                public void mouseExited(MouseEvent e) {
                    Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), Row.this);
                    if (!new Rectangle(getSize()).contains(p)) setBackground(GuiTheme.LEFT_BG);
                }
            };
            addTo(this, hover, extra);
        }

        private static void addTo(Component c, MouseAdapter hover, MouseAdapter extra) {
            c.addMouseListener(hover);
            if (extra != null && !(c instanceof AbstractButton) && !(c instanceof CircleCheck)) {
                c.addMouseListener(extra);
            }
            if (c instanceof Container) {
                for (Component child : ((Container) c).getComponents()) addTo(child, hover, extra);
            }
        }

        static GridBagConstraints at(int x, double weight, Insets in) {
            GridBagConstraints g = new GridBagConstraints();
            g.gridx = x;
            g.gridy = 0;
            g.weightx = weight;
            g.fill = weight > 0 ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
            g.anchor = GridBagConstraints.WEST;
            g.insets = in;
            return g;
        }
    }

    // ------------------------------------------------------------------
    // 프로젝트 줄:  [>] 이름 ........ D-7 [■■■□□] 50% ⋯
    // ------------------------------------------------------------------
    private class ProjectRow extends Row {
        ProjectRow(Project project, List<Todo> todos) {
            super(ROW_H);
            String name = project.getName();
            boolean open = expanded.contains(name);
            setBorder(new MatteBorder(0, 0, 1, 0, new Color(0xE3E5E8)));

            JComponent arrow = new Chevron(open);
            JLabel title = new JLabel(name);
            title.setFont(GuiTheme.bold(19));
            title.setToolTipText(name);
            title.setPreferredSize(new Dimension(10, ROW_H));   // 남는 폭을 모두 받고, 모자라면 "…"로 줄임
            title.setMinimumSize(new Dimension(10, ROW_H));

            JLabel dday = new JLabel(GuiTheme.dDay(project.getDeadLine() == null ? null : project.getDeadLine().toLocalDate()));
            dday.setFont(GuiTheme.regular(14));
            dday.setForeground(GuiTheme.TEXT_DONE);

            int pct = progress(todos);
            ProgressBar barView = new ProgressBar(pct);
            JLabel pctLabel = new JLabel(pct + "%", SwingConstants.RIGHT);
            pctLabel.setFont(GuiTheme.regular(14));
            pctLabel.setPreferredSize(new Dimension(40, 20));
            pctLabel.setMinimumSize(new Dimension(40, 20));

            JButton more = GuiTheme.textButton("⋯", GuiTheme.bold(18));
            more.setToolTipText("수정 / 삭제");
            more.setPreferredSize(new Dimension(28, 28));
            more.addActionListener(e -> projectMenu(project).show(more, 0, more.getHeight()));

            add(arrow, at(0, 0, new Insets(0, 12, 0, 6)));
            add(title, at(1, 1, new Insets(0, 0, 0, 8)));
            add(dday, at(2, 0, new Insets(0, 0, 0, 10)));
            add(barView, at(3, 0, new Insets(0, 0, 0, 0)));
            add(pctLabel, at(4, 0, new Insets(0, 4, 0, 2)));
            add(more, at(5, 0, new Insets(0, 2, 0, 8)));

            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            installHover(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) { popup(e); }

                @Override
                public void mouseReleased(MouseEvent e) { popup(e); }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) toggle(name);
                }

                private void popup(MouseEvent e) {
                    if (e.isPopupTrigger()) projectMenu(project).show(e.getComponent(), e.getX(), e.getY());
                }
            });
        }
    }

    private void toggle(String name) {
        if (!expanded.remove(name)) expanded.add(name);
        refresh();
    }

    private JPopupMenu projectMenu(Project project) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem edit = menuItem("수정");
        JMenuItem delete = menuItem("삭제");
        edit.addActionListener(e ->
                ProjectFormDialog.forEdit(window(), projectManager, project, this::changed).showBeside(anchorFor(project)));
        delete.addActionListener(e -> {
            int answer = JOptionPane.showConfirmDialog(window(),
                    "'" + project.getName() + "' 프로젝트를 삭제할까요?\n연결된 할 일은 지워지지 않고 연결만 해제됩니다.",
                    "프로젝트 삭제", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (answer == JOptionPane.YES_OPTION) {
                // ProjectManager.deleteProject: 할 일의 태그에서 이 프로젝트만 지운다
                projectManager.deleteProject(project.getName());
                changed();
            }
        });
        menu.add(edit);
        menu.add(delete);
        return menu;
    }

    /** 수정 창을 띄울 기준 줄 (해당 프로젝트 줄) */
    private Component anchorFor(Project project) {
        for (Component c : list.getComponents()) {
            if (c instanceof ProjectRow && c.isShowing()) {
                JLabel title = (JLabel) ((ProjectRow) c).getComponent(1);
                if (title.getText().equals(project.getName())) return c;
            }
        }
        return this;
    }

    private static JMenuItem menuItem(String text) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(GuiTheme.regular(14));
        return item;
    }

    // ------------------------------------------------------------------
    // 할 일 줄:      ( ) 할 일 내용   D-3  ⋯
    // ------------------------------------------------------------------
    private class TodoRow extends Row {
        TodoRow(Todo todo) {
            super(TODO_H);
            CircleCheck check = new CircleCheck(CircleCheck.Style.DOT, 20, todo.isDone());
            check.setToolTipText(todo.isDone() ? "미완료로 바꾸기" : "완료로 바꾸기");
            check.addChangeListener(done -> {
                todo.setDone(done);
                changed();   // 취소선과 진행률을 바로 반영
            });

            JLabel content = new JLabel(todo.getContent());
            content.setPreferredSize(new Dimension(10, TODO_H));
            content.setMinimumSize(new Dimension(10, TODO_H));
            content.setToolTipText(todo.getContent());
            if (todo.isDone()) {
                Map<TextAttribute, Object> attr = new HashMap<>();
                attr.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
                content.setFont(GuiTheme.regular(16).deriveFont(attr));
                content.setForeground(GuiTheme.TEXT_DONE);
            } else {
                content.setFont(GuiTheme.regular(16));
            }

            // 완료한 할 일은 디데이를 숨기고, Anytime은 "Anytime"으로 표시
            String info = todo.isDone() ? "" : (todo.getDate() == null ? "Anytime" : GuiTheme.dDay(todo.getDate()));
            JLabel dday = new JLabel(info);
            dday.setFont(GuiTheme.regular(14));
            dday.setForeground(GuiTheme.TEXT_DONE);

            JButton more = GuiTheme.textButton("⋯", GuiTheme.bold(16));
            more.setToolTipText("수정");
            more.setPreferredSize(new Dimension(28, 24));
            more.addActionListener(e -> todoMenu(todo, TodoRow.this).show(more, 0, more.getHeight()));

            add(check, at(0, 0, new Insets(0, 36, 0, 10)));
            add(content, at(1, 1, new Insets(0, 0, 0, 8)));
            add(dday, at(2, 0, new Insets(0, 0, 0, 6)));
            add(more, at(3, 0, new Insets(0, 0, 0, 8)));

            installHover(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) { popup(e); }

                @Override
                public void mouseReleased(MouseEvent e) { popup(e); }

                private void popup(MouseEvent e) {
                    if (e.isPopupTrigger()) todoMenu(todo, TodoRow.this).show(e.getComponent(), e.getX(), e.getY());
                }
            });
        }
    }

    private JPopupMenu todoMenu(Todo todo, Component anchor) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem edit = menuItem("수정");
        edit.addActionListener(e ->
                TodoFormDialog.forEdit(window(), todoManager, projectManager, todo, this::changed).showBeside(anchor));
        menu.add(edit);
        return menu;
    }

    // ------------------------------------------------------------------
    // "+ 할 일 추가" 줄: 누르면 바로 옆에 할 일 추가 창
    // ------------------------------------------------------------------
    private class AddTodoRow extends Row {
        AddTodoRow(Project project) {
            super(TODO_H + 4);
            JLabel label = new JLabel("+  할 일 추가");
            label.setFont(GuiTheme.regular(15));
            label.setForeground(GuiTheme.TEXT_HINT);
            label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            add(label, at(0, 0, new Insets(0, 38, 0, 0)));
            add(Box.createHorizontalGlue(), at(1, 1, new Insets(0, 0, 0, 0)));
            installHover(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    TodoFormDialog.forProject(window(), todoManager, projectManager, project,
                            ProjectListPanel.this::changed).showBeside(AddTodoRow.this);
                }
            });
        }
    }

    // ------------------------------------------------------------------
    // 작은 그림 부품
    // ------------------------------------------------------------------

    /** 접힘(>) / 펼침(v) 화살표. Figma의 접기 버튼 모양을 따랐다. */
    private static class Chevron extends JComponent {
        private final boolean open;

        Chevron(boolean open) {
            this.open = open;
            Dimension d = new Dimension(16, 16);
            setPreferredSize(d);
            setMinimumSize(d);
            setToolTipText(open ? "접기" : "펼치기");
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = GuiTheme.smooth(g);
            g2.setColor(GuiTheme.TEXT);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            if (open) {
                g2.drawPolyline(new int[]{cx - 5, cx, cx + 5}, new int[]{cy - 3, cy + 3, cy - 3}, 3);
            } else {
                g2.drawPolyline(new int[]{cx - 3, cx + 3, cx - 3}, new int[]{cy - 5, cy, cy + 5}, 3);
            }
            g2.dispose();
        }
    }

    /** 진행률 막대: 흰 바탕 + 검은 1px 테두리 + 파란 채움 */
    private static class ProgressBar extends JComponent {
        private final int percent;

        ProgressBar(int percent) {
            this.percent = Math.max(0, Math.min(100, percent));
            setPreferredSize(new Dimension(BAR_W, 12));
            setMinimumSize(new Dimension(BAR_W, 12));
            setToolTipText("진행률 " + this.percent + "%");
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth() - 1;
            int h = getHeight() - 1;
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, w, h);
            g2.setColor(GuiTheme.PROGRESS_FILL);
            g2.fillRect(1, 1, Math.round((w - 1) * percent / 100f), h - 1);
            g2.setColor(GuiTheme.LINE);
            g2.drawRect(0, 0, w, h);
            g2.dispose();
        }
    }
}
