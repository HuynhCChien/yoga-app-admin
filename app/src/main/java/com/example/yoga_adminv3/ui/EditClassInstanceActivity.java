package com.example.yoga_adminv3.ui;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yoga_adminv3.R;
import com.example.yoga_adminv3.data.DatabaseHelper;
import com.example.yoga_adminv3.data.FirebaseSyncManager;
import com.example.yoga_adminv3.model.ClassInstance;

import java.util.Calendar;

public class EditClassInstanceActivity extends AppCompatActivity {

    private EditText editTextTeacher, editTextComments;
    private Button buttonSaveChanges, buttonPickDate;
    private DatabaseHelper databaseHelper;
    private int classInstanceId;
    private FirebaseSyncManager firebaseSync;
    private int courseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class_instance);

        // Initialize views
        buttonPickDate = findViewById(R.id.buttonPickDate);
        editTextTeacher = findViewById(R.id.editTextTeacherName);
        editTextComments = findViewById(R.id.editTextComments);
        buttonSaveChanges = findViewById(R.id.buttonSaveClass);

        // Set button text to "Update" instead of "Save"
        buttonSaveChanges.setText("Update Class Instance");

        // Get DatabaseHelper instance
        databaseHelper = new DatabaseHelper(this);
        firebaseSync = new FirebaseSyncManager(this);

        // Get the classInstanceId from the intent
        classInstanceId = getIntent().getIntExtra("classInstanceId", -1);

        if (classInstanceId != -1) {
            // Load class instance details and set them to the views
            loadClassInstanceDetails(classInstanceId);
            courseId = getCourseId(classInstanceId);
        } else {
            Toast.makeText(this, "Error: Invalid Class Instance ID", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set up date picker dialog on button click
        buttonPickDate.setOnClickListener(v -> {
            // Get current date as default
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Create DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    EditClassInstanceActivity.this,
                    (view, yearSelected, monthSelected, dayOfMonthSelected) -> {
                        // Set selected date on the button
                        String selectedDate = String.format("%02d/%02d/%04d", dayOfMonthSelected, monthSelected + 1, yearSelected);
                        buttonPickDate.setText(selectedDate);
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        // Set up the save changes button click listener
        buttonSaveChanges.setOnClickListener(v -> {
            String date = buttonPickDate.getText().toString().trim();
            String teacher = editTextTeacher.getText().toString().trim();
            String comments = editTextComments.getText().toString().trim();

            if (TextUtils.isEmpty(date) || TextUtils.isEmpty(teacher)) {
                Toast.makeText(this, "Date and teacher name are required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update class instance in the database
            try (SQLiteDatabase db = databaseHelper.getWritableDatabase()) {
                ContentValues values = new ContentValues();
                values.put("class_date", date);
                values.put("teacher", teacher);
                values.put("comments", comments);

                int rowsAffected = db.update("class_instances", values, "id = ?", new String[]{String.valueOf(classInstanceId)});
                if (rowsAffected > 0) {
                    // Create the updated ClassInstance object
                    ClassInstance updatedClassInstance = new ClassInstance(
                            classInstanceId,  // Use the existing class instance ID
                            courseId,         // Assuming `courseId` is available in this context
                            date,
                            teacher,
                            comments
                    );

                    // Update the class instance in Firebase
                    firebaseSync.updateClassInstanceInFirebase(updatedClassInstance);

                    Toast.makeText(this, "Class instance updated successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Close activity after saving
                } else {
                    Toast.makeText(this, "Failed to update class instance", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void loadClassInstanceDetails(int id) {
        ClassInstance classInstance = databaseHelper.getClassInstanceById(id);
        if (classInstance != null) {
            buttonPickDate.setText(classInstance.getDate());
            editTextTeacher.setText(classInstance.getTeacher());
            editTextComments.setText(classInstance.getComments());
        } else {
            Toast.makeText(this, "Error: Could not load class instance details", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private int getCourseId(int id) {
        ClassInstance classInstance = databaseHelper.getClassInstanceById(id);
        return classInstance.getCourseId();
    }
}
