package com.dante.ui.favorite;

import com.dante.data.model.UnLimit91PornItem;
import com.dante.ui.BaseView;

import java.util.List;

/**
 * @author flymegoc
 * @date 2017/11/25
 * @describe
 */

public interface FavoriteView extends BaseView {
    void setFavoriteData(List<UnLimit91PornItem> unLimit91PornItemList);

    void loadMoreDataComplete();

    void loadMoreFailed();

    void noMoreData();

    void setMoreData(List<UnLimit91PornItem> unLimit91PornItemList);

    void deleteFavoriteSucc(String message);

    void deleteFavoriteError(String message);

    void showDeleteDialog();
}
