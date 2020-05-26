package com.example.musicplayerdome.song.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.SeekBar;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.musicplayerdome.R;
import com.example.musicplayerdome.abstractclass.SongContract;
import com.example.musicplayerdome.base.BaseActivity;
import com.example.musicplayerdome.databinding.ActivitySongBinding;
import com.example.musicplayerdome.login.bean.LoginBean;
import com.example.musicplayerdome.main.bean.LikeListBean;
import com.example.musicplayerdome.rewrite.MaxHeightRecyclerView;
import com.example.musicplayerdome.song.adapter.MusicListAdapter;
import com.example.musicplayerdome.song.other.SongPlayManager;
import com.example.musicplayerdome.song.other.MusicPauseEvent;
import com.example.musicplayerdome.song.other.MusicStartEvent;
import com.example.musicplayerdome.song.other.SongPresenter;
import com.example.musicplayerdome.song.bean.CommentLikeBean;
import com.example.musicplayerdome.song.bean.LikeMusicBean;
import com.example.musicplayerdome.song.bean.LyricBean;
import com.example.musicplayerdome.song.bean.MusicCommentBean;
import com.example.musicplayerdome.song.bean.PlayListCommentBean;
import com.example.musicplayerdome.song.bean.SongDetailBean;
import com.example.musicplayerdome.song.dialog.SongDetailDialog;
import com.example.musicplayerdome.song.dialog.SongListDialog;
import com.example.musicplayerdome.util.GsonUtil;
import com.example.musicplayerdome.util.SharePreferenceUtil;
import com.example.musicplayerdome.util.SharedPreferencesUtil;
import com.example.musicplayerdome.util.TimeUtil;
import com.example.musicplayerdome.util.VolumeChangeObserver;
import com.example.musicplayerdome.util.XToastUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.gyf.immersionbar.ImmersionBar;
import com.lzx.starrysky.manager.MusicManager;
import com.lzx.starrysky.model.SongInfo;
import com.lzx.starrysky.utils.TimerTaskManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.wasabeef.glide.transformations.BlurTransformation;

import static com.example.musicplayerdome.main.view.SongSheetActivityMusic.COMPLETED;


public class SongActivity extends BaseActivity<SongPresenter> implements SongContract.View, VolumeChangeObserver.VolumeChangeListener,View.OnClickListener{
    private static final String TAG = "SongActivity";

