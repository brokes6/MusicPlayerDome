package com.example.musicplayerdome.song.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayerdome.R;
import com.example.musicplayerdome.abstractclass.SingerContract;
import com.example.musicplayerdome.base.BaseFragment;
import com.example.musicplayerdome.databinding.FragmentRecyclerviewBinding;
import com.example.musicplayerdome.search.bean.FeedSearchBean;
import com.example.musicplayerdome.search.bean.MvBean;
import com.example.musicplayerdome.search.bean.SimiSingerBean;
import com.example.musicplayerdome.search.bean.SingerAblumSearchBean;
import com.example.musicplayerdome.search.bean.SingerDescriptionBean;
import com.example.musicplayerdome.search.bean.SingerSongSearchBean;
import com.example.musicplayerdome.song.adapter.FeedAdapter;
import com.example.musicplayerdome.song.other.SingIdEvent;
import com.example.musicplayerdome.song.other.SingerPresenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.ArrayList;
import java.util.List;

public class SingerFeedSearchFragment extends BaseFragment<SingerPresenter> implements SingerContract.View {
    private static final String TAG = "SingerFeedSearchFragmen";
    FragmentRecyclerviewBinding binding;
    private String type;
    private String keywords;
    private List<FeedSearchBean.ResultBean.VideosBean> videoList = new ArrayList<>();
    private List<MvBean> mvList = new ArrayList<>();
    private FeedAdapter adapter;
    private int searchType = 1014;
    private String singerName;

    public SingerFeedSearchFragment() {
        setFragmentTitle("相关视频");
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onGetSingerIdEvent(SingIdEvent event) {
        singerName = event.getSingerName();
    }

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_recyclerview, container, false);
        EventBus.getDefault().register(this);
        return binding.getRoot();
    }

    @Override
    protected void initData() {
        adapter = new FeedAdapter(getContext());
        adapter.setType(2);
        binding.rv.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rv.setAdapter(adapter);

        if (singerName != null) {
            showDialog();
            mPresenter.getFeedSearch(singerName, searchType);
        }
    }

    @Override
    public SingerPresenter onCreatePresenter() {
        return new SingerPresenter(this);
    }

    @Override
    protected void initVariables(Bundle bundle) {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onGetSingerHotSongSuccess(SingerSongSearchBean bean) {

    }

    @Override
    public void onGetSingerHotSongFail(String e) {

    }

    @Override
    public void onGetSingerAlbumSuccess(SingerAblumSearchBean bean) {

    }

    @Override
    public void onGetSingerAlbumFail(String e) {

    }

    @Override
    public void onGetFeedSearchSuccess(FeedSearchBean bean) {
        hideDialog();
        Log.d(TAG, "onGetFeedSearchSuccess : " + bean);
        videoList.clear();
        videoList.addAll(bean.getResult().getVideos());
        addDataToAdapter();
    }

    private void addDataToAdapter() {
        mvList.clear();
        for (int i = 0; i < videoList.size(); i++) {
            MvBean mvBean = new MvBean();
            mvBean.setCoverUrl(videoList.get(i).getCoverUrl());
            mvBean.setCreator(videoList.get(i).getCreator());
            mvBean.setDuration(videoList.get(i).getDurationms());
            mvBean.setPlayTime(videoList.get(i).getPlayTime());
            mvBean.setTitle(videoList.get(i).getTitle());
            mvBean.setType(videoList.get(i).getType());
            mvBean.setVid(videoList.get(i).getVid());
            mvList.add(mvBean);
        }
        adapter.loadMore(mvList);
    }

    @Override
    public void onGetFeedSearchFail(String e) {

    }

    @Override
    public void onGetSingerDescSuccess(SingerDescriptionBean bean) {

    }

    @Override
    public void onGetSingerDescFail(String e) {

    }

    @Override
    public void onGetSimiSingerSuccess(SimiSingerBean bean) {

    }

    @Override
    public void onGetSimiSingerFail(String e) {

    }
}
