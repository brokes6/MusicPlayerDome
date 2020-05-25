package com.example.musicplayerdome.song.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.android.liuzhuang.rcimageview.CircleImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.musicplayerdome.R;
import com.example.musicplayerdome.song.bean.MusicCommentBean;
import com.example.musicplayerdome.util.TimeUtil;
import com.xuexiang.xui.adapter.recyclerview.BaseRecyclerAdapter;
import com.xuexiang.xui.adapter.recyclerview.RecyclerViewHolder;

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends BaseRecyclerAdapter<MusicCommentBean.CommentsBean> {
    private static final String TAG = "CommentAdapter";

    private List<MusicCommentBean.CommentsBean> list = new ArrayList<>();
    private Context mContext;
    private OnLikeCommentListener listener;
    TextView tvUserName, tvContent, tvPublishTime;
    CircleImageView ivAvatar;
    TextView tvLikeCount;
    ImageView ivLike;
    RelativeLayout rlLike, rlGap;
    TextView tvGap;

    public CommentAdapter(Context context) {
        mContext = context;
    }

    @Override
    protected int getItemLayoutId(int viewType) {
        return R.layout.item_comment;
    }

    public void setListener(OnLikeCommentListener listener) {
        this.listener = listener;
    }

    @Override
    protected void bindData(@NonNull RecyclerViewHolder holder, int position, MusicCommentBean.CommentsBean item) {
        if (item!=null) {
            tvUserName = holder.findViewById(R.id.tv_username);
            ivAvatar = holder.findViewById(R.id.iv_avatar);
            tvContent = holder.findViewById(R.id.tv_content);
            tvPublishTime = holder.findViewById(R.id.tv_publish_time);
            tvLikeCount = holder.findViewById(R.id.tv_like_count);
            ivLike = holder.findViewById(R.id.iv_like);
            rlLike = holder.findViewById(R.id.rl_like);
            rlGap = holder.findViewById(R.id.rl_gap);
            tvGap = holder.findViewById(R.id.tv_gap);
        }
        setBean(item, position);
        setListener(listener, position);
    }
    public void setBean(MusicCommentBean.CommentsBean bean, int position) {
        Glide.with(mContext).load(bean.getUser().getAvatarUrl()).transition(new DrawableTransitionOptions().crossFade()).into(ivAvatar);
        tvUserName.setText(bean.getUser().getNickname());

        tvPublishTime.setText(TimeUtil.getTimeStandard(bean.getTime()));
        if (bean.isLiked()) {
            ivLike.setImageResource(R.drawable.shape_comment_like);
        } else {
            ivLike.setImageResource(R.drawable.shape_comment_unlike);
        }
        float likeCount = bean.getLikedCount();
        boolean moreThanWan = false;
        if (likeCount > 10000) {
            likeCount = likeCount / 10000f;
            moreThanWan = true;
        }
        String res;
        if (moreThanWan) {
            Log.w(TAG, "bean : " + bean + " likeCount" + likeCount);
            res = String.format("%.1f", likeCount) + "万";
        } else {
            int likeCountInt = (int) likeCount;
            res = String.valueOf(likeCountInt);
        }
        tvLikeCount.setText(res);
        if (!TextUtils.isEmpty(bean.getContent())) {
            tvContent.setVisibility(View.VISIBLE);
            tvContent.setText(bean.getContent());
        }
        if (position > 0) {
            rlGap.setVisibility(View.VISIBLE);
        }
        if (position == list.size() - 1) {
            tvGap.setVisibility(View.GONE);
        }
    }
    public void setListener(OnLikeCommentListener listener, int position) {
        rlLike.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLikeClick(position);
            }
        });
    }

    public interface OnLikeCommentListener {
        void onLikeClick(int position);
    }

}