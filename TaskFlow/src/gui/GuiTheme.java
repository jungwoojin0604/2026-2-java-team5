package gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Swing 화면에서 함께 쓰는 색, 글꼴, 테두리 모음 (GuiTheme)
 *
 * <p>값은 Figma "Task Flow 디자인" 프레임에서 가져왔다.</p>
 * <ul>
 *   <li>왼쪽 패널 배경 #F4F5F7, 탭 바 #E0E1E2, 선택 탭 #9FD7F8, 비선택 탭 #D9D9D9</li>
 *   <li>오른쪽 상단 바 #F7F7F7, 버튼 #D1D1D1 + 검은 1px 테두리</li>
 *   <li>완료된 할 일 글자색 #757575, 보조 아이콘 #A9AEB2</li>
 * </ul>
 *
 * <p>Figma 글꼴은 Roboto지만 Roboto에는 한글 글자가 없어서,
 * 한글을 그릴 수 있는 시스템 글꼴을 골라 쓴다.</p>
 */
public final class GuiTheme {
    private GuiTheme() {
    }

    // ----- 색 (Figma) -----
    public static final Color LEFT_BG = new Color(0xF4F5F7);
    public static final Color TAB_BAR_BG = new Color(0xE0E1E2);
    public static final Color TAB_ACTIVE = new Color(0x9FD7F8);
    public static final Color TAB_INACTIVE = new Color(0xD9D9D9);
    public static final Color HEADER_BG = new Color(0xF7F7F7);
    public static final Color BUTTON_BG = new Color(0xD1D1D1);
    public static final Color LINE = Color.BLACK;
    public static final Color TEXT = Color.BLACK;
    public static final Color TEXT_DONE = new Color(0x757575);
    public static final Color TEXT_HINT = new Color(0x868686);
    public static final Color ICON_GRAY = new Color(0xA9AEB2);
    public static final Color ERROR = new Color(0xD93025);

    // ----- 색 (Figma에 없어 정한 값) -----
    /** 진행률 막대 채움색. docx 스케치의 파란 막대를 따랐다. */
    public static final Color PROGRESS_FILL = new Color(0x3B8EDB);
    /** 행에 마우스를 올렸을 때 배경 */
    public static final Color ROW_HOVER = new Color(0xE8EAED);

    private static String family;

    /**
     * 한글을 그릴 수 있는 글꼴을 고른다.
     * 후보가 하나도 없으면 자바 기본 논리 글꼴(Dialog)을 쓴다.
     */
    private static String family() {
        if (family != null) return family;
        String[] candidates = {
                "Apple SD Gothic Neo", "Malgun Gothic", "맑은 고딕",
                "Noto Sans KR", "Noto Sans CJK KR", "NanumGothic", "나눔고딕"
        };
        String[] installed = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String c : candidates) {
            for (String f : installed) {
                if (f.equalsIgnoreCase(c) && new Font(f, Font.PLAIN, 12).canDisplay('가')) {
                    family = f;
                    return family;
                }
            }
        }
        family = Font.DIALOG;
        return family;
    }

    public static Font font(int style, int size) {
        return new Font(family(), style, size);
    }

    public static Font regular(int size) {
        return font(Font.PLAIN, size);
    }

    public static Font bold(int size) {
        return font(Font.BOLD, size);
    }

    // ----- 테두리 -----
    public static Border line() {
        return new LineBorder(LINE, 1);
    }

    /** 검은 1px 테두리 + 안쪽 여백 (입력칸용) */
    public static Border field() {
        return new CompoundBorder(new LineBorder(LINE, 1), new EmptyBorder(6, 10, 6, 10));
    }

    /**
     * Figma TodoAddBtn 모양의 버튼 (#D1D1D1 배경, 검은 1px 테두리)
     */
    public static JButton grayButton(String text, int fontSize) {
        JButton b = new JButton(text);
        b.setFont(regular(fontSize));
        b.setBackground(BUTTON_BG);
        b.setForeground(TEXT);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(true);
        b.setBorder(line());
        b.setFocusPainted(false);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /**
     * 테두리 없는 글자 버튼 (X, ⋯, 화살표 등)
     */
    public static JButton textButton(String text, Font font) {
        JButton b = new JButton(text);
        b.setFont(font);
        b.setForeground(TEXT);
        b.setBorder(new EmptyBorder(0, 0, 0, 0));
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /**
     * 남은 날짜를 "D-7", "D-Day", "D+2" 형식으로 만든다. (docx 스케치 표기)
     * 날짜가 없으면 빈 문자열.
     */
    public static String dDay(LocalDate target) {
        if (target == null) return "";
        long days = ChronoUnit.DAYS.between(LocalDate.now(), target);
        if (days > 0) return "D-" + days;
        if (days == 0) return "D-Day";
        return "D+" + (-days);
    }

    /** 안티앨리어싱을 켠 Graphics2D 복사본 */
    public static Graphics2D smooth(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g2;
    }
}
