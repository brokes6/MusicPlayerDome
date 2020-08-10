package com.example.musicplayerdome.main.view;

import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.view.View;

import com.example.musicplayerdome.R;
import com.example.musicplayerdome.abstractclass.WowContract;
import com.example.musicplayerdome.base.BaseActivity;
import com.example.musicplayerdome.base.BaseFragment;
import com.example.musicplayerdome.bean.BannerBean;
import com.example.musicplayerdome.bean.MusicCanPlayBean;
import com.example.musicplayerdome.databinding.ActivityPlayListRecommendBinding;
import com.example.musicplayerdome.main.adapter.MultiFragmentPagerAdapter;
import com.example.musicplayerdome.main.bean.CollectionListBean;
import com.example.musicplayerdome.main.bean.DailyRecommendBean;
import com.example.musicplayerdome.main.bean.HighQualityPlayListBean;
import com.example.musicplayerdome.main.bean.MainRecommendPlayListBean;
import com.example.musicplayerdome.main.bean.PlaylistDetailBean;
import com.example.musicplayerdome.main.bean.RecommendPlayListBean;
import com.example.musicplayerdome.main.bean.RecommendsongBean;
import com.example.musicplayerdome.main.bean.TopListBean;
import com.example.musicplayerdome.main.fragment.HighQualityPlayListFragment;
import com.example.musicplayerdome.main.other.WowPresenter;
import com.example.musicplayerdome.song.bean.SongDetailBean;
import com.example.musicplayerdome.song.other.SongPlayManager;
import com.gyf.immersionbar.ImmersionBar;

import java.util.ArrayList;
import java.util.List;

/**
 * HomeFragment.PlayListRecommendActivity（HomeFragment页面 5个按钮之一）
 * 展示（系统推荐的）歌单
 */
public class PlayListRecommendActivity extends BaseActivity<WowPresenter> implements WowContract.View{
    private static final String TAG = "PlayListRecommendActivi";
    ActivityPlayListRecommendBinding binding;
    private List<BaseFragment> fragments = new ArrayList<>();
    private MultiFragmentPagerAdapter pagerAdapter;

    @Override
    protected void onCreateView(Bundle savedInstanceState) {
        binding = DataBindingUtil.setContentView(this,R.layout.activity_play_list_recommend);

        ImmersionBar.with(this)
                .statusBarColor(R.color.red)
                .statusBarDarkFont(false)
                .init();

        pagerAdapter = new MultiFragmentPagerAdapter(getSupportFragmentManager());
        fragments.add(new HighQualityPlayListFragment());
        pagerAdapter.init(fragments);
    }

    @Override
    protected WowPresenter onCreatePresenter() {
        return new WowPresenter(this);
    }

    @Override
    protected void initData() {
        binding.vpContainer.setAdapter(pagerAdapter);
        binding.vpContainer.setOffscreenPageLimit(6);
        binding.vpContainer.setCurrentItem(0);
        pagerAdapter.getItem(0).setUserVisibleHint(true);
        binding.tabType.setViewPager(binding.vpContainer);
    }

    @Override
    protected void initView() {
        setBackBtn(getString(R.string.colorWhite));
        setLeftTitleText(getString(R.string.playlist_playground), getString(R.string.colorWhite));
        setMargins(binding.rlTitle,0,getStatusBarHeight(this)-5,0,0);
    }

    @Override
    public void onClick(View v) {

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

    @Override
    public void onGetPlaylistDetailSuccess(PlaylistDetailBean bean) {

    }

    @Override
    public void onGetPlaylistDetailFail(String e) {

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
    public void onGetRecommendsongSuccess(RecommendsongBean bean) {

    }

    @Override
    public void onGetRecommendsongFail(String e) {

    }

    @Override
    public void onGetCollectionListSuccess(CollectionListBean bean) {

    }

    @Override
    public void onGetCollectionListFail(String e) {

    }

    @Override
    public void onGetSongDetailSuccess(SongDetailBean bean) {

    }

    @Override
    public void onGetSongDetailFail(String e) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SongPlayManager.getInstance().isDisplay()) {
            binding.bottomController.setVisibility(View.VISIBLE);
        } else {
            binding.bottomController.setVisibility(View.GONE);
        }
    }
}