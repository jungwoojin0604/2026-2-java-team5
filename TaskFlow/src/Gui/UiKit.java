package Gui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicMenuItemUI;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.time.LocalDate;

/**
 * Figma Components를 Swing 컴포넌트로 만든 공통 부품 모음
 *
 * <p>+ 버튼, 검색칸, 추가 버튼, 동그라미 선택, 진행률 막대, ⋯ 메뉴,
 * 입력칸, 날짜칸, 시간칸을 같은 모양으로 만들어 줌</p>
 */
public final class UiKit {

    private UiKit() {
    }

    /** 검은 1px 테두리 */
    public static Border blackLine() {
        return BorderFactory.createLineBorder(Color.BLACK, 1);
    }

    /** 글자 하나를 원하는 글꼴·색으로 만든 JLabel */
    public static JLabel label(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    /** 마우스로 누를 수 있게 만들기 (손가락 커서 + 클릭 동작) */
    public static void onClick(JComponent component, Runnable action) {
        component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (component.isEnabled() && e.getButton() == MouseEvent.BUTTON1) {
                    action.run();
                }
            }
        });
    }

    /** 35x35 회색 + 버튼 (Figma Button/Plus) */
    public static JButton plusButton(String tooltip) {
        JButton button = new JButton(Icons.plus(20));
        button.setToolTipText(tooltip);
        button.setPreferredSize(new Dimension(35, 35));
        button.setMinimumSize(new Dimension(35, 35));
        button.setMaximumSize(new Dimension(35, 35));
        button.setBackground(Theme.BUTTON_GRAY);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorder(blackLine());
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    /** 76x36 하늘색 버튼 (창 아래 "추가"/"저장") */
    public static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(Theme.bold(15));
        button.setForeground(Color.BLACK);
        button.setBackground(Theme.ACCENT);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorder(blackLine());
        button.setPreferredSize(new Dimension(76, 36));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    /** 검은 테두리 입력칸 (안쪽 여백 9/12, 16pt) */
    public static JTextField textInput() {
        JTextField field = new JTextField();
        field.setFont(Theme.plain(16));
        field.setBorder(BorderFactory.createCompoundBorder(
                blackLine(), BorderFactory.createEmptyBorder(8, 11, 8, 11)));
        field.setPreferredSize(new Dimension(340, 38));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    /** 90x12 진행률 막대 (JProgressBar 모양만 Figma대로 바꿈) */
    public static JProgressBar progressBar(int value) {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(value);
        bar.setBorderPainted(false);
        bar.setUI(new BasicProgressBarUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                int w = c.getWidth();
                int h = c.getHeight();
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, w, h);
                int fill = (int) Math.round((w - 1) * bar.getPercentComplete());
                g.setColor(Theme.PROGRESS);
                g.fillRect(0, 0, fill, h);
                g.setColor(Color.BLACK);
                g.drawRect(0, 0, w - 1, h - 1);
            }
        });
        Dimension size = new Dimension(90, 12);
        bar.setPreferredSize(size);
        bar.setMinimumSize(size);
        bar.setMaximumSize(size);
        return bar;
    }

    /**
     * 흰 바탕, 검은 테두리의 ⋯ 메뉴 (Figma ContextMenu)
     *
     * @param labels  메뉴 이름 (이름이 "삭제"면 빨간 글자)
     * @param actions 메뉴를 눌렀을 때 실행할 동작
     */
    public static JPopupMenu contextMenu(String[] labels, Runnable[] actions) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(Color.WHITE);
        menu.setBorder(BorderFactory.createCompoundBorder(
                blackLine(), BorderFactory.createEmptyBorder(4, 0, 4, 0)));
        for (int i = 0; i < labels.length; i++) {
            JMenuItem item = new JMenuItem(labels[i]);
            item.setUI(new BasicMenuItemUI() {
                @Override
                protected void installDefaults() {
                    super.installDefaults();
                    selectionBackground = Theme.ROW_SELECTED;
                    selectionForeground = menuItem.getForeground();
                }
            });
            item.setFont(Theme.plain(15));
            item.setForeground("삭제".equals(labels[i]) ? Theme.RED : Color.BLACK);
            item.setBackground(Color.WHITE);
            item.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
            item.setPreferredSize(new Dimension(120, 32));
            Runnable action = actions[i];
            item.addActionListener(e -> action.run());
            menu.add(item);
        }
        return menu;
    }

    /**
     * 동그라미 + 글자로 된 선택 항목 (Figma toggle: Anytime, 사용, 연결된 프로젝트)
     */
    public static class CheckToggle extends JLabel {
        private boolean selected;
        private boolean locked;
        private Runnable onChange = () -> {
        };

        public CheckToggle(String text, boolean selected) {
            super(text);
            this.selected = selected;
            setFont(Theme.plain(15));
            setIconTextGap(8);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            updateLook();
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!locked && isEnabled()) {
                        setSelected(!CheckToggle.this.selected);
                        onChange.run();
                    }
                }
            });
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            updateLook();
        }

        /** 잠금: 회색 체크로 고정되어 눌러도 바뀌지 않음 (현재 프로젝트) */
        public void setLocked(boolean locked) {
            this.locked = locked;
            if (locked) {
                selected = true;
            }
            setCursor(Cursor.getPredefinedCursor(locked ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));
            updateLook();
        }

        public void setOnChange(Runnable onChange) {
            this.onChange = onChange;
        }

        private void updateLook() {
            setIcon(Icons.check(Icons.CheckType.TOGGLE, selected, locked));
            setForeground(locked ? Theme.TEXT_GRAY : Color.BLACK);
        }
    }

    /**
     * 167x35 회색 검색칸 (돋보기 + "검색" 안내 문구)
     *
     * <p>누르면 바로 글자를 입력할 수 있고, 비어 있을 때만 "검색"이 보임</p>
     */
    public static class SearchBox extends JPanel {
        private final JTextField field;

        public SearchBox() {
            super(new BorderLayout());
            setBackground(Theme.BUTTON_GRAY);
            setBorder(blackLine());
            Dimension size = new Dimension(167, 35);
            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);

            JLabel icon = new JLabel(Icons.search());
            icon.setPreferredSize(new Dimension(34, 33));
            icon.setHorizontalAlignment(SwingConstants.CENTER);
            add(icon, BorderLayout.WEST);

            field = new JTextField() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (getText().isEmpty() && !isFocusOwner()) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                        g2.setColor(Theme.TEXT_LABEL);
                        g2.setFont(getFont());
                        Insets in = getInsets();
                        int y = (getHeight() + g2.getFontMetrics().getAscent()
                                - g2.getFontMetrics().getDescent()) / 2;
                        g2.drawString("검색", in.left, y);
                        g2.dispose();
                    }
                }
            };
            field.setOpaque(false);
            field.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));
            field.setFont(Theme.plain(15));
            field.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    field.repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    field.repaint();
                }
            });
            add(field, BorderLayout.CENTER);

            // 돋보기를 눌러도 입력 시작
            icon.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
            icon.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    field.requestFocusInWindow();
                }
            });
        }

        public JTextField getField() {
            return field;
        }

        public String getText() {
            return field.getText().trim();
        }
    }

    /**
     * 날짜칸 (달력 아이콘 + 날짜 + 아래 화살표). 누르면 Figma DatePicker가 아래에 뜸
     */
    public static class DateInput extends JPanel {
        private final DatePickerPanel picker;
        private final JLabel text;
        private final JPopupMenu popup;
        private Runnable onChange = () -> {
        };

        public DateInput(LocalDate initial) {
            super(new BorderLayout(8, 0));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    blackLine(), BorderFactory.createEmptyBorder(8, 11, 8, 9)));
            setPreferredSize(new Dimension(340, 38));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            picker = new DatePickerPanel(initial);
            text = label("", Theme.plain(16), Color.BLACK);
            add(new JLabel(Icons.calendar()), BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
            add(new JLabel(Icons.chevronDown()), BorderLayout.EAST);
            refreshText();

            popup = new JPopupMenu();
            popup.setBorder(BorderFactory.createEmptyBorder());
            popup.add(picker);

            picker.setOnDateSelected(() -> {
                refreshText();
                popup.setVisible(false);
                onChange.run();
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (isEnabled()) {
                        popup.show(DateInput.this, 0, getHeight() + 2);
                    }
                }
            });
        }

        private void refreshText() {
            text.setText(picker.getSelectedDate().format(Theme.DASH_DATE));
        }

        public LocalDate getDate() {
            return picker.getSelectedDate();
        }

        public void setOnChange(Runnable onChange) {
            this.onChange = onChange;
        }
    }

    /**
     * 76x38 숫자 입력칸 + 위·아래 화살표 (시간 "시", "분")
     *
     * <p>화살표 위쪽/아래쪽 클릭, 마우스 휠, 직접 입력 모두 가능.
     * 범위를 넘으면 반대편으로 돌아감 (23 → 0)</p>
     */
    public static class TimeSpinner extends JPanel {
        private final JTextField field;
        private final int max;

        public TimeSpinner(int value, int max) {
            super(new BorderLayout(4, 0));
            this.max = max;
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    blackLine(), BorderFactory.createEmptyBorder(0, 13, 0, 5)));
            Dimension size = new Dimension(76, 38);
            setPreferredSize(size);
            setMaximumSize(size);

            field = new JTextField(String.format("%02d", value));
            field.setFont(Theme.plain(18));
            field.setBorder(BorderFactory.createEmptyBorder());
            field.setOpaque(false);
            add(field, BorderLayout.CENTER);

            JLabel arrows = new JLabel(Icons.spin());
            arrows.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            arrows.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    step(e.getY() < arrows.getHeight() / 2 ? 1 : -1);
                }
            });
            add(arrows, BorderLayout.EAST);

            addMouseWheelListener((MouseWheelEvent e) -> step(e.getWheelRotation() < 0 ? 1 : -1));
            field.addMouseWheelListener((MouseWheelEvent e) -> step(e.getWheelRotation() < 0 ? 1 : -1));
        }

        private void step(int delta) {
            int current;
            try {
                current = Integer.parseInt(field.getText().trim());
            } catch (NumberFormatException e) {
                current = 0;
            }
            int next = ((current + delta) % (max + 1) + (max + 1)) % (max + 1);
            field.setText(String.format("%02d", next));
        }

        /**
         * 입력된 숫자 반환
         *
         * @return 0~max 사이 숫자, 형식이 틀리면 -1
         */
        public int getValue() {
            try {
                int value = Integer.parseInt(field.getText().trim());
                return (value >= 0 && value <= max) ? value : -1;
            } catch (NumberFormatException e) {
                return -1;
            }
        }
    }

    /** 세로로 쌓는 투명 패널 */
    public static JPanel column() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        return panel;
    }

    /** 왼쪽 글자 + 오른쪽 부품을 양 끝에 둔 한 줄 (예: "일정 설정 ........ ○ Anytime") */
    public static JPanel spread(JComponent left, JComponent right) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (left != null) {
            row.add(left, BorderLayout.WEST);
        }
        if (right != null) {
            row.add(right, BorderLayout.EAST);
        }
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    /** 세로 간격 */
    public static Component gap(int height) {
        return Box.createVerticalStrut(height);
    }

    /** 아이콘만 있는 투명 버튼 (X 닫기, 화살표 등) */
    public static JButton iconButton(Icon icon) {
        JButton button = new JButton(icon);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }
}
