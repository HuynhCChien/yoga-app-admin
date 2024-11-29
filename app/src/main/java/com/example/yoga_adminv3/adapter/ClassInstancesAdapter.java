package com.example.yoga_adminv3.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import com.example.yoga_adminv3.model.ClassInstance;
import com.example.yoga_adminv3.ui.EditClassInstanceActivity;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class ClassInstancesAdapter extends RecyclerView.Adapter<ClassInstancesAdapter.ClassInstanceViewHolder> {

    private final List<ClassInstance> classInstanceList;
    private final Context context;
    private FirebaseSyncManager firebaseSync;
    private int courseId;

    public ClassInstancesAdapter(Context context, List<ClassInstance> classInstanceList, int courseId) {
        this.context = context;
        this.classInstanceList = classInstanceList;
        this.firebaseSync = new FirebaseSyncManager(context);
        this.courseId = courseId;
    }

    @NonNull
    @Override
    public ClassInstanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class_instance, parent, false);
        return new ClassInstanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassInstanceViewHolder holder, int position) {
        ClassInstance classInstance = classInstanceList.get(position);
        holder.textViewDate.setText("Date: " + classInstance.getDate());
        holder.textViewTeacher.setText("Teacher: " + classInstance.getTeacher());
        holder.textViewComments.setText("Comments: " + (classInstance.getComments().isEmpty() ? "No comments available" : classInstance.getComments()));

        // Set up Edit button click listener
        holder.buttonEditClassInstance.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                ClassInstance currentClassInstance = classInstanceList.get(currentPosition);
                Intent intent = new Intent(context, EditClassInstanceActivity.class);
                intent.putExtra("classInstanceId", currentClassInstance.getId());
                context.startActivity(intent);
            }
        });

        // Set up Delete button click listener
        holder.buttonDeleteClassInstance.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                ClassInstance currentClassInstance = classInstanceList.get(currentPosition);

                // Ensure that DatabaseHelper resources are properly closed
                try (DatabaseHelper databaseHelper = new DatabaseHelper(context)) {
                    boolean deleted = databaseHelper.deleteClassInstance(currentClassInstance.getId());

                    if (deleted) {
                        classInstanceList.remove(currentPosition);
                        notifyItemRemoved(currentPosition);
                        notifyItemRangeChanged(currentPosition, classInstanceList.size());
                        Toast.makeText(context, "Class instance deleted successfully", Toast.LENGTH_SHORT).show();

                        firebaseSync.deleteClassInstanceFromFirebase(courseId , currentClassInstance.getId());
                    } else {
                        Toast.makeText(context, "Failed to delete class instance", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return classInstanceList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateClassInstanceList(List<ClassInstance> newClassInstanceList) {
        classInstanceList.clear();
        classInstanceList.addAll(newClassInstanceList);
        notifyDataSetChanged();
    }

    public static class ClassInstanceViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDate, textViewTeacher, textViewComments;
        MaterialButton buttonEditClassInstance, buttonDeleteClassInstance;

        public ClassInstanceViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.textViewClassDate);
            textViewTeacher = itemView.findViewById(R.id.textViewTeacher);
            textViewComments = itemView.findViewById(R.id.textViewComments);
            buttonEditClassInstance = itemView.findViewById(R.id.buttonEditClassInstance);
            buttonDeleteClassInstance = itemView.findViewById(R.id.buttonDeleteClassInstance);
        }
    }
}
