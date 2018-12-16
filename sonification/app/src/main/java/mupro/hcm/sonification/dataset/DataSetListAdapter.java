package mupro.hcm.sonification.dataset;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import mupro.hcm.sonification.DataActivity;
import mupro.hcm.sonification.R;
import mupro.hcm.sonification.database.DataSet;

public class DataSetListAdapter extends RecyclerView.Adapter<DataSetListAdapter.ViewHolder> {

    private static final String TAG = DataSetListAdapter.class.getName();

    private Context mContext;
    // Cached copy of DataSets
    private List<DataSet> mDataSets;

    public DataSetListAdapter(Context context) {
        mContext = context;
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
                intent.putExtra("DATASET_ID", current.getId());
                mContext.startActivity(intent);
            });
        } else {
            viewHolder.title.setText("~");
        }
    }

    public void setDataSets(List<DataSet> dataSets) {
        mDataSets = dataSets;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mDataSets != null)
            return mDataSets.size();
        else return 0;
    }

    public void onItemDismiss(int position) {
        notifyItemRemoved(position);
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
