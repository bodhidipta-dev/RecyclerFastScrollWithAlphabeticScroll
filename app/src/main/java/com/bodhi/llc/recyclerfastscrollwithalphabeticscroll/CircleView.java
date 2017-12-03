package com.bodhi.llc.recyclerfastscrollwithalphabeticscroll;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.WeakHashMap;


/**
 * Created by bodhidiptab on 27/11/17.
 * This View is Done ON SAMSUNG J7 PRO
 */

public class CircleView extends AppCompatImageView {
    private Path path;
    private Context mcontext;
    int totalScreenHeight, totalScreenWidth;
    private static int circle1Size = 800;
    private static int circle2Size = 1400;
    private static int circle3Size = 2000;
    private static int circle4Size = 2600;


    private String GTAG = "@Gesture";
    private Canvas mCanvaas;
    private Bitmap currentBitmap = null;
    private GestureDetectorCompat mGestureDetector;

    private int circle1Count = 0;
    private int circle2Count = 0;
    private int circle3Count = 0;
    private int circle4Count = 0;
    private int circle5Count = 0;

    private int selectedColor = -1;
    Resources resources;
    private WeakHashMap<String, Bitmap> markerBitmap = new WeakHashMap();
    private WeakHashMap<String, PointF> markerPositions = new WeakHashMap();

    private OnMarkerClickListener onMarkerClickListener = null;
    private String preferdDistance = "km";

    private SCALE maxZoomOutLevel = SCALE.ONE;

    public enum SCALE {
        ONE, TWO, THREE, FOUR, FIVE;
    }

    private enum DISTANCE_UNIT {
        KILOMETER("km"), MILES("ml");
        private final String preferedUnit;

        DISTANCE_UNIT(String preferedUnit) {
            this.preferedUnit = preferedUnit;
        }

        public String getPreferredUnit() {
            return preferedUnit;
        }
    }

    private boolean isviewMeasured = false;

    private SCALE currentZoom = SCALE.ONE;

    private int colorC1 = Color.parseColor("#083050"), colorC2 = Color.parseColor("#44779F"), colorC3 = Color.parseColor("#648DAD"), colorC4 = Color.parseColor("#95B1C5"), colorC5 = Color.parseColor("#FFBACAD6"), colorSel = Color.parseColor("#FFD69D23");

    public CircleView(Context context) {
        super(context);
        mcontext = context;
        init(context, null);
    }

