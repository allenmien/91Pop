package com.dante.ui.porn91video.play;

import android.arch.lifecycle.Lifecycle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.bugsnag.android.Bugsnag;
import com.bugsnag.android.Severity;
import com.dante.cookie.CookieManager;
import com.dante.custom.TastyToast;
import com.dante.data.DataManager;
import com.dante.data.model.UnLimit91PornItem;
import com.dante.data.model.VideoComment;
import com.dante.data.model.VideoResult;
import com.dante.di.PerActivity;
import com.dante.exception.VideoException;
import com.dante.rxjava.CallBackWrapper;
import com.dante.rxjava.RetryWhenProcess;
import com.dante.rxjava.RxSchedulersHelper;
import com.dante.ui.download.DownloadPresenter;
import com.dante.ui.favorite.FavoritePresenter;
import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;
import com.orhanobut.logger.Logger;
import com.trello.rxlifecycle2.LifecycleProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

/**
 * @author flymegoc
 * @date 2017/11/15
 * @describe
 */
@PerActivity
public class PlayVideoPresenter extends MvpBasePresenter<PlayVideoView> implements IPlay {

    private static final String TAG = PlayVideoPresenter.class.getSimpleName();
    private final static int COMMENT_PER_PAGE_NUM = 20;
    private FavoritePresenter favoritePresenter;
    private DownloadPresenter downloadPresenter;
    private LifecycleProvider<Lifecycle.Event> provider;
    private int start = 1;
    private DataManager dataManager;

    private CookieManager cookieManager;

    @Inject
    public PlayVideoPresenter(FavoritePresenter favoritePresenter, DownloadPresenter downloadPresenter, LifecycleProvider<Lifecycle.Event> provider, DataManager dataManager, CookieManager cookieManager) {
        this.favoritePresenter = favoritePresenter;
        this.downloadPresenter = downloadPresenter;
        this.provider = provider;
        this.dataManager = dataManager;
        this.cookieManager = cookieManager;
    }

    @Override
    public void loadVideoUrl(final UnLimit91PornItem unLimit91PornItem) {
        String viewKey = unLimit91PornItem.getViewKey();
        dataManager.loadPorn91VideoUrl(viewKey)
                .map(new Function<VideoResult, VideoResult>() {
                    @Override
                    public VideoResult apply(VideoResult videoResult) throws VideoException {
                        if (TextUtils.isEmpty(videoResult.getVideoUrl())) {
                            if (VideoResult.OUT_OF_WATCH_TIMES.equals(videoResult.getId())) {
                                //尝试强行重置，并上报异常
                                cookieManager.resetPorn91VideoWatchTiem(true);
                                Bugsnag.notify(new Throwable(TAG + ":ten videos each day "), Severity.WARNING);
                                throw new VideoException("观看次数达到上限了！");
                            } else {
                                throw new VideoException("解析视频链接失败了");
                            }
                        }
                        return videoResult;
                    }
                })
                .retryWhen(new RetryWhenProcess(RetryWhenProcess.PROCESS_TIME))
                .compose(RxSchedulersHelper.<VideoResult>ioMainThread())
                .compose(provider.<VideoResult>bindUntilEvent(Lifecycle.Event.ON_DESTROY))
                .subscribe(new CallBackWrapper<VideoResult>() {
                    @Override
                    public void onBegin(Disposable d) {
                        ifViewAttached(new ViewAction<PlayVideoView>() {
                            @Override
                            public void run(@NonNull PlayVideoView view) {
                                view.showParsingDialog();
                            }
                        });
                    }

                    @Override
                    public void onSuccess(final VideoResult videoResult) {
                        cookieManager.resetPorn91VideoWatchTiem(false);
                        ifViewAttached(new ViewAction<PlayVideoView>() {
                            @Override
                            public void run(@NonNull PlayVideoView view) {
                                view.playVideo(saveVideoUrl(videoResult, unLimit91PornItem));
                            }
                        });
                    }

                    @Override
                    public void onError(final String msg, int code) {
                        ifViewAttached(new ViewAction<PlayVideoView>() {
                            @Override
                            public void run(@NonNull PlayVideoView view) {
                                view.errorParseVideoUrl(msg);
                            }
                        });
                    }
                });
    }

