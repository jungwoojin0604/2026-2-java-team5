package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 할 일 창과 프로젝트 창이 함께 쓰는 입력 화면 조각 (FormKit)
 *
 * <p>두 창의 글꼴·간격을 똑같이 맞추기 위해 한곳에 모았다.</p>
 */
final class FormKit {
    private FormKit() {
    }

    /** 창 내용 너비 */
    static final int WIDTH = 360;

    /** 왼쪽 정렬된 세로 묶음 */
    static JPanel column() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    /** 위쪽: 제목 + 오른쪽 X 버튼 */
    static JPanel header(String title, Runnable onClose) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(GuiTheme.bold(20));
        JButton x = GuiTheme.textButton("", GuiTheme.regular(18));
        x.setIcon(new CloseIcon());
        x.setPreferredSize(new Dimension(24, 24));
        x.setToolTipText("저장하지 않고 닫기");
        x.addActionListener(e -> onClose.run());
        p.add(t, BorderLayout.WEST);
        p.add(x, BorderLayout.EAST);
        return fullWidth(p);
    }

    /** 작은 회색 입력 이름표 (예: "할 일 내용 *") */
    static JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(GuiTheme.regular(14));
        l.setForeground(GuiTheme.TEXT_HINT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(0, 0, 6, 0));
        return l;
    }

    /** 굵은 구역 제목 (예: "일정 설정") */
    static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(GuiTheme.bold(15));
        return l;
    }

    static JTextField textField(String value) {
        JTextField f = new JTextField(value == null ? "" : value);
        f.setFont(GuiTheme.regular(15));
        f.setBorder(GuiTheme.field());
        return fullWidth(f);
    }

    /**
     * 동그라미 체크 + 글자. 글자를 눌러도 체크가 바뀐다.
     */
    static JPanel checkRow(CircleCheck check, String text, Color color) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(text);
        l.setFont(GuiTheme.regular(15));
        l.setForeground(color);
        l.setBorder(new EmptyBorder(0, 8, 0, 0));
        l.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                check.toggleByUser();
            }
        });
        p.add(check);
        p.add(l);
        return p;
    }

    /** 왼쪽에 구역 제목, 오른쪽에 체크 묶음이 있는 한 줄 */
    static JPanel sectionRow(String title, JComponent right) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(sectionLabel(title), BorderLayout.WEST);
        if (right != null) p.add(right, BorderLayout.EAST);
        return fullWidth(p);
    }

    /** 구역 사이 가는 회색 선 */
    static JComponent divider() {
        JPanel line = new JPanel();
        line.setBackground(new Color(0xE0E0E0));
        line.setPreferredSize(new Dimension(WIDTH, 1));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        return line;
    }

    static Component gap(int h) {
        Component c = Box.createVerticalStrut(h);
        ((JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    /** 빨간 안내 문구. 내용이 없으면 숨는다. */
    static JLabel errorLabel() {
        JLabel l = new JLabel(" ");
        l.setFont(GuiTheme.regular(13));
        l.setForeground(GuiTheme.ERROR);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setVisible(false);
        return l;
    }

    /** 오른쪽 아래 "추가" 버튼. 선택 탭 색(#9FD7F8) + 검은 테두리. */
    static JButton primaryButton(String text) {
        JButton b = GuiTheme.grayButton(text, 15);
        b.setFont(GuiTheme.bold(15));
        b.setBackground(GuiTheme.TAB_ACTIVE);
        b.setPreferredSize(new Dimension(76, 34));
        return b;
    }

    static JPanel footer(JButton button) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        p.setOpaque(false);
        p.add(button);
        return fullWidth(p);
    }

    /** X 모양 아이콘 (14px). 글꼴마다 ✕ 글자가 없을 수 있어 직접 그린다. */
    static class CloseIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = GuiTheme.smooth(g);
            g2.setColor(GuiTheme.TEXT);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x + 2, y + 2, x + 12, y + 12);
            g2.drawLine(x + 12, y + 2, x + 2, y + 12);
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return 14; }

        @Override
        public int getIconHeight() { return 14; }
    }

    /** 너비는 고정하고 높이는 내용에 맞추는 감싸개 */
    static JPanel fixedWidth(JComponent content, int width) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(width, super.getPreferredSize().height);
            }
        };
        p.setOpaque(false);
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    /** 세로 BoxLayout 안에서 가로로 꽉 차고 세로로는 늘어나지 않게 한다. */
    static <T extends JComponent> T fullWidth(T c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        return c;
    }
}
