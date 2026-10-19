package com.kociarcade

import android.app.Activity
import android.os.Bundle
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.media.AudioManager
import android.media.ToneGenerator
import android.view.MotionEvent
import android.view.View
import kotlin.math.hypot
import kotlin.random.Random

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(20, 18, 38)
        window.navigationBarColor = Color.rgb(20, 18, 38)
        setContentView(CatGameView(this))
    }
}

class CatGameView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 65)
    private val prefs = context.getSharedPreferences("koci_arcade", Context.MODE_PRIVATE)
    private var score = 0
    private var best = prefs.getInt("best", 0)
    private var catches = prefs.getInt("catches", 0)
    private var mode = 0
    private var difficulty = 1
    private var sound = true
    private var playing = false
    private var startTime = 0L
    private var duration = 30_000L
    private var x = 200f
    private var y = 400f
    private var radius = 45f
    private var color = Color.MAGENTA
    private var remaining = 30
    private var message = "Dotknij START i łap cele!"
    private val colors = intArrayOf(Color.rgb(255, 94, 168), Color.rgb(70, 210, 255), Color.rgb(255, 204, 70), Color.rgb(150, 255, 120))

    private fun text(c: Canvas, value: String, xx: Float, yy: Float, size: Float, col: Int = Color.WHITE, bold: Boolean = true) {
        paint.color = col
        paint.textSize = size
        paint.typeface = if (bold) Typeface.create("sans-serif", Typeface.BOLD) else Typeface.create("sans-serif", Typeface.NORMAL)
        c.drawText(value, xx, yy, paint)
    }

    private fun button(c: Canvas, label: String, xx: Float, yy: Float, ww: Float, hh: Float, col: Int) {
        paint.color = col
        c.drawRoundRect(xx, yy, xx + ww, yy + hh, 24f, 24f, paint)
        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.typeface = Typeface.DEFAULT_BOLD
        val tw = paint.measureText(label)
        c.drawText(label, xx + (ww - tw) / 2, yy + hh / 2 + 8, paint)
    }

    private fun spawn() {
        val pad = 65f
        x = Random.nextFloat() * (width - pad * 2).coerceAtLeast(1f) + pad
        y = Random.nextFloat() * (height * 0.48f).coerceAtLeast(1f) + height * 0.30f
        radius = when (difficulty) { 0 -> 58f; 1 -> 43f; else -> 30f }
        color = colors[Random.nextInt(colors.size)]
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        c.drawColor(Color.rgb(20, 18, 38))
        paint.color = Color.rgb(37, 31, 65)
        c.drawCircle(width * .12f, height * .18f, 95f, paint)
        c.drawCircle(width * .92f, height * .30f, 125f, paint)
        text(c, "🐾 KOCI ARCADE", 28f, 62f, 28f, Color.rgb(255, 195, 90))
        text(c, "Wynik: $score", 28f, 105f, 23f)
        text(c, "Rekord: $best", 28f, 140f, 18f, Color.rgb(180, 175, 210), false)
        if (playing) {
            remaining = ((duration - (System.currentTimeMillis() - startTime)) / 1000L).toInt().coerceAtLeast(0)
            text(c, "Czas: $remaining s", width - 150f, 105f, 20f, Color.WHITE)
            paint.color = color
            c.drawCircle(x, y, radius, paint)
            paint.color = Color.WHITE
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 4f
            c.drawCircle(x, y, radius * .65f, paint)
            paint.style = Paint.Style.FILL
            text(c, "✦", x - 12f, y + 12f, 30f)
            if (remaining <= 0) finishGame()
            else postInvalidateDelayed(100)
        } else {
            text(c, message, 28f, height * .29f, 20f, Color.rgb(235, 225, 255))
            button(c, "START", width * .16f, height * .36f, width * .68f, 66f, Color.rgb(180, 80, 255))
            button(c, "Tryb: " + arrayOf("Łap kolory", "Szybkie łapy", "Spokojna zabawa")[mode], width * .10f, height * .36f + 86f, width * .80f, 56f, Color.rgb(55, 90, 160))
            button(c, "Trudność: " + arrayOf("Łatwa", "Średnia", "Trudna")[difficulty], width * .10f, height * .36f + 156f, width * .80f, 56f, Color.rgb(55, 125, 115))
            button(c, "Dźwięk: " + if (sound) "WŁ." else "WYŁ.", width * .10f, height * .36f + 226f, width * .80f, 56f, Color.rgb(120, 65, 120))
            text(c, "Łącznie złapano: $catches", 28f, height * .36f + 330f, 18f, Color.rgb(180, 175, 210), false)
            text(c, "Rekord: $best", 28f, height * .36f + 362f, 18f, Color.rgb(180, 175, 210), false)
        }
    }

    private fun finishGame() {
        playing = false
        if (score > best) best = score
        prefs.edit().putInt("best", best).putInt("catches", catches).apply()
        message = "Koniec rundy! Wynik: $score"
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) return true
        val tx = event.x
        val ty = event.y
        if (playing) {
            if (hypot(tx - x, ty - y) <= radius * 1.8f) {
                score += if (mode == 1) 2 else 1
                catches++
                if (sound) tone.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
                spawn()
                invalidate()
            }
        } else {
            if (ty in (height * .36f)..(height * .36f + 76f)) {
                score = 0
                playing = true
                startTime = System.currentTimeMillis()
                duration = if (mode == 2) 45_000L else 30_000L
                message = ""
                spawn()
            } else if (ty in (height * .36f + 80f)..(height * .36f + 150f)) {
                mode = (mode + 1) % 3
            } else if (ty in (height * .36f + 152f)..(height * .36f + 220f)) {
                difficulty = (difficulty + 1) % 3
            } else if (ty in (height * .36f + 224f)..(height * .36f + 292f)) {
                sound = !sound
            }
            invalidate()
        }
        return true
    }
}
