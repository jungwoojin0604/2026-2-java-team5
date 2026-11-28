package manager;

import model.Theme;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 프로그램의 테마를 관리하는 클래스
 *
 * <p>기본 테마 및 사용자 지정 테마 등록과 조회 기능 제공</p>
 * <p>현재 선택된 테마 관리 및 변경 기능 제공</p>
 */
public class ThemeManager {
    private final Map<String, Theme> themes = new LinkedHashMap<>();
    private Theme currentTheme;

    /**
     * 테마 관리 객체 생성 및 기본 테마 초기화
     */
    public ThemeManager() {
        initializeThemes();
        currentTheme = themes.get("기본");
    }

    /**
     * 프로그램에서 제공하는 기본 테마 등록
     */
    private void initializeThemes() {
        Theme defaultTheme = new Theme(
                "기본",
                Color.WHITE,
                Color.WHITE,
                new Color(37, 99, 235),
                Color.BLACK,
                Color.GRAY,
                new Color(241, 245, 249),
                new Font(Font.SANS_SERIF, Font.PLAIN, 14),
                null
        );
        addTheme(defaultTheme);

        // 쿠 테마
        Theme kuTheme = new Theme(
                "쿠",
                new Color(255, 251, 243),
                new Color(255, 245, 223),
                new Color(248, 199, 108),
                new Color(104, 84, 58),
                new Color(133, 116, 93),
                new Color(221, 234, 241),
                new Font(Font.DIALOG, Font.PLAIN, 14),
                "res/themes/ku.png"
        );

        addTheme(kuTheme);
    }

    /**
     * 새로운 테마 등록 또는 기존 테마 교체
     *
     * @param theme 등록할 테마 객체
     */
    public void addTheme(Theme theme) {
        themes.put(theme.getName(), theme);
        if (currentTheme != null && currentTheme.getName().equals(theme.getName())) {
            currentTheme = theme;
        }
    }

    /**
     * 테마 이름을 기준으로 등록된 테마 조회
     *
     * @param name 조회할 테마 이름
     * @return 해당 테마 객체 (존재하지 않으면 null)
     */
    public Theme getTheme(String name) {
        return themes.get(name);
    }

    /**
     * 등록된 전체 테마 목록 반환
     *
     * @return 등록 순서대로 구성된 테마 목록
     */
    public List<Theme> getAllThemes() {
        return new ArrayList<>(themes.values());
    }

    /**
     * 현재 선택된 테마 반환
     *
     * @return 현재 테마 객체
     */
    public Theme getCurrentTheme() {
        return currentTheme;
    }

    /**
     * 테마 이름을 기준으로 현재 테마 변경
     *
     * @param name 변경할 테마 이름
     * @return 변경 성공 시 true, 존재하지 않는 테마는 false
     */
    public boolean setCurrentTheme(String name) {
        Theme theme = themes.get(name);
        if (theme == null) {
            return false;
        }
        currentTheme = theme;
        return true;
    }
}