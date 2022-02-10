package com.reactnativeaudiowaveform.visualizer

import android.content.Context
import android.graphics.*
import android.util.TypedValue
import com.reactnativeaudiowaveform.gradient.GradientType
import com.reactnativeaudiowaveform.gradient.LinearGradientPojo
import kotlin.math.*

object Utils {

    @JvmStatic
    fun dp(context: Context?, dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context!!.resources.displayMetrics)
    }

    fun roundedRect(useBezier: Boolean, width: Int, height: Int,
                        left: Float, top: Float, right: Float, bottom: Float,
                        topLeftRadius: Float, topRightRadius: Float, bottomLeftRadius: Float, bottomRightRadius: Float
    ): Path {
        val path = Path()
        val maxSize = min(width / 2f, height / 2f)

        var topLeftRadiusAbs = abs(topLeftRadius)
        var topRightRadiusAbs = abs(topRightRadius)
        var bottomLeftRadiusAbs = abs(bottomLeftRadius)
        var bottomRightRadiusAbs = abs(bottomRightRadius)

        if (topLeftRadiusAbs > maxSize) {
          topLeftRadiusAbs = maxSize
        }
        if (topRightRadiusAbs > maxSize) {
          topRightRadiusAbs = maxSize
        }
        if (bottomLeftRadiusAbs > maxSize) {
          bottomLeftRadiusAbs = maxSize
        }
        if (bottomRightRadiusAbs > maxSize) {
          bottomRightRadiusAbs = maxSize
        }

        path.moveTo(left + topLeftRadiusAbs, top)
        path.lineTo(right - topRightRadiusAbs, top)

        if (useBezier) {
          path.quadTo(right, top, right, top + topRightRadiusAbs)
        } else {
          val arc = (if (topRightRadius > 0) 90 else -270).toFloat()
          path.arcTo(RectF(right - topRightRadiusAbs * 2f, top, right, top + topRightRadiusAbs * 2f), -90f, arc)
        }
        path.lineTo(right, bottom - bottomRightRadiusAbs)
        if (useBezier) {
          path.quadTo(right, bottom, right - bottomRightRadiusAbs, bottom)
        } else {
          val arc = (if (bottomRightRadiusAbs > 0) 90 else -270).toFloat()
          path.arcTo(
            RectF(right - bottomRightRadiusAbs * 2f, bottom - bottomRightRadiusAbs * 2f, right, bottom),
            0f,
            arc
          )
        }
        path.lineTo(left + bottomLeftRadiusAbs, bottom)
        if (useBezier) {
          path.quadTo(left, bottom, left, bottom - bottomLeftRadiusAbs)
        } else {
          val arc = (if (bottomLeftRadiusAbs > 0) 90 else -270).toFloat()
          path.arcTo(RectF(left, bottom - bottomLeftRadiusAbs * 2f, left + bottomLeftRadiusAbs * 2f, bottom), 90f, arc)
        }
        path.lineTo(left, top + topLeftRadiusAbs)
        if (useBezier) {
          path.quadTo(left, top, left + topLeftRadiusAbs, top)
        } else {
          val arc = (if (topLeftRadiusAbs > 0) 90 else -270).toFloat()
          path.arcTo(RectF(left, top, left + topLeftRadiusAbs * 2f, top + topLeftRadiusAbs * 2f), 180f, arc)
        }
        path.close()
        return path
    }

    fun shader(gradientType: GradientType, radialRadius: Float?, data: LinearGradientPojo, width: Int, height: Int, left: Float, top: Float, right: Float, bottom: Float): Shader {
      val centerX: Float = (left + right) / 2.0f
      val centerY: Float = (top+ bottom) / 2.0f
      val scaleX = 1.0f
      val scaleY = 1.0f
      val matrix = Matrix()
      matrix.setScale(scaleX, scaleY, centerX, centerY)
      matrix.postRotate(data.angle.toFloat()-45, centerX, centerY)

      return when (gradientType) {
        GradientType.LINEAR -> {
          val shader = LinearGradient(left, top, right, bottom, data.colors, data.positions, Shader.TileMode.CLAMP)
          shader.setLocalMatrix(matrix)
          shader
        }
        GradientType.RADIAL -> {
          val r = radialRadius ?: (max(width, height) / 2f)
          val shader = RadialGradient(centerX, centerY, r, data.colors, data.positions, Shader.TileMode.CLAMP)
          shader.setLocalMatrix(matrix)
          shader
        }
        GradientType.SWEEP -> {
          val shader = SweepGradient(centerX, centerY, data.colors, data.positions)
          shader.setLocalMatrix(matrix)
          shader
        }
      }
    }

    fun getAllShader(
      progressProps: WaveformSeekBarProps,
      mWaveRect: RectF,
      waveHeight: Float
    ): Array<Shader?> {
      val backgroundShaders = if(progressProps.waveBackgroundGradient != null && progressProps.waveBackgroundGradientData != null) shader(
        progressProps.waveBackgroundGradient!!.type,
        progressProps.waveBackgroundGradient!!.radialRadius,
        progressProps.waveBackgroundGradientData!!,
        progressProps.waveWidth.toInt(),
        waveHeight.toInt(),
        mWaveRect.left,
        mWaveRect.top,
        mWaveRect.right,
        mWaveRect.bottom
      ) else null
      val strokeBackgroundShaders = if(progressProps.waveStrokeBackgroundGradient != null && progressProps.waveStrokeBackgroundGradientData != null) shader(
        progressProps.waveStrokeBackgroundGradient!!.type,
        progressProps.waveStrokeBackgroundGradient!!.radialRadius,
        progressProps.waveStrokeBackgroundGradientData!!,
        progressProps.waveWidth.toInt(),
        waveHeight.toInt(),
        mWaveRect.left,
        mWaveRect.top,
        mWaveRect.right,
        mWaveRect.bottom
      ) else null
      val progressShaders = if(progressProps.waveProgressGradient != null && progressProps.waveProgressGradientData != null) shader(
        progressProps.waveProgressGradient!!.type,
        progressProps.waveProgressGradient!!.radialRadius,
        progressProps.waveProgressGradientData!!,
        progressProps.waveWidth.toInt(),
        waveHeight.toInt(),
        mWaveRect.left,
        mWaveRect.top,
        mWaveRect.right,
        mWaveRect.bottom
      ) else null
      val strokeProgressShaders = if(progressProps.waveStrokeProgressGradient != null && progressProps.waveStrokeProgressGradientData != null)  shader(
        progressProps.waveStrokeProgressGradient!!.type,
        progressProps.waveStrokeProgressGradient!!.radialRadius,
        progressProps.waveStrokeProgressGradientData!!,
        progressProps.waveWidth.toInt(),
        waveHeight.toInt(),
        mWaveRect.left,
        mWaveRect.top,
        mWaveRect.right,
        mWaveRect.bottom
      ) else null
      return arrayOf(backgroundShaders, strokeBackgroundShaders, progressShaders, strokeProgressShaders)
    }
}