    public static final String SONG_INFO = "songInfo";
    AudioManager mAudioManager;
    private SongInfo currentSongInfo;
    private long ids;
    private SongDetailBean songDetail;
    private TimerTaskManager mTimerTask;
    private boolean isLike = false;
    private int playMode;
    private ObjectAnimator rotateAnimator;
    private ObjectAnimator alphaAnimator;
    private boolean isShowLyrics = false;
    private LyricBean lyricBean;
    ActivitySongBinding binding;
    private VolumeChangeObserver mVolumeChangeObserver;
    private SongListDialog songListDialog;
    private List<SongInfo> songList = new ArrayList<>();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMusicStartEvent(MusicStartEvent event) {
        Log.d(TAG, "onMusicStartEvent");
        SongInfo songInfo = event.getSongInfo();
        if (!songInfo.getSongId().equals(currentSongInfo.getSongId())) {
            //说明该界面下，切歌了，则要重新设置一遍
            currentSongInfo = songInfo;
            if (lyricBean != null) {
                lyricBean = null;
            }
            checkMusicState();
        } else {
            //如果没有没有切歌，则check一下就行了
            checkMusicPlaying();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMusicPauseEvent(MusicPauseEvent event) {
        Log.d(TAG, "onMusicPauseEvent");
        checkMusicPlaying();
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState) {
        binding = DataBindingUtil.setContentView(this,R.layout.activity_song);

        ImmersionBar.with(this)
                .transparentStatusBar()
                .statusBarDarkFont(false)
                .init();
        goDialog();
    }

    @Override
    protected SongPresenter onCreatePresenter() {
        return new SongPresenter(this);
    }

    @Override
    protected void initModule() {
        EventBus.getDefault().register(this);
    }

    @Override
    protected void initData() {
        showDialog();
        initView();
        getIntentData();
        initAudioManager();
        setBackBtn(getString(R.string.colorWhite));
        playMode = SongPlayManager.getInstance().getMode();
        mTimerTask = new TimerTaskManager();
        initTimerTaskWork();

        checkMusicState();
    }
    private void initView(){
        binding.rlCenter.setOnClickListener(this);
        binding.ivPlay.setOnClickListener(this);
        binding.ivLike.setOnClickListener(this);
        binding.ivDownload.setOnClickListener(this);
        binding.ivComment.setOnClickListener(this);
        binding.ivInfo.setOnClickListener(this);
        binding.ivPlayMode.setOnClickListener(this);
        binding.ivPre.setOnClickListener(this);
        binding.ivNext.setOnClickListener(this);
        binding.ivList.setOnClickListener(this);
        binding.actAudioVolumeControl.setOnSeekBarChangeListener(new SeekBarChangeVolumeControl());
        setMargins(binding.rlTitle,0,getStatusBarHeight(this),0,0);
    }
    /**
     * 获取状态栏高度
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

    /**
     * 用来防止状态栏和控件重叠在一起（设置控件的Margins值）
     * @param v 传递进来最上方的控件
     * @param l 左
     * @param t 上
     * @param r 右
     * @param b 下
     */
    public static void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    /**
     * 初始化音乐进度条
     */
    private void initTimerTaskWork() {
        mTimerTask.setUpdateProgressTask(() -> {
            long position = MusicManager.getInstance().getPlayingPosition();
            //SeekBar 设置 Max
            binding.seekBar.setProgress((int) position);
            binding.tvPastTime.setText(TimeUtil.getTimeNoYMDH(position));
            binding.lrc.updateTime(position);
        });

        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SongPlayManager.getInstance().seekTo(seekBar.getProgress());
                SongActivity.this.binding.seekBar.setProgress(seekBar.getProgress());
                binding.lrc.updateTime(seekBar.getProgress());
            }
        });
    }

    /**
     * 接收从音乐列表界面传递进来的音乐数据（接收对象）
     */
    private void getIntentData() {
        Intent intent = getIntent();
        currentSongInfo = intent.getParcelableExtra(SONG_INFO);
    }

    /**
     * 初始化歌词，是否喜欢，音乐进度条最大值，当前播放模式
     */
    private void checkMusicState() {
        setSongInfo(currentSongInfo.getSongName(), currentSongInfo.getArtist());
        if (judgeContainsStr(currentSongInfo.getSongId())) {
            binding.PropsColumnS.setVisibility(View.GONE);
        } else {
            if (lyricBean == null) {
                mPresenter.getLyric(Long.parseLong(currentSongInfo.getSongId()));
            }
            binding.PropsColumnS.setVisibility(View.VISIBLE);
            ids = Long.parseLong(currentSongInfo.getSongId());
            String songId = currentSongInfo.getSongId();
            List<String> likeList = SharePreferenceUtil.getInstance(this).getLikeList();
            if (likeList.contains(songId)) {
                isLike = true;
                binding.ivLike.setImageResource(R.drawable.shape_like_white);
            } else {
                isLike = false;
            }
            if (SongPlayManager.getInstance().getSongDetail(ids) == null) {
                mPresenter.getSongDetail(ids);
            } else {
                songDetail = SongPlayManager.getInstance().getSongDetail(ids);
                setSongDetailBean(songDetail);
            }
        }

        long duration = currentSongInfo.getDuration();
        if (binding.seekBar.getMax() != duration) {
            binding.seekBar.setMax((int) duration);
        }

        switch (playMode) {
            case SongPlayManager.MODE_LIST_LOOP_PLAY:
                binding.ivPlayMode.setImageResource(R.drawable.shape_list_cycle);
                break;
            case SongPlayManager.MODE_RANDOM:
                binding.ivPlayMode.setImageResource(R.drawable.shape_list_random);
                break;
            case SongPlayManager.MODE_SINGLE_LOOP_PLAY:
                binding.ivPlayMode.setImageResource(R.drawable.shape_single_cycle);
                break;
        }
        binding.totalTime.setText(TimeUtil.getTimeNoYMDH(duration));
        checkMusicPlaying();
    }

    private void checkMusicPlaying() {
        mTimerTask.startToUpdateProgress();
        if (SongPlayManager.getInstance().isPlaying()) {
            hideDialog();
            Log.e(TAG, "--music正在播放--");
            if (getRotateAnimator().isPaused()) {
                getRotateAnimator().resume();
            } else if (getRotateAnimator().isRunning()) {
            } else {
                getRotateAnimator().start();
            }
            binding.ivPlay.setImageResource(R.drawable.shape_pause);
        } else {
            Log.e(TAG, "--music没有播放--");
            getRotateAnimator().pause();
            binding.ivPlay.setImageResource(R.drawable.shape_play_white);
        }
    }

    /**
     * 该方法主要使用正则表达式来判断字符串中是否包含字母
     */
    public boolean judgeContainsStr(String cardNum) {
        String regex = ".*[a-zA-Z]+.*";
        Matcher m = Pattern.compile(regex).matcher(cardNum);
        return m.matches();
    }

    /**
     * 控制封面旋转
     * @return
     */
    private ObjectAnimator getRotateAnimator() {
        if (rotateAnimator == null) {
            rotateAnimator = ObjectAnimator.ofFloat(binding.ivRecord, "rotation", 360f);
            rotateAnimator.setDuration(50 * 1000);
            rotateAnimator.setInterpolator(new LinearInterpolator());
            rotateAnimator.setRepeatCount(100000);
            rotateAnimator.setRepeatMode(ValueAnimator.RESTART);
        }
        return rotateAnimator;
    }

    private ObjectAnimator getAlphaAnimator() {
        if (alphaAnimator == null) {
            alphaAnimator = ObjectAnimator.ofFloat(binding.ivBg, "alpha", 0f, 0.13f);
            alphaAnimator.setDuration(1500);
            alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        }
        return alphaAnimator;
    }

    private void setSongDetailBean(SongDetailBean songDetail) {
        String coverUrl = songDetail.getSongs().get(0).getAl().getPicUrl();
        Glide.with(this)
                .load(coverUrl)
                .placeholder(R.drawable.shape_record)
                .into(binding.ivRecord);
        Glide.with(this)
                .load(coverUrl)
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 12)))
                .transition(new DrawableTransitionOptions().crossFade(1500))
                .into(binding.ivBg);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.rl_center:
                isShowLyrics = true;
                showLyrics(true);
                break;
            case R.id.iv_play:
                if (SongPlayManager.getInstance().isPlaying()) {
                    SongPlayManager.getInstance().pauseMusic();
                } else if (SongPlayManager.getInstance().isPaused()) {
                    SongPlayManager.getInstance().playMusic();
                } else if (SongPlayManager.getInstance().isIdle()) {
                    SongPlayManager.getInstance().clickASong(currentSongInfo);
                }
                break;
            case R.id.iv_like:
                if (isLike) {
                    mPresenter.NolikeMusic(ids);
                } else {
                    mPresenter.likeMusic(ids);
                }
                break;
            case R.id.iv_download:
                XToastUtils.info("Sorry啊，歌都不是我的，不能下载的");
                break;
            case R.id.iv_comment:
                if (songDetail == null) {
                    XToastUtils.info("获取不到歌曲信息，稍后再试");
                    return;
                }
                intent.setClass(SongActivity.this, CommentActivity.class);
                intent.putExtra(CommentActivity.ID, songDetail.getSongs().get(0).getId());
                intent.putExtra(CommentActivity.NAME, songDetail.getSongs().get(0).getName());
                intent.putExtra(CommentActivity.ARTIST, songDetail.getSongs().get(0).getAr().get(0).getName());
                intent.putExtra(CommentActivity.COVER, songDetail.getSongs().get(0).getAl().getPicUrl());
                intent.putExtra(CommentActivity.FROM, CommentActivity.SONG_COMMENT);
                startActivity(intent);
                break;
            case R.id.iv_info:
                SongDetailDialog songDetailDialog = new SongDetailDialog(mContext,currentSongInfo);
                songDetailDialog.setCanceledOnTouchOutside(true);
                songDetailDialog.show();
                break;
            case R.id.iv_play_mode:
                if (playMode == SongPlayManager.MODE_LIST_LOOP_PLAY) {
                    SongPlayManager.getInstance().setMode(SongPlayManager.MODE_SINGLE_LOOP_PLAY);
                    binding.ivPlayMode.setImageResource(R.drawable.shape_single_cycle);
                    playMode = SongPlayManager.MODE_SINGLE_LOOP_PLAY;
                    XToastUtils.info("切换到单曲循环模式");
                } else if (playMode == SongPlayManager.MODE_SINGLE_LOOP_PLAY) {
                    SongPlayManager.getInstance().setMode(SongPlayManager.MODE_RANDOM);
                    binding.ivPlayMode.setImageResource(R.drawable.shape_list_random);
                    playMode = SongPlayManager.MODE_RANDOM;
                    XToastUtils.info("切换到随机播放模式");
                } else if (playMode == SongPlayManager.MODE_RANDOM) {
                    SongPlayManager.getInstance().setMode(SongPlayManager.MODE_LIST_LOOP_PLAY);
                    binding.ivPlayMode.setImageResource(R.drawable.shape_list_cycle);
                    playMode = SongPlayManager.MODE_LIST_LOOP_PLAY;
                    XToastUtils.info("切换到列表循环模式");
                }
                break;
            case R.id.iv_pre:
                SongPlayManager.getInstance().playPreMusic();
                break;
            case R.id.iv_next:
                SongPlayManager.getInstance().playNextMusic();
                break;
            case R.id.iv_list:
                songListDialog = new SongListDialog(this);
                songListDialog.setCanceledOnTouchOutside(true);
                songListDialog.show();
                break;
        }
    }
    /**
     * 初始化音量控制器
     */
    private void initAudioManager(){
        //实例化对象并设置监听器
        mVolumeChangeObserver = new VolumeChangeObserver(this);
        mVolumeChangeObserver.setVolumeChangeListener(this);
        int initVolume = mVolumeChangeObserver.getCurrentMusicVolume();
        Log.e(TAG, "initVolume = " + initVolume);
        //音量控制,初始化定义
        if (mAudioManager==null){
            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }
        //设置音量控制器的进度为当前音量
        binding.actAudioVolumeControl.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        binding.actAudioVolumeControl.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
    }

    @Override
    public void onVolumeChanged(int volume) {
        Log.e(TAG, "onVolumeChanged: 当前音量为"+volume);
        binding.actAudioVolumeControl.setProgress(volume);
    }

    @Override
    protected void onResume() {
        mVolumeChangeObserver.registerReceiver();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mVolumeChangeObserver.unregisterReceiver();
        super.onPause();
    }

    /**
     *音量控制器
     * 滑动监听
     */
    class SeekBarChangeVolumeControl implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser){
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    //根据isShowLyrics来判断是否展示歌词
    private void showLyrics(boolean isShowLyrics) {
        if (isShowLyrics == true){
            binding.ivRecord.setVisibility(View.GONE);
            binding.PropsColumnS.setVisibility(View.GONE);
            binding.lrc.setVisibility(View.VISIBLE);
            binding.volume.setVisibility(View.VISIBLE);
        }else{
            binding.ivRecord.setVisibility(View.VISIBLE);
            binding.PropsColumnS.setVisibility(View.VISIBLE);
            binding.lrc.setVisibility(View.GONE);
            binding.volume.setVisibility(View.GONE);
        }
//        binding.ivRecord.setVisibility(isShowLyrics ? View.GONE : View.VISIBLE);
//        binding.lrc.setVisibility(isShowLyrics ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        mTimerTask.removeUpdateProgressTask();
        if (rotateAnimator != null) {
            if (rotateAnimator.isRunning()) {
                rotateAnimator.cancel();
            }
            rotateAnimator = null;
        }
        if (alphaAnimator != null) {
            if (alphaAnimator.isRunning()) {
                alphaAnimator.cancel();
            }
            alphaAnimator = null;
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == COMPLETED) {
                binding.ivBg.setBackground((Drawable) msg.obj);
                getAlphaAnimator().start();
            }
        }
    };


    @Override
    public void onGetSongDetailSuccess(SongDetailBean bean) {
        songDetail = bean;
        setSongDetailBean(songDetail);
        SongPlayManager.getInstance().putSongDetail(songDetail);
    }

    @Override
    public void onGetSongDetailFail(String e) {
        XToastUtils.error(e);
    }

    @Override
    public void onLikeMusicSuccess(LikeMusicBean bean) {
        if (bean.getCode() == 200) {
            XToastUtils.info("喜欢成功");
            binding.ivLike.setImageResource(R.drawable.shape_like_white);
            isLike = true;
            LoginBean loginBean = GsonUtil.fromJSON(SharePreferenceUtil.getInstance(this).getUserInfo(""), LoginBean.class);
            mPresenter.getLikeList(loginBean.getAccount().getId());
        } else {
            XToastUtils.info("喜欢失败TAT ErrorCode = " + bean.getCode());
        }
    }

    @Override
    public void onNoLikeMusicSuccess(LikeMusicBean bean) {
        if (bean.getCode() == 200) {
            XToastUtils.info("取消喜欢成功");
            binding.ivLike.setImageResource(R.drawable.shape_not_like);
            isLike = true;
            LoginBean loginBean = GsonUtil.fromJSON(SharePreferenceUtil.getInstance(this).getUserInfo(""), LoginBean.class);
            mPresenter.getLikeList(loginBean.getAccount().getId());
        } else {
            XToastUtils.info("取消喜欢失败TAT ErrorCode = " + bean.getCode());
        }
    }

    @Override
    public void onLikeMusicFail(String e) {
        Log.e(TAG, "onLikeMusicFail : " + e);
        XToastUtils.error(e);
    }

    @Override
    public void onGetLikeListSuccess(LikeListBean bean) {
        List<Long> idsList = bean.getIds();
        List<String> likeList = new ArrayList<>();
        for (int i = 0; i < idsList.size(); i++) {
            String ids = String.valueOf(idsList.get(i));
            likeList.add(ids);
        }
        SharePreferenceUtil.getInstance(this).saveLikeList(likeList);
    }

    @Override
    public void onGetLikeListFail(String e) {
        Log.d(TAG, "onGetLikeListFail");
        XToastUtils.error(e);
    }

    @Override
    public void onGetMusicCommentSuccess(MusicCommentBean bean) {

    }

    @Override
    public void onGetMusicCommentFail(String e) {

    }

    @Override
    public void onLikeCommentSuccess(CommentLikeBean bean) {

    }

    @Override
    public void onLikeCommentFail(String e) {

    }

    @Override
    public void onGetLyricSuccess(LyricBean bean) {
        Log.e(TAG, "onGetLyricSuccess : " + bean);
        SharedPreferencesUtil.putData("Ykey",2);
        if (bean.getLrc() != null) {
            if (bean.getTlyric().getLyric() != null) {
                binding.lrc.loadLrc(bean.getLrc().getLyric(), bean.getTlyric().getLyric());
            } else {
                binding.lrc.loadLrc(bean.getLrc().getLyric(), "");
            }
        } else {
            binding.lrc.loadLrc("", "");
        }
        initLrcListener();
    }

    private void initLrcListener() {
        binding.lrc.setListener(time -> {
            SongPlayManager.getInstance().seekTo(time);
            if (SongPlayManager.getInstance().isPaused()) {
                SongPlayManager.getInstance().playMusic();
            } else if (SongPlayManager.getInstance().isIdle()) {
                SongPlayManager.getInstance().clickASong(currentSongInfo);
            }else{
            }
            return true;
        });

        binding.lrc.setCoverChangeListener(() -> {
            showLyrics(false);
        });
    }

    @Override
    public void onGetLyricFail(String e) {
        Log.e(TAG, "onGetLyricFail: " + e);
    }

    @Override
    public void onGetPlaylistCommentSuccess(PlayListCommentBean bean) {

    }

    @Override
    public void onGetPlaylistCommentFail(String e) {

    }
}
