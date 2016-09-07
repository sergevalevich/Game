package com.valevich.game.storage.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.annotation.UniqueGroup;
import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;
import com.valevich.game.network.model.QuestionApiModel;
import com.valevich.game.storage.GameDatabase;
import com.valevich.game.util.UrlFormatter;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.res.StringRes;

import java.util.ArrayList;
import java.util.List;

import retrofit2.http.Url;
import rx.Observable;
import timber.log.Timber;

@EBean
@ModelContainer
@Table(
        database = GameDatabase.class,
        uniqueColumnGroups = {@UniqueGroup(groupNumber = 0, uniqueConflict = ConflictAction.IGNORE)})
public class Question extends BaseModel {

    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    private String themeQuest;

    @Unique(unique = false, uniqueGroups = 0)
    @Column
    private String textQuest;

    @Column
    private String mediaType;

    @Column
    private String mediaPath;

    @Column
    private String answers;

    @Column
    private String rightAnswer;

    @Column
    private int isPlayed;

    public int getIsPlayed() {
        return isPlayed;
    }

    public void setIsPlayed(int isPlayed) {
        this.isPlayed = isPlayed;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getThemeQuest() {
        return themeQuest;
    }

    public void setThemeQuest(String themeQuest) {
        this.themeQuest = themeQuest;
    }

    public String getTextQuest() {
        return textQuest;
    }

    public void setTextQuest(String textQuest) {
        this.textQuest = textQuest;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMediaPath() {
        return mediaPath;
    }

    public void setMediaPath(String mediaPath) {
        this.mediaPath = mediaPath;
    }

    public String getAnswers() {
        return answers;
    }

    public String[] getFormattedAnswers() {
        return answers.split(",");
    }

    public void setAnswers(String answers) {
        this.answers = answers;
    }

    public String getRightAnswer() {
        return rightAnswer;
    }

    public void setRightAnswer(String rightAnswer) {
        this.rightAnswer = rightAnswer;
    }

    public static Observable<Question> getQuestion() { // FIXME: 05.09.2016 return unplayed question
        return Observable.defer(() -> Observable.just(
                SQLite.select()
                        .from(Question.class)
                        .where(Question_Table.isPlayed.notEq(1))
                        .and(Question_Table.mediaPath.isNull()) // FIXME: 07.09.2016
                        .querySingle()));
    }

    public static Observable<QuestionApiModel> insertQuestion(QuestionApiModel apiQuestion) {
        return Observable.defer(() -> {
            Question question = new Question();
            question.setAnswers(apiQuestion.getAnswers());
            question.setMediaType(apiQuestion.getMediaType());
            question.setRightAnswer(apiQuestion.getRightAnswer());
            question.setTextQuest(apiQuestion.getTextQuest());
            question.setThemeQuest(apiQuestion.getThemeQuest());
            String url = apiQuestion.getMediaUrl();
            if (url != null) question.setMediaPath(UrlFormatter.getFileNameFrom(url));
            question.save();
            return Observable.just(apiQuestion);
        });
    }

}