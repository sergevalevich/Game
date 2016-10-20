package com.balinasoft.clever.storage.model;

import com.balinasoft.clever.storage.GameDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.annotation.UniqueGroup;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.List;

import rx.Observable;

@ModelContainer
@Table(
        database = GameDatabase.class,
        uniqueColumnGroups = {@UniqueGroup(groupNumber = 0, uniqueConflict = ConflictAction.IGNORE)})
public class News extends BaseModel {

    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    private String imageUrl;

    @Unique(unique = false, uniqueGroups = 0)
    @Column
    private String description;

    @Column
    private String topic;

    @Column
    private String date;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public static Observable<List<News>> getNews() {
        return Observable.defer(() -> Observable.just(SQLite.select()
                .from(News.class)
                .orderBy(News_Table.date,false)
                .queryList()));
    }

    public static Observable<News> insertNews(News news) {
        return Observable.defer(() -> {
            news.save();
            return Observable.just(news);
        });
    }
}
