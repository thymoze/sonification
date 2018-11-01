package mupro.hcm.sonification.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface SensorDataDao {

    @Query("SELECT * FROM sensordata")
    List<SensorData> getAll();

    @Query("SELECT * FROM sensordata WHERE id IN (:dataIds)")
    List<SensorData> loadAllByIds(int[] dataIds);

    @Insert
    void insertAll(SensorData... data);

    @Delete
    void delete(SensorData data);

}
