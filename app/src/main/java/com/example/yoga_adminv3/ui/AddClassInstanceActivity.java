package com.example.yoga_adminv3.ui;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yoga_adminv3.R;
import com.example.yoga_adminv3.data.DatabaseHelper;
import com.example.yoga_adminv3.data.FirebaseSyncManager;
import com.example.yoga_adminv3.model.ClassInstance;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddClassInstanceActivity extends AppCompatActivity {

    private EditText editTextTeacherName, editTextComments;
    private Button buttonPickDate, buttonSaveClass;
    private String selectedClassDate;
    private DatabaseHelper databaseHelper;
    private int courseId;
    private String courseDayOfWeek; // Represents the day of the week of the associated course
    private FirebaseSyncManager firebaseSync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class_instance);

        // Initialize views
        editTextTeacherName = findViewById(R.id.editTextTeacherName);
        editTextComments = findViewById(R.id.editTextComments);
        buttonPickDate = findViewById(R.id.buttonPickDate);
        buttonSaveClass = findViewById(R.id.buttonSaveClass);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(this);
        firebaseSync = new FirebaseSyncManager(this);

        // Get courseId and day of the week from the intent
        courseId = getIntent().getIntExtra("courseId", -1);
        courseDayOfWeek = getIntent().getStringExtra("courseDayOfWeek");

        // Log to check received values
        Log.d("AddClassInstanceActivity", "Received courseId: " + courseId);
        Log.d("AddClassInstanceActivity", "Received courseDayOfWeek: " + courseDayOfWeek);

        // Set button click listeners
        buttonPickDate.setOnClickListener(v -> showDatePickerDialog());
        buttonSaveClass.setOnClickListener(v -> saveClass());
    }

    // Method to show Date Picker Dialog
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(AddClassInstanceActivity.this,
                (view, year, month, dayOfMonth) -> {
                    // Create a calendar object with the selected date
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth);

                    // Convert the selected date to a day of the week (e.g., "Monday")
                    String selectedDayOfWeek = new SimpleDateFormat("EEEE", Locale.getDefault())
                            .format(selectedCalendar.getTime());

                    // Compare with the course day of the week
                    if (selectedDayOfWeek.equalsIgnoreCase(courseDayOfWeek)) {
                        // Set the selected class date in the format "dd/MM/yyyy"
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        selectedClassDate = sdf.format(selectedCalendar.getTime());
                        buttonPickDate.setText(selectedClassDate);  // Update button text to display selected date
                    } else {
                        // Show an error message if the day of week doesn't match
                        Toast.makeText(AddClassInstanceActivity.this,
                                "Selected date must match the course day (" + courseDayOfWeek + ")",
                                Toast.LENGTH_SHORT).show();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void saveClass() {
        // Get user input
        String teacherName = editTextTeacherName.getText().toString().trim();
        String comments = editTextComments.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(selectedClassDate) || TextUtils.isEmpty(teacherName)) {
            Toast.makeText(AddClassInstanceActivity.this, R.string.fill_required_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        // Insert new class instance into the database
        try (SQLiteDatabase db = databaseHelper.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put("courseId", courseId);  // Associate the class with the correct course
            values.put("class_date", selectedClassDate);
            values.put("teacher", teacherName);
            values.put("comments", comments);

            long result = db.insert("class_instances", null, values);

            if (result != -1) {
                // Create a new ClassInstance object
                ClassInstance newClassInstance = new ClassInstance(
                        (int) result, // result contains the generated ID for the new entry
                        courseId,
                        selectedClassDate,
                        teacherName,
                        comments
                );

                // Upload the new class instance to Firebase
                firebaseSync.uploadClassInstanceToFirebase(newClassInstance);

                Toast.makeText(AddClassInstanceActivity.this, R.string.class_added_successfully, Toast.LENGTH_SHORT).show();
                finish();  // Close the activity after saving
            } else {
                Toast.makeText(AddClassInstanceActivity.this, R.string.failed_to_add_class, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
