package me.wsj.lib.specialeffects

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.animation.LinearInterpolator
import androidx.collection.ArraySet
import kotlinx.coroutines.*
import me.wsj.lib.specialeffects.entity.Cloud
import me.wsj.lib.specialeffects.entity.Rain
import per.wsj.commonlib.utils.LogUtil
import java.util.*
import kotlin.math.PI
import kotlin.math.sin

/**
 * create by shiju.wang
 * cloud
 */
class EffectCloudDrawable(val clouds: Array<Drawable>) :
    Drawable(), ICancelable {

    private val scope by lazy { CoroutineScope(Job() + Dispatchers.Main) }

    val random = Random()

    val speed = 1

    private var mWidth = 0

    val cloudList = ArrayList<Cloud>()

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)

        mWidth = bounds.right - bounds.left

        for (i in clouds.indices) {
            val nextX = random.nextInt(mWidth)
            val nextY = random.nextInt((mWidth * 0.7).toInt())

            cloudList.add(
                Cloud(
                    nextX.toFloat(),
                    nextY.toFloat(),
                    random.nextFloat() * speed + speed,
                    i
                )
            )
        }

        startAnim()
    }

    @Volatile
    var isRunning = false

    private fun startAnim() {
        if (!isRunning) {
            isRunning = true
            scope.launch {
                while (isRunning) {
                    withContext(Dispatchers.Default) {
                        delay(25)
                    }
                    updatePosition()
                }
            }
        }
    }

    private fun updatePosition() {
        cloudList.forEach {
            it.x += it.speed
            val drawable = clouds[it.type]
            if (it.x > mWidth + drawable.intrinsicWidth) {
                it.x = 0f
                it.y = random.nextInt((mWidth * 0.7).toInt()).toFloat()
                it.speed = random.nextFloat() * speed + speed
            }
        }
        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        for (cloud in cloudList) {
            val drawable = clouds[cloud.type]
            drawable.setBounds(
                (cloud.x - drawable.intrinsicWidth).toInt(),
                (cloud.y).toInt(),
                cloud.x.toInt(),
                (cloud.y + drawable.intrinsicHeight).toInt()
            )

            drawable.draw(canvas)
        }
    }

    override fun setAlpha(alpha: Int) {

    }

    override fun setColorFilter(colorFilter: ColorFilter?) {

    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun start() {
        //To do sth.
    }

    override fun cancel() {
        isRunning = false
        scope.cancel()
        LogUtil.d("Effect2Drawable cancel ---------------------------> ")
    }
}