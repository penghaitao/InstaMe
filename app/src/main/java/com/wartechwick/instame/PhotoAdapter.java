package com.wartechwick.instame;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wartechwick.instame.ui.OnPhotoClickListener;
import com.wartechwick.instame.db.Photo;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by penghaitao on 2015/12/19.
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

    private List<Photo> mGrams;
    private Context mContext;
    private OnPhotoClickListener iPhotoClickListener;
//    private boolean bVideoIsBeingTouched = false;
//    private Handler mHandler = new Handler();

    public PhotoAdapter(Context context, List<Photo> grams) {
        mGrams = grams;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View gramView = inflater.inflate(R.layout.insta_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(gramView);
        return viewHolder;
    }

    public void setiPhotoClickListener(OnPhotoClickListener iPhotoClickListener) {
        this.iPhotoClickListener = iPhotoClickListener;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        Photo photo = mGrams.get(position);
        holder.position = position;
        holder.authorNameView.setText(photo.getAuthorName());

        if (photo.getVideoUrl() != null) {
            holder.playView.setVisibility(View.VISIBLE);
//            holder.shareButton.setVisibility(View.GONE);
            holder.wallPaperButton.setVisibility(View.GONE);
        } else {
            holder.playView.setVisibility(View.GONE);
//            holder.shareButton.setVisibility(View.VISIBLE);
            holder.wallPaperButton.setVisibility(View.VISIBLE);
        }

        // use device width for photo height
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        holder.photoImageView.getLayoutParams().height = displayMetrics.widthPixels*photo.getThumbnailHeight()/photo.getThumbnailWidth();
        holder.photoImageView.getLayoutParams().width = displayMetrics.widthPixels;

        // Reset the images from the recycled view
        holder.photoImageView.setImageResource(0);

        Picasso.with(mContext).load(photo.getThumbnailLargeUrl()).into(holder.photoImageView);

//        Glide.with(mContext).load(photo.getThumbnailLargeUrl()).into(holder.photoImageView);
    }

    @Override
    public int getItemCount() {
        return mGrams.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        @Bind(R.id.insta_image) ImageView photoImageView;
        @Bind(R.id.author_name) TextView authorNameView;
        @Bind(R.id.btn_save) ImageView downloadButton;
        @Bind(R.id.btn_share) ImageView shareButton;
        @Bind(R.id.btn_wallpaper) ImageView wallPaperButton;
        @Bind(R.id.btn_delete) ImageView deleteButton;
        @Bind(R.id.insta_play) ImageView playView;
        public int position;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            authorNameView.setOnClickListener(this);
            downloadButton.setOnClickListener(this);
            shareButton.setOnClickListener(this);
            wallPaperButton.setOnClickListener(this);
            deleteButton.setOnClickListener(this);
            playView.setOnClickListener(this);
            photoImageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            iPhotoClickListener.onTouch(v, photoImageView, position);
        }


    }
}
