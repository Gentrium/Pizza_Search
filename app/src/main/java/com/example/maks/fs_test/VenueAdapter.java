package com.example.maks.fs_test;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import br.com.condesales.models.Venue;

public class VenueAdapter extends RecyclerView.Adapter<VenueAdapter.VenueViewHolder> {

    private ArrayList<Venue> venues;
    private Context context;

    public VenueAdapter(ArrayList<Venue> venues, Context context) {
        this.venues = venues;
        this.context = context;
    }

    @Override
    public VenueViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rv_venue_item, parent, false);
        VenueViewHolder viewHolder = new VenueViewHolder(view);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(VenueViewHolder holder, final int position) {
        final Venue current = venues.get(position);
        holder.name.setText(current.getName());
        if(current.getUrl()!= null && current.getUrl().length() > 0){
            holder.url.setText(current.getUrl());
            holder.url.setTextColor(Color.BLUE);
            holder.url.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(current.getUrl()));
                    context.startActivity(i);
                }
            });
        } else {
            holder.url.setText(R.string.no_web_page);
        }
        holder.category.setText(current.getCategories().get(0).getName());
        holder.adress.setText(current.getLocation().getAddress());

        holder.background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(venues.get(position));
            }
        });

        if(position == venues.size()-1 && venues.size() != 50 && venues.size()%10 == 0){
            ((MainAppActivity)context).requestVenuesNearby(true);
        }
    }
    void showDialog(Venue venue) {

        FragmentTransaction ft = ((MainAppActivity)context).getFragmentManager().beginTransaction();
        Fragment prev = ((MainAppActivity)context).getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = VenueFragment.newInstance(venue);
        newFragment.show(ft, "dialog");
    }

    @Override
    public int getItemCount() {
        return venues.size();
    }
    class VenueViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView url;
        TextView category;
        TextView adress;
        LinearLayout background;
        public VenueViewHolder(View itemView) {
            super(itemView);
            background = (LinearLayout) itemView.findViewById(R.id.rv_item_layout);
            name = (TextView) itemView.findViewById(R.id.tv_venue_name);
            url = (TextView) itemView.findViewById(R.id.tv_venue_url);
            category = (TextView) itemView.findViewById(R.id.tv_venue_category);
            adress = (TextView) itemView.findViewById(R.id.tv_venue_adress);
        }
    }

}
