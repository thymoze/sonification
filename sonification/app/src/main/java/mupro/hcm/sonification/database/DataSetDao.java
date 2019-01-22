package mupro.hcm.sonification.database;



import java.time.Instant;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;

@Dao
public interface DataSetDao {

    @Query("SELECT * FROM DataSet WHERE id = (:id)")
    DataSet getById(long id);

    @Query("SELECT * FROM DataSet ORDER BY id DESC")
    LiveData<List<DataSet>> getAll();

    @Query("SELECT * FROM DataSet ORDER BY datetime(timestamp) DESC LIMIT 30")
    List<DataSet> getLast30();

    @Query("SELECT * FROM DataSet WHERE id IN (:dataIds)")
    List<DataSet> loadAllByIds(int[] dataIds);

    @Query("SELECT timestamp FROM DataSet WHERE id = (:id)")
    @TypeConverters(AppDatabase.class)
    Instant getTimestampById(int id);

    @Query("UPDATE DataSet SET distanceInKm = :distance WHERE id = :id")
    void setDistanceforId(double distance, long id);

    @Insert
    long insert(DataSet data);

    @Insert
    List<Long> insertAll(DataSet... data);

    @Delete
    void delete(DataSet data);

    @Query("DELETE FROM DataSet WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT COUNT(id) FROM DataSet")
    int size();
}
