package Gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

/**
 * Figma 디자인(Task Flow 디자인)의 색상, 글꼴, 날짜 표시 형식을 모아 둔 클래스
 *
 * <p>모든 화면이 같은 값을 쓰도록 한곳에서 관리</p>
 */
public final class Theme {

    private Theme() {
    }

    // ===== 색상 (Figma 값 그대로) =====
    /** 왼쪽 패널 배경 */
    public static final Color PANEL_BG = new Color(0xF4F5F7);
    /** 왼쪽 탭 바 배경 */
    public static final Color TAB_BAR_BG = new Color(0xE0E1E2);
    /** 선택된 탭, 선택한 날짜, 추가 버튼 */
    public static final Color ACCENT = new Color(0x9FD7F8);
    /** 선택되지 않은 탭 */
    public static final Color TAB_OFF = new Color(0xD9D9D9);
    /** 오른쪽 상단 바 배경 */
    public static final Color HEADER_BG = new Color(0xF7F7F7);
    /** + 버튼, 검색칸 배경 */
    public static final Color BUTTON_GRAY = new Color(0xD1D1D1);
    /** 선택한 프로젝트 행 배경 */
    public static final Color ROW_SELECTED = new Color(0xDCEFFB);
    /** 프로젝트 행 사이 구분선 */
    public static final Color ROW_DIVIDER = new Color(0xE3E5E8);
    /** 달력 칸 선 */
    public static final Color GRID_LINE = new Color(0xE0E1E2);
    /** 달력 요일 줄 배경 */
    public static final Color WEEKDAY_BG = new Color(0xE9EAEC);
    /** 진행률 막대 채움 */
    public static final Color PROGRESS = new Color(0x3B8EDB);
    /** 창 안 구분선 */
    public static final Color DIVIDER = new Color(0xE0E0E0);

    /** 일요일, 오류, 삭제 */
    public static final Color RED = new Color(0xD93025);
    /** 완료한 할 일, 보조 글자 */
    public static final Color TEXT_GRAY = new Color(0x757575);
    /** 입력칸 이름표, 검색 안내 문구 */
    public static final Color TEXT_LABEL = new Color(0x868686);
    /** 할 일 옆 날짜 */
    public static final Color TEXT_DATE = new Color(0xA0A0A0);
    /** 다른 달 날짜 */
    public static final Color TEXT_MUTED = new Color(0xBDBDBD);
    /** 끌 수 있는 동그라미 체크 테두리 */
    public static final Color CHECK_OFF = new Color(0xB5B5B5);
    /** 잠긴 동그라미 체크 */
    public static final Color CHECK_LOCKED = new Color(0xA8A8A8);

    // ===== 글꼴 =====
    // Figma는 Roboto지만 한글이 없어서 운영체제별 한글 글꼴을 순서대로 찾음
    private static final List<String> FONT_CANDIDATES = Arrays.asList(
            "Apple SD Gothic Neo", "Malgun Gothic", "맑은 고딕",
            "Noto Sans CJK KR", "Noto Sans KR", "NanumGothic", "나눔고딕");

    private static final String FONT_FAMILY = findFontFamily();

    private static String findFontFamily() {
        try {
            List<String> installed = Arrays.asList(GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getAvailableFontFamilyNames());
            for (String name : FONT_CANDIDATES) {
                if (installed.contains(name)) {
                    return name;
                }
            }
        } catch (Exception ignored) {
            // 화면이 없는 환경에서는 기본 글꼴 사용
        }
        return Font.SANS_SERIF;
    }

    /** 보통 굵기 글꼴 */
    public static Font plain(float size) {
        return new Font(FONT_FAMILY, Font.PLAIN, 12).deriveFont(size);
    }

    /** 굵은 글꼴 (Figma의 Bold, ExtraBold) */
    public static Font bold(float size) {
        return new Font(FONT_FAMILY, Font.BOLD, 12).deriveFont(size);
    }

    // ===== 날짜 표시 =====
    /** 오른쪽 상단 날짜, 할 일 옆 날짜 (2026.11.25) */
    public static final DateTimeFormatter DOT_DATE = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    /** 입력창 날짜칸 (2026-11-25) */
    public static final DateTimeFormatter DASH_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /** 시간 (18:30) */
    public static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 오늘 기준 D-Day 문자열
     *
     * @param date 기준 날짜 (null이면 빈 문자열)
     * @return 미래 D-n, 오늘 D-Day, 지난 날짜 D+n
     */
    public static String dDay(LocalDate date) {
        if (date == null) {
            return "";
        }
        long days = ChronoUnit.DAYS.between(LocalDate.now(), date);
        if (days > 0) {
            return "D-" + days;
        } else if (days == 0) {
            return "D-Day";
        }
        return "D+" + (-days);
    }
}
