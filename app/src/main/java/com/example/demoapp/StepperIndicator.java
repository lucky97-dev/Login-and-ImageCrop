package com.example.demoapp;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StepperIndicator extends View implements ViewPager.OnPageChangeListener {

    private static final String TAG = "StepperIndicator";
    private static final int DEFAULT_ANIMATION_DURATION = 200;
    private static final float EXPAND_MARK = 1.3f;
    private static final int STEP_INVALID = -1;
    private Paint circlePaint;
    private List<Paint> stepsCirclePaintList;
    private float circleRadius;
    private boolean showStepTextNumber;
    private Paint stepTextNumberPaint;
    private List<Paint> stepsTextNumberPaintList;
    private Paint indicatorPaint;
    private List<Paint> stepsIndicatorPaintList;
    private Paint linePaint;
    private Paint lineDonePaint;
    private Paint lineDoneAnimatedPaint;
    private List<Path> linePathList = new ArrayList<>();
    @SuppressWarnings("unused")
    private float animProgress;
    private float animIndicatorRadius;
    private float animCheckRadius;
    private boolean useBottomIndicator;
    private float bottomIndicatorMarginTop = 0;
    private float bottomIndicatorWidth = 0;
    private float bottomIndicatorHeight = 0;
    private boolean useBottomIndicatorWithStepColors;
    private float lineLength;
    private float checkRadius;
    private float indicatorRadius;
    private float lineMargin;
    private int animDuration;

    private List<OnStepClickListener> onStepClickListeners = new ArrayList<>(0);
    private List<RectF> stepsClickAreas;

    private GestureDetector gestureDetector;
    private int stepCount;
    private int currentStep;
    private int previousStep;
    private float[] indicators;
    private Rect stepAreaRect = new Rect();
    private RectF stepAreaRectF = new RectF();

    private ViewPager pager;
    private Drawable doneIcon;
    private boolean showDoneIcon;

    private TextPaint labelPaint;
    private CharSequence[] labels;
    private boolean showLabels;
    private float labelMarginTop;
    private StaticLayout[] labelLayouts;
    private float maxLabelHeight;

    private AnimatorSet animatorSet;
    private ObjectAnimator lineAnimator, indicatorAnimator, checkAnimator;

    private GestureDetector.OnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            int clickedStep = STEP_INVALID;
            if (isOnStepClickListenerAvailable()) {
                for (int i = 0; i < stepsClickAreas.size(); i++) {
                    if (stepsClickAreas.get(i).contains(e.getX(), e.getY())) {
                        clickedStep = i;
                        break;
                    }
                }
            }
            if (clickedStep != STEP_INVALID) {
                for (OnStepClickListener listener : onStepClickListeners) {
                    listener.onStepClicked(clickedStep);
                }
            }

            return super.onSingleTapConfirmed(e);
        }
    };

    public StepperIndicator(Context context) {
        this(context, null);
    }

    public StepperIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StepperIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StepperIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr);
    }

    public static int getPrimaryColor(final Context context) {
        int color = context.getResources().getIdentifier("colorPrimary", "attr", context.getPackageName());
        if (color != 0) {
            TypedValue t = new TypedValue();
            context.getTheme().resolveAttribute(color, t, true);
            color = t.data;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TypedArray t = context.obtainStyledAttributes(new int[]{android.R.attr.colorPrimary});
            color = t.getColor(0, ContextCompat.getColor(context, R.color.stpi_default_primary_color));
            t.recycle();
        } else {
            TypedArray t = context.obtainStyledAttributes(new int[]{R.attr.colorPrimary});
            color = t.getColor(0, ContextCompat.getColor(context, R.color.stpi_default_primary_color));
            t.recycle();
        }

        return color;
    }

    public static int getTextColorSecondary(final Context context) {
        TypedArray t = context.obtainStyledAttributes(new int[]{android.R.attr.textColorSecondary});
        int color = t.getColor(0, ContextCompat.getColor(context, R.color.stpi_default_text_color));
        t.recycle();
        return color;
    }

    private static PathEffect createPathEffect(float pathLength, float phase, float offset) {
        // Create a PathEffect to set on a Paint to only draw some part of the line
        return new DashPathEffect(new float[]{pathLength, pathLength}, Math.max(phase * pathLength, offset));
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        final Resources resources = getResources();

        // Default values
        int defaultPrimaryColor = getPrimaryColor(context);

        int defaultCircleColor = ContextCompat.getColor(context, R.color.stpi_default_circle_color);
        float defaultCircleRadius = resources.getDimension(R.dimen.stpi_default_circle_radius);
        float defaultCircleStrokeWidth = resources.getDimension(R.dimen.stpi_default_circle_stroke_width);

        //noinspection UnnecessaryLocalVariable
        int defaultIndicatorColor = defaultPrimaryColor;
        float defaultIndicatorRadius = resources.getDimension(R.dimen.stpi_default_indicator_radius);

        float defaultLineStrokeWidth = resources.getDimension(R.dimen.stpi_default_line_stroke_width);
        float defaultLineMargin = resources.getDimension(R.dimen.stpi_default_line_margin);
        int defaultLineColor = ContextCompat.getColor(context, R.color.stpi_default_line_color);
        int defaultLineDoneColor = defaultPrimaryColor;

        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.StepperIndicator, defStyleAttr, 0);

        circlePaint = new Paint();
        circlePaint.setStrokeWidth(
                typedArray.getDimension(R.styleable.StepperIndicator_stpi_circleStrokeWidth, defaultCircleStrokeWidth));
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setColor(typedArray.getColor(R.styleable.StepperIndicator_stpi_circleColor, defaultCircleColor));
        circlePaint.setAntiAlias(true);

        setStepCount(typedArray.getInteger(R.styleable.StepperIndicator_stpi_stepCount, 2));

        final int stepsCircleColorsResId = typedArray.getResourceId(R.styleable.StepperIndicator_stpi_stepsCircleColors, 0);
        if (stepsCircleColorsResId != 0) {
            stepsCirclePaintList = new ArrayList<>(stepCount);

            for (int i = 0; i < stepCount; i++) {
                Paint circlePaint = new Paint(this.circlePaint);
                if (isInEditMode()) {
                    circlePaint.setColor(getRandomColor());
                } else {
                    TypedArray colorResValues = context.getResources().obtainTypedArray(stepsCircleColorsResId);

                    if (stepCount > colorResValues.length()) {
                        throw new IllegalArgumentException(
                                "Invalid number of colors for the circles. Please provide a list " +
                                        "of colors with as many items as the number of steps required!");
                    }

                    circlePaint.setColor(colorResValues.getColor(i, 0));
                    colorResValues.recycle();
                }

                stepsCirclePaintList.add(circlePaint);
            }
        }

        indicatorPaint = new Paint(circlePaint);
        indicatorPaint.setStyle(Paint.Style.FILL);
        indicatorPaint.setColor(typedArray.getColor(R.styleable.StepperIndicator_stpi_indicatorColor, defaultIndicatorColor));
        indicatorPaint.setAntiAlias(true);

        stepTextNumberPaint = new Paint(indicatorPaint);
        stepTextNumberPaint.setTextSize(getResources().getDimension(R.dimen.stpi_default_text_size));

        showStepTextNumber = typedArray.getBoolean(R.styleable.StepperIndicator_stpi_showStepNumberInstead, false);

        final int stepsIndicatorColorsResId = typedArray
                .getResourceId(R.styleable.StepperIndicator_stpi_stepsIndicatorColors, 0);
        if (stepsIndicatorColorsResId != 0) {
            stepsIndicatorPaintList = new ArrayList<>(stepCount);
            if (showStepTextNumber) {
                stepsTextNumberPaintList = new ArrayList<>(stepCount);
            }

            for (int i = 0; i < stepCount; i++) {
                Paint indicatorPaint = new Paint(this.indicatorPaint);

                Paint textNumberPaint = showStepTextNumber ? new Paint(stepTextNumberPaint) : null;
                if (isInEditMode()) {
                    indicatorPaint.setColor(getRandomColor());
                    if (null != textNumberPaint) {
                        textNumberPaint.setColor(indicatorPaint.getColor());
                    }
                } else {
                    TypedArray colorResValues = context.getResources().obtainTypedArray(stepsIndicatorColorsResId);

                    if (stepCount > colorResValues.length()) {
                        throw new IllegalArgumentException(
                                "Invalid number of colors for the indicators. Please provide a list " +
                                        "of colors with as many items as the number of steps required!");
                    }

                    indicatorPaint.setColor(colorResValues.getColor(i, 0)); // specific color
                    if (null != textNumberPaint) {
                        textNumberPaint.setColor(indicatorPaint.getColor());
                    }
                    colorResValues.recycle();
                }

                stepsIndicatorPaintList.add(indicatorPaint);
                if (showStepTextNumber && null != textNumberPaint) {
                    stepsTextNumberPaintList.add(textNumberPaint);
                }
            }
        }

        linePaint = new Paint();
        linePaint.setStrokeWidth(
                typedArray.getDimension(R.styleable.StepperIndicator_stpi_lineStrokeWidth, defaultLineStrokeWidth));
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(typedArray.getColor(R.styleable.StepperIndicator_stpi_lineColor, defaultLineColor));
        linePaint.setAntiAlias(true);

        lineDonePaint = new Paint(linePaint);
        lineDonePaint.setColor(typedArray.getColor(R.styleable.StepperIndicator_stpi_lineDoneColor, defaultLineDoneColor));

        lineDoneAnimatedPaint = new Paint(lineDonePaint);
        useBottomIndicator = typedArray.getBoolean(R.styleable.StepperIndicator_stpi_useBottomIndicator, false);

        float defaultHeight = resources.getDimension(R.dimen.stpi_default_bottom_indicator_height);
        bottomIndicatorHeight = typedArray
                .getDimension(R.styleable.StepperIndicator_stpi_bottomIndicatorHeight, defaultHeight);

        if (bottomIndicatorHeight <= 0) {
            Log.d(TAG, "init: Invalid indicator height, disabling bottom indicator feature! Please provide " +
                    "a value greater than 0.");
            useBottomIndicator = false;
        }
        float defaultWidth = resources.getDimension(R.dimen.stpi_default_bottom_indicator_width);
        bottomIndicatorWidth = typedArray.getDimension(R.styleable.StepperIndicator_stpi_bottomIndicatorWidth, defaultWidth);

        float defaultTopMargin = resources.getDimension(R.dimen.stpi_default_bottom_indicator_margin_top);
        bottomIndicatorMarginTop = typedArray
                .getDimension(R.styleable.StepperIndicator_stpi_bottomIndicatorMarginTop, defaultTopMargin);

        useBottomIndicatorWithStepColors = typedArray
                .getBoolean(R.styleable.StepperIndicator_stpi_useBottomIndicatorWithStepColors, false);

        circleRadius = typedArray.getDimension(R.styleable.StepperIndicator_stpi_circleRadius, defaultCircleRadius);
        checkRadius = circleRadius + circlePaint.getStrokeWidth() / 2f;
        indicatorRadius = typedArray.getDimension(R.styleable.StepperIndicator_stpi_indicatorRadius, defaultIndicatorRadius);
        animIndicatorRadius = indicatorRadius;
        animCheckRadius = checkRadius;
        lineMargin = typedArray.getDimension(R.styleable.StepperIndicator_stpi_lineMargin, defaultLineMargin);

        animDuration = typedArray.getInteger(R.styleable.StepperIndicator_stpi_animDuration, DEFAULT_ANIMATION_DURATION);
        showDoneIcon = typedArray.getBoolean(R.styleable.StepperIndicator_stpi_showDoneIcon, true);
        doneIcon = typedArray.getDrawable(R.styleable.StepperIndicator_stpi_doneIconDrawable);

        labelPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setTextAlign(Paint.Align.CENTER);

        float defaultLabelSize = resources.getDimension(R.dimen.stpi_default_label_size);
        float labelSize = typedArray.getDimension(R.styleable.StepperIndicator_stpi_labelSize, defaultLabelSize);
        labelPaint.setTextSize(labelSize);

        float defaultLabelMarginTop = resources.getDimension(R.dimen.stpi_default_label_margin_top);
        labelMarginTop = typedArray.getDimension(R.styleable.StepperIndicator_stpi_labelMarginTop, defaultLabelMarginTop);

        showLabels(typedArray.getBoolean(R.styleable.StepperIndicator_stpi_showLabels, false));
        setLabels(typedArray.getTextArray(R.styleable.StepperIndicator_stpi_labels));

        if (typedArray.hasValue(R.styleable.StepperIndicator_stpi_labelColor)) {
            setLabelColor(typedArray.getColor(R.styleable.StepperIndicator_stpi_labelColor, 0));
        } else {
            setLabelColor(getTextColorSecondary(getContext()));
        }

        if (isInEditMode() && showLabels && labels == null) {
            labels = new CharSequence[]{"First", "Second", "Third", "Fourth", "Fifth"};
        }

        if (!typedArray.hasValue(R.styleable.StepperIndicator_stpi_stepCount) && labels != null) {
            setStepCount(labels.length);
        }

        typedArray.recycle();

        if (showDoneIcon && doneIcon == null) {
            doneIcon = ContextCompat.getDrawable(context, R.drawable.ic_done_white_18dp);
        }
        if (doneIcon != null) {
            int size = getContext().getResources().getDimensionPixelSize(R.dimen.stpi_done_icon_size);
            doneIcon.setBounds(0, 0, size, size);
        }

        if (isInEditMode()) {
            currentStep = Math.max((int) Math.ceil(stepCount / 2f), 1);
        }

        gestureDetector = new GestureDetector(getContext(), gestureListener);
    }


    private Paint getRandomPaint() {
        Paint paint = new Paint(indicatorPaint);
        paint.setColor(getRandomColor());

        return paint;
    }

    private int getRandomColor() {
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        compute();
    }

    private void compute() {
        if (null == circlePaint) {
            throw new IllegalArgumentException("circlePaint is invalid! Make sure you setup the field circlePaint " +
                    "before calling compute() method!");
        }

        indicators = new float[stepCount];
        linePathList.clear();

        float startX = circleRadius * EXPAND_MARK + circlePaint.getStrokeWidth() / 2f;
        if (useBottomIndicator) {
            startX = bottomIndicatorWidth / 2F;
        }
        if (showLabels) {
            int gridWidth = getMeasuredWidth() / stepCount;
            startX = gridWidth / 2F;
        }


        float divider = (getMeasuredWidth() - startX * 2f) / (stepCount - 1);
        lineLength = divider - (circleRadius * 2f + circlePaint.getStrokeWidth()) - (lineMargin * 2);

        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = startX + divider * i;
        }
        for (int i = 0; i < indicators.length - 1; i++) {
            float position = ((indicators[i] + indicators[i + 1]) / 2) - lineLength / 2;
            final Path linePath = new Path();
            float lineY = getStepCenterY();
            linePath.moveTo(position, lineY);
            linePath.lineTo(position + lineLength, lineY);
            linePathList.add(linePath);
        }

        computeStepsClickAreas();
    }

    public void computeStepsClickAreas() {
        if (stepCount == STEP_INVALID) {
            throw new IllegalArgumentException("stepCount wasn't setup yet. Make sure you call setStepCount() " +
                    "before computing the steps click area!");
        }

        if (null == indicators) {
            throw new IllegalArgumentException("indicators wasn't setup yet. Make sure the indicators are " +
                    "initialized and setup correctly before trying to compute the click " +
                    "area for each step!");
        }

        stepsClickAreas = new ArrayList<>(stepCount);

        for (float indicator : indicators) {

            float left = indicator - circleRadius * 2;
            float right = indicator + circleRadius * 2;
            float top = getStepCenterY() - circleRadius * 2;
            float bottom = getStepCenterY() + circleRadius + getBottomIndicatorHeight();

            RectF area = new RectF(left, top, right, bottom);
            stepsClickAreas.add(area);
        }
    }


    private int getBottomIndicatorHeight() {
        if (useBottomIndicator) {
            return (int) (bottomIndicatorHeight + bottomIndicatorMarginTop);
        } else {
            return 0;
        }
    }

    private float getMaxLabelHeight() {
        return showLabels ? maxLabelHeight + labelMarginTop : 0;
    }

    private void calculateMaxLabelHeight(final int measuredWidth) {
        if (!showLabels) return;

        int twoDp = getContext().getResources().getDimensionPixelSize(R.dimen.stpi_two_dp);
        int gridWidth = measuredWidth / stepCount - twoDp;

        if (gridWidth <= 0) return;

        labelLayouts = new StaticLayout[labels.length];
        maxLabelHeight = 0F;
        float labelSingleLineHeight = labelPaint.descent() - labelPaint.ascent();
        for (int i = 0; i < labels.length; i++) {
            if (labels[i] == null) continue;

            labelLayouts[i] = new StaticLayout(labels[i], labelPaint, gridWidth,
                    Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
            maxLabelHeight = Math.max(maxLabelHeight, labelLayouts[i].getLineCount() * labelSingleLineHeight);
        }
    }

    private float getStepCenterY() {
        return (getMeasuredHeight() - getBottomIndicatorHeight() - getMaxLabelHeight()) / 2f;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        float centerY = getStepCenterY();

        boolean inAnimation = animatorSet != null && animatorSet.isRunning();
        boolean inLineAnimation = lineAnimator != null && lineAnimator.isRunning();
        boolean inIndicatorAnimation = indicatorAnimator != null && indicatorAnimator.isRunning();
        boolean inCheckAnimation = checkAnimator != null && checkAnimator.isRunning();

        boolean drawToNext = previousStep == currentStep - 1;
        boolean drawFromNext = previousStep == currentStep + 1;

        for (int i = 0; i < indicators.length; i++) {
            final float indicator = indicators[i];

            boolean drawCheck = i < currentStep || (drawFromNext && i == currentStep);

            canvas.drawCircle(indicator, centerY, circleRadius, getStepCirclePaint(i));

            if (showStepTextNumber) {
                final String stepLabel = String.valueOf(i + 1);

                stepAreaRect.set((int) (indicator - circleRadius), (int) (centerY - circleRadius),
                        (int) (indicator + circleRadius), (int) (centerY + circleRadius));
                stepAreaRectF.set(stepAreaRect);

                Paint stepTextNumberPaint = getStepTextNumberPaint(i);

                stepAreaRectF.right = stepTextNumberPaint.measureText(stepLabel, 0, stepLabel.length());
                stepAreaRectF.bottom = stepTextNumberPaint.descent() - stepTextNumberPaint.ascent();

                stepAreaRectF.left += (stepAreaRect.width() - stepAreaRectF.right) / 2.0f;
                stepAreaRectF.top += (stepAreaRect.height() - stepAreaRectF.bottom) / 2.0f;

                canvas.drawText(stepLabel, stepAreaRectF.left, stepAreaRectF.top - stepTextNumberPaint.ascent(),
                        stepTextNumberPaint);
            }

            if (showLabels && labelLayouts != null &&
                    i < labelLayouts.length && labelLayouts[i] != null) {
                drawLayout(labelLayouts[i],
                        indicator, getHeight() - getBottomIndicatorHeight() - maxLabelHeight,
                        canvas, labelPaint);
            }

            if (useBottomIndicator) {
                if (i == currentStep) {
                    canvas.drawRect(indicator - bottomIndicatorWidth / 2, getHeight() - bottomIndicatorHeight,
                            indicator + bottomIndicatorWidth / 2, getHeight(),
                            useBottomIndicatorWithStepColors ? getStepIndicatorPaint(i) : indicatorPaint);
                }
            } else {
                if ((i == currentStep && !drawFromNext) || (i == previousStep && drawFromNext && inAnimation)) {
                    canvas.drawCircle(indicator, centerY, animIndicatorRadius, getStepIndicatorPaint(i));
                }
            }

            if (drawCheck) {
                float radius = checkRadius;
                if ((i == previousStep && drawToNext) || (i == currentStep && drawFromNext)) radius = animCheckRadius;
                canvas.drawCircle(indicator, centerY, radius, getStepIndicatorPaint(i));

                if (!isInEditMode() && showDoneIcon) {
                    if ((i != previousStep && i != currentStep) ||
                            (!inCheckAnimation && !(i == currentStep && !inAnimation))) {
                        canvas.save();
                        canvas.translate(indicator - (doneIcon.getIntrinsicWidth() / 2),
                                centerY - (doneIcon.getIntrinsicHeight() / 2));
                        doneIcon.draw(canvas);
                        canvas.restore();
                    }
                }
            }

            if (i < linePathList.size()) {
                if (i >= currentStep) {
                    canvas.drawPath(linePathList.get(i), linePaint);
                    if (i == currentStep && drawFromNext && (inLineAnimation || inIndicatorAnimation)) {
                        canvas.drawPath(linePathList.get(i), lineDoneAnimatedPaint);
                    }
                } else {
                    if (i == currentStep - 1 && drawToNext && inLineAnimation) {
                        canvas.drawPath(linePathList.get(i), linePaint);
                        canvas.drawPath(linePathList.get(i), lineDoneAnimatedPaint);
                    } else {
                        canvas.drawPath(linePathList.get(i), lineDonePaint);
                    }
                }
            }
        }
    }


    public static void drawLayout(Layout layout, float x, float y,
                                  Canvas canvas, TextPaint paint) {
        canvas.save();
        canvas.translate(x, y);
        layout.draw(canvas);
        canvas.restore();
    }


    private Paint getStepIndicatorPaint(final int stepPosition) {
        return getPaint(stepPosition, stepsIndicatorPaintList, indicatorPaint);
    }


    private Paint getStepTextNumberPaint(final int stepPosition) {
        return getPaint(stepPosition, stepsTextNumberPaintList, stepTextNumberPaint);
    }


    private Paint getStepCirclePaint(final int stepPosition) {
        return getPaint(stepPosition, stepsCirclePaintList, circlePaint);
    }


    private Paint getPaint(final int stepPosition, final List<Paint> sourceList, final Paint defaultPaint) {
        isStepValid(stepPosition);

        Paint paint = null;
        if (null != sourceList && !sourceList.isEmpty()) {
            try {
                paint = sourceList.get(stepPosition);
            } catch (IndexOutOfBoundsException e) {
                Log.d(TAG, "getPaint: could not find the specific step paint to use! Try to use default instead!");
            }
        }

        if (null == paint && null != defaultPaint) {
            paint = defaultPaint;
        }

        if (null == paint) {
            Log.d(TAG, "getPaint: could not use default paint for the specific step! Using random Paint instead!");
            paint = getRandomPaint();
        }

        return paint;
    }

    private boolean isStepValid(final int stepPos) {
        if (stepPos < 0 || stepPos > stepCount - 1) {
            throw new IllegalArgumentException("Invalid step position. " + stepPos + " is not a valid position! it " +
                    "should be between 0 and stepCount(" + stepCount + ")");
        }

        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int width = widthMode == MeasureSpec.EXACTLY ? widthSize : getSuggestedMinimumWidth();

        calculateMaxLabelHeight(width);

        int desiredHeight = (int) Math.ceil(
                (circleRadius * EXPAND_MARK * 2) +
                        circlePaint.getStrokeWidth() +
                        getBottomIndicatorHeight() +
                        getMaxLabelHeight()
        );

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int height = heightMode == MeasureSpec.EXACTLY ? heightSize : desiredHeight;

        setMeasuredDimension(width, height);
    }

    @SuppressWarnings("unused")
    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        if (stepCount < 2) {
            throw new IllegalArgumentException("stepCount must be >= 2");
        }

        this.stepCount = stepCount;
        currentStep = 0;
        compute();
        invalidate();
    }

    @SuppressWarnings("unused")
    public int getCurrentStep() {
        return currentStep;
    }

    @UiThread
    public void setCurrentStep(int currentStep) {
        if (currentStep < 0 || currentStep > stepCount) {
            throw new IllegalArgumentException("Invalid step value " + currentStep);
        }

        previousStep = this.currentStep;
        this.currentStep = currentStep;

        if (animatorSet != null) {
            animatorSet.cancel();
        }

        animatorSet = null;
        lineAnimator = null;
        indicatorAnimator = null;


        if (currentStep == previousStep + 1) {
            animatorSet = new AnimatorSet();
            lineAnimator = ObjectAnimator.ofFloat(StepperIndicator.this, "animProgress", 1.0f, 0.0f);
            checkAnimator = ObjectAnimator.ofFloat(StepperIndicator.this, "animCheckRadius", indicatorRadius,
                    checkRadius * EXPAND_MARK, checkRadius);

            animIndicatorRadius = 0;
            indicatorAnimator = ObjectAnimator.ofFloat(StepperIndicator.this, "animIndicatorRadius", 0f,
                    indicatorRadius * 1.4f, indicatorRadius);

            animatorSet.play(lineAnimator).with(checkAnimator).before(indicatorAnimator);
        } else if (currentStep == previousStep - 1) {
            animatorSet = new AnimatorSet();

            indicatorAnimator = ObjectAnimator
                    .ofFloat(StepperIndicator.this, "animIndicatorRadius", indicatorRadius, 0f);

            animProgress = 1.0f;
            lineDoneAnimatedPaint.setPathEffect(null);
            lineAnimator = ObjectAnimator.ofFloat(StepperIndicator.this, "animProgress", 0.0f, 1.0f);

            animCheckRadius = checkRadius;
            checkAnimator = ObjectAnimator
                    .ofFloat(StepperIndicator.this, "animCheckRadius", checkRadius, indicatorRadius);

            animatorSet.playSequentially(indicatorAnimator, lineAnimator, checkAnimator);
        }

        if (animatorSet != null) {
            lineAnimator.setDuration(Math.min(500, animDuration));
            lineAnimator.setInterpolator(new DecelerateInterpolator());
            indicatorAnimator.setDuration(lineAnimator.getDuration() / 2);
            checkAnimator.setDuration(lineAnimator.getDuration() / 2);

            animatorSet.start();
        }

        invalidate();
    }


    @SuppressWarnings("unused")
    public void setAnimProgress(float animProgress) {
        this.animProgress = animProgress;
        lineDoneAnimatedPaint.setPathEffect(createPathEffect(lineLength, animProgress, 0.0f));
        invalidate();
    }

    @SuppressWarnings("unused")
    public void setAnimIndicatorRadius(float animIndicatorRadius) {
        this.animIndicatorRadius = animIndicatorRadius;
        invalidate();
    }

    @SuppressWarnings("unused")
    public void setAnimCheckRadius(float animCheckRadius) {
        this.animCheckRadius = animCheckRadius;
        invalidate();
    }


    @SuppressWarnings("unused")
    public void setViewPager(ViewPager pager) {
        if (pager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        setViewPager(pager, pager.getAdapter().getCount());
    }

    public void setViewPager(ViewPager pager, boolean keepLastPage) {
        if (pager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        setViewPager(pager, pager.getAdapter().getCount() - (keepLastPage ? 1 : 0));
    }


    public void setViewPager(ViewPager pager, int stepCount) {
        if (this.pager == pager) {
            return;
        }
        if (this.pager != null) {
            pager.removeOnPageChangeListener(this);
        }
        if (pager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }

        this.pager = pager;
        this.stepCount = stepCount;
        currentStep = 0;
        pager.addOnPageChangeListener(this);

        if (showLabels && labels == null) {
            setLabelsUsingPageTitles();
        }

        requestLayout();
        invalidate();
    }

    private void setLabelsUsingPageTitles() {
        PagerAdapter pagerAdapter = pager.getAdapter();
        int pagerCount = pagerAdapter.getCount();
        labels = new CharSequence[pagerCount];
        for (int i = 0; i < pagerCount; i++) {
            labels[i] = pagerAdapter.getPageTitle(i);
        }
    }

    public void setLabels(CharSequence[] labelsArray) {
        if (labelsArray == null) {
            labels = null;
            return;
        }
        if (stepCount > labelsArray.length) {
            throw new IllegalArgumentException(
                    "Invalid number of labels for the indicators. Please provide a list " +
                            "of labels with at least as many items as the number of steps required!");
        }
        labels = labelsArray;
        showLabels(true);
    }

    public void setLabelColor(int color) {
        labelPaint.setColor(color);
        requestLayout();
        invalidate();
    }

    public void setLabelSize(float textSize) {
        labelPaint.setTextSize(textSize);
        requestLayout();
        invalidate();
    }

    public void setIndicatorColor(int indicatorColor) {
        indicatorPaint.setColor(indicatorColor);
        stepTextNumberPaint.setColor(indicatorColor);
        requestLayout();
        invalidate();
    }

    public void setLineColor(int lineColor) {
        linePaint.setColor(lineColor);
        requestLayout();
        invalidate();
    }

    public void setLineDoneColor(int lineDoneColor) {
        lineDonePaint.setColor(lineDoneColor);
        lineDoneAnimatedPaint.setColor(lineDoneColor);
        requestLayout();
        invalidate();
    }

    public void useBottomIndicator(boolean useBottomIndicator) {
        this.useBottomIndicator = useBottomIndicator;
        requestLayout();
        invalidate();
    }

    public void showLabels(boolean show) {
        showLabels = show;
        requestLayout();
        invalidate();
    }

    public void showStepNumberInstead(boolean showStepNumberInstead) {
        this.showStepTextNumber = showStepNumberInstead;
        requestLayout();
        invalidate();
    }

    public void addOnStepClickListener(OnStepClickListener listener) {
        onStepClickListeners.add(listener);
    }

    @SuppressWarnings("unused")
    public void removeOnStepClickListener(OnStepClickListener listener) {
        onStepClickListeners.remove(listener);
    }

    @SuppressWarnings("unused")
    public void clearOnStepClickListeners() {
        onStepClickListeners.clear();
    }

    public boolean isOnStepClickListenerAvailable() {
        return null != onStepClickListeners && !onStepClickListeners.isEmpty();
    }

    public void setDoneIcon(@Nullable Drawable doneIcon) {
        this.doneIcon = doneIcon;
        if (doneIcon != null) {
            showDoneIcon = true;
            int size = getContext().getResources().getDimensionPixelSize(R.dimen.stpi_done_icon_size);
            doneIcon.setBounds(0, 0, size, size);
        }
        invalidate();
    }

    public void setShowDoneIcon(boolean showDoneIcon) {
        this.showDoneIcon = showDoneIcon;
        invalidate();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        setCurrentStep(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        currentStep = savedState.mCurrentStep;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.mCurrentStep = currentStep;
        return savedState;
    }

    public interface OnStepClickListener {

        void onStepClicked(int step);
    }
    private static class SavedState extends BaseSavedState {

        @SuppressWarnings("UnusedDeclaration")
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        private int mCurrentStep;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            mCurrentStep = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mCurrentStep);
        }
    }
}
