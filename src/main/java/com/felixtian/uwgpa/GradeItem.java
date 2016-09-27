package com.felixtian.uwgpa;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Felix on 2016/9/26.
 */

public class GradeItem implements Parcelable {
    private String courseCode;
    private String grade;
    private String GPA;

    public GradeItem(String courseCode, String grade, String GPA) {
        this.courseCode = courseCode;
        this.grade = grade;
        this.GPA = GPA;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getGPA() {
        return GPA;
    }

    public void setGPA(String GPA) {
        this.GPA = GPA;
    }


    protected GradeItem(Parcel in) {
        courseCode = in.readString();
        grade = in.readString();
        GPA = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(courseCode);
        dest.writeString(grade);
        dest.writeString(GPA);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<GradeItem> CREATOR = new Parcelable.Creator<GradeItem>() {
        @Override
        public GradeItem createFromParcel(Parcel in) {
            return new GradeItem(in);
        }

        @Override
        public GradeItem[] newArray(int size) {
            return new GradeItem[size];
        }
    };
}
