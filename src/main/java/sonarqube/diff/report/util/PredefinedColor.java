package sonarqube.diff.report.util;

public enum PredefinedColor {

    GREEN(0, 128, 0),
    LEATHER_ORANGE(237, 125, 49),
    LIGHT_YELLOW(255, 242, 204),
    LIGHT_GREY(237, 237, 237),
    RED(255, 0, 0);

    private final int red;
    private final int green;
    private final int blue;

    PredefinedColor(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }
}
