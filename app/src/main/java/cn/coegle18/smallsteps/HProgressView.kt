package cn.coegle18.smallsteps

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.abs

/**
 * https://gitee.com/tangbuzhi
 *
 * @author: Tangbuzhi
 * @version:
 * @package:
 * @description:
 * @modify:
 * @date: 2018/3/24
 */
class HProgressView : View {

    private val defProgressColor: Int = context.getColor(R.color.progress_color)
    private val defNormalColor: Int = context.getColor(R.color.progress_normal_color)
    private val defProgressVWidth: Float
    private val defHMargin: Float

    private var hMargin: Float = 0f
    private var progressColor: Int = 0
    private var normalColor: Int = 0
    private var progressVWidth: Float = 0f
    private var withAnim: Boolean
    private var mWidth: Int = 0
    private var mHeight: Int = 0

    private var startCenterX: Float = 0f
    private var startCenterY: Float = 0f
    private var endCenterX: Float = 0f
    private var endCenterY: Float = 0f
    private var circleRadius: Float = 0f
    private var top: Float = 0f
    private var bottom: Float = 0f

    private lateinit var recLeftCircle: RectF
    private lateinit var recRightCircle: RectF
    private lateinit var rectProgressArea: RectF
    private lateinit var rectProgressPass: RectF
    private val bounds: Rect

    private val normalPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var mProgress: Float = 0f

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        defProgressVWidth = dip2px(20f)
        defHMargin = dip2px(10f)

        val a = context.obtainStyledAttributes(attrs, R.styleable.HProgressView)
        try {
            mProgress = a.getFloat(R.styleable.HProgressView_progress, 0f)
            hMargin = a.getDimension(R.styleable.HProgressView_h_margin, defHMargin)
            progressColor = a.getColor(R.styleable.HProgressView_progress_color, defProgressColor)
            normalColor = a.getColor(R.styleable.HProgressView_normal_color, defNormalColor)
            progressVWidth = a.getDimension(R.styleable.HProgressView_progress_v_width, defProgressVWidth)
            withAnim = a.getBoolean(R.styleable.HProgressView_with_anim, true)
        } finally {
            a.recycle()
        }

        if (mProgress < 0) mProgress = 0f
        if (mProgress > 100) mProgress = 100f

        normalPaint.color = normalColor
        normalPaint.style = Paint.Style.FILL

        progressPaint.color = progressColor
        progressPaint.style = Paint.Style.FILL

        bounds = Rect()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
        circleRadius = progressVWidth / 2
        startCenterX = circleRadius + hMargin
        endCenterX = mWidth - circleRadius - hMargin
        startCenterY = (mHeight / 2).toFloat()
        endCenterY = startCenterY

        top = startCenterY - circleRadius
        bottom = startCenterY + circleRadius
    }

    override fun onDraw(canvas: Canvas) {
        drawNormal(canvas)
        drawProgress(canvas)
    }

    private fun drawNormal(canvas: Canvas) {
        recLeftCircle = RectF(hMargin, top, hMargin + progressVWidth, bottom)
        canvas.drawArc(recLeftCircle, 90f, 180f, true, normalPaint)

        recRightCircle = RectF(mWidth - progressVWidth - hMargin, top, mWidth - hMargin, bottom)
        canvas.drawArc(recRightCircle, -90f, 180f, true, normalPaint)

        rectProgressArea = RectF(hMargin + circleRadius, top, mWidth - hMargin - circleRadius, bottom)
        canvas.drawRect(rectProgressArea, normalPaint)
    }

    private fun drawProgress(canvas: Canvas) {
        if (mProgress > 0) {
            canvas.drawArc(recLeftCircle, 90f, 180f, true, progressPaint)

            if (mProgress >= 0.5f) {//为了防止 mProgress 为 0.1-0.9 时左边绘制有误
                val left = hMargin + circleRadius
                val right = (mWidth - 2 * hMargin - progressVWidth) * mProgress / 100 + hMargin + circleRadius
                rectProgressPass = RectF(left, top, right, bottom)
                canvas.drawRect(rectProgressPass, progressPaint)

                rectProgressPass = RectF(right - circleRadius, top, right + circleRadius, bottom)
                canvas.drawArc(rectProgressPass, -90f, 180f, true, progressPaint)
            }
        }
    }

    fun setProgress(progress: Float) = setProgress(progress, withAnim)
    fun setProgressColor(color: Int) {
        progressColor = color
        progressPaint.color = progressColor
        invalidate()
    }

    private fun setProgress(progress: Float, withAnim: Boolean) {
        if (progress < 0) mProgress = 0f
        if (progress > 100) mProgress = 100f
        if (withAnim) {
            val animator = ValueAnimator.ofFloat(mProgress, progress)
            animator.addUpdateListener { anim ->
                mProgress = String.format("%.1f", anim.animatedValue).toFloat()
                invalidate()
            }
            animator.interpolator = LinearInterpolator()
            animator.duration = (abs(mProgress - progress) * 50).toLong()
            animator.start()
        } else {
            mProgress = progress
            invalidate()
        }
    }

    private fun dip2px(dipValue: Float) = context.resources.displayMetrics.density * dipValue
}