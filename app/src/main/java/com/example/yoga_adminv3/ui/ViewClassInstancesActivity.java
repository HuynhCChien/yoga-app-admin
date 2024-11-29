package com.example.yoga_adminv3.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yoga_adminv3.R;
import com.example.yoga_adminv3.adapter.ClassInstancesAdapter;
import com.example.yoga_adminv3.data.DatabaseHelper;
import com.example.yoga_adminv3.model.ClassInstance;

import java.util.ArrayList;
import java.util.List;

public class ViewClassInstancesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewClassInstances;
    private ClassInstancesAdapter classInstancesAdapter;
    private DatabaseHelper databaseHelper;
    private TextView textViewEmptyState;
    private EditText editTextSearch;
    private Button buttonAddClass;

    private int courseId;
    private String courseDayOfWeek;

    private List<ClassInstance> allClassInstances = new ArrayList<>();
    private List<ClassInstance> filteredClassInstances = new ArrayList<>();

    // Handler for debouncing
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_class_instances);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        recyclerViewClassInstances = findViewById(R.id.recyclerViewClassInstances);
        recyclerViewClassInstances.setLayoutManager(new LinearLayoutManager(this));
        textViewEmptyState = findViewById(R.id.textViewEmptyState);
        editTextSearch = findViewById(R.id.editTextSearch);
        buttonAddClass = findViewById(R.id.buttonAddNewClass);

        // Get courseId and courseDayOfWeek from intent
        courseId = getIntent().getIntExtra("courseId", -1);
        courseDayOfWeek = getIntent().getStringExtra("courseDayOfWeek");

        // Load class instances if courseId is valid
        if (courseId != -1) {
            loadClassInstances(courseId);
        }

        // Add New Class button listener
        buttonAddClass.setOnClickListener(v -> {
            Log.d("ViewClassInstances", "Add Class button clicked");
            Intent intent = new Intent(ViewClassInstancesActivity.this, AddClassInstanceActivity.class);
            intent.putExtra("courseId", courseId);

            if (courseDayOfWeek != null) {
                intent.putExtra("courseDayOfWeek", courseDayOfWeek);
            }

            startActivity(intent);
        });

        // Search functionality with debouncing
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Remove any existing callbacks to avoid executing too frequently
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                // Use a delay to debounce the search
                searchRunnable = () -> filterClassInstances(s.toString().trim());
                searchHandler.postDelayed(searchRunnable, 300); // 300ms delay for debouncing
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (courseId != -1) {
            loadClassInstances(courseId);
        }
    }

    private void loadClassInstances(int courseId) {
        // Get all class instances for the given courseId
        allClassInstances = databaseHelper.getClassInstancesForCourse(courseId);
        filteredClassInstances = new ArrayList<>(allClassInstances);

        // Update RecyclerView
        updateRecyclerView();
    }

    private void filterClassInstances(String query) {
        if (query.isEmpty()) {
            // If the search query is empty, reset to all class instances
            filteredClassInstances.clear();
            filteredClassInstances.addAll(allClassInstances);
        } else {
            // Perform filtering by teacher name, date, day of the week, or comments
            filteredClassInstances.clear();
            for (ClassInstance classInstance : allClassInstances) {
                if (classInstance.getTeacher().toLowerCase().contains(query.toLowerCase()) ||
                        classInstance.getDate().toLowerCase().contains(query.toLowerCase()) ||
                        (courseDayOfWeek != null && courseDayOfWeek.toLowerCase().contains(query.toLowerCase())) ||
                        (classInstance.getComments() != null && classInstance.getComments().toLowerCase().contains(query.toLowerCase()))) {

                    filteredClassInstances.add(classInstance);
                }
            }
        }

        // Update RecyclerView with the filtered list
        updateRecyclerView();
    }


    private void updateRecyclerView() {
        if (filteredClassInstances.isEmpty()) {
            textViewEmptyState.setVisibility(View.VISIBLE);
            recyclerViewClassInstances.setVisibility(View.GONE);
        } else {
            textViewEmptyState.setVisibility(View.GONE);
            recyclerViewClassInstances.setVisibility(View.VISIBLE);

            if (classInstancesAdapter == null) {
                classInstancesAdapter = new ClassInstancesAdapter(this, filteredClassInstances, courseId);
                recyclerViewClassInstances.setAdapter(classInstancesAdapter);
            } else {
                classInstancesAdapter.updateClassInstanceList(filteredClassInstances);
            }
        }
    }
}
