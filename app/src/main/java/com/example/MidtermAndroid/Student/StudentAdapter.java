package com.example.MidtermAndroid.Student;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.MidtermAndroid.R;

import java.util.ArrayList;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Student> students;

    public StudentAdapter(Context context, ArrayList<Student> students) {
        this.context = context;
        this.students = students;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.student_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Student student = students.get(position);

        TextView tv_name = holder.tv_name;
        tv_name.setText(student.getName());

        TextView tv_faculty = holder.tv_faculty;
        tv_faculty.setText(student.getFaculty());

        TextView tv_studentID = holder.tv_studentID;
        tv_studentID.setText("MSSV: " + student.getStudentID());

        TextView tv_grade = holder.tv_grade;
        tv_grade.setText("Lớp: " + student.getGrade());
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tv_name, tv_faculty, tv_studentID, tv_grade;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_name = itemView.findViewById(R.id.tv_name);
            tv_faculty = itemView.findViewById(R.id.tv_faculty);
            tv_studentID = itemView.findViewById(R.id.tv_studentID);
            tv_grade = itemView.findViewById(R.id.tv_grade);
        }
    }
}
