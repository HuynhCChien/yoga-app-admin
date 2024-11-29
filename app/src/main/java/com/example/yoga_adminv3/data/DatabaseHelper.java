package com.example.yoga_adminv3.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.yoga_adminv3.model.Course;
import com.example.yoga_adminv3.model.ClassInstance;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "yoga_classes.db";
    private static final int DATABASE_VERSION = 2; // Incremented version number

    // Table Names
    private static final String TABLE_COURSES = "courses";
    private static final String TABLE_CLASS_INSTANCES = "class_instances";

    // Course Table Columns
    private static final String COLUMN_COURSE_ID = "id";
    private static final String COLUMN_DAY_OF_WEEK = "dayOfWeek";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_CAPACITY = "capacity";
    private static final String COLUMN_DURATION = "duration";
    private static final String COLUMN_PRICE = "price";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_DESCRIPTION = "description";

    // Class Instance Table Columns
    private static final String COLUMN_CLASS_INSTANCE_ID = "id";
    private static final String COLUMN_COURSE_ID_FK = "courseId";
    private static final String COLUMN_CLASS_DATE = "class_date"; // Updated column name for consistency
    private static final String COLUMN_TEACHER = "teacher";
    private static final String COLUMN_COMMENTS = "comments";

    // SQL Statements to create tables
    private static final String CREATE_COURSE_TABLE = "CREATE TABLE " + TABLE_COURSES + " (" +
            COLUMN_COURSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_DAY_OF_WEEK + " TEXT NOT NULL, " +
            COLUMN_TIME + " TEXT NOT NULL, " +
            COLUMN_CAPACITY + " INTEGER NOT NULL, " +
            COLUMN_DURATION + " INTEGER NOT NULL, " +
            COLUMN_PRICE + " REAL NOT NULL, " +
            COLUMN_TYPE + " TEXT NOT NULL, " +
            COLUMN_DESCRIPTION + " TEXT);";

    private static final String CREATE_CLASS_INSTANCE_TABLE = "CREATE TABLE " + TABLE_CLASS_INSTANCES + " (" +
            COLUMN_CLASS_INSTANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_COURSE_ID_FK + " INTEGER, " +
            COLUMN_CLASS_DATE + " TEXT NOT NULL, " +
            COLUMN_TEACHER + " TEXT NOT NULL, " +
            COLUMN_COMMENTS + " TEXT, " +
            "FOREIGN KEY(" + COLUMN_COURSE_ID_FK + ") REFERENCES " + TABLE_COURSES + "(" + COLUMN_COURSE_ID + "));";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_COURSE_TABLE);
        db.execSQL(CREATE_CLASS_INSTANCE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASS_INSTANCES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSES);
        onCreate(db);
    }

    // Insert a new Course
    public long insertCourse(Course course) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = -1;
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_DAY_OF_WEEK, course.getDayOfWeek());
            values.put(COLUMN_TIME, course.getTime());
            values.put(COLUMN_CAPACITY, course.getCapacity());
            values.put(COLUMN_DURATION, course.getDuration());
            values.put(COLUMN_PRICE, course.getPrice());
            values.put(COLUMN_TYPE, course.getType());
            values.put(COLUMN_DESCRIPTION, course.getDescription());

            // Insert the row
            id = db.insert(TABLE_COURSES, null, values);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return id;
    }

    // Insert a new Class Instance
    public long insertClassInstance(ClassInstance classInstance) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = -1;
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_COURSE_ID_FK, classInstance.getCourseId());
            values.put(COLUMN_CLASS_DATE, classInstance.getDate()); // Updated column name
            values.put(COLUMN_TEACHER, classInstance.getTeacher());
            values.put(COLUMN_COMMENTS, classInstance.getComments());

            // Insert the row
            id = db.insert(TABLE_CLASS_INSTANCES, null, values);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return id;
    }

    // Retrieve all Courses
    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_COURSES, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Course course = new Course(
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COURSE_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAY_OF_WEEK)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CAPACITY)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DURATION)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
                    );
                    courses.add(course);
                } while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return courses;
    }

    // Retrieve a Course by ID
    public Course getCourseById(int courseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Course course = null;

        Cursor cursor = db.query(TABLE_COURSES, null, COLUMN_COURSE_ID + " = ?",
                new String[]{String.valueOf(courseId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            course = new Course(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COURSE_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAY_OF_WEEK)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CAPACITY)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DURATION)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
            );
            cursor.close();
        }
        db.close();
        return course;
    }

    // Retrieve all Class Instances for a specific Course
    public List<ClassInstance> getClassInstancesForCourse(int courseId) {
        List<ClassInstance> classInstances = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_CLASS_INSTANCES, null, COLUMN_COURSE_ID_FK + " = ?",
                    new String[]{String.valueOf(courseId)}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ClassInstance classInstance = new ClassInstance(
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CLASS_INSTANCE_ID)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COURSE_ID_FK)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASS_DATE)), // Updated column name
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEACHER)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENTS))
                    );
                    classInstances.add(classInstance);
                } while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return classInstances;
    }

    // Method to get a ClassInstance by ID
    public ClassInstance getClassInstanceById(int classInstanceId) {
        SQLiteDatabase db = this.getReadableDatabase();
        ClassInstance classInstance = null;

        Cursor cursor = db.query(TABLE_CLASS_INSTANCES, null, COLUMN_CLASS_INSTANCE_ID + " = ?",
                new String[]{String.valueOf(classInstanceId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            classInstance = new ClassInstance(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CLASS_INSTANCE_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COURSE_ID_FK)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASS_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEACHER)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENTS))
            );
            cursor.close();
        }
        db.close();
        return classInstance;
    }

    // Update an existing Course
    public int updateCourse(Course course) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = 0;
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_DAY_OF_WEEK, course.getDayOfWeek());
            values.put(COLUMN_TIME, course.getTime());
            values.put(COLUMN_CAPACITY, course.getCapacity());
            values.put(COLUMN_DURATION, course.getDuration());
            values.put(COLUMN_PRICE, course.getPrice());
            values.put(COLUMN_TYPE, course.getType());
            values.put(COLUMN_DESCRIPTION, course.getDescription());

            // Update row
            rowsAffected = db.update(TABLE_COURSES, values, COLUMN_COURSE_ID + " = ?",
                    new String[]{String.valueOf(course.getId())});
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return rowsAffected;
    }

    // Update an existing Class Instance
    public int updateClassInstance(ClassInstance classInstance) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = 0;
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_COURSE_ID_FK, classInstance.getCourseId());
            values.put(COLUMN_CLASS_DATE, classInstance.getDate()); // Updated column name
            values.put(COLUMN_TEACHER, classInstance.getTeacher());
            values.put(COLUMN_COMMENTS, classInstance.getComments());

            // Update row
            rowsAffected = db.update(TABLE_CLASS_INSTANCES, values, COLUMN_CLASS_INSTANCE_ID + " = ?",
                    new String[]{String.valueOf(classInstance.getId())});
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return rowsAffected;
    }

    public boolean deleteCourse(int courseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // First delete all related class instances
            db.delete(TABLE_CLASS_INSTANCES, COLUMN_COURSE_ID_FK + " = ?", new String[]{String.valueOf(courseId)});

            // Then delete the course itself
            int rowsDeleted = db.delete(TABLE_COURSES, COLUMN_COURSE_ID + " = ?", new String[]{String.valueOf(courseId)});
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            db.close();
        }
    }

    // Delete a Class Instance by ID
    public boolean deleteClassInstance(int classInstanceId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = 0;
        try {
            rowsDeleted = db.delete(TABLE_CLASS_INSTANCES, COLUMN_CLASS_INSTANCE_ID + " = ?", new String[]{String.valueOf(classInstanceId)});
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return rowsDeleted > 0;
    }
}
