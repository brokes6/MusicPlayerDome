package com.example.musicplayerdome.song.view;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.musicplayerdome.R;
import com.example.musicplayerdome.abstractclass.SongMvContract;
import com.example.musicplayerdome.base.BaseActivity;
import com.example.musicplayerdome.databinding.ActivitySongMvBinding;
import com.example.musicplayerdome.song.adapter.CommentAdapter;
import com.example.musicplayerdome.song.bean.MVDetailBean;
import com.example.musicplayerdome.song.bean.MusicCommentBean;
import com.example.musicplayerdome.song.bean.SongMvBean;
import com.example.musicplayerdome.song.bean.SongMvDataBean;
import com.example.musicplayerdome.song.other.MvPersenter;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;

import java.util.ArrayList;
import java.util.List;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

public class SongMvActivity extends BaseActivity<MvPersenter> implements SongMvContract.View,View.OnClickListener{
    private static final String TAG = "SongMvActivity";
    ActivitySongMvBinding binding;
    public static final String MVSONG_INFO = "mvsongInfo";
    private SongMvBean mvData;
    private int pid;
    private long sid;
    private String MVurl;
    private CommentAdapter hotAdapter, newAdapter;
    private List<MusicCommentBean.CommentsBean> hotCommentList = new ArrayList<>();
    private List<MusicCommentBean.CommentsBean> newCommentList = new ArrayList<>();

    @Override
    protected void onCreateView(Bundle savedInstanceState) {
        binding = DataBindingUtil.setContentView(this,R.layout.activity_song_mv);

        ImmersionBar.with(this)
                .transparentStatusBar()
                .statusBarDarkFont(false)
                .hideBar(BarHide.FLAG_HIDE_STATUS_BAR)
                .init();
        goDialog();
        initView();
    }

    @Override
    protected MvPersenter onCreatePresenter() {
        return new MvPersenter(this);
    }

    @Override
    protected void initModule() {

    }

    @Override
    protected void initData() {
        showDialog();
        getMvIntent();
    }

    //初始化视频播放器
    private void initView(){
        //不保存播放进度
        Jzvd.SAVE_PROGRESS = false;
        //自动播放
//        binding.jzVideo.startVideo();
        //重力感应
        Jzvd.FULLSCREEN_ORIENTATION=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        Jzvd.NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        binding.rvHotComment.setLayoutManager(new LinearLayoutManager(this));
        binding.rvNewComment.setLayoutManager(new LinearLayoutManager(this));
        hotAdapter = new CommentAdapter(this);
        newAdapter = new CommentAdapter(this);
        binding.rvHotComment.setAdapter(hotAdapter);
        binding.rvNewComment.setAdapter(newAdapter);
    }

    private void getMvIntent(){
        Intent intent = getIntent();
        if (intent!=null){
            sid = intent.getLongExtra(MVSONG_INFO,-1);
            pid = intent.getIntExtra("pid",-1);
            mPresenter.getSongMv(sid);
            mPresenter.getMVDetail(sid);
            mPresenter.getSongMvComment(sid);
        }
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onGetgetSongMvSuccess(SongMvDataBean bean) {
        MVurl = bean.getData().getUrl();
    }

    @Override
    public void onGetgetSongMvFail(String e) {
        hideDialog();
        Log.e(TAG, "获取MV播放地址错误"+e);
    }

    @Override
    public void onGetSongMvCommentSuccess(MusicCommentBean bean) {
        notifyList(bean.getHotComments(), bean.getComments());
        binding.SMComment.setText(""+bean.getTotal());
    }

    private void notifyList(List<MusicCommentBean.CommentsBean> hotComments, List<MusicCommentBean.CommentsBean> comments) {
        hotCommentList.clear();
        newCommentList.clear();
        if (hotComments != null) {
            hotCommentList = hotComments;
        }
        if (comments != null) {
            newCommentList = comments;
        }
        hotAdapter.loadMore(hotCommentList);
        newAdapter.loadMore(newCommentList);
    }

    @Override
    public void onGetSongMvCommentFail(String e) {
        Log.d(TAG, "评论错误 : " + e);
    }

    @Override
    public void onGetMVDetailSuccess(MVDetailBean bean) {
        hideDialog();
        binding.jzVideo.setUp(MVurl,bean.getData().getName());
        Glide.with(this).load(bean.getData().getCover()).into(binding.jzVideo.posterImageView);

        binding.userName.setText(bean.getData().getArtistName());
        binding.SMTitle.setText(bean.getData().getDesc());
        binding.SMShare.setText(""+bean.getData().getShareCount());
        binding.SMCollect.setText(""+bean.getData().getSubCount());
        binding.SMNumber.setText(bean.getData().getPlayCount()+"次观看");
        Glide.with(this).load(bean.getData().getArtists().get(0).getImg1v1Url()).into(binding.userImg);
    }

    @Override
    public void onGetMVDetailFail(String e) {
        hideDialog();
        Log.e(TAG, "获取MV详情错误"+e);
    }

    @Override
    public void onBackPressed() {
        if (Jzvd.backPress()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //home back
        JzvdStd.goOnPlayOnResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //     Jzvd.clearSavedProgress(this, null);
        //home back
        JzvdStd.goOnPlayOnPause();
        Jzvd.releaseAllVideos();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}
