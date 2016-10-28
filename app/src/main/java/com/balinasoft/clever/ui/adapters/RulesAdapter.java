package com.balinasoft.clever.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.balinasoft.clever.R;
import com.balinasoft.clever.storage.model.Rule;
import com.balinasoft.clever.util.UrlFormatter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.aakira.expandablelayout.ExpandableLayoutListenerAdapter;
import com.github.aakira.expandablelayout.ExpandableLinearLayout;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

@EBean
public class RulesAdapter extends RecyclerView.Adapter<RulesAdapter.RulesHolder> {

    @RootContext
    Context mContext;

    private List<Rule> mRules;

    private SparseBooleanArray mExpandState = new SparseBooleanArray();

    @Override
    public RulesHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rule_item, parent,
                false);
        return new RulesAdapter.RulesHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RulesHolder holder, int position) {
        holder.setIsRecyclable(false);
        holder.bind(mRules.get(position));
    }

    @Override
    public int getItemCount() {
        return mRules.size();
    }

    public void setData(List<Rule> rules) {
        if (mRules == null) mRules = rules;
        else refresh(rules);
        setExpandState(rules.size());
    }

    private void setExpandState(int dataSize) {
        mExpandState.clear();
        for (int i = 0; i < dataSize; i++) {
            mExpandState.append(i, false);
        }
    }

    private void refresh(List<Rule> rules) {
        mRules.clear();
        mRules.addAll(rules);
        notifyDataSetChanged();
    }

    class RulesHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.title)
        TextView mTitle;

        @BindView(R.id.expandableLayout)
        ExpandableLinearLayout mExpandableLayout;

        @BindView(R.id.description)
        TextView mDescription;

//        @BindView(R.id.image)
//        ImageView mImage;

        private boolean mIsExpanded = false;

        @OnClick(R.id.title)
        void toggle() {
            toggleArrow();
            toggleLayout();
        }

        RulesHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void bind(Rule rule) {
            setUpExpandableView();
            mTitle.setText(rule.getTitle());
            mDescription.setText(rule.getDescription());
//            String imagePath = rule.getImagePath();
//            if (imagePath == null || imagePath.isEmpty())
//                Glide.with(mContext).load(rule.getImageResId()).listener(new RequestListener<Integer, GlideDrawable>() {
//                    @Override
//                    public boolean onException(Exception e, Integer model, Target<GlideDrawable> target, boolean isFirstResource) {
//                        return false;
//                    }
//
//                    @Override
//                    public boolean onResourceReady(GlideDrawable resource, Integer model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                        return false;
//                    }
//                }).into(mImage);
//            else
//                Glide.with(mContext).load(UrlFormatter.getLocalFilePath(imagePath, mContext)).listener(new RequestListener<String, GlideDrawable>() {
//                    @Override
//                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
//                        return false;
//                    }
//
//                    @Override
//                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                        return false;
//                    }
//                }).into(mImage);
        }

        private void setUpExpandableView() {
            mExpandableLayout.setInRecyclerView(true);
            boolean isExp = mExpandState.get(getAdapterPosition());
            Timber.d("set item expanded %s",String.valueOf(isExp));
            mExpandableLayout.setExpanded(isExp);
            Timber.d("item now is expanded %s",String.valueOf(mExpandableLayout.isExpanded()));
            mExpandableLayout.setListener(new ExpandableLayoutListenerAdapter() {
                @Override
                public void onPreOpen() {
                    Timber.d("preOpen");
                    mExpandState.put(getAdapterPosition(), true);
                }

                @Override
                public void onPreClose() {
                    Timber.d("preClose");
                    mExpandState.put(getAdapterPosition(), false);
                }
            });
        }

        private void toggleArrow() {
            if (mIsExpanded) {
                mTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_toggle, 0);
                mIsExpanded = false;
            } else {
                mTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_toggle_up, 0);
                mIsExpanded = true;
            }
        }

        private void toggleLayout() {
            mExpandableLayout.toggle();
        }
    }
}
