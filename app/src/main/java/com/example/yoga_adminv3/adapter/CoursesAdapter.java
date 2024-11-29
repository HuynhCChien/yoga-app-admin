package com.example.yoga_adminv3.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yoga_adminv3.R;
import com.example.yoga_adminv3.data.DatabaseHelper;
import com.example.yoga_adminv3.data.FirebaseSyncManager;
import com.example.yoga_adminv3.model.Course;
import com.example.yoga_adminv3.ui.EditCourseActivity;
import com.example.yoga_adminv3.ui.ViewClassInstancesActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import android.app.AlertDialog;

import java.util.List;

public class CoursesAdapter extends RecyclerView.Adapter<CoursesAdapter.CourseViewHolder> {

    private final List<Course> courseList;
    private final Context context;
    private final OnCourseClickListener onCourseClickListener;
    private final FirebaseSyncManager firebaseSync;

    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    public CoursesAdapter(Context context, List<Course> courseList, OnCourseClickListener onCourseClickListener) {
        this.context = context;
        this.courseList = courseList;
        this.onCourseClickListener = onCourseClickListener;
        firebaseSync = new FirebaseSyncManager(context);
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courseList.get(position);

        holder.textViewCourseTitle.setText(course.getType() + " - " + course.getDayOfWeek() + ", " + course.getTime());
        holder.textViewCapacity.setText("Capacity: " + course.getCapacity());
        holder.textViewDuration.setText("Duration: " + course.getDuration() + " mins");
        holder.textViewPrice.setText("Price: $" + course.getPrice());
        holder.textViewDescription.setText(course.getDescription());

        // Set default maxLines for the description
        holder.textViewDescription.setMaxLines(2);
        holder.buttonExpandDescription.setText("View More");

        // Handle "View More"/"View Less" functionality
        holder.buttonExpandDescription.setOnClickListener(v -> {
            if (holder.textViewDescription.getMaxLines() == 2) {
                // Expand the description
                holder.textViewDescription.setMaxLines(Integer.MAX_VALUE);
                holder.buttonExpandDescription.setText("View Less");
            } else {
                // Collapse the description
                holder.textViewDescription.setMaxLines(2);
                holder.buttonExpandDescription.setText("View More");
            }
        });

        // Set up onClick listener for card to view all class instances
        holder.materialCardView.setOnClickListener(v -> onCourseClickListener.onCourseClick(course));

        // Set up Update button click listener
        holder.buttonUpdateCourse.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                Course currentCourse = courseList.get(currentPosition);
                Intent intent = new Intent(context, EditCourseActivity.class);
                intent.putExtra("courseId", currentCourse.getId());
                context.startActivity(intent);
            }
        });

        // Set up Delete button click listener
        holder.buttonDeleteCourse.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                Course currentCourse = courseList.get(currentPosition);

                // Show a confirmation dialog before deleting
                new AlertDialog.Builder(context)
                        .setTitle("Delete Course")
                        .setMessage("Are you sure you want to delete this course?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Ensure that DatabaseHelper resources are properly closed
                            try (DatabaseHelper databaseHelper = new DatabaseHelper(context);
                                 SQLiteDatabase db = databaseHelper.getWritableDatabase()) {

                                boolean deleted = databaseHelper.deleteCourse(currentCourse.getId());

                                    if (deleted) {
                                        courseList.remove(currentPosition);
                                        notifyItemRemoved(currentPosition);
                                        notifyItemRangeChanged(currentPosition, courseList.size());
                                        // Delete from Firebase
                                        firebaseSync.deleteCourseFromFirebase(currentCourse.getId());

                                        Toast.makeText(context, "Course deleted successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context, "Failed to delete course", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateCourseList(List<Course> newCourseList) {
        courseList.clear();
        courseList.addAll(newCourseList);
        notifyDataSetChanged();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView materialCardView;
        TextView textViewCourseTitle, textViewCapacity, textViewDuration, textViewPrice, textViewDescription;
        MaterialButton buttonUpdateCourse, buttonDeleteCourse;
        TextView buttonExpandDescription;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            materialCardView = itemView.findViewById(R.id.materialCardView);
            textViewCourseTitle = itemView.findViewById(R.id.textViewCourseTitle);
            textViewCapacity = itemView.findViewById(R.id.textViewCapacity);
            textViewDuration = itemView.findViewById(R.id.textViewDuration);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            buttonUpdateCourse = itemView.findViewById(R.id.buttonUpdateCourse);
            buttonDeleteCourse = itemView.findViewById(R.id.buttonDeleteCourse);
            buttonExpandDescription = itemView.findViewById(R.id.buttonExpandDescription); // Initialize the "View More" button
        }
    }
}
