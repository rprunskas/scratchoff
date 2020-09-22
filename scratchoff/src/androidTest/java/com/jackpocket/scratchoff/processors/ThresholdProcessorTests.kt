package com.jackpocket.scratchoff.processors

import android.view.MotionEvent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jackpocket.scratchoff.paths.ScratchPathPoint
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThresholdProcessorTests {

    @Test
    fun testCalculatePercentScratchCompletedAlwaysBetweenZeroAndOne() {
        val acceptedRange = 0.0..1.0

        assert(ThresholdProcessor.calculatePercentScratched(-10, 1, 1) in acceptedRange)
        assert(ThresholdProcessor.calculatePercentScratched(10, 1, 1) in acceptedRange)
    }

    @Test
    fun testCountColorMatches() {
        val subject = intArrayOf(0, 0, 1, 1, 1)

        assertEquals(2, ThresholdProcessor.countColorMatches(0, subject))
        assertEquals(3, ThresholdProcessor.countColorMatches(1, subject))
    }

    @Test
    fun testThresholdCalculationMatchesFromHistoryLoad() {
        var scratchPercent: Float = 0.0f

        val processor = ThresholdProcessor(5, 1.0, object: ThresholdProcessor.Delegate {
            override fun postScratchThresholdReached() { }

            override fun postScratchPercentChanged(percent: Float) {
                scratchPercent = percent
            }

            override fun getScratchableLayoutSize(): IntArray {
                return intArrayOf(10, 10)
            }
        })

        val events = listOf(
                ScratchPathPoint(0f, 0f, MotionEvent.ACTION_DOWN),
                ScratchPathPoint(0f, 10f, MotionEvent.ACTION_MOVE)
        )

        processor.prepareBitmapAndCanvasForDrawing()
        processor.enqueueScratchMotionEvents(events)
        processor.safelyReleaseCurrentBitmap()
        processor.prepareBitmapAndCanvasForDrawing()
        processor.drawQueuedScratchMotionEvents()
        processor.processScratchedImagePercent()

        assertEquals(0.5f, scratchPercent)
    }

    @Test
    fun testProcessImageTriggersThresholdReachedOnlyOnce() {
        var thresholdReachedCount: Int = 0

        val processor = ThresholdProcessor(10, 0.5, object: ThresholdProcessor.Delegate {
            override fun postScratchPercentChanged(percent: Float) { }

            override fun postScratchThresholdReached() {
                thresholdReachedCount += 1
            }

            override fun getScratchableLayoutSize(): IntArray {
                return intArrayOf(10, 10)
            }
        })

        val events = listOf(
                ScratchPathPoint(0f, 0f, MotionEvent.ACTION_DOWN),
                ScratchPathPoint(0f, 10f, MotionEvent.ACTION_MOVE)
        )

        processor.prepareBitmapAndCanvasForDrawing()
        processor.enqueueScratchMotionEvents(events)
        processor.drawQueuedScratchMotionEvents()
        processor.processScratchedImagePercent()
        processor.enqueueScratchMotionEvents(events)
        processor.drawQueuedScratchMotionEvents()
        processor.processScratchedImagePercent()

        assertEquals(1, thresholdReachedCount)
    }

    @Test
    fun testScratchPercentNotUpdatesAfterThresholdReachedTriggered() {
        var scratchPercent: Float = 0.0f

        val processor = ThresholdProcessor(1, 0.5, object: ThresholdProcessor.Delegate {
            override fun postScratchThresholdReached() { }

            override fun postScratchPercentChanged(percent: Float) {
                scratchPercent = percent
            }

            override fun getScratchableLayoutSize(): IntArray {
                return intArrayOf(1, 10)
            }
        })

        processor.prepareBitmapAndCanvasForDrawing()
        processor.enqueueScratchMotionEvents(listOf(
                ScratchPathPoint(0f, 0f, MotionEvent.ACTION_DOWN),
                ScratchPathPoint(0f, 3f, MotionEvent.ACTION_MOVE)
        ))
        processor.drawQueuedScratchMotionEvents()
        processor.processScratchedImagePercent()

        assertEquals(0.3f, scratchPercent)

        processor.enqueueScratchMotionEvents(listOf(
                ScratchPathPoint(0f, 3f, MotionEvent.ACTION_DOWN),
                ScratchPathPoint(0f, 6f, MotionEvent.ACTION_MOVE)
        ))
        processor.drawQueuedScratchMotionEvents()
        processor.processScratchedImagePercent()

        assertEquals(0.6f, scratchPercent)

        processor.enqueueScratchMotionEvents(listOf(
                ScratchPathPoint(0f, 6f, MotionEvent.ACTION_DOWN),
                ScratchPathPoint(0f, 10f, MotionEvent.ACTION_MOVE)
        ))
        processor.drawQueuedScratchMotionEvents()
        processor.processScratchedImagePercent()

        assertEquals(0.6f, scratchPercent)
    }
}