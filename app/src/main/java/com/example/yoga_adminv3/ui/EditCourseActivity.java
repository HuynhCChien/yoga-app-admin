package com.example.yoga_adminv3.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yoga_adminv3.R;
import com.example.yoga_adminv3.data.DatabaseHelper;
import com.example.yoga_adminv3.data.FirebaseSyncManager;
import com.example.yoga_adminv3.model.Course;

import android.app.TimePickerDialog;
import android.widget.Button;
import java.util.Calendar;

public class EditCourseActivity extends AppCompatActivity {

    private Spinner spinnerDayOfWeek, spinnerType;
    private EditText editTextCapacity, editTextDuration, editTextPrice, editTextDescription;
    private Button buttonSaveCourse, buttonPickTime;
    private DatabaseHelper databaseHelper;
    private int courseId;
    private FirebaseSyncManager firebaseSync;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course); // Reusing activity_add_course layout

        // Initialize views
        spinnerDayOfWeek = findViewById(R.id.spinnerDayOfWeek);
        spinnerType = findViewById(R.id.spinnerType);
        editTextCapacity = findViewById(R.id.editTextCapacity);
        editTextDuration = findViewById(R.id.editTextDuration);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextDescription = findViewById(R.id.editTextDescription);
        buttonSaveCourse = findViewById(R.id.buttonSaveCourse);
        buttonPickTime = findViewById(R.id.buttonPickTime); // Initialize the time picker button

        // Set button text to "Update Course" instead of "Save Course"
        buttonSaveCourse.setText("Update Course");

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(this);
        firebaseSync = new FirebaseSyncManager(this);

        // Initialize the Spinners with their respective adapters
        ArrayAdapter<CharSequence> dayOfWeekAdapter = ArrayAdapter.createFromResource(this,
                R.array.days_of_week, android.R.layout.simple_spinner_item);
        dayOfWeekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDayOfWeek.setAdapter(dayOfWeekAdapter);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.class_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        // Get courseId from intent
        courseId = getIntent().getIntExtra("courseId", -1);

        // Load course details if courseId is valid
        if (courseId != -1) {
            loadCourseDetails(courseId);
        }

        // Set button click listener for updating the course
        buttonSaveCourse.setOnClickListener(view -> updateCourse());

        // Set button click listener for picking the time
        buttonPickTime.setOnClickListener(view -> showTimePickerDialog());
    }

    // Method to show Time Picker Dialog
    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(EditCourseActivity.this,
                (timePicker, selectedHour, selectedMinute) -> {
                    // Format the selected time and set it to the button text
                    String formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
                    buttonPickTime.setText(formattedTime);
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void loadCourseDetails(int courseId) {
        Course course = databaseHelper.getCourseById(courseId);
        if (course != null) {
            // Set spinner values based on the course details
            if (spinnerDayOfWeek.getAdapter() != null) {
                int dayOfWeekPosition = ((ArrayAdapter<CharSequence>) spinnerDayOfWeek.getAdapter())
                        .getPosition(course.getDayOfWeek());
                spinnerDayOfWeek.setSelection(dayOfWeekPosition);
            }

            if (spinnerType.getAdapter() != null) {
                int typePosition = ((ArrayAdapter<CharSequence>) spinnerType.getAdapter())
                        .getPosition(course.getType());
                spinnerType.setSelection(typePosition);
            }

            // Set the other fields
            buttonPickTime.setText(course.getTime()); // Make sure the button shows the saved time
            editTextCapacity.setText(String.valueOf(course.getCapacity()));
            editTextDuration.setText(String.valueOf(course.getDuration()));
            editTextPrice.setText(String.valueOf(course.getPrice()));
            editTextDescription.setText(course.getDescription());
        }
    }

    private void updateCourse() {
        // Get user input
        String dayOfWeek = spinnerDayOfWeek.getSelectedItem().toString().trim();
        String type = spinnerType.getSelectedItem().toString().trim();
        String capacityStr = editTextCapacity.getText().toString().trim();
        String durationStr = editTextDuration.getText().toString().trim();
        String priceStr = editTextPrice.getText().toString().trim();
        String time = buttonPickTime.getText().toString().trim(); // Get the time from buttonPickTime
        String description = editTextDescription.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(dayOfWeek) || TextUtils.isEmpty(type) ||
                TextUtils.isEmpty(capacityStr) || TextUtils.isEmpty(durationStr) ||
                TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(time)) {
            Toast.makeText(EditCourseActivity.this, R.string.fill_required_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        int capacity = Integer.parseInt(capacityStr);
        int duration = Integer.parseInt(durationStr);
        double price = Double.parseDouble(priceStr);

        // Create Course object and update the database
        Course course = new Course(courseId, dayOfWeek, time, capacity, duration, price, type, description);
        boolean result = databaseHelper.updateCourse(course) > 0;

        if (result) {
            // Update Firebase
            firebaseSync.updateCourseInFirebase(course);
            Toast.makeText(EditCourseActivity.this, R.string.course_updated_successfully, Toast.LENGTH_SHORT).show();

            // Pass the updated course back to the previous activity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("updatedCourse", (CharSequence) course);
            setResult(RESULT_OK, resultIntent);

            finish(); // Close the activity after updating
        } else {
            Toast.makeText(EditCourseActivity.this, R.string.failed_to_update_course, Toast.LENGTH_SHORT).show();
        }

    }
}
