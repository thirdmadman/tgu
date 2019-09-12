package com.thirdmadman.tgu;

/**
 * Created by third on 30.11.2017.
 */

public class WeeklyTimetable {
    
    private String[][] timetable, timetableIds;
    private String weekNumber = "0";
    private String weekId = "0";

    public WeeklyTimetable(String[][] inputTimetable, String[][] timetableIds) {
        this.timetable = inputTimetable;
        this.timetableIds = timetableIds;
    }

    public void setWeekNumber(String weekNumber) {
        this.weekNumber = weekNumber;
    }

    public void setWeekId(String weekId) {
        this.weekId = weekId;
    }

    public String getWeekNumber() {
        return weekNumber;
    }

    public String getWeekId() {
        return weekId;
    }

    public String getDayLessons(int dayNumber) {
        String getDayLessons = "";
        for (int i = 0; i < 8; i++) {
            if (i == 0) {
                getDayLessons += "\n" + timetable[dayNumber - 1][i] + "\n";
            } else {
                getDayLessons += i + ". " + getLessonStartTime(i) + " " + timetable[dayNumber - 1][i] + "\n";
            }
        }
        return getDayLessons;
    }

    public String[] getDayLessonsIds(int dayNumber) {
        String[] dayLessonsIds = new String[8];
        for (int i = 1; i < 8; i++) {
            dayLessonsIds[i] = timetableIds[dayNumber - 1][i];
        }
        return dayLessonsIds;
    }

    private String getLessonStartTime(int lessonNumber) {
        String times[] = {"08:30", "10:15", "12:45", "14:30", "16:15", "18:00", "19:45"};
        if (lessonNumber < 9 && lessonNumber > 0)
            return times[lessonNumber - 1];
        else {
            return "error of time finding";
        }
    }

    public String[] getTimetableArrayForWeek() {
        String[] timetableArrayForWeek = new String[7];
        for (int i = 1; i < 8; i++) {
            timetableArrayForWeek[i - 1] = getDayLessons(i);
        }
        return timetableArrayForWeek;
    }

    public String[][] getTimetableIdsArrayForWeek() {
        String[][] timetableArrayForWeek = new String[8][8];
        for (int i = 1; i < 8; i++) {
            timetableArrayForWeek[i - 1] = getDayLessonsIds(i);
        }
        return timetableArrayForWeek;
    }
}
