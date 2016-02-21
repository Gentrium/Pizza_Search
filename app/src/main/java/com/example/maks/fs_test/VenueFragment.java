package com.example.maks.fs_test;

import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.StringTokenizer;

import br.com.condesales.models.Category;
import br.com.condesales.models.Venue;

public class VenueFragment extends DialogFragment {
    private Venue venue;
    private TextView url;
    private TextView address;
    private TextView tips;
    private TextView checkings;
    private TextView hereNow;
    private TextView distance;
    private TextView category;

    public static VenueFragment newInstance(Venue venue) {
        VenueFragment f = new VenueFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putSerializable("venue", venue);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        venue = (Venue) getArguments().getSerializable("venue");
        setStyle(DialogFragment.STYLE_NORMAL , android.R.style.Theme_Holo_Light_Dialog);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle(venue.getName());
        View v = inflater.inflate(R.layout.venue_fragment, container, false);
        url = (TextView) v.findViewById(R.id.tv_frag_url);
        address = (TextView) v.findViewById(R.id.tv_adress);
        distance = (TextView) v.findViewById(R.id.tv_distance);
        tips = (TextView) v.findViewById(R.id.tv_tips);
        checkings = (TextView) v.findViewById(R.id.tv_checkins);
        hereNow = (TextView) v.findViewById(R.id.tv_here_now);
        category = (TextView) v.findViewById(R.id.tv_category_frag);
        setData();
        return v;
    }
    private void setData(){
        if(venue.getUrl()!= null && venue.getUrl().length() > 0){
            url.setText(venue.getUrl());
            url.setTextColor(Color.BLUE);
            url.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(venue.getUrl()));
                    getActivity().startActivity(i);
                }
            });
        } else {
            url.setText(R.string.no_web_page);
        }
        StringBuilder categories = new StringBuilder();
        for (Category cat:venue.getCategories()) {
            categories.append(cat.getName());
        }
        category.setText(categories.toString());
        address.setText(venue.getLocation().getAddress());
        distance.setText("Distance :" + venue.getLocation().getDistance());
        tips.setText("Tips :" + (venue.getStats().getTipCount()));
        checkings.setText("Checkings:" +(venue.getStats().getCheckinsCount()));
        hereNow.setText(venue.getHereNow().getSummary());

    }
}

