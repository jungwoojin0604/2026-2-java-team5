package model;

import java.awt.Color;
import java.awt.Font;

/**
 * 프로그램의 테마 정보를 관리하는 클래스
 *
 * <p>테마 이름, 배경색, 패널 색상, 강조색,
 * 글자색, 버튼 색상 및 폰트 정보 저장</p>
 *
 * <p>달력 하단에 표시할 이미지 경로 관리</p>
 *
 * <p>기본 테마 및 사용자 지정 테마에 공통으로 사용 가능</p>
 */
public class Theme {
    private final String name;
    // 테마 색상
    private final Color backgroundColor;
    private final Color panelColor;
    private final Color accentColor;
    private final Color textColor;
    private final Color secondaryTextColor;
    private final Color buttonColor;

    // 테마 폰트
    private final Font font;

    // 달력 하단 이미지 경로
    private final String imagePath;

    /**
     * 새로운 테마 객체 생성
     *
     * @param name 테마 이름
     * @param backgroundColor 전체 배경색
     * @param panelColor 패널 배경색
     * @param accentColor 강조색
     * @param textColor 기본 글자색
     * @param secondaryTextColor 보조 글자색
     * @param buttonColor 버튼 배경색
     * @param font 테마 기본 폰트
     * @param imagePath 이미지 경로 (이미지가 없으면 null)
     */
    public Theme(String name,
                 Color backgroundColor,
                 Color panelColor,
                 Color accentColor,
                 Color textColor,
                 Color secondaryTextColor,
                 Color buttonColor,
                 Font font,
                 String imagePath) {

        this.name = name;
        this.backgroundColor = backgroundColor;
        this.panelColor = panelColor;
        this.accentColor = accentColor;
        this.textColor = textColor;
        this.secondaryTextColor = secondaryTextColor;
        this.buttonColor = buttonColor;
        this.font = font;
        this.imagePath = imagePath;
    }

    public String getName() {
        return name;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public Color getPanelColor() {
        return panelColor;
    }

    public Color getAccentColor() {
        return accentColor;
    }

    public Color getTextColor() {
        return textColor;
    }

    public Color getSecondaryTextColor() {
        return secondaryTextColor;
    }

    public Color getButtonColor() {
        return buttonColor;
    }

    public Font getFont() {
        return font;
    }

    public String getImagePath() {
        return imagePath;
    }

    /**
     * 테마 이름 반환
     *
     * <p>테마 선택 목록에서 테마 이름 표시 용도로 사용</p>
     *
     * @return 테마 이름
     */
    @Override
    public String toString() {
        return name;
    }
}