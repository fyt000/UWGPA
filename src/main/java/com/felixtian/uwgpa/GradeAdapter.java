package com.felixtian.uwgpa;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Felix on 2016/9/26.
 */

public class GradeAdapter extends ArrayAdapter<GradeItem> {
    public GradeAdapter(Context context, ArrayList objects) {
        super(context, R.layout.grade_row, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater gradeInflator = LayoutInflater.from(getContext());
        View thisView = gradeInflator.inflate(R.layout.grade_row,parent,false);

        GradeItem gradeItem = getItem(position);
        TextView course = (TextView)thisView.findViewById(R.id.courseCodeView);
        TextView grade = (TextView)thisView.findViewById(R.id.courseGradeView);
        TextView gpa = (TextView) thisView.findViewById(R.id.courseGPAView);
        course.setText(gradeItem.getCourseCode());
        grade.setText(gradeItem.getGrade());
        gpa.setText(gradeItem.getGPA());
        return thisView;
    }
}