    @Override
    public void loadVideoComment(String videoId, String viewKey, final boolean pullToRefresh) {
        if (pullToRefresh) {
            start = 1;
        }
        dataManager.loadPorn91VideoComments(videoId, start, viewKey)
                .retryWhen(new RetryWhenProcess(RetryWhenProcess.PROCESS_TIME))
                .compose(RxSchedulersHelper.<List<VideoComment>>ioMainThread())
                .compose(provider.<List<VideoComment>>bindUntilEvent(Lifecycle.Event.ON_STOP))
                .map(new Function<List<VideoComment>, List<VideoComment>>() {
                    @Override
                    public List<VideoComment> apply(List<VideoComment> videoComments) throws Exception {
                        List<VideoComment> newList=new ArrayList<>();
                        for (VideoComment c : videoComments) {
                            List<String> list = c.getCommentQuoteList();
                            StringBuilder builder = new StringBuilder();
                            for (int i = 0; i < list.size(); i++) {
                                builder.append(list.get(i));
                            }
                            if (invalidComment(builder.toString())) {
                                Logger.d("invalidComment " + builder.toString());
                            }else {
                                newList.add(c);
                            }
                        }
                        return newList;
                    }
                })
                .subscribe(new CallBackWrapper<List<VideoComment>>() {
                    @Override
                    public void onBegin(Disposable d) {
                        ifViewAttached(new ViewAction<PlayVideoView>() {
                            @Override
                            public void run(@NonNull PlayVideoView view) {
                                if (start == 1 && pullToRefresh) {
                                    view.showLoading(pullToRefresh);
                                }
                            }
                        });
                    }

                    @Override
                    public void onSuccess(final List<VideoComment> videoCommentList) {
                        ifViewAttached(new ViewAction<PlayVideoView>() {
                            @Override
                            public void run(@NonNull PlayVideoView view) {
                                if (start == 1) {
                                    view.setVideoCommentData(videoCommentList, pullToRefresh);
                                } else {
                                    view.setMoreVideoCommentData(videoCommentList);
                                }
                                if (videoCommentList.size() == 0 && start == 1) {
                                    view.noMoreVideoCommentData("暂无评论");
                                } else if (videoCommentList.size() == 0 && start > 1) {
                                    view.noMoreVideoCommentData("没有更多评论了");
                                }
                                start++;
                                view.showContent();
                            }
                        });
                    }

                    @Override
                    public void onError(final String msg, int code) {
                        ifViewAttached(new ViewAction<PlayVideoView>() {
                            @Override
                            public void run(@NonNull PlayVideoView view) {
                                if (start == 1) {
                                    view.loadVideoCommentError(msg);
                                } else {
                                    view.loadMoreVideoCommentError(msg);
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancel(boolean isCancel) {
                        ifViewAttached(new ViewAction<PlayVideoView>() {
                            @Override
                            public void run(@NonNull PlayVideoView view) {
                                Logger.t(TAG).d("------getVideoComments  onCancel----------------------------");
                                if (start == 1) {
                                    view.loadVideoCommentError("取消请求");
                                } else {
                                    view.loadMoreVideoCommentError("取消请求");
                                }
                            }
                        });
                    }
                });
    }

    private boolean invalidComment(String s) {
        return s.contains("谢谢") | s.contains("加速") | s.length() <= 2;
    }

    @Override
    public void commentVideo(String comment, String uid, String vid, String viewKey) {
        String cpaintFunction = "process_comments";
        String responseType = "json";
        String comments = "\"" + comment + "\"";
        Logger.t(TAG).d(comments);
        dataManager.commentPorn91Video(cpaintFunction, comments, uid, vid, viewKey, responseType)
                .retryWhen(new RetryWhenProcess(RetryWhenProcess.PROCESS_TIME))
                .compose(RxSchedulersHelper.<String>ioMainThread())
                .compose(provider.<String>bindUntilEvent(Lifecycle.Event.ON_DESTROY))
                .subscribe(new CallBackWrapper<String>() {
                    @Override
                    public void onBegin(Disposable d) {

                    }

                    @Override
                    public void onSuccess(final String result) {
                        ifViewAttached(new ViewAction<PlayVideoView>() {
                            @Override
                            public void run(@NonNull PlayVideoView view) {
                                view.commentVideoSuccess(result);
                            }
                        });
                    }

                    @Override
                    public void onError(final String msg, int code) {
                        ifViewAttached(new ViewAction<PlayVideoView>() {
                            @Override
                            public void run(@NonNull PlayVideoView view) {
                                view.showError(msg);
                            }
                        });
                    }
                });
    }

    @Override
    public void replyComment(String comment, String username, String vid, String commentId, String viewKey) {
        dataManager.replyPorn91VideoComment(comment, username, vid, commentId, viewKey)
                .retryWhen(new RetryWhenProcess(RetryWhenProcess.PROCESS_TIME))
                .compose(RxSchedulersHelper.<String>ioMainThread())
                .compose(provider.<String>bindUntilEvent(Lifecycle.Event.ON_DESTROY))
                .subscribe(new CallBackWrapper<String>() {
                    @Override
                    public void onBegin(Disposable d) {

                    }

                    @Override
                    public void onSuccess(final String s) {
                        ifViewAttached(new ViewAction<PlayVideoView>() {
                            @Override
                            public void run(@NonNull PlayVideoView view) {
                                if ("OK".equals(s)) {
                                    view.replyVideoCommentSuccess("留言已经提交，审核后通过");
                                } else {
                                    view.replyVideoCommentError("回复评论失败");
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(final String msg, int code) {
                        ifViewAttached(new ViewAction<PlayVideoView>() {
                            @Override
                            public void run(@NonNull PlayVideoView view) {
                                view.showError(msg);
                            }
                        });
                    }
                })
        ;
    }

    private UnLimit91PornItem saveVideoUrl(VideoResult videoResult, UnLimit91PornItem unLimit91PornItem) {
        dataManager.saveVideoResult(videoResult);
        unLimit91PornItem.setVideoResult(videoResult);
        unLimit91PornItem.setViewHistoryDate(new Date());
        dataManager.saveUnLimit91PornItem(unLimit91PornItem);
        return unLimit91PornItem;
    }

    @Override
    public void downloadVideo(UnLimit91PornItem unLimit91PornItem, boolean isDownloadNeedWifi, boolean isForceReDownload) {
        downloadPresenter.downloadVideo(unLimit91PornItem, isDownloadNeedWifi, isForceReDownload, new DownloadPresenter.DownloadListener() {
            @Override
            public void onSuccess(final String message) {
                ifViewAttached(new ViewAction<PlayVideoView>() {
                    @Override
                    public void run(@NonNull PlayVideoView view) {
                        view.showMessage(message, TastyToast.SUCCESS);
                    }
                });
            }

            @Override
            public void onError(final String message) {
                ifViewAttached(new ViewAction<PlayVideoView>() {
                    @Override
                    public void run(@NonNull PlayVideoView view) {
                        view.showMessage(message, TastyToast.ERROR);
                    }
                });
            }
        });
    }

    @Override
    public void favorite(String uId, String videoId, String ownnerId) {
        favoritePresenter.favorite(uId, videoId, ownnerId, new FavoritePresenter.FavoriteListener() {
            @Override
            public void onSuccess(String message) {
                ifViewAttached(new ViewAction<PlayVideoView>() {
                    @Override
                    public void run(@NonNull PlayVideoView view) {
                        view.favoriteSuccess();
                    }
                });
            }

            @Override
            public void onError(final String message) {
                ifViewAttached(new ViewAction<PlayVideoView>() {
                    @Override
                    public void run(@NonNull PlayVideoView view) {
                        view.showError(message);
                    }
                });
            }
        });
    }
}
