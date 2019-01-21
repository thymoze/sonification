package mupro.hcm.sonification.dataset;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import mupro.hcm.sonification.DataActivity;
import mupro.hcm.sonification.MainActivity;
import mupro.hcm.sonification.R;
import mupro.hcm.sonification.database.DataSet;

import static mupro.hcm.sonification.MainActivity.CURRENT_DATASET;
import static mupro.hcm.sonification.MainActivity.EXTRA_DATASETID;

public class DataSetListAdapter extends RecyclerView.Adapter<DataSetListAdapter.ViewHolder> {

    private static final String TAG = DataSetListAdapter.class.getName();

    private MainActivity mContext;
    // Cached copy of DataSets
    private List<DataSet> mDataSets;
    private int mPendingIndex;
    private DataSet mPendingDataset;

    public DataSetListAdapter(MainActivity context) {
        mContext = context;
        mDataSets = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View item = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.dataset_card, viewGroup,  false);
        return new ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        if (mDataSets != null) {
            DataSet current = mDataSets.get(position);
            viewHolder.title.setText(current.getName());
            viewHolder.timestamp.setText(current.getTimestamp()
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("dd. MMM YYYY HH:mm")));
            viewHolder.distance.setText("x.x km");

            viewHolder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, DataActivity.class);
                intent.putExtra(EXTRA_DATASETID, current.getId());
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(mContext, viewHolder.title, "dataset_title");
                mContext.startActivity(intent, options.toBundle());
            });
        } else {
            viewHolder.title.setText("~");
        }
    }

    public void setDataSets(List<DataSet> dataSets) {
        mDataSets.clear();
        mDataSets.addAll(dataSets);

        // if a dataset pending removal exist in the new dataset
        // we save the index in the new dataset and a reference to the newer object
        // otherwise we discard the pending values
        int index = mDataSets.indexOf(mPendingDataset);
        if (index != -1) {
            mPendingDataset = mDataSets.remove(index);
        } else {
            mPendingDataset = null;
        }
        mPendingIndex = index;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mDataSets != null)
            return mDataSets.size();
        else return 0;
    }

    public DataSet getItem(int position) {
        return mDataSets.get(position);
    }

    public void pendingRemoval(int position) {
        Log.e(TAG, "pendingRemoval: " + position);
        mPendingIndex = position;
        mPendingDataset = mDataSets.remove(position);
        notifyItemRemoved(position);
    }

    public void cancelRemoval() {
        mDataSets.add(mPendingIndex, mPendingDataset);
        notifyItemInserted(mPendingIndex);
        mPendingDataset = null;
        mPendingIndex = -1;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.dataset_title)
        public TextView title;
        @BindView(R.id.dataset_timestamp)
        public TextView timestamp;
        @BindView(R.id.dataset_distance)
        public TextView distance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
