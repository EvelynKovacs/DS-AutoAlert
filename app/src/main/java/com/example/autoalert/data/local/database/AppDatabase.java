package com.example.autoalert.data.local.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.autoalert.data.local.dao.ProjectDao;
import com.example.autoalert.model.Converters;
import com.example.autoalert.model.entities.ProjectModel;

@Database(entities = {ProjectModel.class}, exportSchema = false, version = 6)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "project_database.db";
    public static AppDatabase instance;
    private static final Object LOCK = new Object();

    public abstract ProjectDao projectDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, DATABASE_NAME)
                            .fallbackToDestructiveMigration()  // Destruye y recrea la BD si hay un cambio de esquema
                            .build();
                }
            }
        }
        return instance;
    }
}
