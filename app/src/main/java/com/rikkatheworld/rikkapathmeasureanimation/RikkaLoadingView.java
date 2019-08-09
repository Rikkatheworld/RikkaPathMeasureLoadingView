package com.rikkatheworld.rikkapathmeasureanimation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;

public class RikkaLoadingView extends View {

    //勾的路径动画
    private PathMeasure pathMeasureGou;
    //大圆的路径动画
    private PathMeasure pathMeasureBigCircle1, pathMeasureBigCircle2;
    //勾的原始，大圆的原始路径
    private Path gouPath, bigPath1, bigPath2;
    //勾和大圆的目标路径
    private Path dstGouPath, dstBigPath1, dstBigPath2;
    //画布要切成圆的路径
    private Path canvasPath;
    //勾的画笔
    private Paint gouPaint;
    //大圆画笔
    private Paint bigPaint1, bigPaint2;
    //小圆画笔
    private Paint smallPaint1, smallPaint2;
    //原点
    private float originX, originY;
    //整个画布的圆半径
    private float bigRadius;
    //小圆半径
    private float smallRadius;
    //分别是，勾的动画、小圆的动画、大圆的动画、透明度动画
    private ValueAnimator animatorSmallCircle, animatorGouAndBigCircle, animatorAlpha;
    //分别是小圆动画进度、大圆动画进度、透明度动画进度
    private float smallProgress, bigProgress, alphaProgress;

    public RikkaLoadingView(Context context) {
        this(context, null);
    }

