package com.teamx.tuner;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.teamx.tuner.ToneFragment.OnListFragmentInteractionListener;

import java.util.List;

public class ToneRecyclerViewAdapter extends RecyclerView.Adapter<ToneRecyclerViewAdapter.ViewHolder> {

    private final List<Tone> mValues;
    private final OnListFragmentInteractionListener mListener;

    public ToneRecyclerViewAdapter(List<Tone> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_tone, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.name.setText(mValues.get(position).name);
        holder.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onAddToneClick(holder.mItem);
                }
            }
        });
        holder.playButton.setImageResource(holder.mItem.on ? R.drawable.play_on : R.drawable.play_off);
        holder.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    holder.mItem.on = !holder.mItem.on;
                    mListener.onPlayClick(holder.mItem, holder.getAdapterPosition());
                }
            }
        });
        holder.progressBar.setProgress(holder.mItem.progress);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView name;
        public Tone mItem;
        public ImageView addButton;
        public ImageView playButton;
        public ProgressBar progressBar;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            name = (TextView) view.findViewById(R.id.name);
            addButton = (ImageView) view.findViewById(R.id.add_button);
            playButton = (ImageView) view.findViewById(R.id.play_button);
            progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + name.getText() + "'";
        }
    }
}
