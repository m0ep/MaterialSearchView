package de.florianm.android.materialsearchview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;

/* package */ final class MaterialSearchViewUtils {

    private MaterialSearchViewUtils() {
        /* hidden constructor */
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void enterCircularReveal(@NonNull View view, int cx, int cy, long duration, @Nullable Animator.AnimatorListener listener) {
        final int endRadius = Math.max(view.getMeasuredWidth(), view.getMeasuredHeight());

        Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, endRadius);
        anim.setDuration(duration);
        anim.setInterpolator(new DecelerateInterpolator());
        if (null != listener) {
            anim.addListener(listener);
        }

        view.setVisibility(View.VISIBLE);
        anim.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void exitCircularReveal(@NonNull final View view, int cx, int cy, int duration, @Nullable final Animator.AnimatorListener listener) {
        final int startRadius = Math.max(view.getMeasuredWidth(), view.getMeasuredHeight());

        Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, startRadius, 0);
        anim.setDuration(duration);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                view.setVisibility(View.VISIBLE);
                if (null != listener) {
                    listener.onAnimationCancel(animation);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
                if (null != listener) {
                    listener.onAnimationEnd(animation);
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                if (null != listener) {
                    listener.onAnimationRepeat(animation);
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                if (null != listener) {
                    listener.onAnimationStart(animation);
                }
            }
        });
        anim.setInterpolator(new AccelerateInterpolator());

        anim.start();
    }

    public static void fadeIn(@NonNull final View view, int durationMillis, @Nullable final Animator.AnimatorListener listener) {
        view.setVisibility(View.VISIBLE);
        ViewCompat.setAlpha(view, 0f);
        view.animate()
                .alpha(1f)
                .setDuration(durationMillis)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        view.setDrawingCacheEnabled(true);
                        if (null != listener) {
                            listener.onAnimationStart(animation);
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setDrawingCacheEnabled(false);
                        if (null != listener) {
                            listener.onAnimationEnd(animation);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                        if (null != listener) {
                            listener.onAnimationRepeat(animation);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        view.setDrawingCacheEnabled(false);
                        if (null != listener) {
                            listener.onAnimationCancel(animation);
                        }
                    }
                })

                .start();
    }

    public static void fadeIn(@NonNull View view, int durationMillis) {
        fadeIn(view, durationMillis, null);
    }

    public static void fadeOut(@NonNull final View view, int durationMillis, @Nullable final Animator.AnimatorListener listener) {
        view.animate()
                .alpha(0f)
                .setDuration(durationMillis)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (null != listener) {
                            listener.onAnimationStart(animation);
                        }

                        view.setDrawingCacheEnabled(true);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setDrawingCacheEnabled(false);
                        view.setVisibility(View.GONE);

                        if (null != listener) {
                            listener.onAnimationEnd(animation);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                        if (null != listener) {
                            listener.onAnimationRepeat(animation);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        view.setDrawingCacheEnabled(false);

                        if (null != listener) {
                            listener.onAnimationCancel(animation);
                        }
                    }
                })
                .start();
    }

    public static void fadeOut(@NonNull View view, int durationMillis) {
        fadeOut(view, durationMillis, null);
    }

    /**
     * Shows the soft keyboard for the specified <code>view</code>.
     *
     * @param view View that should receive input events from the soft keyboard.
     */
    public static void showKeyboard(@NonNull View view) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1 && view.hasFocus()) {
            view.clearFocus();
        }

        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, 0);
    }


    /**
     * Hides the soft keyboard of the specified <code>view</code>.
     *
     * @param view View that has the the input focus.
     */
    public static void hideKeyboard(@NonNull View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static boolean isNullOrEmpty(CharSequence charSequence) {
        return null == charSequence || 0 == TextUtils.getTrimmedLength(charSequence);
    }

    public static CharSequence nullToEmpty(CharSequence charSequence) {
        return isNullOrEmpty(charSequence) ? "" : charSequence;
    }
}