    public RikkaLoadingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RikkaLoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        gouPath = new Path();
        canvasPath = new Path();
        bigPath1 = new Path();
        bigPath2 = new Path();
        dstGouPath = new Path();
        dstBigPath1 = new Path();
        dstBigPath2 = new Path();
        initPaint();
    }

    //初始化画笔
    private void initPaint() {
        gouPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gouPaint.setStyle(Paint.Style.STROKE);
        gouPaint.setStrokeWidth(10);
        gouPaint.setColor(Color.parseColor("#4b66ed"));

        bigPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        bigPaint1.setStyle(Paint.Style.STROKE);
        bigPaint1.setStrokeWidth(20);
        bigPaint1.setColor(Color.parseColor("#f9d95f"));

        bigPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        bigPaint2.setStyle(Paint.Style.STROKE);
        bigPaint2.setStrokeWidth(20);
        bigPaint2.setColor(Color.parseColor("#fe9e7e"));

        smallPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        smallPaint1.setStyle(Paint.Style.FILL_AND_STROKE);
        smallPaint1.setStrokeWidth(3);
        smallPaint1.setColor(Color.parseColor("#fe9e7e"));

        smallPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        smallPaint2.setStyle(Paint.Style.FILL_AND_STROKE);
        smallPaint2.setStrokeWidth(3);
        smallPaint2.setColor(Color.parseColor("#f9d95f"));
    }

    //初始化坐标参数
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //找出坐标原点，并初始化大圆半径和小圆半径
        originX = (float) (getWidth() / 2);
        originY = (float) (getHeight() / 2);
        bigRadius = (float) getWidth() / 2;
        smallRadius = bigRadius / 10;

        //画勾
        gouPath = new Path();
        gouPath.moveTo(originX - bigRadius / 2, originY);
        gouPath.lineTo(originX, originY + bigRadius / 2);
        gouPath.lineTo(originX + bigRadius / 2, originY - bigRadius / 3);

        //画大圆
        bigPath1 = new Path();
        bigPath1.moveTo(originX - bigRadius, originY);
        bigPath1.addArc(0, 0, getWidth(), getHeight(), 180, 360);

        bigPath2 = new Path();
        bigPath2.moveTo(originX + bigRadius, originY);
        bigPath2.addArc(0, 0, getWidth(), getHeight(), 0, 360);

        //画布的形状path
        canvasPath.addCircle(originX, originY, bigRadius, Path.Direction.CW);

        //连接路径动画和路径
        pathMeasureGou = new PathMeasure(gouPath, false);
        pathMeasureBigCircle1 = new PathMeasure(bigPath1, false);
        pathMeasureBigCircle2 = new PathMeasure(bigPath2, false);
    }

    //两个小圆分开的动画
    private ValueAnimator getSmallAnimation() {
        if (animatorSmallCircle == null) {
            animatorSmallCircle = ValueAnimator.ofFloat(0, 1);
            animatorSmallCircle.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    //获取小圆的动画进度
                    smallProgress = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            animatorSmallCircle.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    //在动画结束的时候，大圆开始转圈圈并且开始打钩,播放着两个动画
                    getGouAndBigCircleAnimation().start();
                }
            });
            animatorSmallCircle.setDuration(700);
            animatorSmallCircle.setInterpolator(new AccelerateInterpolator());
        }
        return animatorSmallCircle;
    }

    //打勾动画和大圆转圈的动画是同时进行的，所以公用一个动画
    public ValueAnimator getGouAndBigCircleAnimation() {
        if (animatorGouAndBigCircle == null) {
            animatorGouAndBigCircle = ValueAnimator.ofFloat(0, 1);
            animatorGouAndBigCircle.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    bigProgress = (float) animation.getAnimatedValue();
                    //在打勾动画的进度快要结束时，设置透明度动画
                    if (bigProgress <= 0.3f) {
                        if (!getAnimatorAlpha().isStarted()) {
                            getAnimatorAlpha().start();
                        }
                    }
                    invalidate();
                }
            });
            animatorGouAndBigCircle.setDuration(1000);
            animatorGouAndBigCircle.setInterpolator(new LinearInterpolator());
        }
        return animatorGouAndBigCircle;
    }

    //透明度动画
    public ValueAnimator getAnimatorAlpha() {
        if (animatorAlpha == null) {
            //透明度从1-0，用ObjectAnimator也可以做
            animatorAlpha = ValueAnimator.ofFloat(1.0f, 0f);
            animatorAlpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    alphaProgress = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            animatorAlpha.setDuration(1000);
            animatorAlpha.setInterpolator(new AccelerateInterpolator());
        }
        return animatorAlpha;
    }

    //就是开始各种动画
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //将画布裁剪成圆形，填充灰色
        canvas.save();
        canvas.clipPath(canvasPath);
        canvas.drawColor(Color.parseColor("#f0f0f0"));

        //小圆动画,在x轴上随着小圆动画进度而移动
        if (smallProgress <= 0.955f && smallProgress >= 0) {
            canvas.drawCircle(originX + (smallProgress * bigRadius), originY, smallRadius, smallPaint1);
            canvas.drawCircle(originX - (smallProgress * bigRadius), originY, smallRadius, smallPaint2);
        }

        //打勾动画,大圆转圈的动画，大圆是画布周长的长度是1/4
        if (bigProgress <= 1f && bigProgress >= 0) {
            float bigStop1 = pathMeasureBigCircle1.getLength() * bigProgress;
            dstBigPath1.reset();
            pathMeasureBigCircle1.getSegment(bigStop1 - (pathMeasureBigCircle1.getLength() * 0.25f), bigStop1, dstBigPath1, true);

            float bigStop2 = pathMeasureBigCircle2.getLength() * bigProgress;
            dstBigPath2.reset();
            pathMeasureBigCircle2.getSegment(bigStop1 - (pathMeasureBigCircle2.getLength() * 0.25f), bigStop2, dstBigPath2, true);

            float gouStop = pathMeasureGou.getLength() * bigProgress;
            dstGouPath.reset();
            pathMeasureGou.getSegment(0, gouStop, dstGouPath, true);
        }

        //透明度动画
        if (alphaProgress >= 0f && alphaProgress <= 1f) {
            bigPaint1.setAlpha((int) (255 * alphaProgress));
            bigPaint2.setAlpha((int) (255 * alphaProgress));
            gouPaint.setAlpha((int) (255 * alphaProgress));
        }

        //绘制路径
        canvas.drawPath(dstGouPath, gouPaint);
        canvas.drawPath(dstBigPath1, bigPaint1);
        canvas.drawPath(dstBigPath2, bigPaint2);
    }

    //开始动画，每次开始时都要清一遍值
    public void startAnim() {
        alphaProgress = 0f;
        bigProgress = 0f;
        smallProgress = 0f;
        if (getAnimatorAlpha() != null) {
            getAnimatorAlpha().setFloatValues(1f, 0);
        }
        if (getSmallAnimation() != null) {
            getSmallAnimation().setFloatValues(0, 1f);
        }
        if (getGouAndBigCircleAnimation() != null) {
            getGouAndBigCircleAnimation().setFloatValues(0, 1f);
        }
        getSmallAnimation().start();
    }
}
