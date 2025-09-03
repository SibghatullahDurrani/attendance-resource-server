package com.main.face_recognition_resource_server.constants.attendance;

public enum AttendanceRetakeCron {
    EVERY_HOUR("0 0 * * * *"),
    EVERY_TWO_HOURS("0 0 */2 * * *"),
    EVERY_THREE_HOURS("0 0 */3 * * *"),
    EVERY_FOUR_HOURS("0 0 */4 * * *"),
    EVERY_FIVE_HOURS("0 0 */5 * * *"),
    EVERY_DAY("0 0 0 * * *");

    public final String label;

    AttendanceRetakeCron(String label) {
        this.label = label;
    }

    public static String getCron(int hours) {
        if (hours == 1) return EVERY_HOUR.label;
        if (hours == 2) return EVERY_TWO_HOURS.label;
        if (hours == 3) return EVERY_FOUR_HOURS.label;
        if (hours == 4) return EVERY_FOUR_HOURS.label;
        if (hours == 5) return EVERY_FIVE_HOURS.label;
        if (hours == 0) return EVERY_DAY.label;
        return EVERY_DAY.label;
    }
}
