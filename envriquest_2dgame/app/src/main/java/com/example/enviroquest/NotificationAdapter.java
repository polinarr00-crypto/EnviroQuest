package com.example.enviroquest;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private List<NotificationModel> notifList;

    public NotificationAdapter(List<NotificationModel> notifList) {
        this.notifList = notifList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationModel model = notifList.get(position);

        holder.tvTitle.setText(model.getTitle());
        holder.tvPoints.setText("+" + model.getPoints() + " PTS");

        boolean isHighlighted = "NEW".equals(model.getType()) && !model.isSubmitted();

        if (isHighlighted) {
            // NEW MISSION - Highlighted
            holder.tvStatus.setText("Status: NEW MISSION");
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#38BDF8")); // Blue
            holder.cardView.setStrokeColor(android.graphics.Color.parseColor("#38BDF8"));
            holder.cardView.setCardBackgroundColor(android.graphics.Color.parseColor("#2D3748"));

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), QuestListActivity.class);
                v.getContext().startActivity(intent);
            });
        } else {
            // Normal / Not highlighted (Approved or Already Submitted NEW Quest)
            if ("APPROVED".equals(model.getType())) {
                holder.tvStatus.setText("Status: APPROVED");
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#22C55E")); // Green
            } else {
                holder.tvStatus.setText("Status: NEW MISSION (Submitted)");
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#94A3B8")); // Muted Gray
            }
            
            holder.cardView.setStrokeColor(android.graphics.Color.parseColor("#334155"));
            holder.cardView.setCardBackgroundColor(android.graphics.Color.parseColor("#1E293B"));
            
            // Allow clicking to quest list even if not highlighted, or remove? 
            // User said "dili na ma highlight", didn't specify removal of click.
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), QuestListActivity.class);
                v.getContext().startActivity(intent);
            });
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPoints, tvStatus;
        MaterialCardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.notif_title);
            tvPoints = itemView.findViewById(R.id.notif_points);
            tvStatus = itemView.findViewById(R.id.notif_status);
            cardView = (MaterialCardView) itemView;
        }
    }

    @Override
    public int getItemCount() { return notifList.size(); }
}