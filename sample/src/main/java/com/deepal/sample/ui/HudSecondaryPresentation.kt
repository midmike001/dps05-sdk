package com.deepal.sample.ui

import android.app.Activity
import android.app.Presentation
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Display
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.deepal.sdk.DeepalS05Telemetry

/**
 * Native Secondary Display Presentation for Deepal S05 800x480 Windshield AR-HUD.
 * Matches the optical layout specifications in HUD_DEVELOPMENT_GUIDE.md.
 */
class HudSecondaryPresentation(
    activity: Activity,
    display: Display
) : Presentation(activity, display) {

    companion object {
        const val HUD_WIDTH = 800
        const val HUD_HEIGHT = 480
        const val MAP_X = 573
        const val MAP_Y = 167
        const val MAP_W = 227
        const val MAP_H = 188
    }

    private lateinit var tvSoC: TextView
    private lateinit var tvSpeed: TextView
    private lateinit var tvGear: TextView
    private lateinit var tvManeuverArrow: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvRoadName: TextView
    private lateinit var mapPlaceholder: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Root Pure Black Canvas (RGB 0,0,0 transparent projection)
        val root = FrameLayout(context).apply {
            setBackgroundColor(Color.BLACK)
            layoutParams = ViewGroup.LayoutParams(HUD_WIDTH, HUD_HEIGHT)
        }

        // 2. Left Telemetry Zone (x=40)
        val leftZone = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val lp = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
                marginStart = 40
            }
            layoutParams = lp

            tvSoC = TextView(context).apply {
                text = "⚡ 78% • 345km"
                setTextColor(Color.parseColor("#00E5FF"))
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
            }
            addView(tvSoC)

            val tvLimit = TextView(context).apply {
                text = "LIMIT 60"
                setTextColor(Color.parseColor("#FF5252"))
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                setPadding(0, 10, 0, 0)
            }
            addView(tvLimit)

            val tvTpms = TextView(context).apply {
                text = "TPMS 2.4 | 2.4 bar"
                setTextColor(Color.parseColor("#94A3B8"))
                textSize = 12f
                setPadding(0, 8, 0, 0)
            }
            addView(tvTpms)
        }
        root.addView(leftZone)

        // 3. Center Navigation Axis (Gravity.CENTER)
        val centerZone = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            val lp = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
            layoutParams = lp

            val tvChevrons = TextView(context).apply {
                text = "▲  ▲  ▲  ▲"
                setTextColor(Color.parseColor("#00E5FF"))
                textSize = 18f
                gravity = Gravity.CENTER
            }
            addView(tvChevrons)

            tvManeuverArrow = TextView(context).apply {
                text = "↰"
                setTextColor(Color.WHITE)
                textSize = 46f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
            }
            addView(tvManeuverArrow)

            tvDistance = TextView(context).apply {
                text = "250m"
                setTextColor(Color.WHITE)
                textSize = 28f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
            }
            addView(tvDistance)

            val speedGearLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER

                tvGear = TextView(context).apply {
                    text = "D "
                    setTextColor(Color.parseColor("#00E5FF"))
                    textSize = 20f
                    typeface = Typeface.DEFAULT_BOLD
                }
                addView(tvGear)

                tvSpeed = TextView(context).apply {
                    text = "42 km/h"
                    setTextColor(Color.WHITE)
                    textSize = 20f
                    typeface = Typeface.DEFAULT_BOLD
                }
                addView(tvSpeed)
            }
            addView(speedGearLayout)
        }
        root.addView(centerZone)

        // 4. Right Zone: Road Name ribbon and Optical Map Window (x=573, y=167, w=227, h=188)
        tvRoadName = TextView(context).apply {
            text = "Preah Monivong Blvd (A)"
            setTextColor(Color.parseColor("#4DD0E1"))
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            val lp = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = MAP_X - 20
                topMargin = MAP_Y - 32
            }
            layoutParams = lp
        }
        root.addView(tvRoadName)

        mapPlaceholder = FrameLayout(context).apply {
            setBackgroundColor(Color.parseColor("#10192C"))
            val lp = FrameLayout.LayoutParams(MAP_W, MAP_H).apply {
                marginStart = MAP_X
                topMargin = MAP_Y
            }
            layoutParams = lp

            val label = TextView(context).apply {
                text = "OPTICAL MAP\nTEXTURE VIEW\n[227 x 188]"
                setTextColor(Color.parseColor("#64748B"))
                textSize = 11f
                gravity = Gravity.CENTER
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            addView(label)
        }
        root.addView(mapPlaceholder)

        setContentView(root)
    }

    fun updateTelemetry(t: DeepalS05Telemetry) {
        tvSoC.text = "⚡ ${t.batterySocPercent}% • ${t.remainingRangeKm}km"
        tvSpeed.text = "${t.speedKmh.toInt()} km/h"
        tvGear.text = "${t.gear} "
    }

    fun updateManeuver(arrow: String, dist: String, road: String) {
        tvManeuverArrow.text = arrow
        tvDistance.text = dist
        tvRoadName.text = "$road (A)"
    }
}
