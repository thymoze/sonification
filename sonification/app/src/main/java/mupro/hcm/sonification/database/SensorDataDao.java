package mupro.hcm.sonification.database;

import java.time.Instant;
import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;
import mupro.hcm.sonification.sensors.Sensor;

@Dao
public abstract class SensorDataDao {

    @Query("SELECT * FROM SensorData")
    public abstract List<SensorData> getAll();

    @Query("SELECT * FROM SensorData ORDER BY datetime(timestamp) DESC LIMIT 30")
    public abstract List<SensorData> getLast30();

    @Query("SELECT * FROM SensorData WHERE id IN (:dataIds)")
    public abstract List<SensorData> loadAllByIds(int[] dataIds);

    @Query("SELECT timestamp FROM SensorData WHERE id = (:id)")
    @TypeConverters(AppDatabase.class)
    public abstract Instant getTimestampById(int id);

    @Insert
    public abstract long insert(SensorData data);

    @Insert
    public abstract List<Long> insertAll(SensorData... data);

    @Delete
    public abstract void delete(SensorData data);

    @Query("SELECT * FROM SensorData WHERE dataSetId=(:id)")
    public abstract List<SensorData> getSensorDataForDataSet(final long id);

    @RawQuery(observedEntities = SensorData.class)
    abstract List<Double> getSensorForDataSet(SupportSQLiteQuery query);

    public List<Double> getSensorForDataSet(Sensor sensor, Long dataSetId) {
        return getSensorForDataSet(new SimpleSQLiteQuery("SELECT " + sensor.getId() + " FROM SensorData WHERE dataSetId = (:id)", new Long[] { dataSetId }));
    }

    @Query("SELECT timestamp FROM SensorData WHERE dataSetId = :dataSetId")
    @TypeConverters(AppDatabase.class)
    public abstract List<Instant> getTimestampForDataSet(long dataSetId);
}
