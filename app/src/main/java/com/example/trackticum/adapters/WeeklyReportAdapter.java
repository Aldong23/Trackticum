package com.example.trackticum.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackticum.R;
import com.example.trackticum.models.StudSkill;
import com.example.trackticum.models.WeeklyReport;

import java.util.List;

public class WeeklyReportAdapter extends RecyclerView.Adapter<WeeklyReportAdapter.WeeklyReportViewHolder>  {

    Context context;
    private List<WeeklyReport> weeklyReportList;
    private WeeklyReportAdapter.WeeklyReportActions actions;

    public WeeklyReportAdapter(Context context, List<WeeklyReport> weeklyReportList, WeeklyReportActions actions) {
        this.context = context;
        this.weeklyReportList = weeklyReportList;
        this.actions = actions;
    }

    public interface WeeklyReportActions {
        void onViewWeeklyReport(String weeklyId);
    }

    @NonNull
    @Override
    public WeeklyReportAdapter.WeeklyReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weekly_report, parent, false);
        return new WeeklyReportAdapter.WeeklyReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeeklyReportAdapter.WeeklyReportViewHolder holder, int position) {
        WeeklyReport report = weeklyReportList.get(position);
        String studID = report.getStudID();
        String weeklyReportId = report.getWeeklyID();
        String isSigned = report.getIsSigned();

        holder.titleTV.setText(report.getTitle());
        holder.dateTV.setText(report.getDate());

        if (isSigned.equals("1")) {
            holder.isSignedIV.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_check_circle_mark));
        } else {
            holder.isSignedIV.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_circle_mark));
        }

        holder.weeklyContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actions.onViewWeeklyReport(weeklyReportId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return weeklyReportList.size();
    }

    public static class WeeklyReportViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout weeklyContainer;
        TextView titleTV, dateTV;
        ImageView isSignedIV;

        public WeeklyReportViewHolder(@NonNull View itemView) {
            super(itemView);
            weeklyContainer = itemView.findViewById(R.id.weeklyContainer);
            titleTV = itemView.findViewById(R.id.title_tv);
            dateTV = itemView.findViewById(R.id.date);
            isSignedIV = itemView.findViewById(R.id.status_IV);;
        }
    }
}
