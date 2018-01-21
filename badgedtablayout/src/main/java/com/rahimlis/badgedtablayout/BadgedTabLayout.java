package com.rahimlis.badgedtablayout;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.transition.TransitionManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * BadgedTabLayout extends {@link TabLayout} class with additional functionality of
 * adding small badges near titles. This is useful when you want to show specific tab
 * information (like new messages, or searched result count etc)
 * <p>
 * Tab text color can be updated using TabLayout methods
 * Badges are also fully customizable
 */

public class BadgedTabLayout extends TabLayout {


    private static final String TAG = "BadgedTabLayout";
    protected ColorStateList badgeBackgroundColors;
    protected ColorStateList badgeTextColors;
    protected float badgeTextSize = 0;
    protected float tabTextSize = 0;
    protected Typeface tabFont = null;
    protected Typeface badgeFont = null;
    protected TextUtils.TruncateAt tabTruncateAt = null;
    protected TextUtils.TruncateAt badgeTruncateAt = null;

    public BadgedTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);


        //set default colors from resources
        badgeBackgroundColors = ContextCompat.getColorStateList(context, R.color.badge_color);
        //      badgeTextColors = ContextCompat.getColorStateList(context, R.color.badge_text_color);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.BadgedTabLayout, 0, 0);

        badgeTextColors = getContextColors();

        try {

            // If we have an explicit text color set, use it instead
            if (a.hasValue(R.styleable.BadgedTabLayout_badgeBackgroundColor))
                badgeBackgroundColors = a.getColorStateList(R.styleable.BadgedTabLayout_badgeBackgroundColor);

            if (a.hasValue(R.styleable.BadgedTabLayout_badgeTextColor))
                badgeTextColors = a.getColorStateList(R.styleable.BadgedTabLayout_badgeTextColor);

            if (a.hasValue(R.styleable.BadgedTabLayout_badgeTextSize))
                badgeTextSize = a.getDimension(R.styleable.BadgedTabLayout_badgeTextSize, 0);

            if (a.hasValue(R.styleable.BadgedTabLayout_tabTextSize))
                tabTextSize = a.getDimension(R.styleable.BadgedTabLayout_tabTextSize, getResources().getDimension(R.dimen.tab_text_size));

            // We have an explicit selected text color set, so we need to make merge it with the
            // current colors. This is exposed so that developers can use theme attributes to set
            // this (theme attrs in ColorStateLists are Lollipop+)

            if (a.hasValue(R.styleable.BadgedTabLayout_badgeSelectedBackgroundColor)) {
                final int selected = a.getColor(R.styleable.BadgedTabLayout_badgeSelectedBackgroundColor, 0);
                badgeBackgroundColors = createColorStateList(badgeBackgroundColors.getDefaultColor(), selected);
            }

            if (a.hasValue(R.styleable.BadgedTabLayout_badgeSelectedTextColor)) {
                final int selected = a.getColor(R.styleable.BadgedTabLayout_badgeSelectedTextColor, 0);
                badgeTextColors = createColorStateList(badgeTextColors.getDefaultColor(), selected);
            }

        } finally {
            a.recycle();
        }
    }

    public ColorStateList getBadgeBackgroundColors() {
        return badgeBackgroundColors;
    }

    public float getTabTextSize() {
        return tabTextSize;
    }

    /**
     * @param tabTextSize in pixels
     */
    public void setTabTextSize(float tabTextSize) {
        this.tabTextSize = tabTextSize;
        updateTabViews();
    }

    /**
     * @param dimensionRes resource value of dimension ex: R.dimen.example
     */
    public void setTabTextSize(@DimenRes int dimensionRes) {
        this.tabTextSize = getResources().getDimension(dimensionRes);
        updateTabViews();
    }

    public float getBadgeTextSize() {
        return badgeTextSize;
    }

    /**
     * @param badgeTextSize in pixels
     */
    public void setBadgeTextSize(float badgeTextSize) {
        this.badgeTextSize = badgeTextSize;
        updateTabViews();
    }

    public Typeface getTabFont() {
        return tabFont;
    }

    public void setTabFont(Typeface tabFont) {
        this.tabFont = tabFont;
        updateTabViews();
    }

    public Typeface getBadgeFont() {
        return badgeFont;
    }

    public void setBadgeFont(Typeface badgeFont) {
        this.badgeFont = badgeFont;
        updateTabViews();
    }

    /**
     * sets badge background color
     *
     * @param badgeBackgroundColors state color list for badge background (selected/unselected)
     */
    public void setBadgeBackgroundColors(ColorStateList badgeBackgroundColors) {
        this.badgeBackgroundColors = badgeBackgroundColors;
        updateTabViews();
    }

    public ColorStateList getBadgeTextColors() {
        return badgeTextColors;
    }

    public TextUtils.TruncateAt getTabTruncateAt() {
        return tabTruncateAt;
    }

    public void setTabTruncateAt(TextUtils.TruncateAt tabTruncateAt) {
        this.tabTruncateAt = tabTruncateAt;
        updateTabViews();
    }

    public TextUtils.TruncateAt getBadgeTruncateAt() {
        return badgeTruncateAt;
    }

    public void setBadgeTruncateAt(TextUtils.TruncateAt badgeTruncateAt) {
        this.badgeTruncateAt = badgeTruncateAt;
        updateTabViews();
    }

    /**
     * sets badge text color
     *
     * @param badgeTextColors state color list for badge text (selected/unselected)
     */
    public void setBadgeTextColors(ColorStateList badgeTextColors) {
        this.badgeTextColors = badgeTextColors;
        updateTabViews();
    }


    @Override
    public void addTab(@NonNull Tab tab, int position, boolean setSelected) {
        super.addTab(tab, position, setSelected);
        onTabAdded(tab);
    }


    /**
     * Invalidates the tab views
     */
    public void updateTabViews() {
        for (int i = 0; i < getTabCount(); i++) {
            TabLayout.Tab tab = getTabAt(i);

            if (tab != null)
                tab.setCustomView(makeCustomView(tab, R.layout.badged_tab));
        }
    }

    /**
     * Creates new view for the tab from custom layout, look at {@link R.layout#badged_tab}
     *
     * @param tab   the tab for which new custom view will be created; required to extract title
     * @param resId layout id which is used to inflate new appearance of the tab
     * @return new customized view of the tab
     */
    private View makeCustomView(TabLayout.Tab tab, int resId) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = tab.getCustomView() == null ?
                inflater.inflate(resId, null, false)
                : tab.getCustomView();


        makeCustomTitle(tab, view);

        makeCustomIcon(tab, view);

        makeBadge(view);

        return view;
    }

    /**
     * @param tab  for which custom icon is created
     * @param view custom view created from badged_tab.xml
     */
    private void makeCustomIcon(Tab tab, View view) {
        if (tab.getIcon() == null) {
            return;
        }

        ImageView icon = view.findViewById(R.id.imageview_tab_icon);

        DrawableCompat.setTintList(tab.getIcon(), getTabTextColors());

        icon.setImageDrawable(tab.getIcon());

        icon.setVisibility(VISIBLE);
    }


    /**
     * @param position of tab where icon need to be set
     * @param resourse drawable resourse of vector icon
     */
    public void setIcon(int position, @DrawableRes int resourse) {
        Tab tab = getTabAt(position);

        if (tab == null) {
            Log.e(TAG, "Tab at position " + position + " is not initialized. Check your tablayout implementation and if you properly initialized the view.");
            return;
        }
        tab.setIcon(resourse);

        makeCustomIcon(tab, tab.getCustomView());
    }


    /**
     * @param view custom view, manually inflated from badged_tab.xml
     */
    private void makeBadge(View view) {
        TextView badge = view.findViewById(R.id.textview_tab_badge);
        badge.setTextColor(badgeTextColors);

        if (badgeTruncateAt != null)
            badge.setEllipsize(badgeTruncateAt);

        if (badgeFont != null)
            badge.setTypeface(badgeFont);

        if (badgeTextSize != 0)
            badge.setTextSize(TypedValue.COMPLEX_UNIT_PX, badgeTextSize);

        DrawableCompat.setTintList(badge.getBackground(), badgeBackgroundColors);
    }


    /**
     * @param tab  for which custom title is created
     * @param view custom view, manually inflated from badged_tab.xml
     */
    private void makeCustomTitle(Tab tab, View view) {
        TextView title = view.findViewById(R.id.textview_tab_title);

        title.setTextColor(getTabTextColors());

        if (tabTextSize != 0)
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize);

        if (tabTruncateAt != null)
            title.setEllipsize(tabTruncateAt);

        if (tabFont != null)
            title.setTypeface(tabFont);

        if (!TextUtils.isEmpty(tab.getText()))
            title.setText(tab.getText());
        else
            title.setVisibility(GONE);
    }

    /**
     * @param index of tab where badge should be added
     * @param text  the text of the badge (null to hide the badge)
     */
    public void setBadgeText(int index, @Nullable String text) {
        TabLayout.Tab tab = getTabAt(index);
        if (tab == null || tab.getCustomView() == null) {
            Log.e("BadgedTabLayout", "Tab is null. Not setting custom view");
            return;
        }

        TextView badge = tab.getCustomView().findViewById(R.id.textview_tab_badge);
        TextView tabText = tab.getCustomView().findViewById(R.id.textview_tab_title);

        if (text == null) {
            badge.setVisibility(View.GONE);
            tabText.setMaxWidth(Integer.MAX_VALUE);
        } else {
            int maxWidth = getResources().getDimensionPixelSize(R.dimen.tab_text_max_width);
            badge.setText(text);
            tabText.setMaxWidth(maxWidth);
            badge.setVisibility(View.VISIBLE);
        }
        TransitionManager.beginDelayedTransition((ViewGroup) tab.getCustomView());
    }


    public void onTabAdded(Tab tab) {
        if (tab == null) {
            Log.e("BadgedTabLayout", "Tab is null. Not setting custom view");
            return;
        }
        tab.setCustomView(makeCustomView(tab, R.layout.badged_tab));
    }


    /**
     * takes primary and primaryDark colors from context
     *
     * @return {@link ColorStateList} object, with primary color at selected state
     * and primaryDark on unselected state
     */
    private ColorStateList getContextColors() {
        TypedValue typedValue = new TypedValue();
        TypedArray a = getContext().obtainStyledAttributes(typedValue.data, new int[]{
                R.attr.colorPrimary, R.attr.colorPrimaryDark});
        int primaryColor = a.getColor(0, 0);
        int primaryDarkColor = a.getColor(1, 0);
        a.recycle();
        return createColorStateList(primaryDarkColor, primaryColor);
    }


    /**
     * Creates color states list out of two given params
     *
     * @param defaultColor  color for state_selected = false
     * @param selectedColor color for state_selected = true
     * @return {@link ColorStateList} object
     */
    private static ColorStateList createColorStateList(int defaultColor, int selectedColor) {
        final int[][] states = new int[2][];
        final int[] colors = new int[2];
        int i = 0;
        states[i] = SELECTED_STATE_SET;
        colors[i] = selectedColor;
        i++;
        // Default enabled state
        states[i] = EMPTY_STATE_SET;
        colors[i] = defaultColor;
        i++;
        return new ColorStateList(states, colors);
    }

}
