package com.dante.data.db.dao;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.dante.data.model.AutoCompleteModel;
import com.dante.data.model.Category;
import com.dante.data.model.UnLimit91PornItem;
import com.dante.data.model.VideoResult;

import com.dante.data.db.dao.AutoCompleteModelDao;
import com.dante.data.db.dao.CategoryDao;
import com.dante.data.db.dao.UnLimit91PornItemDao;
import com.dante.data.db.dao.VideoResultDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig autoCompleteModelDaoConfig;
    private final DaoConfig categoryDaoConfig;
    private final DaoConfig unLimit91PornItemDaoConfig;
    private final DaoConfig videoResultDaoConfig;

    private final AutoCompleteModelDao autoCompleteModelDao;
    private final CategoryDao categoryDao;
    private final UnLimit91PornItemDao unLimit91PornItemDao;
    private final VideoResultDao videoResultDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        autoCompleteModelDaoConfig = daoConfigMap.get(AutoCompleteModelDao.class).clone();
        autoCompleteModelDaoConfig.initIdentityScope(type);

        categoryDaoConfig = daoConfigMap.get(CategoryDao.class).clone();
        categoryDaoConfig.initIdentityScope(type);

        unLimit91PornItemDaoConfig = daoConfigMap.get(UnLimit91PornItemDao.class).clone();
        unLimit91PornItemDaoConfig.initIdentityScope(type);

        videoResultDaoConfig = daoConfigMap.get(VideoResultDao.class).clone();
        videoResultDaoConfig.initIdentityScope(type);

        autoCompleteModelDao = new AutoCompleteModelDao(autoCompleteModelDaoConfig, this);
        categoryDao = new CategoryDao(categoryDaoConfig, this);
        unLimit91PornItemDao = new UnLimit91PornItemDao(unLimit91PornItemDaoConfig, this);
        videoResultDao = new VideoResultDao(videoResultDaoConfig, this);

        registerDao(AutoCompleteModel.class, autoCompleteModelDao);
        registerDao(Category.class, categoryDao);
        registerDao(UnLimit91PornItem.class, unLimit91PornItemDao);
        registerDao(VideoResult.class, videoResultDao);
    }
    
    public void clear() {
        autoCompleteModelDaoConfig.clearIdentityScope();
        categoryDaoConfig.clearIdentityScope();
        unLimit91PornItemDaoConfig.clearIdentityScope();
        videoResultDaoConfig.clearIdentityScope();
    }

    public AutoCompleteModelDao getAutoCompleteModelDao() {
        return autoCompleteModelDao;
    }

    public CategoryDao getCategoryDao() {
        return categoryDao;
    }

    public UnLimit91PornItemDao getUnLimit91PornItemDao() {
        return unLimit91PornItemDao;
    }

    public VideoResultDao getVideoResultDao() {
        return videoResultDao;
    }

}
