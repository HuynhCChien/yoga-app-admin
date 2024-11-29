package com.example.yoga_adminv3.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yoga_adminv3.R;
import com.example.yoga_adminv3.data.DatabaseHelper;
import com.example.yoga_adminv3.data.FirebaseSyncManager;
import com.example.yoga_adminv3.model.Course;
import com.example.yoga_adminv3.adapter.CoursesAdapter;

import java.util.List;

public class ViewCoursesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCourses;
    private CoursesAdapter coursesAdapter;
    private DatabaseHelper databaseHelper;
    private TextView textViewEmptyState;

    private FirebaseSyncManager firebaseSync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_courses);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        // Initialize RecyclerView
        recyclerViewCourses = findViewById(R.id.recyclerViewCourses);
        recyclerViewCourses.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Empty State TextView
        textViewEmptyState = findViewById(R.id.textViewEmptyState);

        // Initialize Add Course Button
        Button buttonAddCourse = findViewById(R.id.buttonAddCourse);
        buttonAddCourse.setOnClickListener(v -> {
            // Start AddCourseActivity to add a new course
            Intent intent = new Intent(ViewCoursesActivity.this, AddCourseActivity.class);
            startActivity(intent);
        });

        // Initialize FirebaseSync
        firebaseSync = new FirebaseSyncManager(this);

        // Trigger initial data sync when the app starts
        firebaseSync.uploadCoursesToFirebase();
        firebaseSync.listenForFirebaseUpdates();

        // Load Courses from Database
        loadCourses();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Sync data again when the app resumes
        firebaseSync.uploadCoursesToFirebase();
        firebaseSync.listenForFirebaseUpdates();

        // Reload courses when returning to this activity, in case a new course was added
        loadCourses();
    }

    private void loadCourses() {
        // Load courses from the database
        List<Course> courseList = databaseHelper.getAllCourses();

        // Check if course list is empty and show/hide empty state message
        updateEmptyStateVisibility(courseList.isEmpty());

        // Initialize or update the adapter
        if (coursesAdapter == null) {
            coursesAdapter = new CoursesAdapter(this, courseList, course -> {
                // Start ViewClassInstancesActivity to view classes for a specific course
                Intent intent = new Intent(ViewCoursesActivity.this, ViewClassInstancesActivity.class);

                if (course.getDayOfWeek() != null) {
                    intent.putExtra("courseId", course.getId());
                    intent.putExtra("courseDayOfWeek", course.getDayOfWeek());
                }
                startActivity(intent);
            });
            recyclerViewCourses.setAdapter(coursesAdapter);
        } else {
            coursesAdapter.updateCourseList(courseList);
        }
    }

    private void updateEmptyStateVisibility(boolean isEmpty) {
        if (isEmpty) {
            textViewEmptyState.setVisibility(View.VISIBLE);
            recyclerViewCourses.setVisibility(View.GONE);
        } else {
            textViewEmptyState.setVisibility(View.GONE);
            recyclerViewCourses.setVisibility(View.VISIBLE);
        }
    }
}
