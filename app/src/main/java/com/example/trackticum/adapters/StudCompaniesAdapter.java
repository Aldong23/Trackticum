package com.example.trackticum.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackticum.R;
import com.example.trackticum.models.StudCompanies;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

public class StudCompaniesAdapter extends RecyclerView.Adapter<StudCompaniesAdapter.StudCompaniesViewHolder> {

    Context context;
    private List<StudCompanies> studCompaniesList;
    private StudCompaniesAdapter.StudCompaniesAction actions;

    public StudCompaniesAdapter(Context context, List<StudCompanies> studCompaniesList, StudCompaniesAction actions) {
        this.context = context;
        this.studCompaniesList = studCompaniesList;
        this.actions = actions;
    }

    public interface StudCompaniesAction {
        void onViewCompanies(String comID);
    }

    @NonNull
    @Override
    public StudCompaniesAdapter.StudCompaniesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stud_companies, parent, false);
        return new StudCompaniesAdapter.StudCompaniesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudCompaniesAdapter.StudCompaniesViewHolder holder, int position) {
        StudCompanies companies = studCompaniesList.get(position);
        String comID = companies.getId();
        String comLogo = companies.getLogo();
        String comName = companies.getName();
        String comAddress = companies.getAddress();
        String comDescription = companies.getDescription();
        String comSlot = companies.getSlot();

        if (!comLogo.isEmpty()) {
            Picasso.get()
                    .load(comLogo)
                    .placeholder(R.drawable.img_placeholder)
                    .error(R.drawable.img_placeholder)
                    .resize(500, 500)
                    .centerCrop()
                    .into(holder.comLogo);
        }

        holder.comName.setText(comName);
        holder.comAddress.setText(comAddress);
        holder.comDescription.setText(Html.fromHtml(comDescription, Html.FROM_HTML_MODE_LEGACY));
        holder.comSlot.setText("Slot: " + comSlot);

        holder.comContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actions.onViewCompanies(comID);
            }
        });
    }

    @Override
    public int getItemCount() {
        return studCompaniesList.size();
    }

    public static class StudCompaniesViewHolder extends RecyclerView.ViewHolder {

        CardView comContainer;
        RoundedImageView comLogo;
        TextView comName, comAddress, comDescription, comSlot;

        public StudCompaniesViewHolder(@NonNull View itemView) {
            super(itemView);
            comContainer = itemView.findViewById(R.id.companyContainer);
            comLogo = itemView.findViewById(R.id.logoImageView);
            comName = itemView.findViewById(R.id.companyNameTextView);
            comAddress = itemView.findViewById(R.id.addressTextView);
            comDescription = itemView.findViewById(R.id.descriptionTextView);
            comSlot = itemView.findViewById(R.id.slotTextView);
        }
    }

}
