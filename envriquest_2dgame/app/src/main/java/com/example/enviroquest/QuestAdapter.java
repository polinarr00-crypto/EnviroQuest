package com.example.enviroquest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.ViewHolder> {
    private List<QuestModel> questList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(QuestModel quest);
    }

    public QuestAdapter(List<QuestModel> questList, OnItemClickListener listener) {
        this.questList = questList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quest_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuestModel quest = questList.get(position);
        holder.tvTitle.setText(quest.getTitle());
        holder.tvPoints.setText(quest.getPoints() + " Points");
        holder.btnView.setOnClickListener(v -> listener.onItemClick(quest));
    }

    @Override
    public int getItemCount() { return questList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPoints; // Gi-add ang tvPoints
        Button btnView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_quest_title);
            tvPoints = itemView.findViewById(R.id.tv_quest_points);
            btnView = itemView.findViewById(R.id.btn_action_quest);
        }
    }
}