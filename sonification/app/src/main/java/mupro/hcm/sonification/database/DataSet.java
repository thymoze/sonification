package mupro.hcm.sonification.database;


import java.time.Instant;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity
public class DataSet {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;

    @TypeConverters(AppDatabase.class)
    private Instant timestamp;

    public DataSet(String name, Instant timestamp) {
        setName(name);
        setTimestamp(timestamp);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
