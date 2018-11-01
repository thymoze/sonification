package mupro.hcm.sonification.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {SensorData.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SensorDataDao sensorDataDao();
}
