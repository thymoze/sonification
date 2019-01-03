package mupro.hcm.sonification.dataset;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import mupro.hcm.sonification.database.AppDatabase;
import mupro.hcm.sonification.database.DataSet;
import mupro.hcm.sonification.database.DataSetDao;

public class DataSetViewModel extends AndroidViewModel {

    private DataSetDao mDataSetDao;
    private LiveData<List<DataSet>> mDataSets;

    public DataSetViewModel(@NonNull Application application) {
        super(application);
        mDataSetDao = AppDatabase.getDatabase(application).dataSetDao();
        mDataSets = mDataSetDao.getAll();
    }

    public LiveData<List<DataSet>> getAllDataSets() { return mDataSets; }

    public void insert(DataSet dataSet) {
        AsyncTask.execute(() -> mDataSetDao.insert(dataSet));
    }

    public void delete(int position) {
        AsyncTask.execute(() -> mDataSetDao.delete(mDataSets.getValue().get(position)));
    }
}
