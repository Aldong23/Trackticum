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
import com.example.trackticum.models.StudDtr;

import java.util.List;

public class StudDtrAdapter extends RecyclerView.Adapter<StudDtrAdapter.StudDtrViewHolder>  {

    Context context;
    private List<StudDtr> dtrList;
    private StudDtrAdapter.StudDtrVAction actions;

    public StudDtrAdapter(Context context, List<StudDtr> dtrList, StudDtrVAction actions) {
        this.context = context;
        this.dtrList = dtrList;
        this.actions = actions;
    }

    public interface StudDtrVAction {
        void onViewDtr(String dtrId, String amTimeIn, String amTimeOut, String pmTimeIn, String pmTimeOut, String isSigned);
    }

    @NonNull
    @Override
    public StudDtrAdapter.StudDtrViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stud_dtr, parent, false);
        return new StudDtrAdapter.StudDtrViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudDtrAdapter.StudDtrViewHolder holder, int position) {
        StudDtr dtr = dtrList.get(position);
        String dtrId = dtr.getDtrId();
        String studId = dtr.getStudID();
        String date = dtr.getDate();
        String amTimeIn = dtr.getAmTimeIn();
        String amTimeOut = dtr.getAmTimeOut();
        String pmTimeIn = dtr.getPmTimeIn();
        String pmTimeOut = dtr.getPmTimeOut();
        String isSigned = dtr.getIsSigned();

        holder.dateTV.setText(date);
        holder.amTimeInTV.setText("Time in: " + (!amTimeIn.equals("null") ? amTimeIn : ""));
        holder.amTimeOutTV.setText("Time out: " + (!amTimeOut.equals("null") ? amTimeOut : ""));
        holder.pmTimeInTV.setText("Time in: " + (!pmTimeIn.equals("null") ? pmTimeIn : ""));
        holder.pmTimeOutTV.setText("Time out: " + (!pmTimeOut.equals("null") ? pmTimeOut : ""));

        if (isSigned.equals("1")) {
            holder.signedIV.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_check_circle_mark));
        } else {
            holder.signedIV.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_circle_mark));
        }

        holder.dtrContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actions.onViewDtr(dtrId, amTimeIn, amTimeOut, pmTimeIn, pmTimeOut, isSigned);
            }
        });

    }

    @Override
    public int getItemCount() {
        return dtrList.size();
    }

    public static class StudDtrViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout dtrContainer;
        TextView dateTV, amTimeInTV, amTimeOutTV, pmTimeInTV, pmTimeOutTV;
        ImageView signedIV;

        public StudDtrViewHolder(@NonNull View itemView) {
            super(itemView);
            dtrContainer = itemView.findViewById(R.id.dtrContainer);
            dateTV = itemView.findViewById(R.id.date_TV);
            amTimeInTV = itemView.findViewById(R.id.am_timein_tv);
            amTimeOutTV = itemView.findViewById(R.id.am_timeout_tv);
            pmTimeInTV = itemView.findViewById(R.id.pm_timein_tv);
            pmTimeOutTV = itemView.findViewById(R.id.pm_timeout_tv);
            signedIV = itemView.findViewById(R.id.status_IV);
        }
    }
}
