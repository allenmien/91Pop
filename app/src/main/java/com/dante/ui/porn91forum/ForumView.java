package com.dante.ui.porn91forum;

import com.dante.data.model.Forum91PronItem;
import com.dante.data.model.PinnedHeaderEntity;
import com.dante.ui.BaseView;

import java.util.List;

/**
 * @author flymegoc
 * @date 2018/1/23
 */

public interface ForumView extends BaseView {
    void setForumListData(List<Forum91PronItem> forum91PronItemList);

    void setForumIndexListData(List<PinnedHeaderEntity<Forum91PronItem>> pinnedHeaderEntityList);

    void loadMoreFailed();

    void noMoreData();

    void setMoreData(List<Forum91PronItem> forum91PronItemList);

    void loadData(boolean pullToRefresh);
}
