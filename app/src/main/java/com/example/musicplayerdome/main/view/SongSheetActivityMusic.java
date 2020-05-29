package com.example.musicplayerdome.main.view;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.musicplayerdome.R;
import com.example.musicplayerdome.abstractclass.WowContract;
import com.example.musicplayerdome.databinding.SongPlayListBinding;
import com.example.musicplayerdome.song.adapter.MySongListAdapter;
import com.example.musicplayerdome.base.BaseActivity;
import com.example.musicplayerdome.bean.BannerBean;
import com.example.musicplayerdome.bean.MusicCanPlayBean;
import com.example.musicplayerdome.databinding.SongSheetBinding;
import com.example.musicplayerdome.main.bean.DailyRecommendBean;
import com.example.musicplayerdome.main.bean.HighQualityPlayListBean;
import com.example.musicplayerdome.main.bean.MainRecommendPlayListBean;
import com.example.musicplayerdome.main.bean.PlaylistDetailBean;
import com.example.musicplayerdome.main.bean.RecommendPlayListBean;
import com.example.musicplayerdome.main.bean.TopListBean;
import com.example.musicplayerdome.main.other.WowPresenter;
import com.example.musicplayerdome.song.other.SongPlayManager;
import com.gyf.immersionbar.ImmersionBar;
import com.lzx.starrysky.model.SongInfo;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

import static com.example.musicplayerdome.main.fragment.HomeFragment.PLAYLIST_CREATOR_AVATARURL;
import static com.example.musicplayerdome.main.fragment.HomeFragment.PLAYLIST_CREATOR_NICKNAME;
import static com.example.musicplayerdome.main.fragment.HomeFragment.PLAYLIST_ID;
import static com.example.musicplayerdome.main.fragment.HomeFragment.PLAYLIST_NAME;
import static com.example.musicplayerdome.main.fragment.HomeFragment.PLAYLIST_PICURL;

public class SongSheetActivityMusic extends BaseActivity<WowPresenter> implements View.OnClickListener, WowContract.View{
    SongSheetBinding binding;
//    SongPlayListBinding binding;
    private static final String TAG = "SongSheetActivity";
    private MySongListAdapter adapter;
    private List<PlaylistDetailBean.PlaylistBean.TracksBean> beanList = new ArrayList<>();
    private List<SongInfo> songInfos = new ArrayList<>();
    private long playlistId;
    private boolean go = false;
    private String creatorUrl;
    private String playlistName;
    private String playlistPicUrl;
    private String creatorName;
    //计算完成后发送的Handler msg
    public static final int COMPLETED = 0;

    @Override
    protected void onCreateView(Bundle savedInstanceState) {
        binding = DataBindingUtil.setContentView(this,R.layout.song_sheet);
//        binding = DataBindingUtil.setContentView(this,R.layout.song_play_list);
        ImmersionBar.with(this)
                .statusBarDarkFont(false)
                .fitsSystemWindows(true)  //使用该属性,必须指定状态栏颜色
                .statusBarColor(R.color.A3A3)
                .init();
        goDialog();
    }

    @Override
    protected void initData() {
        initView();
        showDialog();
        LinearLayoutManager lm = new LinearLayoutManager(SongSheetActivityMusic.this);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        adapter = new MySongListAdapter(this);
        adapter.setType(2);
        binding.recyclerView.setLayoutManager(lm);
        binding.recyclerView.setAdapter(adapter);

        if (getIntent() != null) {
            playlistPicUrl = getIntent().getStringExtra(PLAYLIST_PICURL);
            Glide.with(this).load(playlistPicUrl).into(binding.XLogin);
            playlistName = getIntent().getStringExtra(PLAYLIST_NAME);
            binding.XTitle.setText(playlistName);
            creatorName = getIntent().getStringExtra(PLAYLIST_CREATOR_NICKNAME);
            binding.tvPlaylistName.setText(creatorName);
            creatorUrl = getIntent().getStringExtra(PLAYLIST_CREATOR_AVATARURL);
            Glide.with(this).load(creatorUrl).into(binding.userImg);
            playlistId = getIntent().getLongExtra(PLAYLIST_ID, 0);

            mPresenter.getPlaylistDetail(playlistId);
        }
    }

    private void initView(){
        binding.Pback.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.Pback:
                finish();
                break;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (SongPlayManager.getInstance().isPlaying()) {
            binding.bottomController.setVisibility(View.VISIBLE);
        } else {
            binding.bottomController.setVisibility(View.GONE);
        }
    }

    @Override
    public void onGetBannerSuccess(BannerBean bean) {

    }

    @Override
    public void onGetBannerFail(String e) {

    }

    @Override
    public void onGetRecommendPlayListSuccess(MainRecommendPlayListBean bean) {

    }

    @Override
    public void onGetRecommendPlayListFail(String e) {

    }

    @Override
    public void onGetRecommendPlayListAgainSuccess(MainRecommendPlayListBean bean) {

    }

    @Override
    public void onGetRecommendPlayListAgainFail(String e) {

    }

    @Override
    public void onGetDailyRecommendSuccess(DailyRecommendBean bean) {

    }

    @Override
    public void onGetDailyRecommendFail(String e) {

    }

    @Override
    public void onGetTopListSuccess(TopListBean bean) {

    }

    @Override
    public void onGetTopListFail(String e) {

    }

    @Override
    public void onGetPlayListSuccess(RecommendPlayListBean bean) {

    }

    @Override
    public void onGetPlayListFail(String e) {

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onGetPlaylistDetailSuccess(PlaylistDetailBean bean) {
        hideDialog();
        Log.d(TAG, "获取歌单成功 : " + bean);
        if (!TextUtils.isEmpty(creatorUrl)) {
            Glide.with(this).load(bean.getPlaylist().getCreator().getAvatarUrl()).into(binding.userImg);
        }
        beanList.addAll(bean.getPlaylist().getTracks());
        songInfos.clear();
        for (int i = 0; i < beanList.size(); i++) {
            SongInfo beanInfo = new SongInfo();
            beanInfo.setArtist(beanList.get(i).getAr().get(0).getName());
            beanInfo.setSongName(beanList.get(i).getName());
            beanInfo.setSongCover(beanList.get(i).getAl().getPicUrl());
            beanInfo.setSongId(String.valueOf(beanList.get(i).getId()));
            beanInfo.setDuration(beanList.get(i).getDt());
            beanInfo.setSongUrl(SONG_URL + beanList.get(i).getId() + ".mp3");
            beanInfo.setArtistId(String.valueOf(beanList.get(i).getAr().get(0).getId()));
            beanInfo.setArtistKey(beanList.get(i).getAl().getPicUrl());
            songInfos.add(beanInfo);
        }
        adapter.setList(songInfos);
        adapter.loadMore(songInfos);

    }

    @Override
    public void onGetPlaylistDetailFail(String e) {
        hideDialog();
    }

    @Override
    public void onGetMusicCanPlaySuccess(MusicCanPlayBean bean) {

    }

    @Override
    public void onGetMusicCanPlayFail(String e) {

    }

    @Override
    public void onGetHighQualitySuccess(HighQualityPlayListBean bean) {

    }

    @Override
    public void onGetHighQualityFail(String e) {

    }

    @Override
    protected WowPresenter onCreatePresenter() {
        return new WowPresenter(this);
    }

    @Override
    protected void initModule() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
