package mupro.hcm.sonification.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.TypeConverters;
import android.util.Pair;

import java.time.Instant;
import java.util.List;

@Dao
public interface SensorDataDao {

    @Query("SELECT * FROM sensordata")
    List<SensorData> getAll();

    @Query("SELECT * FROM SensorData ORDER BY datetime(timestamp) DESC LIMIT 30")
    List<SensorData> getLast30();

    @Query("SELECT * FROM sensordata WHERE id IN (:dataIds)")
    List<SensorData> loadAllByIds(int[] dataIds);

    @Query("SELECT timestamp FROM SensorData WHERE id = (:id)")
    @TypeConverters(AppDatabase.class)
    Instant getTimestampById(int id);

    @Insert
    long insert(SensorData data);

    @Insert
    List<Long> insertAll(SensorData... data);

    @Delete
    void delete(SensorData data);
}
