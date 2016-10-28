package com.balinasoft.clever.storage.model;

import com.balinasoft.clever.storage.GameDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.annotation.UniqueGroup;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.List;

import rx.Observable;

@ModelContainer
@Table(
        database = GameDatabase.class,
        uniqueColumnGroups = {@UniqueGroup(groupNumber = 0, uniqueConflict = ConflictAction.REPLACE)})
public class Rule extends BaseModel {

    @PrimaryKey(autoincrement = true)
    long id;

    @Unique(unique = false, uniqueGroups = 0)
    @Column
    private String serverId;

    @Column
    private String title;

    @Column
    private String imagePath;

    @Column
    private int imageResId;

    @Column
    private String description;

    public Rule(String description, String title, int imageResId, String serverId) {
        this.description = description;
        this.title = title;
        this.imageResId = imageResId;
        this.serverId = serverId;
    }

    public Rule() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public static Observable<List<Rule>> getRules() {
        return Observable.defer(() -> Observable.just(SQLite.select().from(Rule.class).queryList()));
    }

    public static Observable<List<Rule>> insertRules(List<Rule> rules) {
        return Observable.defer(() -> {
            FlowManager.getDatabase(GameDatabase.class).executeTransaction(databaseWrapper -> {
                for (Rule rule : rules) {
                    rule.save();
                }
            });
            return Observable.just(rules);
        });
    }

}
