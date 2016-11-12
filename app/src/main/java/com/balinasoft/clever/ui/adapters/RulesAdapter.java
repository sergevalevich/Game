package com.balinasoft.clever.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
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

import net.cachapa.expandablelayout.ExpandableLayout;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@EBean
public class RulesAdapter extends RecyclerView.Adapter<RulesAdapter.RulesHolder> {

    @RootContext
    Context mContext;

    private List<Rule> mRules;

    private String mSearchQuery;

    private SparseBooleanArray mOpenedItems = new SparseBooleanArray();

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
    }

    public void setSearchQuery(String searchQuery) {
        mSearchQuery = searchQuery.toLowerCase();
    }

    private void refresh(List<Rule> rules) {
        //items count is bigger|less
        SparseBooleanArray openedItems = new SparseBooleanArray(rules.size());
        for(int i = 0; i<rules.size(); i++) {
            boolean isOpened = false;
            Rule rule = rules.get(i);
            for(int j = 0; j<mRules.size(); j++) {
                if(rule.getServerId().equals(mRules.get(j).getServerId())) {
                    isOpened = mOpenedItems.get(j);
                    break;
                }
            }
            openedItems.put(i,isOpened);
        }
        mOpenedItems = openedItems;
        mRules.clear();
        mRules.addAll(rules);
        notifyDataSetChanged();
    }

    private Spannable getSpannableString(String original) {
        Spannable spanText = Spannable.Factory.getInstance().newSpannable(original);
        if(mSearchQuery != null && !mSearchQuery.isEmpty()) {
            int lastIndex = 0;

            while (lastIndex != -1) {

                lastIndex = original.toLowerCase().indexOf(mSearchQuery, lastIndex);

                if (lastIndex != -1) {
                    spanText.setSpan(new BackgroundColorSpan(0xBFFFDE76), lastIndex, lastIndex + mSearchQuery.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    lastIndex += mSearchQuery.length();
                }
            }
        }
        return spanText;
    }

    class RulesHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.title)
        TextView mTitle;

        @BindView(R.id.expandable_layout)
        ExpandableLayout mExpandableLayout;

        @BindView(R.id.description)
        TextView mDescription;

        @BindView(R.id.image)
        ImageView mImage;

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
            if(wasExpanded()) {
                toggleArrow();
                mExpandableLayout.expand(false);
            }
            mTitle.setText(getSpannableString(rule.getTitle()));
            mDescription.setText(getSpannableString(rule.getDescription()));
            String imagePath = rule.getImagePath();
            if (imagePath == null || imagePath.isEmpty())
                Glide.with(mContext).load(rule.getImageResId()).into(mImage);
            else
                Glide.with(mContext).load(UrlFormatter.getLocalFilePath(imagePath, mContext)).into(mImage);
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

        private boolean wasExpanded() {
            return mOpenedItems.get(getAdapterPosition());
        }

        private void toggleLayout() {
            if(mExpandableLayout.isExpanded()) {
                mOpenedItems.put(getAdapterPosition(),false);
                mExpandableLayout.collapse();
            } else {
                mOpenedItems.put(getAdapterPosition(),true);
                mExpandableLayout.expand();
            }
        }
    }
}
