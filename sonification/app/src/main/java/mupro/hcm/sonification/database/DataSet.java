package mupro.hcm.sonification.database;


import java.time.Instant;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    @NonNull
    @Override
    public String toString() {
        return getId() + ": " + getName();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof DataSet)) {
            return false;
        }
        DataSet other = (DataSet) obj;
        return this.getId() == other.getId() && this.getName().equals(other.getName())
                && this.getTimestamp().equals(other.getTimestamp());
    }
}
