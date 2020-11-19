package com.example.theo_androidtv.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.theo_androidtv.model.Channel;
import com.example.theo_androidtv.R;

import java.util.List;

public class ChannelAdapter extends RecyclerView.Adapter<BaseViewHolder>
        implements View.OnClickListener {

    private static final String TAG = "ChannelAdapter";
    private View.OnClickListener listener;
    private List<Channel> mChannelList;


    public ChannelAdapter(List<Channel> channelList) {
        mChannelList = channelList;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_item, parent, false);

        view.setOnClickListener(this);

        //return new ViewHolder(
        //      LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_item, parent, false));

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);

    }

    @Override
    public int getItemCount() {
        if (mChannelList != null && mChannelList.size() > 0) {
            return mChannelList.size();
        } else {
            return 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    public void setOnClickListener(View.OnClickListener listener){
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        if(listener != null){
            listener.onClick(view);
        }
    }

    public class ViewHolder extends BaseViewHolder {

        ImageView tvLogo;
        TextView tvName;
        TextView tvCategory;
        TextView tvUrl;
        LinearLayout linearLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            tvLogo = itemView.findViewById(R.id.logo_canal);
            tvName = itemView.findViewById(R.id.nombre_canal);
            tvCategory = itemView.findViewById(R.id.cat_canal);
            tvUrl = itemView.findViewById(R.id.url_canal);
        }

        protected void clear() {
            tvLogo.setImageDrawable(null);
            tvName.setText("");
            tvUrl.setText("");
        }

        public void onBind(int position) {
            super.onBind(position);

            final Channel mChannel = mChannelList.get(position);

            if (mChannel.getIcon_url() != null) {
                Glide.with(itemView.getContext())
                        .load(mChannel.getIcon_url())
                        .into(tvLogo);
            }

            if (mChannel.getTitle() != null) {
                tvName.setText(mChannel.getTitle());
            }

            if (mChannel.getGenre_id() != null) {
                tvCategory.setText(mChannel.getGenre_id());
            }

            if (mChannel.getStream_url() != null) {
                tvUrl.setText(mChannel.getStream_url());
            }

            /* accion onclick sobre elemento lista */
            /*
            tvUrl.setOnClickListener(v -> {
                if (mChannel.getStream_url() != null) {
                    try {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.addCategory(Intent.CATEGORY_BROWSABLE);
                        intent.setData(Uri.parse(mChannel.getLink()));
                        itemView.getContext().startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "onClick: Image url is not correct");
                    }
                }
            });
            */



        }
    }

}
