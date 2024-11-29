package com.example.yoga_adminv3.ui;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yoga_adminv3.R;
import com.example.yoga_adminv3.data.DatabaseHelper;
import com.example.yoga_adminv3.data.FirebaseSyncManager;
import com.example.yoga_adminv3.model.Course;

import java.util.Calendar;
import java.util.Locale;

public class AddCourseActivity extends AppCompatActivity {

    private Spinner spinnerDayOfWeek, spinnerType;
    private EditText editTextCapacity, editTextDuration, editTextPrice, editTextDescription;
    private Button buttonPickTime;
    private String selectedTime;
    private DatabaseHelper databaseHelper;
    private FirebaseSyncManager firebaseSync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        spinnerDayOfWeek = findViewById(R.id.spinnerDayOfWeek);
        spinnerType = findViewById(R.id.spinnerType);
        editTextCapacity = findViewById(R.id.editTextCapacity);
        editTextDuration = findViewById(R.id.editTextDuration);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextDescription = findViewById(R.id.editTextDescription);
        buttonPickTime = findViewById(R.id.buttonPickTime);
        Button buttonSaveCourse = findViewById(R.id.buttonSaveCourse);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(this);
        firebaseSync = new FirebaseSyncManager(this);

        // Set up Day of the Week Spinner
        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(this,
                R.array.days_of_week, android.R.layout.simple_spinner_item);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDayOfWeek.setAdapter(dayAdapter);

        // Set up Type of Class Spinner
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.class_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        // Set up Time Picker
        buttonPickTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(AddCourseActivity.this,
                    (view, hourOfDay, minuteOfHour) -> {
                        selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                        buttonPickTime.setText(selectedTime);
                    }, hour, minute, true);
            timePickerDialog.show();
        });

        // Set button click listener to save the course
        buttonSaveCourse.setOnClickListener(view -> {
            // Get user input
            String dayOfWeek = spinnerDayOfWeek.getSelectedItem().toString().trim();
            String type = spinnerType.getSelectedItem().toString().trim();
            String capacityStr = editTextCapacity.getText().toString().trim();
            String durationStr = editTextDuration.getText().toString().trim();
            String priceStr = editTextPrice.getText().toString().trim();
            String description = editTextDescription.getText().toString().trim();

            // Validate input
            if (TextUtils.isEmpty(dayOfWeek) || TextUtils.isEmpty(selectedTime) || TextUtils.isEmpty(capacityStr) ||
                    TextUtils.isEmpty(durationStr) || TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(type)) {
                Toast.makeText(AddCourseActivity.this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int capacity = Integer.parseInt(capacityStr);
            int duration = Integer.parseInt(durationStr);
            double price = Double.parseDouble(priceStr);

            // Create Course object and save to database
            Course course = new Course(0, dayOfWeek, selectedTime, capacity, duration, price, type, description);
            long result = databaseHelper.insertCourse(course);

            if (result != -1) {
                firebaseSync.addCourseToFirebase(course);
                Toast.makeText(AddCourseActivity.this, "Course added successfully!", Toast.LENGTH_SHORT).show();
                finish(); // Close the activity
            } else {
                Toast.makeText(AddCourseActivity.this, "Failed to add course", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
