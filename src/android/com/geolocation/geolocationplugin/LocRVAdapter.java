package com.geolocation.geolocationplugin;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.View;
import android.content.Context;

import java.util.List;


public class LocRVAdapter extends RecyclerView.Adapter<LocRVAdapter.LocRvViewHolder> {

    private List<LocModel> locModelList;
    private Context context;
    private String package_name;

    public class LocRvViewHolder extends RecyclerView.ViewHolder{

        TextView Longitude, Latitude, Distance;

        LocRvViewHolder(View view){
            super(view);
            Longitude = (TextView) view.findViewById(context.getResources().getIdentifier("tv_longitude", "id", package_name));
            Latitude = (TextView) view.findViewById(context.getResources().getIdentifier("tv_latitude", "id", package_name));
            Distance = (TextView) view.findViewById(context.getResources().getIdentifier("tv_distance", "id", package_name));
        }
    }

    public LocRVAdapter(List<LocModel> locModelList, Context context){
        this.locModelList = locModelList;
        this.context = context;
        this.package_name = context.getPackageName();
    }

    @Override
    public void onBindViewHolder(LocRvViewHolder holder, int position) {
        holder.Longitude.setText(locModelList.get(position).getLongitude());
        holder.Latitude.setText(locModelList.get(position).getLatitude());
        holder.Distance.setText(locModelList.get(position).getDistance());
    }

    @Override
    public int getItemCount() {
        return locModelList.size();
    }

    @Override
    public LocRvViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(context.getResources().getIdentifier("single_element", "layout", package_name), parent, false);
        return new LocRvViewHolder(view);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
