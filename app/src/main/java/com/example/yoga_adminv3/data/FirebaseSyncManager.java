package com.example.yoga_adminv3.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.yoga_adminv3.model.ClassInstance;
import com.example.yoga_adminv3.model.Course;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class FirebaseSyncManager {
    private static final String TAG = "FirebaseSyncManager";
    private final DatabaseHelper databaseHelper;
    private final DatabaseReference databaseReference;
    private final Context context;

    public FirebaseSyncManager(Context context) {
        this.context = context;
        this.databaseHelper = new DatabaseHelper(context);
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // Upload all courses and their class instances to Firebase
    public void uploadCoursesToFirebase() {
        if (!isNetworkAvailable()) {
            Log.w(TAG, "No network available. Sync postponed.");
            return;
        }

        List<Course> courseList = databaseHelper.getAllCourses();
        for (Course course : courseList) {
            String courseId = String.valueOf(course.getId());
            databaseReference.child("courses").child(courseId).setValue(course);

            // Upload associated class instances for each course
            uploadClassInstancesForCourse(courseId, course.getId());
        }
    }

    // Upload all class instances for a specific course
    private void uploadClassInstancesForCourse(String courseId, int localCourseId) {
        List<ClassInstance> classInstanceList = databaseHelper.getClassInstancesForCourse(localCourseId);
        for (ClassInstance classInstance : classInstanceList) {
            String classInstanceId = String.valueOf(classInstance.getId());
            databaseReference.child("courses").child(courseId).child("classInstances").child(classInstanceId).setValue(classInstance);
        }
    }

    // Add a new course to Firebase
    public void addCourseToFirebase(Course course) {
        if (!isNetworkAvailable()) {
            Log.w(TAG, "No network available. Add postponed.");
            return;
        }

        String courseId = String.valueOf(course.getId());
        databaseReference.child("courses").child(courseId).setValue(course);

        // Add associated class instances for the course
        uploadClassInstancesForCourse(courseId, course.getId());
    }

    // Update a course in Firebase
    public void updateCourseInFirebase(Course course) {
        if (!isNetworkAvailable()) {
            Log.w(TAG, "No network available. Update postponed.");
            return;
        }

        String courseId = String.valueOf(course.getId());
        databaseReference.child("courses").child(courseId).setValue(course);

        // Update class instances for the course as well
        uploadClassInstancesForCourse(courseId, course.getId());
    }

    // Delete a course from Firebase
    public void deleteCourseFromFirebase(int courseId) {
        if (!isNetworkAvailable()) {
            Log.w(TAG, "No network available. Delete postponed.");
            return;
        }

        databaseReference.child("courses").child(String.valueOf(courseId)).removeValue();
    }

    // Upload a specific class instance to Firebase
    public void uploadClassInstanceToFirebase(ClassInstance classInstance) {
        if (!isNetworkAvailable()) {
            Log.w(TAG, "No network available. Add postponed.");
            return;
        }

        String courseId = String.valueOf(classInstance.getCourseId());
        String classInstanceId = String.valueOf(classInstance.getId());
        databaseReference.child("courses").child(courseId).child("classInstances").child(classInstanceId).setValue(classInstance);
    }

    // Update a class instance in Firebase
    public void updateClassInstanceInFirebase(ClassInstance classInstance) {
        if (!isNetworkAvailable()) {
            Log.w(TAG, "No network available. Update postponed.");
            return;
        }

        String courseId = String.valueOf(classInstance.getCourseId());
        String classInstanceId = String.valueOf(classInstance.getId());
        databaseReference.child("courses").child(courseId).child("classInstances").child(classInstanceId).setValue(classInstance);
    }

    // Delete a class instance from Firebase
    public void deleteClassInstanceFromFirebase(int courseId, int classInstanceId) {
        if (!isNetworkAvailable()) {
            Log.w(TAG, "No network available. Delete postponed.");
            return;
        }

        databaseReference.child("courses").child(String.valueOf(courseId)).child("classInstances").child(String.valueOf(classInstanceId)).removeValue();
    }

    // Listen for changes in Firebase and update the local database accordingly
    public void listenForFirebaseUpdates() {
        // Listen for changes in courses
        databaseReference.child("courses").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Course course = snapshot.getValue(Course.class);
                if (course != null && databaseHelper.getCourseById(course.getId()) == null) {
                    databaseHelper.insertCourse(course);
                    // Load class instances for this course
                    listenForClassInstanceUpdates(snapshot.getKey());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                Course course = snapshot.getValue(Course.class);
                if (course != null) {
                    databaseHelper.updateCourse(course);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Course course = snapshot.getValue(Course.class);
                if (course != null) {
                    databaseHelper.deleteCourse(course.getId());
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {
                // Not needed in this use case
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    // Listen for class instance updates for a specific course
    private void listenForClassInstanceUpdates(String courseId) {
        databaseReference.child("courses").child(courseId).child("classInstances").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                ClassInstance classInstance = snapshot.getValue(ClassInstance.class);
                if (classInstance != null && databaseHelper.getClassInstanceById(classInstance.getId()) == null) {
                    databaseHelper.insertClassInstance(classInstance);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                ClassInstance classInstance = snapshot.getValue(ClassInstance.class);
                if (classInstance != null) {
                    databaseHelper.updateClassInstance(classInstance);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                ClassInstance classInstance = snapshot.getValue(ClassInstance.class);
                if (classInstance != null) {
                    databaseHelper.deleteClassInstance(classInstance.getId());
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {
                // Not needed in this use case
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }
}
