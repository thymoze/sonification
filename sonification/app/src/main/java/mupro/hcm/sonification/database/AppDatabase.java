package mupro.hcm.sonification.database;


import android.content.Context;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverter;

@Database(entities = {SensorData.class, DataSet.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract SensorDataDao sensorDataDao();

    public abstract DataSetDao dataSetDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "sonification.db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    @TypeConverter
    public static Instant toInstant(String value) {
        return value == null ? null : Instant.parse(value);
    }

    @TypeConverter
    public static String fromInstant(Instant value) {
        return value == null ? null : value.toString();
    }
}
