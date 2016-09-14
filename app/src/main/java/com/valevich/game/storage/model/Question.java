package com.valevich.game.storage.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.annotation.UniqueGroup;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.valevich.game.network.model.QuestionApiModel;
import com.valevich.game.storage.GameDatabase;
import com.valevich.game.util.UrlFormatter;

import java.util.List;

import rx.Observable;

@ModelContainer
@Table(
        database = GameDatabase.class,
        uniqueColumnGroups = {@UniqueGroup(groupNumber = 0, uniqueConflict = ConflictAction.IGNORE)})
public class Question extends BaseModel implements Parcelable {

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

    public static Observable<Question> replaceWithNonMedia(List<String> questions) {
        return Observable.defer(() -> Observable.just(
                SQLite.select()
                        .from(Question.class)
                        .where(Question_Table.isPlayed.notEq(1))
                        .and(Question_Table.mediaPath.isNull())
                        .and(Question_Table.textQuest.notIn(questions))
                        .querySingle()));
    }

    public static Observable<List<Question>> getQuestions(int limit, boolean isMediaAllowed) {
        return Observable.defer(() -> {
            Where<Question> query = SQLite.select()
                    .from(Question.class)
                    .where(Question_Table.isPlayed.notEq(1));
            if (!isMediaAllowed) query = query.and(Question_Table.mediaPath.isNull());
            return Observable.just(query.limit(limit).queryList());
        });
    }


    public static Observable<List<QuestionApiModel>> insertQuestions(List<QuestionApiModel> apiModels) {
        return Observable.defer(() -> {

            FlowManager.getDatabase(GameDatabase.class).executeTransaction(databaseWrapper -> {
                for(QuestionApiModel apiQuestion:apiModels) {
                    Question question = new Question();
                    question.setAnswers(apiQuestion.getAnswers());
                    question.setMediaType(apiQuestion.getMediaType());
                    question.setRightAnswer(apiQuestion.getRightAnswer());
                    question.setTextQuest(apiQuestion.getTextQuest());
                    question.setThemeQuest(apiQuestion.getThemeQuest());
                    String url = apiQuestion.getMediaUrl();
                    if (url != null) question.setMediaPath(UrlFormatter.getFileNameFrom(url));
                    question.save(databaseWrapper);
                }
            });

            return Observable.just(apiModels);

        });
    }

    public Question() {}

    protected Question(Parcel in) {
        id = in.readLong();
        themeQuest = in.readString();
        textQuest = in.readString();
        mediaType = in.readString();
        mediaPath = in.readString();
        answers = in.readString();
        rightAnswer = in.readString();
        isPlayed = in.readInt();
    }

    public static final Creator<Question> CREATOR = new Creator<Question>() {
        @Override
        public Question createFromParcel(Parcel in) {
            return new Question(in);
        }

        @Override
        public Question[] newArray(int size) {
            return new Question[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(themeQuest);
        parcel.writeString(textQuest);
        parcel.writeString(mediaType);
        parcel.writeString(mediaPath);
        parcel.writeString(answers);
        parcel.writeString(rightAnswer);
        parcel.writeInt(isPlayed);
    }

    @Override
    public boolean equals(Object other){
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Question))return false;
        Question question = (Question) other;
        return question.getTextQuest().equals(this.getTextQuest());
    }
}