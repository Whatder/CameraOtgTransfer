package com.hexx.emptyapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by Hexx on 2019-05-06 18:24
 * Desc：
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.Holder> {

    Context mContext;
    List<Bitmap> mBitmaps;

    public ImageAdapter(Context context, List<Bitmap> bitmaps) {
        mContext = context;
        mBitmaps = bitmaps;
    }

    public void setBitmaps(List<Bitmap> bitmaps) {
        mBitmaps = bitmaps;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Holder(LayoutInflater.from(mContext).inflate(R.layout.item_image, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int i) {
        Glide.with(mContext).load(mBitmaps.get(i)).into(holder.iv);
    }

    @Override
    public int getItemCount() {
        return mBitmaps.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        ImageView iv;

        public Holder(@NonNull View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.iv);
        }
    }
}