    public CircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mcontext = context;
        init(context, attrs);
    }

    public CircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mcontext = context;
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
//        if (attrs != null && context != null) {
//            TypedArray a = context.getTheme().obtainStyledAttributes(
//                    attrs,
//                    R.styleable.CircleView,
//                    0, 0
//            );
//
//
//            colorC1 = a.getColor(R.styleable.CircleView_circle_one_color, Color.parseColor("#083050"));
//            colorC2 = a.getColor(R.styleable.CircleView_circle_one_color, Color.parseColor("#44779F"));
//            colorC3 = a.getColor(R.styleable.CircleView_circle_one_color, Color.parseColor("#648DAD"));
//            colorC4 = a.getColor(R.styleable.CircleView_circle_one_color, Color.parseColor("#95B1C5"));
//
//
////            preferdDistance = a.getInteger(R.styleable.CircleView_preferred_distance, 0) == 0 ? "km" : "ml";
//            preferdDistance = AppUtils.getDisplayTextForUnitOfMeasure(YPOApplication.getInstance().getPreferenceHelper());
//
//        }

        resources = context.getResources();
        final ViewConfiguration viewConfig = ViewConfiguration.get(context);
        mViewScaledTouchSlop = viewConfig.getScaledTouchSlop();
        mGestureDetector = new GestureDetectorCompat(context, mGestureListener);
        try {
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            Point size = new Point();
            Display display = wm.getDefaultDisplay();
            display.getSize(size);
            Log.i("@ScreenWi", "" + totalScreenWidth + " / " + totalScreenHeight);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Putting marker with distance, represent positions as marker
        // circle like 0 position for circle 1, 1 position foe circle 2
        markerBitmap.put("circle1", getMarkerView(circle1Count)); //Circle 1
        markerBitmap.put("circle2", getMarkerView(circle2Count)); //Circle 2
        markerBitmap.put("circle3", getMarkerView(circle3Count)); //Circle 3
        markerBitmap.put("circle4", getMarkerView(circle4Count)); //Circle 4
        markerBitmap.put("circle5", getMarkerView(circle5Count)); //Out side

        setDrawingCacheEnabled(false);


        Log.i("@searchGraph", "INIT VALUE  " + currentZoom + " max zoom " + maxZoomOutLevel);

    }


    public void changeDistanceUnit(@NonNull DISTANCE_UNIT unit) {
        preferdDistance = unit.getPreferredUnit();
        invalidate();
    }

    public void setOnMarkerClickListener(OnMarkerClickListener onMarkerClickCallback) {
        onMarkerClickListener = onMarkerClickCallback;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        totalScreenWidth = MeasureSpec.getSize(widthMeasureSpec);
//        totalScreenHeight = MeasureSpec.getSize(heightMeasureSpec);
//        setMeasuredDimension(totalScreenWidth, totalScreenHeight);

        // Try for a width based on our minimum
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        totalScreenWidth = resolveSizeAndState(minw, widthMeasureSpec, 1);

        // Whatever the width ends up being, ask for a height that would let the pie
        // get as big as it can
        int minh = MeasureSpec.getSize(totalScreenWidth) - getPaddingBottom() + getPaddingTop();
        totalScreenHeight = resolveSizeAndState(MeasureSpec.getSize(totalScreenWidth) - minh, heightMeasureSpec, 0);

        setMeasuredDimension(totalScreenWidth, totalScreenHeight);


        // Set the property on zoom 1
        circle1Size = mcontext.getResources().getDimensionPixelSize(R.dimen.circle_1_size);
        circle2Size = mcontext.getResources().getDimensionPixelSize(R.dimen.circle_2_size);
        circle3Size = mcontext.getResources().getDimensionPixelSize(R.dimen.circle_3_size);
        circle4Size = mcontext.getResources().getDimensionPixelSize(R.dimen.circle_4_size);


        switch (100) {
            case 5: {
                maxZoomOutLevel = SCALE.FIVE;
                currentZoom = SCALE.FIVE;
                calculateCircles(SCALE.FIVE);
            }
            break;
            case 25: {
                maxZoomOutLevel = SCALE.FOUR;
                currentZoom = SCALE.FOUR;
                calculateCircles(SCALE.FOUR);
            }
            break;
            case 50: {
                maxZoomOutLevel = SCALE.THREE;
                currentZoom = SCALE.THREE;
                calculateCircles(SCALE.THREE);
            }
            break;
            case 75: {
                maxZoomOutLevel = SCALE.TWO;
                currentZoom = SCALE.TWO;
                calculateCircles(SCALE.TWO);
            }
            break;
            case 100: {
                maxZoomOutLevel = SCALE.ONE;
                currentZoom = SCALE.ONE;
                calculateCircles(SCALE.ONE);
            }
            break;
            default: {
                maxZoomOutLevel = SCALE.ONE;
                currentZoom = SCALE.ONE;
                calculateCircles(SCALE.ONE);
            }

        }

        isviewMeasured = true;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.i("@ScreenWi", "" + totalScreenWidth + " / " + totalScreenHeight);

    }

    private void calculateCircles(SCALE scale) {

        switch (scale.ordinal()) {
            case 0: {
                circle1Size = mcontext.getResources().getDimensionPixelSize(R.dimen.circle_1_size);
                circle2Size = mcontext.getResources().getDimensionPixelSize(R.dimen.circle_2_size);
                circle3Size = mcontext.getResources().getDimensionPixelSize(R.dimen.circle_3_size);
                circle4Size = mcontext.getResources().getDimensionPixelSize(R.dimen.circle_4_size);
            }
            break;
            case 1: {
                circle1Size = mcontext.getResources().getDimensionPixelSize(R.dimen.circle_1_size_zoom2);
                circle2Size = mcontext.getResources().getDimensionPixelSize(R.dimen.circle_2_size_zoom2);
                circle3Size = mcontext.getResources().getDimensionPixelSize(R.dimen.circle_3_size_zoom2);
            }
            break;
            case 2: {
                circle1Size = mcontext.getResources().getDimensionPixelSize(R.dimen.circle_1_size_zoom3);
                circle2Size = mcontext.getResources().getDimensionPixelSize(R.dimen.circle_2_size_zoom3);
            }
            break;
            case 3:
                circle1Size = mcontext.getResources().getDimensionPixelSize(R.dimen.circle_1_size_zoom4);
                circle2Size = mcontext.getResources().getDimensionPixelSize(R.dimen.circle_2_size_zoom4);
                circle3Size = mcontext.getResources().getDimensionPixelSize(R.dimen.circle_3_size_zoom4);
                break;
            case 4:
                circle1Size = mcontext.getResources().getDimensionPixelSize(R.dimen.circle_1_size_zoom5);
                break;
        }

    }

    private boolean isLoadComplete = false;
    private OnGraphLoadedCallback onGraphLoadedCallback = null;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Log.i("@drawCircleCond", " onDraw ");
        mCanvaas = canvas;

        drawArc();
    }


    private void drawArc() {

        if (!isviewMeasured)
            return;

        if (currentBitmap != null)
            currentBitmap.recycle();

        currentBitmap = Bitmap.createBitmap(mCanvaas.getWidth(), mCanvaas.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(currentBitmap);

        //Draw Four Circles here

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);


        RectF rectf;
        int xPosition;
        int yPosition;


        /**
         * **********    New Circle draw LOGIC
         * There are particular six zoom levels
         */

        // First we need to paint the canvas according

        /**
         * *****************************************
         */
        if (selectedColor != -1 && selectedColor == colorC5)
            canvas.drawColor(colorSel);
        else
            canvas.drawColor(colorC5);

        Log.i("@searchGraph", "on graph got current zoom " + currentZoom);

        if (currentZoom.ordinal() == 0) {
            //Coloring the canvas with envirnment color
            canvas.drawBitmap(markerBitmap.get("circle5"), toPixels(convertPixelsToDp(((totalScreenWidth / 2) - 30))), toPixels((circle4Size - 10)), paint); // Marker for outside
            markerPositions.put("circle5", new PointF(toPixels(convertPixelsToDp(((totalScreenWidth / 2) - 30))), toPixels((circle4Size - 10))));

        }


        Log.i("@ZoomOrdinal", "" + currentZoom.ordinal());

        //check if circle 4 is eligible to drawn

        if (currentZoom.ordinal() > 1) {
            //The Circle Size is more than we want
            //There fore we are painting the canvas with Circle 4 Color

            if (selectedColor != -1 && selectedColor == colorC4)
                canvas.drawColor(colorSel);
            else
                canvas.drawColor(colorC4);

        } else {
            //Its ok to draw the circle 4 along with environment color

            if (currentZoom.ordinal() == 1) {

                if (selectedColor != -1 && selectedColor == colorC4)
                    canvas.drawColor(colorSel);
                else
                    canvas.drawColor(colorC4);

            } else {
                //Check the section is selected or not
                if (selectedColor != -1 && selectedColor == colorC4)
                    paint.setColor(colorSel);
                else
                    paint.setColor(colorC4);


                //Drawing the circle 4
                xPosition = totalScreenWidth / 2;
                yPosition = totalScreenHeight;
                xPosition = xPosition - toPixels(circle4Size / 2);
                yPosition = yPosition - toPixels(circle4Size / 2);
                rectf = new RectF(xPosition, yPosition, xPosition + toPixels(circle4Size), yPosition + toPixels(circle4Size));
                canvas.drawArc(rectf, 180, 180, true, paint);

            }

            canvas.drawBitmap(markerBitmap.get("circle4"), toPixels(convertPixelsToDp(((totalScreenWidth / 2) - 30))), toPixels(convertPixelsToDp((totalScreenHeight - toPixels((circle4Size / 2))))), paint); //For circle 4
            markerPositions.put("circle4", new PointF(toPixels(convertPixelsToDp(((totalScreenWidth / 2) - 30))), toPixels(convertPixelsToDp((totalScreenHeight - toPixels((circle4Size / 2)))))));
        }


        //check if circle 3 is elegible to be drawn
        if (currentZoom.ordinal() > 2) {
            //Circle 3 size is greater than what is needed
            //Coloring environment with Circle3 color

            if (selectedColor != -1 && selectedColor == colorC3)
                canvas.drawColor(colorSel);
            else
                canvas.drawColor(colorC3);

        } else {

            if (currentZoom.ordinal() == 2) {

                if (selectedColor != -1 && selectedColor == colorC3)
                    canvas.drawColor(colorSel);
                else
                    canvas.drawColor(colorC3);

            } else {

                //Check if the circle 3 is selected
                if (selectedColor != -1 && selectedColor == colorC3)
                    paint.setColor(colorSel);
                else
                    paint.setColor(colorC3);

                //Drawing the Circle
                xPosition = totalScreenWidth / 2;
                yPosition = totalScreenHeight;
                xPosition = xPosition - toPixels(circle3Size / 2);
                yPosition = yPosition - toPixels(circle3Size / 2);
                rectf = new RectF(xPosition, yPosition, xPosition + toPixels(circle3Size), yPosition + toPixels(circle3Size));
                canvas.drawArc(rectf, 180, 180, true, paint);
            }


            canvas.drawBitmap(markerBitmap.get("circle3"), toPixels(convertPixelsToDp(((totalScreenWidth / 2) - 30))), toPixels(convertPixelsToDp(totalScreenHeight - toPixels(circle3Size / 2))), paint); //marker For circle 3
            markerPositions.put("circle3", new PointF(toPixels(convertPixelsToDp(((totalScreenWidth / 2) - 30))), toPixels(convertPixelsToDp(totalScreenHeight - toPixels(circle3Size / 2)))));
        }


        //Check if circle 2 eligible to be drawn
        if (currentZoom.ordinal() > 3) {
            //Coloring environment with Circle 2 color

            if (selectedColor != -1 && selectedColor == colorC2)
                canvas.drawColor(colorSel);
            else
                canvas.drawColor(colorC2);

        } else {

            if (currentZoom.ordinal() == 3) {
                if (selectedColor != -1 && selectedColor == colorC2)
                    canvas.drawColor(colorSel);
                else
                    canvas.drawColor(colorC2);
            } else {
                //Checking if the Circle 2 is selected
                if (selectedColor != -1 && selectedColor == colorC2)
                    paint.setColor(colorSel);
                else
                    paint.setColor(colorC2);

                //Drawing the Circle
                xPosition = totalScreenWidth / 2;
                yPosition = totalScreenHeight;
                xPosition = xPosition - toPixels(circle2Size / 2);
                yPosition = yPosition - toPixels(circle2Size / 2);
                rectf = new RectF(xPosition, yPosition, xPosition + toPixels(circle2Size), yPosition + toPixels(circle2Size));
                canvas.drawArc(rectf, 180, 180, true, paint);

            }

            canvas.drawBitmap(markerBitmap.get("circle2"), toPixels(convertPixelsToDp(((totalScreenWidth / 2) - 30))), toPixels(convertPixelsToDp((totalScreenHeight - toPixels(circle2Size / 2)))), paint); //marker for circle 2
            markerPositions.put("circle2", new PointF(toPixels(convertPixelsToDp(((totalScreenWidth / 2) - 30))), toPixels(convertPixelsToDp((totalScreenHeight - toPixels(circle2Size / 2))))));

        }


        Log.i("@ScreenWi", "Circle One Size" + circle1Size);

        //Checking if the circle 1 is selected
        if (selectedColor != -1 && selectedColor == colorC1)
            paint.setColor(colorSel);
        else
            paint.setColor(colorC1);

        //Drawing the Circle 1
        xPosition = totalScreenWidth / 2;
        yPosition = totalScreenHeight;
        xPosition = xPosition - toPixels(circle1Size / 2);
        yPosition = yPosition - toPixels(circle1Size / 2);
        rectf = new RectF(xPosition, yPosition, xPosition + toPixels(circle1Size), yPosition + toPixels(circle1Size));
        canvas.drawArc(rectf, 180, 180, true, paint);


        canvas.drawBitmap(markerBitmap.get("circle1"), toPixels(convertPixelsToDp(((totalScreenWidth / 2) - 30))), toPixels(convertPixelsToDp((totalScreenHeight - toPixels(circle1Size / 2)))), paint); //marker for circle 1
        markerPositions.put("circle1", new PointF(toPixels(convertPixelsToDp(((totalScreenWidth / 2) - 30))), toPixels(convertPixelsToDp((totalScreenHeight - toPixels(circle1Size / 2))))));


        // Turning the paint color with With and text size
        paint.setColor(Color.WHITE);
        paint.setTextSize(mcontext.getResources().getDimensionPixelSize(R.dimen.text_size_for_graph));

        //This Paint is for draw the line

        Paint paintForLine = new Paint();
        paintForLine.setAntiAlias(true);
        paintForLine.setStrokeWidth(5);
        paintForLine.setColor(Color.WHITE);

        try {


            canvas.drawLine(totalScreenWidth / 2, totalScreenHeight, 0, 0, paint);

            if (currentZoom == SCALE.ONE) {

                /**
                 * Validating the canvas with DISTANCE METER on zoom level 1
                 */

                canvas.drawText("100 " + preferdDistance, ((totalScreenWidth / 2) - toPixels(getResources().getDimension(R.dimen.circle_5_text_position_zoom1))), (totalScreenHeight - toPixels((circle4Size / 2) + 40)), paint);
                canvas.drawText("75 " + preferdDistance, ((totalScreenWidth / 2) - toPixels(getResources().getDimension(R.dimen.circle_4_text_position_zoom1))), (totalScreenHeight - toPixels((circle4Size / 2) - 50)), paint);
                canvas.drawText("50 " + preferdDistance, ((totalScreenWidth / 2) - toPixels(getResources().getDimension(R.dimen.circle_3_text_position_zoom1))), (totalScreenHeight - toPixels((circle3Size / 2) - 40)), paint);
                canvas.drawText("25 " + preferdDistance, ((totalScreenWidth / 2) - toPixels(getResources().getDimension(R.dimen.circle_2_text_position_zoom1))), (totalScreenHeight - toPixels((circle2Size / 2) - 40)), paint);
                canvas.drawText("5 " + preferdDistance, ((totalScreenWidth / 2) - toPixels(getResources().getDimension(R.dimen.circle_1_text_position_zoom1))), toPixels(convertPixelsToDp((totalScreenHeight - toPixels((circle1Size / 2) - 30)))), paint);

            } else if (currentZoom == SCALE.TWO) {

                canvas.drawText("75 " + preferdDistance, ((totalScreenWidth / 2) - toPixels(getResources().getDimension(R.dimen.circle_4_text_position_zoom2))), (totalScreenHeight - toPixels((circle4Size / 2) + 50)), paint);
                canvas.drawText("50 " + preferdDistance, ((totalScreenWidth / 2) - toPixels(getResources().getDimension(R.dimen.circle_3_text_position_zoom2))), (totalScreenHeight - toPixels((circle3Size / 2) - 50)), paint);
                canvas.drawText("25 " + preferdDistance, ((totalScreenWidth / 2) - toPixels(getResources().getDimension(R.dimen.circle_2_text_position_zoom2))), (totalScreenHeight - toPixels((circle2Size / 2) - 50)), paint);
                canvas.drawText("5 " + preferdDistance, ((totalScreenWidth / 2) - toPixels(getResources().getDimension(R.dimen.circle_1_text_position_zoom2))), toPixels(convertPixelsToDp((totalScreenHeight - toPixels((circle1Size / 2) - 50)))), paint);

            } else if (currentZoom == SCALE.THREE) {
                canvas.drawText("50 " + preferdDistance, ((totalScreenWidth / 2) - toPixels(getResources().getDimension(R.dimen.circle_3_text_position_zoom3))), (totalScreenHeight - toPixels((circle3Size / 2) + 50)), paint);
                canvas.drawText("25 " + preferdDistance, ((totalScreenWidth / 2) - toPixels(getResources().getDimension(R.dimen.circle_2_text_position_zoom3))), (totalScreenHeight - toPixels((circle2Size / 2) - 50)), paint);
                canvas.drawText("5 " + preferdDistance, ((totalScreenWidth / 2) - toPixels(getResources().getDimension(R.dimen.circle_1_text_position_zoom3))), toPixels(convertPixelsToDp((totalScreenHeight - toPixels((circle1Size / 2) - 50)))), paint);

////                canvas.drawBitmap(markerBitmap, totalScreenWidth / 2, (totalScreenHeight - (circle3Size / 2)) - cal_height(200), paint);
//
            } else if (currentZoom == SCALE.FOUR) {
                canvas.drawText("5 " + preferdDistance, ((totalScreenWidth / 2) - toPixels(getResources().getDimension(R.dimen.circle_1_text_position_zoom4))), toPixels(convertPixelsToDp((totalScreenHeight - toPixels((circle1Size / 2) - 50)))), paint);
                canvas.drawText("25 " + preferdDistance, ((totalScreenWidth / 2) - toPixels(getResources().getDimension(R.dimen.circle_2_text_position_zoom4))), (totalScreenHeight - toPixels((circle1Size / 2) + 50)), paint);

//                // canvas.drawBitmap(markerBitmap, totalScreenWidth / 2, (totalScreenHeight - (circle3Size / 2)) - cal_height(200), paint);
//
//
            } else if (currentZoom == SCALE.FIVE) {
                   canvas.drawText("5 " + preferdDistance, ((totalScreenWidth / 2) - toPixels(getResources().getDimension(R.dimen.circle_1_text_position_zoom5))), toPixels(convertPixelsToDp((totalScreenHeight - toPixels((circle1Size / 2) - 50)))), paint);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        paint.setColor(Color.RED);
        paint.setStrokeWidth(1);

        canvas.drawLine(totalScreenWidth / 2, 0, totalScreenWidth / 2, totalScreenHeight, paint);

        canvas.drawText("ZOOM LEVEL " + currentZoom.name(), 0, totalScreenHeight / 2, paint);
        canvas.drawText(" Curcle1Size in DP: " + convertPixelsToDp(circle1Size), 0, totalScreenHeight / 2 - 60, paint);
        canvas.drawText(" Curcle2Size in DP: " + convertPixelsToDp(circle2Size), 0, totalScreenHeight / 2 - 120, paint);
        canvas.drawText(" Curcle3Size in DP: " + convertPixelsToDp(circle3Size), 0, totalScreenHeight / 2 - 180, paint);
        canvas.drawText(" Curcle4Size in DP: " + convertPixelsToDp(circle4Size), 0, totalScreenHeight / 2 - 240, paint);


        mCanvaas.drawBitmap(currentBitmap, 0, 0, null);

        if (!isLoadComplete) {
            isLoadComplete = true;
            if (onGraphLoadedCallback != null)
                onGraphLoadedCallback.onGraphLoadComplete();
        }

    }

    public void setPeopleCount(int circle1, int circle2, int circle3, int circle4, int circle5) {
        circle1Count = circle1;
        circle2Count = circle2;
        circle3Count = circle3;
        circle4Count = circle4;
        circle5Count = circle5;
        markerBitmap.put("circle1", getMarkerView(circle1Count)); //Circle 1
        markerBitmap.put("circle2", getMarkerView(circle2Count)); //Circle 2
        markerBitmap.put("circle3", getMarkerView(circle3Count)); //Circle 3
        markerBitmap.put("circle4", getMarkerView(circle4Count)); //Circle 4
        markerBitmap.put("circle5", getMarkerView(circle5Count)); //Out side
        invalidate();
    }

    public void setOnGraphLoadedCallback(@NonNull OnGraphLoadedCallback callback) {
        onGraphLoadedCallback = callback;
    }

    public void setPreferdDistance(String preferdDistance) {
        this.preferdDistance = preferdDistance;
    }

    //Taking gesture details

    int mPtrCount = 0;
    private float mPrimStartTouchEventX = -1;
    private float mPrimStartTouchEventY = -1;
    private float mSecStartTouchEventX = -1;
    private float mSecStartTouchEventY = -1;
    private float mPrimSecStartTouchDistance = 0;
    private boolean isZoomChanged = false;
    private int mViewScaledTouchSlop = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        try {
            Log.i("@Ges", "event counter " + event.getPointerCount());
            int action = (event.getAction() & MotionEvent.ACTION_MASK);
            switch (action) {
                case MotionEvent.ACTION_POINTER_DOWN:
                case MotionEvent.ACTION_DOWN:
                    mPtrCount++;

                    if (mPtrCount == 1 && mPrimStartTouchEventY == -1 && mPrimStartTouchEventY == -1) {
                        mPrimStartTouchEventX = event.getX(0);
                        mPrimStartTouchEventY = event.getY(0);
                        Log.d("@Ges", String.format("POINTER ONE X = %.5f, Y = %.5f", mPrimStartTouchEventX, mPrimStartTouchEventY));
                    }
                    if (mPtrCount == 2) {
                        if (event.getPointerCount() > 1) {
                            // Starting distance between fingers
                            mSecStartTouchEventX = event.getX(1);
                            mSecStartTouchEventY = event.getY(1);
                            mPrimSecStartTouchDistance = distance(event, 0, 1);
                            isZoomChanged = true;
                        } else {
                            mPtrCount = 1;
                        }

                        //Log.d("@Ges", String.format("POINTER TWO X = %.5f, Y = %.5f", mSecStartTouchEventX, mSecStartTouchEventY));
                    }

                    break;
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_UP:
                    mPtrCount--;
                    if (mPtrCount < 2) {
                        mSecStartTouchEventX = -1;
                        mSecStartTouchEventY = -1;
                    }
                    if (mPtrCount < 1) {
                        mPrimStartTouchEventX = -1;
                        mPrimStartTouchEventY = -1;


                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    try {
                        boolean isPrimMoving = isScrollGesture(event, 0, mPrimStartTouchEventX, mPrimStartTouchEventY);
                        boolean isSecMoving = (mPtrCount > 1 && isScrollGesture(event, 1, mSecStartTouchEventX, mSecStartTouchEventY));

                        // There is a chance that the gesture may be a scroll
                        if (mPtrCount > 1 && isPinchGesture(event)) {
                            Log.d("@Ges", "PINCH! OUCH!");

                        } else if (isPrimMoving || isSecMoving) {
                            // A 1 finger or 2 finger scroll.
                            if (isPrimMoving && isSecMoving) {
                                Log.d("@Ges", "Two finger scroll");
                            } else {
                                Log.d("@Ges", "One finger scroll");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();

                    }

                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            mSecStartTouchEventX = -1;
            mSecStartTouchEventY = -1;
            mPrimStartTouchEventX = -1;
            mPrimStartTouchEventY = -1;
            mPtrCount = 0;
            return false;
        }

        return mGestureDetector.onTouchEvent(event);
    }

    private boolean isScrollGesture(MotionEvent event, int ptrIndex, float originalX, float originalY) {
        float moveX = Math.abs(event.getX(ptrIndex) - originalX);
        float moveY = Math.abs(event.getY(ptrIndex) - originalY);

        if (moveX > mViewScaledTouchSlop || moveY > mViewScaledTouchSlop) {
            return true;
        }
        return false;
    }

    private boolean isPinchGesture(MotionEvent event) {

        if (!isZoomChanged)
            return false;

        if (event.getPointerCount() == 2) {

            final float distanceCurrent = distance(event, 0, 1);
            final float diffPrimX = mPrimStartTouchEventX - event.getX(0);
            final float diffPrimY = mPrimStartTouchEventY - event.getY(0);
            final float diffSecX = mSecStartTouchEventX - event.getX(1);
            final float diffSecY = mSecStartTouchEventY - event.getY(1);


            Log.i("@ZoomG", "mViewScaledTouchSlop ****** " + mViewScaledTouchSlop);
            if (// if the distance between the two fingers has increased past
                // our threshold
                    Math.abs(distanceCurrent - mPrimSecStartTouchDistance) > (mViewScaledTouchSlop / 2)
                            // and the fingers are moving in opposing directions
                            && (diffPrimY * diffSecY) <= 0
                            && (diffPrimX * diffSecX) <= 0) {
                // mPinchClamp = false; // don't clamp initially
                Log.i("@ZoomG", "distanceCurrent @@@ " + distanceCurrent);
                Log.i("@ZoomG", "mPrimSecStartTouchDistance @@@ " + mPrimSecStartTouchDistance);

                //Redrawing Circle here

                if (mPrimSecStartTouchDistance > distanceCurrent) {

                    //ZOOM OUT
                    setZoomOutLevel(currentZoom);
                } else {

                    Log.i("@Zoom", "Zoom IN");
                    //ZOOM IN
                    setZoomInLevel(currentZoom);
                }
                isZoomChanged = false;
                return true;
            }
        }

        return false;
    }

    public void setMinZoomLevel(SCALE level) {
        maxZoomOutLevel = level;
        currentZoom = level;

        Log.i("@searchGraph", " setMinZoomLevel " + level);

        if (level == SCALE.TWO) {
            calculateCircles(SCALE.TWO);
            invalidate();

        } else if (level == SCALE.THREE) {
            calculateCircles(SCALE.THREE);
            invalidate();

        } else if (level == SCALE.FOUR) {
            calculateCircles(SCALE.FOUR);
            invalidate();

        } else if (level == SCALE.FIVE) {
            calculateCircles(SCALE.FIVE);
            invalidate();
        }
        ;
    }

    public void resetSelectedColor() {
        selectedColor = -1;
        invalidate();
    }

    private void setZoomInLevel(SCALE lebel) {
        Log.i("@drawCircleCond", "on graph got current zoom " + lebel);
        if (lebel == SCALE.ONE) {
            currentZoom = SCALE.TWO;
            calculateCircles(SCALE.TWO);
            invalidate();

        } else if (lebel == SCALE.TWO) {
            currentZoom = SCALE.THREE;
            calculateCircles(SCALE.THREE);
            invalidate();

        } else if (lebel == SCALE.THREE) {
            currentZoom = SCALE.FOUR;
            calculateCircles(SCALE.FOUR);
            invalidate();

        } else if (lebel == SCALE.FOUR) {
            currentZoom = SCALE.FIVE;
            calculateCircles(SCALE.FIVE);
            invalidate();
        }
    }

    private void setZoomOutLevel(SCALE level) {

        Log.i("@searchGraph", "set max zoom " + maxZoomOutLevel.ordinal() + " requested level ordinal " + level.ordinal());

        Log.i("@searchGraph", "setting zoom out un expected !! ");
        if (level == SCALE.FIVE && level != maxZoomOutLevel) {
            currentZoom = SCALE.FOUR;
            calculateCircles(SCALE.FOUR);
            invalidate();

        } else if (level == SCALE.FOUR && level != maxZoomOutLevel) {
            currentZoom = SCALE.THREE;
            calculateCircles(SCALE.THREE);
            invalidate();

        } else if (level == SCALE.THREE && level != maxZoomOutLevel) {
            currentZoom = SCALE.TWO;
            calculateCircles(SCALE.TWO);
            invalidate();

        } else if (level == SCALE.TWO && level != maxZoomOutLevel) {
            currentZoom = SCALE.ONE;
            calculateCircles(SCALE.ONE);
            invalidate();

        }
    }

    private float distance(MotionEvent event, int first, int second) {
        if (event.getPointerCount() >= 2) {
            Log.i("@Ges", " ## ## X1:" + event.getX(first) + "/ Y1:" + event.getY(first) + "  --- X2:" + event.getX(second) + "/ Y2:" + event.getY(second));

            final float x = event.getX(first) - event.getX(second);
            final float y = event.getY(first) - event.getY(second);

            return (float) Math.sqrt(x * x + y * y);
        } else {
            return 0;
        }
    }

    private float distance(float x1, float x2, float y1, float y2) {
        final float x = x1 - x2;
        final float y = y1 - y2;
        return (float) Math.sqrt(x * x + y * y);

    }

    private final GestureDetector.OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        boolean hasDisallowed = false;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {

            Log.i("@Tapping", "****************************************  ON TAP UP *******************************************");
            try {

                if (listener != null)
                    listener.onClickCircle((int) e.getX(), (int) e.getY());

                int pixle = currentBitmap.getPixel((int) e.getX(), (int) e.getY());

                Log.i("@Tapping", "* pixle " + pixle);
                Log.i("@Tapping", "* pixle " + pixle);

                if (pixle == colorC1) {
                    PointF markerPoint = markerPositions.get("circle1");
                    //Check region for click
                    if (circle1Count > 0) {
                        selectedColor = colorC1;
                        invalidate();
                    }

                    if (onMarkerClickListener != null)
                        onMarkerClickListener.onClickCircle(markerPoint, CirclePos.ONE);

                } else if (pixle == colorC2) {

                    PointF markerPoint = markerPositions.get("circle2");
                    //Check region for click
                    if (circle2Count > 0) {
                        selectedColor = colorC2;
                        invalidate();
                    }

                    if (onMarkerClickListener != null)
                        onMarkerClickListener.onClickCircle(markerPoint, CirclePos.TWO);


                } else if (pixle == colorC3) {
                    PointF markerPoint = markerPositions.get("circle3");
                    //Check region for click
                    if (circle3Count > 0) {
                        selectedColor = colorC3;
                        invalidate();
                    }

                    if (onMarkerClickListener != null)
                        onMarkerClickListener.onClickCircle(markerPoint, CirclePos.THREE);


                } else if (pixle == colorC4) {
                    PointF markerPoint = markerPositions.get("circle4");
                    //Check region for click
                    if (circle4Count > 0) {
                        selectedColor = colorC4;
                        invalidate();
                    }
                    if (onMarkerClickListener != null)
                        onMarkerClickListener.onClickCircle(markerPoint, CirclePos.FOUR);
                    //    }

                } else if (pixle == colorC5) {
                    PointF markerPoint = markerPositions.get("circle5");
                    //Check region for click
                    if (circle5Count > 0) {
                        selectedColor = colorC5;
                        invalidate();
                    }
                    if (onMarkerClickListener != null)
                        onMarkerClickListener.onClickCircle(markerPoint, CirclePos.FIVE);
                    // }


                }
//

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }
    };

    private onCircleClickListener listener;

    public void setOnClickCircleListener(onCircleClickListener callback) {
        listener = callback;
    }

    public interface onCircleClickListener {
        void onClickCircle(int x, int y);

    }

    private Bitmap getMarkerView(int count) {
        Bitmap returnedBitmap = null;
        try {
            View customMarkerView = ((LayoutInflater) mcontext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marker_view, null);

            TextView countText = (TextView) customMarkerView.findViewById(R.id.nearme_people_count);
            if (count > 500)
                countText.setText("500+");
            else
                countText.setText("" + count);

            customMarkerView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
            customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
            customMarkerView.buildDrawingCache();
            returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(returnedBitmap);
            canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
            Drawable drawable = customMarkerView.getBackground();
            if (drawable != null)
                drawable.draw(canvas);
            customMarkerView.draw(canvas);
            customMarkerView.destroyDrawingCache();

            Bitmap newReturn = compressBitmap(returnedBitmap);
            returnedBitmap.recycle();
            return newReturn;
        } catch (Exception e) {
            e.printStackTrace();
            return returnedBitmap;
        }

    }

    public void zoomIn() {
        setZoomInLevel(currentZoom);
    }

    public void zoomOut() {
        setZoomOutLevel(currentZoom);
    }

    public static Bitmap compressBitmap(Bitmap bitmap) {

        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(0.9f, 0.9f);
        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    public interface OnMarkerClickListener {
        void onClickCircle(PointF screenPoint, CirclePos circlePos);
    }

    public interface OnGraphLoadedCallback {
        void onGraphLoadComplete();
    }


    public enum CirclePos {ONE, TWO, THREE, FOUR, FIVE}


    /**
     * Converts dp to px
     *
     * @param dp the value in dp
     * @return int
     */
    public int toPixels(float dp) {
        return (int) (dp * resources.getDisplayMetrics().density);
    }

    /**
     * Converts sp to px
     *
     * @param res Resources
     * @param sp  the value in sp
     * @return int
     */
    public static int toScreenPixels(Resources res, float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, res.getDisplayMetrics());
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @return A float value to represent dp equivalent to px value
     */
    public float convertPixelsToDp(float px) {
        Resources resources = mcontext.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    public int cal_width(int given_x) {
        int width = 0;
        width = (totalScreenWidth * given_x) / 1080;
        return width;
    }

    public int cal_height(int given_y) {
        int height = 0;
        height = (totalScreenHeight * given_y) / 1920;
        return height;
    }
}
