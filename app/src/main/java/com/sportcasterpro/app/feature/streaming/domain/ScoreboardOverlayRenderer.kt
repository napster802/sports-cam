package com.sportcasterpro.app.feature.streaming.domain

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RectF
import android.view.Surface
import com.pedro.encoder.input.gl.render.filters.`object`.SurfaceFilterRender
import com.pedro.library.view.GlInterface
import com.sportcasterpro.app.core.domain.model.Sport
import com.sportcasterpro.app.feature.scoreboard.domain.PossessionTeam
import com.sportcasterpro.app.feature.scoreboard.domain.ScoreboardState
import javax.inject.Inject

private const val CARD_WIDTH_FRACTION = 0.92f
private const val CARD_TOP_MARGIN_FRACTION = 0.03f
private const val CARD_HEIGHT_FRACTION = 0.12f
private const val SETS_ROW_HEIGHT_FRACTION = 0.05f
private val POSSESSION_COLOR = Color.parseColor("#FFB300")

/**
 * Draws [ScoreboardState] directly into RootEncoder's GL pipeline via a full-frame
 * [SurfaceFilterRender], so the scoreboard ends up in the actual outgoing RTMP stream and local
 * recording rather than only the on-screen Compose preview. RootEncoder encodes frames straight
 * from its OpenGL surface and never sees anything Android draws on top of `OpenGlView` in the
 * view hierarchy (see `ScoreboardOverlay`, which is preview-only) — this is the only way for
 * viewers/recordings to see the scoreboard.
 *
 * [SurfaceFilterRender]'s backing [Surface] is sized to the full encoded frame (its `getWidth`/
 * `getHeight` come from the stream resolution, not an object's own size), so the scoreboard card
 * is drawn at a fixed position within that full canvas and the rest of the canvas is left
 * transparent, rather than scaling/positioning a smaller sub-surface.
 */
class ScoreboardOverlayRenderer @Inject constructor() {

    private val filterRender = SurfaceFilterRender { onSurfaceReady() }
    private var surface: Surface? = null
    private var glInterface: GlInterface? = null
    private var lastState: ScoreboardState? = null
    private var isReady = false

    private fun onSurfaceReady() {
        isReady = true
        surface = filterRender.surface
        render(lastState)
    }

    fun attach(glInterface: GlInterface) {
        this.glInterface = glInterface
        glInterface.addFilter(filterRender)
    }

    fun detach() {
        glInterface?.removeFilter(filterRender)
        glInterface = null
    }

    fun render(state: ScoreboardState?) {
        lastState = state
        val targetSurface = surface ?: return
        val canvas = targetSurface.lockCanvas(null) ?: return
        try {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            if (state != null) drawScoreboard(canvas, state)
        } finally {
            targetSurface.unlockCanvasAndPost(canvas)
        }
    }

    fun release() {
        if (!isReady) return
        filterRender.release()
        surface = null
        isReady = false
    }

    private fun drawScoreboard(canvas: Canvas, state: ScoreboardState) {
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()
        if (width <= 0f || height <= 0f) return

        val showSetsRow = state.sport == Sport.TENNIS || state.sport == Sport.VOLLEYBALL
        val cardWidth = width * CARD_WIDTH_FRACTION
        val cardLeft = (width - cardWidth) / 2f
        val cardTop = height * CARD_TOP_MARGIN_FRACTION
        val mainRowHeight = height * CARD_HEIGHT_FRACTION
        val setsRowHeight = if (showSetsRow) height * SETS_ROW_HEIGHT_FRACTION else 0f
        val cardRect = RectF(cardLeft, cardTop, cardLeft + cardWidth, cardTop + mainRowHeight + setsRowHeight)
        val cornerRadius = mainRowHeight * 0.18f

        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(184, 0, 0, 0) }
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, backgroundPaint)

        val horizontalPadding = cardWidth * 0.04f
        val mainRowCenterY = cardTop + mainRowHeight / 2f
        val namePaint = textPaint(mainRowHeight * 0.22f, Color.WHITE)
        val scorePaint = textPaint(mainRowHeight * 0.42f, Color.WHITE)
        val centerLabelPaint = textPaint(mainRowHeight * 0.2f, Color.argb(217, 255, 255, 255))
        val timerPaint = textPaint(mainRowHeight * 0.26f, Color.WHITE)

        val homeX = cardRect.left + horizontalPadding
        namePaint.textAlign = Paint.Align.LEFT
        scorePaint.textAlign = Paint.Align.LEFT
        canvas.drawText(state.homeTeam.name.ifBlank { "Team" }, homeX, mainRowCenterY - mainRowHeight * 0.06f, namePaint)
        canvas.drawText(state.displayScoreFor(isHome = true), homeX, mainRowCenterY + mainRowHeight * 0.28f, scorePaint)

        val awayX = cardRect.right - horizontalPadding
        namePaint.textAlign = Paint.Align.RIGHT
        scorePaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(state.awayTeam.name.ifBlank { "Team" }, awayX, mainRowCenterY - mainRowHeight * 0.06f, namePaint)
        canvas.drawText(state.displayScoreFor(isHome = false), awayX, mainRowCenterY + mainRowHeight * 0.28f, scorePaint)

        centerLabelPaint.textAlign = Paint.Align.CENTER
        timerPaint.textAlign = Paint.Align.CENTER
        val centerX = cardRect.centerX()
        canvas.drawText(state.periodLabel, centerX, mainRowCenterY - mainRowHeight * 0.06f, centerLabelPaint)
        canvas.drawText(formatTimer(state.timerMillisRemaining), centerX, mainRowCenterY + mainRowHeight * 0.28f, timerPaint)

        if (state.sport.hasPossessionIndicator && state.possession != PossessionTeam.NONE) {
            val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = POSSESSION_COLOR }
            val dotRadius = mainRowHeight * 0.06f
            val dotY = mainRowCenterY - mainRowHeight * 0.16f
            val dotX = if (state.possession == PossessionTeam.HOME) {
                homeX + namePaint.measureText(state.homeTeam.name.ifBlank { "Team" }) + dotRadius * 2.5f
            } else {
                awayX - namePaint.measureText(state.awayTeam.name.ifBlank { "Team" }) - dotRadius * 2.5f
            }
            canvas.drawCircle(dotX, dotY, dotRadius, dotPaint)
        }

        if (showSetsRow) {
            val setsPaint = textPaint(setsRowHeight * 0.55f, Color.argb(179, 255, 255, 255))
            setsPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(
                "Sets ${state.homePeriodsWon} - ${state.awayPeriodsWon}",
                cardRect.left + horizontalPadding,
                cardTop + mainRowHeight + setsRowHeight * 0.7f,
                setsPaint,
            )
        }
    }

    private fun textPaint(textSizePx: Float, textColor: Int): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = textSizePx
        color = textColor
        isFakeBoldText = true
    }

    private fun formatTimer(millisRemaining: Long): String {
        val totalSeconds = millisRemaining / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}
