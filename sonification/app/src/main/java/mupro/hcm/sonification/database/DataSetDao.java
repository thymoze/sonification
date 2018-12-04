package mupro.hcm.sonification.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.TypeConverters;

import java.time.Instant;
import java.util.List;

@Dao
public interface DataSetDao {

    @Query("SELECT * FROM DataSet")
    List<DataSet> getAll();

    @Query("SELECT * FROM DataSet ORDER BY datetime(timestamp) DESC LIMIT 30")
    List<DataSet> getLast30();

    @Query("SELECT * FROM DataSet WHERE id IN (:dataIds)")
    List<DataSet> loadAllByIds(int[] dataIds);

    @Query("SELECT timestamp FROM DataSet WHERE id = (:id)")
    @TypeConverters(AppDatabase.class)
    Instant getTimestampById(int id);

    @Insert
    long insert(DataSet data);

    @Insert
    List<Long> insertAll(DataSet... data);

    @Delete
    void delete(DataSet data);
}
