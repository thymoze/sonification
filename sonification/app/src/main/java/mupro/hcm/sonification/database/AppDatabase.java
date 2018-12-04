package mupro.hcm.sonification.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverter;
import android.content.Context;

import java.time.Instant;

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
                            AppDatabase.class, "sonification.db").allowMainThreadQueries().build();

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
