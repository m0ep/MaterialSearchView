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
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatEditText;
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


    private View tintView;
    private View searchContainer;
    private View searchBar;
    private View suggestionContainer;
    private AppCompatImageButton backIconButton;
    private AppCompatEditText searchTextView;
    private AppCompatImageButton actionButton;
    private ListView suggestionList;

    private CharSequence oldQueryText;
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
        animationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        initViews();
        applyStyles(attrs, defStyleAttr);
    }

    private void initViews() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_material_search_view, this, true);

        tintView = view.findViewById(R.id.tintBackground);
        searchContainer = view.findViewById(R.id.searchContainer);
        searchBar = view.findViewById(R.id.searchBar);

        backIconButton = (AppCompatImageButton) view.findViewById(R.id.backButton);
        searchTextView = (AppCompatEditText) view.findViewById(R.id.searchTextView);
        actionButton = (AppCompatImageButton) view.findViewById(R.id.actionButton);

        suggestionContainer = view.findViewById(R.id.suggestionContainer);
        suggestionList = (ListView) view.findViewById(R.id.suggestionList);

        backIconButton.setOnClickListener(this);
        actionButton.setOnClickListener(this);
        tintView.setOnClickListener(this);

        searchTextView.addTextChangedListener(this);
        searchTextView.setOnEditorActionListener(this);
        searchTextView.setOnFocusChangeListener(this);

        suggestionList.setOnItemClickListener(this);

        updateActionButton();
        hideSearchViewInstantly();
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

            if (a.hasValue(R.styleable.MaterialSearchView_suggestionEntries)) {
                setSuggestionEntries(a.getTextArray(R.styleable.MaterialSearchView_suggestionEntries));
            }
        } finally {
            a.recycle();
        }
    }

    public void showSearchView() {
        showSearchView(
                searchBar.getMeasuredWidth(),
                searchBar.getMeasuredHeight() / 2
        );
    }

    public void showSearchView(int revealCenterX, int revealCenterY) {
        if (isSearchViewVisible()) {
            return;
        }

        showSearchViewAnimated(revealCenterX, revealCenterY);
    }

    public boolean isSearchViewVisible() {
        return View.VISIBLE == getVisibility();
    }

    private void showSearchViewAnimated(int revealCenterX, int revealCenterY) {
        Animator.AnimatorListener listener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                suggestionContainer.setVisibility(GONE);
                setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                MaterialSearchViewUtils.showKeyboard(searchTextView);
                if (null != searchViewListener) {
                    searchViewListener.onShowSearchView();
                }
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            MaterialSearchViewUtils.enterCircularReveal(searchContainer,
                    revealCenterX,
                    revealCenterY,
                    animationDuration,
                    listener
            );
        } else {
            MaterialSearchViewUtils.fadeIn(
                    searchContainer,
                    animationDuration,
                    listener
            );
        }
    }

    private void hideSuggestions() {
        MaterialSearchViewUtils.fadeOut(tintView, animationDuration);
        suggestionContainer.setVisibility(GONE);
    }

    @Override
    public void onClick(View v) {
        if (backIconButton == v) {
            hideSearchView();
        } else if (actionButton == v) {
            if (isVoiceInputActionEnabled()) {
                startVoiceInput();
            } else if (isClearSearchTextActionEnabled()) {
                clearQueryText();
            }
        } else if (tintView == v) {
            hideSearchView();
        }
    }


    public void hideSearchView() {
        hideSearchView(
                searchBar.getMeasuredWidth(),
                searchBar.getMeasuredHeight() / 2
        );
    }

    public void hideSearchView(int revealCenterX, int revealCenterY) {
        if (!isSearchViewVisible()) {
            return;
        }

        hideSearchViewAnimated(revealCenterX, revealCenterY);
    }

    private void hideSearchViewAnimated(int revealCenterX, int revealCenterY) {
        final Animator.AnimatorListener listener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(GONE);
                tintView.setVisibility(GONE);

                if (null != searchViewListener) {
                    searchViewListener.onHideSearchView();
                }
            }
        };

        hideSuggestions();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            MaterialSearchViewUtils.exitCircularReveal(searchContainer,
                    revealCenterX,
                    revealCenterY,
                    animationDuration,
                    listener
            );
        } else {
            MaterialSearchViewUtils.fadeOut(
                    searchContainer,
                    animationDuration,
                    listener
            );
        }
    }

    public void hideSearchViewInstantly(){
        setVisibility(View.GONE);
    }

    private boolean isVoiceInputActionEnabled() {
        return isVoiceAvailable() && TextUtils.isEmpty(searchTextView.getText());
    }

    private boolean isVoiceAvailable() {
        PackageManager packageManager = getContext().getPackageManager();
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        List<ResolveInfo> result = packageManager.queryIntentActivities(intent, 0);
        return null != result && 0 < result.size();
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

    private boolean isClearSearchTextActionEnabled() {
        return !TextUtils.isEmpty(searchTextView.getText());
    }

    private void clearQueryText() {
        searchTextView.setText(null);
        MaterialSearchViewUtils.showKeyboard(searchTextView);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence text, int start, int before, int count) {
        updateActionButton();

        text = MaterialSearchViewUtils.nullToEmpty(text);
        if (null != searchViewListener && !text.equals(oldQueryText)) {
            searchViewListener.onQueryTextChanged(text);
        }

        oldQueryText = text;
    }

    private void updateActionButton() {
        boolean isVisible = isClearSearchTextActionEnabled() || isVoiceInputActionEnabled();
        actionButton.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        if (isVisible) {
            if (isClearSearchTextActionEnabled()) {
                actionButton.setImageResource(R.drawable.ic_close_white_24dp);
            } else if (isVoiceInputActionEnabled()) {
                actionButton.setImageResource(R.drawable.ic_keyboard_voice_white_24dp);
            } else {
                actionButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_SEARCH == actionId) {
            submitQuery(searchTextView.getText());
            MaterialSearchViewUtils.hideKeyboard(searchTextView);
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
        MaterialSearchViewUtils.fadeIn(tintView, animationDuration);
        MaterialSearchViewUtils.fadeIn(suggestionContainer, animationDuration);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CharSequence item = (CharSequence) suggestionList.getItemAtPosition(position);
        searchTextView.setText(item);
        submitQuery(item);
    }

    private void submitQuery(CharSequence query) {
        searchBar.requestFocus();
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
        savedState.oldQueryText = oldQueryText;
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

        oldQueryText = savedState.oldQueryText;
        setQuery(savedState.queryText);

        if (savedState.viewVisible) {
            showSearchViewInstantly();
        }
    }

    private void showSearchViewInstantly() {
        setVisibility(VISIBLE);
        searchContainer.setVisibility(VISIBLE);
        MaterialSearchViewUtils.showKeyboard(searchTextView);

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
        if (null != entries && 0 < entries.length) {
            SuggestionAdapter suggestionAdapter = new SuggestionAdapter();
            suggestionAdapter.setItems(entries);
            setSuggestionAdapter(suggestionAdapter);
        } else {
            setSuggestionAdapter(null);
            hideSuggestions();
        }
    }

    public void setSuggestionAdapter(ListAdapter adapter) {
        if (null == adapter) {
            adapter = new SuggestionAdapter();
        }

        suggestionList.setAdapter(adapter);
    }

    public void setSearchBackground(Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            searchContainer.setBackground(drawable);
        } else {
            //noinspection deprecation
            searchContainer.setBackgroundDrawable(drawable);
        }
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
        CharSequence oldQueryText;
        boolean viewVisible;

        public SavedState(Parcel source) {
            super(source);

            queryText = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
            oldQueryText = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
            viewVisible = 1 == source.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);

            TextUtils.writeToParcel(queryText, out, 0);
            TextUtils.writeToParcel(oldQueryText, out, 0);
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
