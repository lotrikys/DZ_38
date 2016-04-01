package org.itstep.pastukhov.qr_vin_scanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by DWork on 1/20/2016.
 */
public class ZbarScanCodeRect extends View {

    private float frameStrokeWidth;
    private float frameStrokeLength;
    static float FRAME_SIDE_MARGIN = 100;

    private int mTypeScanner = 0;
    private Paint paint;
    private int screenWidth;
    private int screenHeight;
    private int frameHeight;
    float centerX;
    float centerY;

    public ZbarScanCodeRect(Context context) {
        super(context);
        screenHeight = 0;
        screenWidth = 0;
        frameHeight = 0;
        centerX = 0;
        centerY = 0;

    }

    public ZbarScanCodeRect(Context context, AttributeSet attrs) {
        super(context, attrs);
        screenHeight = 0;
        screenWidth = 0;
        frameHeight = 0;
        centerX = 0;
        centerY = 0;
    }

    public void setCodeRect(int screenWidth, int screenHeight, int frameHeight, int typeScanner) {
        frameStrokeLength = getResources().getDimension(R.dimen.frame_stroke_length);
        frameStrokeWidth = getResources().getDimension(R.dimen.frame_stroke_width);

        mTypeScanner = typeScanner;
        this.frameHeight = frameHeight;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;


        paint = new Paint();
        paint.setStrokeWidth(frameStrokeWidth);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);
        paint.setColor(Color.YELLOW);

        centerX = screenWidth / 2;
        centerY = screenHeight / 2;

    }

    @Override
    protected void onDraw(Canvas canvas) {

        Bitmap bitmapFirstLayout = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        Canvas can = new Canvas(bitmapFirstLayout);
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        bgPaint.setColor(Color.parseColor("#99FFFFFF"));
        can.drawRect(0, 0, screenWidth, screenHeight, bgPaint);


        if (mTypeScanner == Globals.REQUEST_CODE_VIN_SCANNER) {

            Path path = new Path();
            path.addRect(FRAME_SIDE_MARGIN, centerY - frameHeight / 2, screenWidth - 100, centerY + frameHeight / 2, Path.Direction.CW);
            Paint ppaint = new Paint();
            ppaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            can.drawPath(path, ppaint);
            canvas.drawBitmap(bitmapFirstLayout, 0, 0, new Paint());
            Paint rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            rectPaint.setStyle(Paint.Style.STROKE);
            rectPaint.setColor(Color.BLACK);
            canvas.drawRect(FRAME_SIDE_MARGIN, centerY - frameHeight / 2, screenWidth - FRAME_SIDE_MARGIN, centerY + frameHeight / 2, rectPaint);

            canvas.drawLine(FRAME_SIDE_MARGIN, centerY - frameHeight / 2, FRAME_SIDE_MARGIN + frameStrokeLength, centerY - frameHeight / 2, paint);
            canvas.drawLine(FRAME_SIDE_MARGIN, centerY - frameHeight / 2, FRAME_SIDE_MARGIN, centerY - frameHeight / 2 + frameStrokeLength, paint);

            canvas.drawLine(screenWidth - FRAME_SIDE_MARGIN, centerY - frameHeight / 2, screenWidth - FRAME_SIDE_MARGIN, centerY - frameHeight / 2 + frameStrokeLength, paint);
            canvas.drawLine(screenWidth - FRAME_SIDE_MARGIN, centerY - frameHeight / 2, screenWidth - FRAME_SIDE_MARGIN - frameStrokeLength, centerY - frameHeight / 2, paint);

            canvas.drawLine(screenWidth - FRAME_SIDE_MARGIN, centerY + frameHeight / 2, screenWidth - FRAME_SIDE_MARGIN - frameStrokeLength, centerY + frameHeight / 2, paint);
            canvas.drawLine(screenWidth - FRAME_SIDE_MARGIN, centerY + frameHeight / 2, screenWidth - FRAME_SIDE_MARGIN, centerY + frameHeight / 2 - frameStrokeLength, paint);

            canvas.drawLine(FRAME_SIDE_MARGIN, centerY + frameHeight / 2, FRAME_SIDE_MARGIN + frameStrokeLength, centerY + frameHeight / 2, paint);
            canvas.drawLine(FRAME_SIDE_MARGIN, centerY + frameHeight / 2, FRAME_SIDE_MARGIN, centerY + frameHeight / 2 - frameStrokeLength, paint);


        } else if (mTypeScanner == Globals.REQUEST_CODE_QR_SCANNER) {

            Path path = new Path();
            path.addRect(centerX - frameHeight / 2, centerY - frameHeight / 2, centerX + frameHeight / 2, centerY + frameHeight / 2, Path.Direction.CW);
            Paint ppaint = new Paint();
            ppaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            can.drawPath(path, ppaint);

            canvas.drawBitmap(bitmapFirstLayout, 0, 0, new Paint());
            Paint rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            rectPaint.setStyle(Paint.Style.STROKE);
            rectPaint.setColor(Color.BLACK);
            canvas.drawRect(centerX - frameHeight / 2, centerY - frameHeight / 2, centerX + frameHeight / 2, centerY + frameHeight / 2, rectPaint);
            canvas.drawLine(centerX - frameHeight / 2, centerY - frameHeight / 2, centerX - frameHeight / 2 + frameStrokeLength, centerY - frameHeight / 2, paint);
            canvas.drawLine(centerX - frameHeight / 2, centerY - frameHeight / 2, centerX - frameHeight / 2, centerY - frameHeight / 2 + frameStrokeLength, paint);

            canvas.drawLine(centerX - frameHeight/2, centerY + frameHeight / 2 , centerX - frameHeight/2  ,(centerY + frameHeight / 2) - frameStrokeLength, paint);
            canvas.drawLine(centerX - frameHeight/2, centerY + frameHeight / 2 , centerX - frameHeight/2 + frameStrokeLength,centerY + frameHeight / 2 , paint);

            canvas.drawLine(centerX + frameHeight / 2, centerY - frameHeight / 2, centerX + frameHeight / 2 - frameStrokeLength, centerY - frameHeight / 2, paint);
            canvas.drawLine(centerX + frameHeight / 2, centerY - frameHeight / 2, centerX + frameHeight / 2 , centerY - frameHeight/2 + frameStrokeLength, paint);

            canvas.drawLine(centerX + frameHeight / 2, centerY + frameHeight / 2, centerX + frameHeight / 2 - frameStrokeLength, centerY + frameHeight / 2, paint);
            canvas.drawLine(centerX + frameHeight / 2, centerY + frameHeight / 2, centerX + frameHeight / 2 , centerY + frameHeight/2 - frameStrokeLength, paint);

        }
    }
}