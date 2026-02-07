package com.example.enviroquest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.ViewHolder> {

    private List<RankingModel> rankingList;

    public RankingAdapter(List<RankingModel> rankingList) {
        this.rankingList = rankingList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ranking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RankingModel player = rankingList.get(position);

        holder.tvRank.setText(String.valueOf(player.getRank()));
        holder.tvName.setText(player.getName());
        holder.tvPoints.setText(String.format("%, d", player.getPoints()) + " PTS");

        if (player.getRank() == 1) holder.tvRank.setTextColor(android.graphics.Color.parseColor("#FACC15")); // Gold
        else if (player.getRank() == 2) holder.tvRank.setTextColor(android.graphics.Color.parseColor("#94A3B8")); // Silver
        else if (player.getRank() == 3) holder.tvRank.setTextColor(android.graphics.Color.parseColor("#B45309")); // Bronze
        else holder.tvRank.setTextColor(android.graphics.Color.parseColor("#38BDF8")); // Default Blue
    }

    @Override
    public int getItemCount() {
        return rankingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvPoints;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tv_rank_number);
            tvName = itemView.findViewById(R.id.tv_rank_name);
            tvPoints = itemView.findViewById(R.id.tv_rank_points);
        }
    }
}