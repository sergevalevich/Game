package com.balinasoft.clever.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.balinasoft.clever.R;
import com.balinasoft.clever.storage.model.News;
import com.balinasoft.clever.util.ConstantsManager;
import com.balinasoft.clever.util.SharingHelper;
import com.bumptech.glide.Glide;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.res.StringRes;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@EBean
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsHolder> {

    @RootContext
    Context mContext;

    @StringRes(R.string.clever_news)
    String mNewsHeader;

    @StringRes(R.string.more)
    String mMoreMessage;

    @Bean
    SharingHelper mSharingHelper;

    private List<News> nNews;

    @Override
    public NewsAdapter.NewsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_item, parent,
                false);
        return new NewsAdapter.NewsHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NewsAdapter.NewsHolder holder, int position) {
        holder.bind(nNews.get(position));
    }

    @Override
    public int getItemCount() {
        return nNews.size();
    }

    public void setData(List<News> newsList) {
        if (nNews == null) nNews = newsList;
        else refresh(newsList);
    }

    private void refresh(List<News> newsList) {
        nNews.clear();
        nNews.addAll(newsList);
        notifyDataSetChanged();
    }

    class NewsHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image)
        ImageView mImage;

        @BindView(R.id.title)
        TextView mTitle;

        @BindView(R.id.date)
        TextView mDateLabel;

        @BindView(R.id.share)
        ImageView mShare;

        @BindView(R.id.expand_text_view)
        ExpandableTextView mDescription;

        @OnClick(R.id.share)
        void share() {
            CharSequence description = mDescription.getText();
            CharSequence title = mTitle.getText();
            mSharingHelper.share(String.format(Locale.getDefault(),
                    "%s %n %s %n %s %n %s %s",
                    mNewsHeader,
                    title == null ? "" :title,
                    description == null ? "" :description,
                    mMoreMessage,
                    ConstantsManager.GOOGLE_PLAY_LINK));
        }


        NewsHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void bind(News news) {
            Glide.with(mContext).load(R.drawable.placeholder).into(mImage);
            mTitle.setText(news.getTopic());
            mDateLabel.setText(news.getDate());
            mDescription.setText(news.getDescription());
        }

    }
}
