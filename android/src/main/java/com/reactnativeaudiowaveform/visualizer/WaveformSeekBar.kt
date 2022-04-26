package com.reactnativeaudiowaveform.visualizer

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.reactnativeaudiowaveform.R
import com.reactnativeaudiowaveform.audio.recorder.model.DebugState
import com.reactnativeaudiowaveform.gradient.getLinearGradientPojo
import kotlin.math.*

open class WaveformSeekBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mCanvasWidth = 0
    private var mCanvasHeight = 0
    private val mWavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mWaveBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mWaveRect = RectF()
    private val mProgressCanvas = Canvas()
    private var mTouchDownX = 0F
    private var mProgress = 0f
    private var mScaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var mAlreadyMoved = false
    private lateinit var progressBitmap: Bitmap
    private lateinit var progressShader: Shader

    var onProgressChanged: SeekBarOnProgressChanged? = null
    private var progressProps: WaveformSeekBarProps = WaveformSeekBarProps()

    var sample: IntArray = intArrayOf()
        set(value) {
            field = value
            invalidate()
        }

    init  {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.WaveformSeekBar)
        with(progressProps) {
          waveMaxHeight = getAvailableHeight()
          waveWidth = ta.getDimension(R.styleable.WaveformSeekBar_wave_width, Utils.dp(context, 5f))
          waveGap = ta.getDimension(R.styleable.WaveformSeekBar_wave_gap, Utils.dp(context, 2f))
          waveTopLeftCornerRadius = ta.getDimension(R.styleable.WaveformSeekBar_wave_corner_radius, Utils.dp(context, 2f))
          waveTopRightCornerRadius = ta.getDimension(R.styleable.WaveformSeekBar_wave_corner_radius, Utils.dp(context, 2f))
          waveBottomLeftCornerRadius = ta.getDimension(R.styleable.WaveformSeekBar_wave_corner_radius, Utils.dp(context, 2f))
          waveBottomRightCornerRadius = ta.getDimension(R.styleable.WaveformSeekBar_wave_corner_radius, Utils.dp(context, 2f))
          waveMinHeight = ta.getDimension(R.styleable.WaveformSeekBar_wave_min_height, Utils.dp(context, 5f))
          waveBackgroundColor = ta.getColor(R.styleable.WaveformSeekBar_wave_background_color, waveBackgroundColor)
          waveProgressColor = ta.getColor(R.styleable.WaveformSeekBar_wave_progress_color, waveProgressColor)
          progress = ta.getFloat(R.styleable.WaveformSeekBar_wave_progress, progress)
          maxProgress = ta.getFloat(R.styleable.WaveformSeekBar_wave_max_progress, maxProgress)
          visibleProgress = ta.getFloat(R.styleable.WaveformSeekBar_wave_visible_progress, visibleProgress)
          val gravity = ta.getString(R.styleable.WaveformSeekBar_wave_gravity)?.toInt() ?: WaveGravity.CENTER.ordinal
          waveGravity = WaveGravity.values()[gravity]
          val type = ta.getString(R.styleable.WaveformSeekBar_wave_type)?.toInt() ?: WaveType.PLAYER.ordinal
          waveType = WaveType.values()[type]
        }
        ta.recycle()
    }

    fun setProgress(progress: Float) {
      progressProps.progress = progress
      onProgressChanged?.onProgressChanged(this, progress, false)
      invalidate()
    }

    fun setMaxProgress(maxProgress: Float) {
      progressProps.maxProgress = maxProgress
      invalidate()
    }

    fun setWaveBackgroundColor(waveBackgroundColor: Int) {
      progressProps.waveBackgroundColor = waveBackgroundColor
      update()
    }

    fun setWaveBackgroundGradient(waveBackgroundGradient: WaveformSeekBarGradientProps?) {
      progressProps.waveBackgroundGradient = waveBackgroundGradient
      update()
    }

    fun setWaveProgressColor(waveProgressColor: Int) {
      progressProps.waveProgressColor = waveProgressColor
      update()
    }

    fun setWaveProgressGradient(waveProgressGradient: WaveformSeekBarGradientProps?) {
      progressProps.waveProgressGradient = waveProgressGradient
      update()
    }

    fun setWaveGap(waveGap: Float) {
      progressProps.waveGap = waveGap
      invalidate()
    }

    fun setWaveWidth(waveWidth: Float) {
      progressProps.waveWidth = waveWidth
      invalidate()
    }

    fun setWaveMinHeight(waveMinHeight: Float) {
      progressProps.waveMinHeight = waveMinHeight
      invalidate()
    }

    fun setWaveMaxHeight(waveMaxHeight: Int) {
      progressProps.waveMaxHeight = waveMaxHeight
      invalidate()
    }

    fun setWaveCornerRadius(waveCornerRadius: Float) {
      progressProps.waveTopLeftCornerRadius = waveCornerRadius
      progressProps.waveTopRightCornerRadius = waveCornerRadius
      progressProps.waveBottomLeftCornerRadius = waveCornerRadius
      progressProps.waveBottomRightCornerRadius = waveCornerRadius
      invalidate()
    }

    fun setWaveTopLeftCornerRadius(waveTopLeftCornerRadius: Float) {
      progressProps.waveTopLeftCornerRadius = waveTopLeftCornerRadius
      invalidate()
    }

    fun setWaveTopRightCornerRadius(waveTopRightCornerRadius: Float) {
      progressProps.waveTopRightCornerRadius = waveTopRightCornerRadius
      invalidate()
    }

    fun setWaveBottomLeftCornerRadius(waveBottomLeftCornerRadius: Float) {
      progressProps.waveBottomLeftCornerRadius = waveBottomLeftCornerRadius
      invalidate()
    }

    fun setWaveBottomRightCornerRadius(waveBottomRightCornerRadius: Float) {
      progressProps.waveBottomRightCornerRadius = waveBottomRightCornerRadius
      invalidate()
    }

    fun setWaveGravity(waveGravity: WaveGravity) {
      progressProps.waveGravity = waveGravity
      invalidate()
    }

    fun setWaveType(waveType: WaveType) {
      progressProps.waveType = waveType
      invalidate()
    }

    fun setVisibleProgress(visibleProgress: Float) {
      progressProps.visibleProgress = visibleProgress
      invalidate()
    }

    fun setStrokeWidth(strokeWidth: Float) {
      progressProps.strokeWidth = strokeWidth
      update()
    }

    fun setStrokeCap(strokeCap: Paint.Cap) {
      progressProps.strokeCap = strokeCap
      update()
    }

    fun setStrokeJoin(strokeJoin: Paint.Join) {
      progressProps.strokeJoin = strokeJoin
      update()
    }

    fun setStrokeMiter(strokeMiter: Float) {
      progressProps.strokeMiter = strokeMiter
      update()
    }

    fun setStrokeDashWidth(strokeDashWidth: Float) {
      progressProps.strokeDashWidth = strokeDashWidth
      update()
    }

    fun setStrokeDashGap(strokeDashGap: Float) {
      progressProps.strokeDashGap = strokeDashGap
      update()
    }

    fun setWaveStrokeBackgroundColor(waveStrokeBackgroundColor: Int) {
      progressProps.waveStrokeBackgroundColor = waveStrokeBackgroundColor
      update()
    }

    fun setWaveStrokeBackgroundGradient(waveStrokeBackgroundGradient: WaveformSeekBarGradientProps?) {
      progressProps.waveStrokeBackgroundGradient = waveStrokeBackgroundGradient
      update()
    }

    fun setWaveStrokeProgressColor(waveStrokeProgressColor: Int) {
      progressProps.waveStrokeProgressColor = waveStrokeProgressColor
      update()
    }

    fun setWaveStrokeProgressGradient(waveStrokeProgressGradient: WaveformSeekBarGradientProps?) {
      progressProps.waveStrokeProgressGradient = waveStrokeProgressGradient
      update()
    }

    fun setSampleFrom(samples: IntArray) {
        this.sample = samples
        updateMaxHeight()
    }

    fun setWaveForm(amps: List<Int>) {
        this.sample = amps.toIntArray()
        updateMaxHeight()
    }

    fun addAmp(amps: Int) {
        val tempAmps = mutableListOf<Int>()
        tempAmps.addAll(this.sample.toList())
        tempAmps.add(amps)
        this.sample = tempAmps.toIntArray()
        updateMaxHeight()
    }

    fun printAmplitudeList() {
      DebugState.error("loadFileAmps = ${this.sample.contentToString()}")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
      if (w >= 0 && h >= 0) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasWidth = w
        mCanvasHeight = h
        try {
          progressBitmap =
            Bitmap.createBitmap(getAvailableWidth(), mCanvasHeight, Bitmap.Config.ARGB_8888)
          progressShader = BitmapShader(progressBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        } catch(e: IllegalArgumentException) {
        }
      }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val totalWaveWidth = progressProps.waveGap + progressProps.waveWidth
        val totalBar = (getAvailableWidth() / totalWaveWidth).toInt()
        val waveSample = if(progressProps.waveType === WaveType.RECORDER) sample.copyOfRange(sample.size - if(sample.size > totalBar) totalBar else sample.size, sample.size) else sample
        if (waveSample.isEmpty())
            return

        canvas.clipRect(paddingLeft, paddingTop, mCanvasWidth - paddingRight, mCanvasHeight - paddingBottom)
        var step = waveSample.size / (getAvailableWidth() / totalWaveWidth)

        var lastWaveRight = paddingLeft.toFloat()
        var sampleItemPosition: Int

        val barsToDraw = if(waveSample.size > totalBar) totalBar else waveSample.size //(getAvailableWidth() / totalWaveWidth).toInt()
        val start: Int
        val progressView: Float
        if (progressProps.visibleProgress > 0) {
            // If visibleProgress is > 0, the bars move instead of the blue colored part
            step *= progressProps.visibleProgress / progressProps.maxProgress
            val barsForProgress = barsToDraw + 1
            // intFactor is required as depending on whether an equal number of bars must be drawn, the start will switch differently
            val intFactor = (((barsForProgress + 1) % 2))
            // Calculate fixed start change depending
            lastWaveRight += (getAvailableWidth() * 0.5F) % totalWaveWidth
            lastWaveRight += intFactor * 0.5F * totalWaveWidth - totalWaveWidth
            // Calculate start change depending on progress, so that it moves smoothly
            lastWaveRight -= ((progressProps.progress + intFactor * progressProps.visibleProgress / barsForProgress * 0.5f) % (progressProps.visibleProgress / barsForProgress)) / (progressProps.visibleProgress / barsForProgress) * totalWaveWidth
            start = (progressProps.progress * barsForProgress / progressProps.visibleProgress - (barsForProgress / 2F)).roundToInt() - 1
            progressView = getAvailableWidth() * 0.5F
        } else {
            start = 0
            progressView = getAvailableWidth() * progressProps.progress / progressProps.maxProgress
        }

        val isShaderListAvailable = progressProps.waveBackgroundGradient != null || progressProps.waveStrokeBackgroundGradient != null || progressProps.waveStrokeProgressGradient != null || progressProps.waveStrokeProgressGradient != null
        for (i in start until barsToDraw + start + 3) {
            sampleItemPosition = floor(i * step).roundToInt()
            var waveHeight = if (sampleItemPosition >= 0 && sampleItemPosition < waveSample.size)
                getAvailableHeight() * (waveSample[sampleItemPosition].toFloat() / progressProps.waveMaxHeight)
            else 0F

            if (waveHeight < progressProps.waveMinHeight)
                waveHeight = progressProps.waveMinHeight

            val top: Float = when (progressProps.waveGravity) {
                WaveGravity.TOP -> paddingTop.toFloat()
                WaveGravity.CENTER -> paddingTop + getAvailableHeight() / 2F - waveHeight / 2F
                WaveGravity.BOTTOM -> mCanvasHeight - paddingBottom - waveHeight
            }

            mWaveRect.set(lastWaveRight, top, lastWaveRight + progressProps.waveWidth, top + waveHeight)
            val shaderList: Array<Shader?>? = if(isShaderListAvailable) Utils.getAllShader(progressProps, mWaveRect, waveHeight) else null
            when {
                mWaveRect.contains(progressView, mWaveRect.centerY()) -> {
                    mProgressCanvas.setBitmap(progressBitmap)
                    handleProgressColor(shaderList)
                    mProgressCanvas.drawRect(0F, 0F, progressView, mWaveRect.bottom, mWavePaint)
                    handleBackgroundColor(shaderList)
                    mProgressCanvas.drawRect(progressView, 0F, getAvailableWidth().toFloat(), mWaveRect.bottom, mWavePaint)
                    //mWavePaint.shader = progressShader
                }
                mWaveRect.right <= progressView -> {
                  handleProgressColor(shaderList)
                }
                else -> {
                  handleBackgroundColor(shaderList)
                }
            }
            canvas.drawPath(
              Utils.roundedRect(
                false, progressProps.waveWidth.toInt(), waveHeight.toInt(),
                mWaveRect.left,
                mWaveRect.top,
                mWaveRect.right,
                mWaveRect.bottom,
                progressProps.waveTopLeftCornerRadius, progressProps.waveTopRightCornerRadius, progressProps.waveBottomLeftCornerRadius, progressProps.waveBottomRightCornerRadius
              ),
              mWavePaint
            )
          if(progressProps.strokeWidth > 0) {
            canvas.drawPath(
              Utils.roundedRect(
                false, progressProps.waveWidth.toInt(), waveHeight.toInt(),
                mWaveRect.left,
                mWaveRect.top,
                mWaveRect.right,
                mWaveRect.bottom,
                progressProps.waveTopLeftCornerRadius, progressProps.waveTopRightCornerRadius, progressProps.waveBottomLeftCornerRadius, progressProps.waveBottomRightCornerRadius
              ),
              mWaveBorderPaint
            )
          }
            lastWaveRight = mWaveRect.right + progressProps.waveGap
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isEnabled || progressProps.waveType === WaveType.RECORDER)
            return false
        if (progressProps.visibleProgress > 0) {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    mTouchDownX = event.x
                    mProgress = progressProps.progress
                    mAlreadyMoved = false
                }
                MotionEvent.ACTION_MOVE -> {
                    if (abs(event.x - mTouchDownX) > mScaledTouchSlop || mAlreadyMoved) {
                        updateProgress(event)
                        mAlreadyMoved = true
                    }
                }
                MotionEvent.ACTION_UP -> {
                    performClick()
                }
            }
        } else {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (isParentScrolling())
                        mTouchDownX = event.x
                    else
                        updateProgress(event)
                }
                MotionEvent.ACTION_MOVE -> {
                    updateProgress(event)
                }
                MotionEvent.ACTION_UP -> {
                    if (abs(event.x - mTouchDownX) > mScaledTouchSlop)
                        updateProgress(event)
                    performClick()
                }
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun handleProgressColor(shaderList: Array<Shader?>?) {
      if(progressProps.waveProgressGradient == null) {
        mWavePaint.color = progressProps.waveProgressColor
      } else {
        mWavePaint.shader = shaderList?.get(2)
        mWavePaint.alpha = progressProps.waveProgressGradient?.angle?.toInt() ?: 255
      }
      if(progressProps.waveStrokeProgressGradient == null) {
        mWaveBorderPaint.color = progressProps.waveStrokeProgressColor
      } else {
        mWaveBorderPaint.shader = shaderList?.get(3)
        mWaveBorderPaint.alpha = progressProps.waveStrokeProgressGradient?.angle?.toInt() ?: 255
      }
    }

    private fun handleBackgroundColor(shaderList: Array<Shader?>?) {
      if(progressProps.waveBackgroundGradient == null) {
        mWavePaint.color = progressProps.waveBackgroundColor
      } else {
        mWavePaint.shader = shaderList?.get(0)
        mWavePaint.alpha = progressProps.waveBackgroundGradient?.angle?.toInt() ?: 255
      }
      if(progressProps.waveStrokeBackgroundGradient == null) {
        mWaveBorderPaint.color = progressProps.waveStrokeBackgroundColor
      } else {
        mWaveBorderPaint.shader = shaderList?.get(1)
        mWaveBorderPaint.alpha = progressProps.waveStrokeBackgroundGradient?.angle?.toInt() ?: 255
      }
    }

    private fun update() {
      if(progressProps.waveBackgroundGradient != null) {
        progressProps.waveBackgroundGradientData = getLinearGradientPojo(
          progressProps.waveBackgroundGradient?.name,
          progressProps.waveBackgroundGradient?.angle,
          progressProps.waveBackgroundGradient?.color,
          progressProps.waveBackgroundGradient?.positions,
          progressProps.waveBackgroundColor
        )
      }
      if(progressProps.waveStrokeBackgroundGradient != null) {
        progressProps.waveStrokeBackgroundGradientData = getLinearGradientPojo(
          progressProps.waveStrokeBackgroundGradient?.name,
          progressProps.waveStrokeBackgroundGradient?.angle,
          progressProps.waveStrokeBackgroundGradient?.color,
          progressProps.waveStrokeBackgroundGradient?.positions,
          progressProps.waveStrokeBackgroundColor
        )
      }
      if(progressProps.waveProgressGradient != null) {
        progressProps.waveProgressGradientData = getLinearGradientPojo(
          progressProps.waveProgressGradient?.name,
          progressProps.waveProgressGradient?.angle,
          progressProps.waveProgressGradient?.color,
          progressProps.waveProgressGradient?.positions,
          progressProps.waveProgressColor
        )
      }
      if(progressProps.waveStrokeProgressGradient != null) {
        progressProps.waveStrokeProgressGradientData = getLinearGradientPojo(
          progressProps.waveStrokeProgressGradient?.name,
          progressProps.waveStrokeProgressGradient?.angle,
          progressProps.waveStrokeProgressGradient?.color,
          progressProps.waveStrokeProgressGradient?.positions,
          progressProps.waveStrokeProgressColor
        )
      }
      mWavePaint.isDither = true
      mWavePaint.isAntiAlias = true

      mWaveBorderPaint.isDither = true
      mWaveBorderPaint.isAntiAlias = true
      mWaveBorderPaint.style = Paint.Style.STROKE
      mWaveBorderPaint.strokeWidth = progressProps.strokeWidth
      mWaveBorderPaint.pathEffect = DashPathEffect(
        floatArrayOf(progressProps.strokeDashWidth, progressProps.strokeDashGap),
        0f
      )
      invalidate()
    }

    private fun updateMaxHeight() {
      if(progressProps.waveMaxHeight != getAvailableHeight()) {
        setWaveMaxHeight(getAvailableHeight())
      }
    }

    private fun isParentScrolling(): Boolean {
      var parent = parent as View
      val root = rootView
      while (true) {
        when {
          parent.canScrollHorizontally(1) -> return true
          parent.canScrollHorizontally(-1) -> return true
          parent.canScrollVertically(1) -> return true
          parent.canScrollVertically(-1) -> return true
        }
        if (parent == root)
          return false
        parent = parent.parent as View
      }
    }

    private fun updateProgress(event: MotionEvent) {
      if (progressProps.visibleProgress > 0) {
        progressProps.progress = mProgress - progressProps.visibleProgress * (event.x - mTouchDownX) / getAvailableWidth()
        progressProps.progress = (progressProps.progress).coerceIn(0F, progressProps.maxProgress)
      } else {
        progressProps.progress = progressProps.maxProgress * event.x / getAvailableWidth()
      }
      onProgressChanged?.onProgressChanged(this, progressProps.progress, true)
      invalidate()
    }

    private fun getAvailableWidth() = mCanvasWidth - paddingLeft - paddingRight

    private fun getAvailableHeight() = mCanvasHeight - paddingTop - paddingBottom
}
