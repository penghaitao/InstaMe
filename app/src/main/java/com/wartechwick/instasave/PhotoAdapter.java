package com.wartechwick.instasave;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wartechwick.instasave.UI.OnPhotoClickListener;
import com.wartechwick.instasave.db.Photo;

import java.util.List;

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
//        Picasso.with(mContext)
//                .load(photo.getThumbnailUrl())
//                .into(target);
    }

//    private Target target = new Target() {
//
//        @Override
//        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
//            new Thread(new Runnable() {
//
//                @Override
//                public void run() {
//
//                    File file = new File(Utils.getImageDirectory(mContext) + ".jpg");
//                    try {
//                        file.createNewFile();
//                        FileOutputStream ostream = new FileOutputStream(file);
//                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream);
//                        ostream.close();
////                        IntentUtils.showSnackbar(R.string.image_saved_already, mContext);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            }).start();
//        }
//
//        @Override
//        public void onBitmapFailed(Drawable errorDrawable) {}
//
//        @Override
//        public void onPrepareLoad(Drawable placeHolderDrawable) {
//            if (placeHolderDrawable != null) {}
//        }
//    };

    @Override
    public int getItemCount() {
        return mGrams.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public ImageView photoImageView;
//        public VideoView gramVideoView;
        public TextView authorNameView;
        public ImageView downloadButton;
        public ImageView shareButton;
        public ImageView wallPaperButton;
        public ImageView deleteButton;
        public int position;
        private View mItemView;
        private ImageView playView;
//        private int stopPosition;

        public ViewHolder(View itemView) {
            super(itemView);

            photoImageView = (ImageView) itemView.findViewById(R.id.insta_image);
            authorNameView = (TextView) itemView.findViewById(R.id.author_name);
            downloadButton = (ImageView) itemView.findViewById(R.id.btn_save);
            shareButton = (ImageView) itemView.findViewById(R.id.btn_share);
            wallPaperButton = (ImageView) itemView.findViewById(R.id.btn_wallpaper);
            deleteButton = (ImageView) itemView.findViewById(R.id.btn_delete);
//            gramVideoView = (VideoView) itemView.findViewById(R.id.insta_vedio);
            playView = (ImageView) itemView.findViewById(R.id.insta_play);

            authorNameView.setOnClickListener(this);
            downloadButton.setOnClickListener(this);
            shareButton.setOnClickListener(this);
            wallPaperButton.setOnClickListener(this);
            deleteButton.setOnClickListener(this);
            playView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            iPhotoClickListener.onTouch(v, photoImageView, position);
        }


    }
}
