package de.florianm.android.materialsearchview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;


public class MaterialSearchView extends FrameLayout
        implements View.OnClickListener,
        View.OnFocusChangeListener,
        TextView.OnEditorActionListener,
        TextWatcher,
        AdapterView.OnItemClickListener {

    private static final String TAG = MaterialSearchView.class.getSimpleName();

    public static final int REQUEST_VOICE_INPUT = 0xff01;

    private View searchLayoutContainer;
    private View tintView;
    private View searchContainer;
    private View searchBarContainer;
    private ImageButton backIconButton;
    private TextView searchTextView;
    private ImageButton voiceIconButton;
    private ImageButton clearIconButton;
    private ListView suggestionList;

    private CharSequence queryTextOld;
    private int animationDuration;

    private OnSearchViewListener searchViewListener;

    public MaterialSearchView(Context context) {
        this(context, null, 0);
    }

    public MaterialSearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaterialSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initWidget(attrs, defStyleAttr);
    }

    private void initWidget(AttributeSet attrs, int defStyleAttr) {
        animationDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);

        initViews();
        applyStyles(attrs, defStyleAttr);
    }

    private void initViews() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_material_search_view, this, true);

        searchLayoutContainer = findViewById(R.id.searchLayoutContainer);
        tintView = view.findViewById(R.id.tintView);
        searchContainer = view.findViewById(R.id.searchContainer);
        searchBarContainer = view.findViewById(R.id.searchBarContainer);
        backIconButton = (ImageButton) view.findViewById(R.id.backIconButton);
        searchTextView = (TextView) view.findViewById(R.id.searchTextView);
        voiceIconButton = (ImageButton) view.findViewById(R.id.voiceIconButton);
        clearIconButton = (ImageButton) view.findViewById(R.id.clearIconButton);
        suggestionList = (ListView) view.findViewById(R.id.suggestionList);

        backIconButton.setOnClickListener(this);
        voiceIconButton.setOnClickListener(this);
        clearIconButton.setOnClickListener(this);
        tintView.setOnClickListener(this);

        boolean isVoiceAvailable = isVoiceAvailable();
        voiceIconButton.setVisibility(isVoiceAvailable ? View.VISIBLE : View.GONE);
        clearIconButton.setVisibility(isVoiceAvailable ? View.GONE : View.VISIBLE);

        searchTextView.addTextChangedListener(this);
        searchTextView.setOnEditorActionListener(this);
        searchTextView.setOnFocusChangeListener(this);

        suggestionList.setOnItemClickListener(this);
    }

    private boolean isVoiceAvailable() {
        PackageManager packageManager = getContext().getPackageManager();
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        List<ResolveInfo> result = packageManager.queryIntentActivities(intent, 0);
        return null != result && 0 < result.size();
    }

    public void setSearchViewListener(OnSearchViewListener searchViewListener) {
        this.searchViewListener = searchViewListener;
    }

    private void applyStyles(AttributeSet attrs, int defStyleAttr) {

        TypedArray a = getContext().obtainStyledAttributes(
                attrs,
                R.styleable.MaterialSearchView,
                defStyleAttr,
                0);

        try {
            if (a.hasValue(R.styleable.MaterialSearchView_android_textColorHint)) {
                setHintTextColor(a.getColor(R.styleable.MaterialSearchView_android_textColorHint, 0));
            }

            if (a.hasValue(R.styleable.MaterialSearchView_android_textColor)) {
                setTextColor(a.getColor(R.styleable.MaterialSearchView_android_textColor, 0));
            }

            if (a.hasValue(R.styleable.MaterialSearchView_android_hint)) {
                setHint(a.getString(R.styleable.MaterialSearchView_android_hint));
            }

            if (a.hasValue(R.styleable.MaterialSearchView_searchBackground)) {
                setSearchBackground(a.getDrawable(R.styleable.MaterialSearchView_searchBackground));
            }

            if (a.hasValue(R.styleable.MaterialSearchView_searchBackIcon)) {
                setBackIcon(a.getDrawable(R.styleable.MaterialSearchView_searchBackIcon));
            }

            if (a.hasValue(R.styleable.MaterialSearchView_searchClearIcon)) {
                setClearIcon(a.getDrawable(R.styleable.MaterialSearchView_searchClearIcon));
            }

            if (a.hasValue(R.styleable.MaterialSearchView_searchVoiceIcon)) {
                setVoiceIcon(a.getDrawable(R.styleable.MaterialSearchView_searchVoiceIcon));
            }

            if (a.hasValue(R.styleable.MaterialSearchView_suggestionEntries)) {
                setSuggestionEntries(a.getTextArray(R.styleable.MaterialSearchView_suggestionEntries));
            }
        } finally {
            a.recycle();
        }
    }

    public void showSearchView() {
        if (isSearchViewVisible()) {
            return;
        }

        showSearchViewAnimated();
    }

    public boolean isSearchViewVisible() {
        return View.VISIBLE == searchLayoutContainer.getVisibility();
    }

    private void showSearchViewAnimated() {
        Animator.AnimatorListener listener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                suggestionList.setVisibility(GONE);
                searchLayoutContainer.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Utils.showKeyboard(searchTextView);
                if (null != searchViewListener) {
                    searchViewListener.onShowSearchView();
                }
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Utils.enterCircularReveal(searchContainer,
                    searchBarContainer.getMeasuredWidth(),
                    searchBarContainer.getMeasuredHeight() / 2,
                    listener);
        } else {
            Utils.fadeIn(searchContainer, animationDuration, listener);
        }
    }

    public void hideSearchView() {
        if (!isSearchViewVisible()) {
            return;
        }

        hideSearchViewAnimated();
    }

    private void hideSearchViewAnimated() {
        final Animator.AnimatorListener listener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                searchLayoutContainer.setVisibility(GONE);
                tintView.setVisibility(GONE);

                if (null != searchViewListener) {
                    searchViewListener.onHideSearchView();
                }
            }
        };

        hideSuggestions();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Utils.exitCircularReveal(searchContainer,
                    searchBarContainer.getMeasuredWidth(),
                    searchBarContainer.getMeasuredHeight() / 2,
                    listener);
        } else {
            Utils.fadeOut(searchContainer, animationDuration, listener);
        }
    }

    private void hideSuggestions() {
        Utils.fadeOut(tintView, animationDuration);
        suggestionList.setVisibility(GONE);
    }

    @Override
    public void onClick(View v) {
        if (backIconButton == v) {
            hideSearchView();
        } else if (voiceIconButton == v) {
            startVoiceInput();
        } else if (clearIconButton == v) {
            clearQueryText();
        } else if (tintView == v) {
            hideSearchView();
        }
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        Context context = getContext();
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, REQUEST_VOICE_INPUT);
        }
    }

    private void clearQueryText() {
        searchTextView.setText(null);
        Utils.showKeyboard(searchTextView);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (TextUtils.isEmpty(s) || 0 == TextUtils.getTrimmedLength(s)) {
            boolean isVoiceAvailable = isVoiceAvailable();
            voiceIconButton.setVisibility(isVoiceAvailable ? View.VISIBLE : View.GONE);
            clearIconButton.setVisibility(GONE);
        } else {
            voiceIconButton.setVisibility(GONE);
            clearIconButton.setVisibility(VISIBLE);
        }

        s = TextUtils.isEmpty(s) ? "" : s;
        if (null != searchViewListener && !s.equals(queryTextOld)) {
            searchViewListener.onQueryTextChanged(s);
        }

        queryTextOld = s;
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_SEARCH == actionId) {
            submitQuery(searchTextView.getText());
            Utils.hideKeyboard(searchTextView);
        }

        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (searchTextView == v) {
            if (hasFocus) {
                showSuggestions();
            } else {
                hideSuggestions();
            }
        }
    }

    public void showSuggestions() {
        Utils.fadeIn(tintView, animationDuration);
        Utils.fadeIn(suggestionList, animationDuration);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CharSequence item = (CharSequence) suggestionList.getItemAtPosition(position);
        searchTextView.setText(item);
        submitQuery(item);
    }

    private void submitQuery(CharSequence query) {
        searchBarContainer.requestFocus();
        if (null != searchViewListener && searchViewListener.onSubmitQuery(query)) {
            hideSearchView();
        } else {
            hideSuggestions();
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState savedState = new SavedState(superState);
        savedState.queryTextOld = queryTextOld;
        savedState.queryText = searchTextView.getText();
        savedState.viewVisible = isSearchViewVisible();

        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState savedState = (SavedState) state;
        onRestoreInstanceState(savedState.getSuperState());

        queryTextOld = savedState.queryTextOld;
        setQuery(savedState.queryText);

        if (savedState.viewVisible) {
            showSearchViewInstantly();
        }
    }

    private void showSearchViewInstantly() {
        searchLayoutContainer.setVisibility(VISIBLE);
        searchContainer.setVisibility(VISIBLE);
        Utils.showKeyboard(searchTextView);

        if (null != searchViewListener) {
            searchViewListener.onShowSearchView();
        }
    }

    public void setQuery(CharSequence query, boolean submit) {
        searchTextView.setText(query);
        if (submit) {
            submitQuery(query);
        }
    }

    @SuppressWarnings("unused")
    public void setQuery(CharSequence query) {
        setQuery(query, false);
    }

    public void setHintTextColor(int color) {
        searchTextView.setHintTextColor(color);
    }

    public void setTextColor(int color) {
        searchTextView.setTextColor(color);
    }

    public void setHint(CharSequence hint) {
        searchTextView.setHint(hint);
    }

    public void setSuggestionEntries(CharSequence[] entries) {
        if(null != entries && 0 < entries.length){
            SuggestionAdapter suggestionAdapter = new SuggestionAdapter();
            suggestionAdapter.setItems(entries);
            setSuggestionAdapter(suggestionAdapter);
        } else{
            setSuggestionAdapter(null);
            hideSuggestions();
        }
    }

    public void setSuggestionAdapter(ListAdapter adapter){
        if(null == adapter){
            adapter = new SuggestionAdapter();
        }

        suggestionList.setAdapter(adapter);
    }

    @SuppressWarnings("deprecation")
    public void setSearchBackground(Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            searchContainer.setBackground(drawable);
        } else {
            searchContainer.setBackgroundDrawable(drawable);
        }
    }

    public void setBackIcon(Drawable drawable) {
        backIconButton.setImageDrawable(drawable);
    }

    public void setClearIcon(Drawable drawable) {
        clearIconButton.setImageDrawable(drawable);
    }

    public void setVoiceIcon(Drawable drawable) {
        voiceIconButton.setImageDrawable(drawable);
    }

    public interface OnSearchViewListener {
        /**
         * Invoked if the query text changed
         *
         * @param text New query text
         */
        void onQueryTextChanged(CharSequence text);

        /**
         * Invoked if the action button on the keyboard is pressed,
         * if a entry was selected from the suggestion list or if
         * {@link MaterialSearchView#setQuery(CharSequence, boolean)} was called with <code>submit=true</code>
         *
         * @param text Current query text
         * @return Return <code>true</code> to close MaterialSearchView or <code>false</code> to let it stay open.
         */
        boolean onSubmitQuery(CharSequence text);

        /**
         * Invoked if the MaterialSearchView is opened.
         */
        void onShowSearchView();

        /**
         * Invoked if the MaterialSearchView is hidden.
         */
        void onHideSearchView();
    }

    /* package */ static class SavedState extends View.BaseSavedState {
        CharSequence queryText;
        CharSequence queryTextOld;
        boolean viewVisible;

        public SavedState(Parcel source) {
            super(source);

            queryText = source.readString();
            queryTextOld = source.readString();
            viewVisible = 0 != source.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(null == queryText ? "" : queryText.toString());
            out.writeString(null == queryTextOld ? "" : queryTextOld.toString());
            out.writeInt(viewVisible ? 1 : 0);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
