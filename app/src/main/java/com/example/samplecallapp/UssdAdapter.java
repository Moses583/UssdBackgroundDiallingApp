package com.example.samplecallapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class UssdAdapter extends RecyclerView.Adapter<UssdAdapter.UssdViewHolder> {
    private Context context;
    private List<UssdResponse> responseList = new ArrayList<>();

    public UssdAdapter(Context context) {
        this.context = context;
    }

    public void setResponseList(List<UssdResponse> responseList) {
        this.responseList = responseList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UssdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UssdViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_view,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull UssdViewHolder holder, int position) {
        String response = responseList.get(position).getUssdResponse();
        holder.txtUssdResponse.setText(response);
    }

    @Override
    public int getItemCount() {
        return responseList.size();
    }

    public static class UssdViewHolder extends RecyclerView.ViewHolder {
        TextView txtUssdResponse;
        public UssdViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUssdResponse = itemView.findViewById(R.id.txtUssdResponse);
        }
    }
}

