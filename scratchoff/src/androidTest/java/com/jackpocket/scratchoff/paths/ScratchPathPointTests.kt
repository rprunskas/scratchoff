package com.jackpocket.scratchoff.paths

import android.view.MotionEvent
import androidx.test.core.view.PointerCoordsBuilder
import androidx.test.core.view.PointerPropertiesBuilder
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScratchPathPointTests {

    @Test
    fun testMotionEventWithMultiplePointersMapsToScratchPathPoint() {
        val pointerCount = 10
        val expectedActions = listOf(
                MotionEvent.ACTION_DOWN,
                MotionEvent.ACTION_MOVE,
                MotionEvent.ACTION_UP
        )

        val events = expectedActions
                .map({ action ->
                    val properties = 0.until(pointerCount)
                            .map({
                                PointerPropertiesBuilder
                                        .newBuilder()
                                        .setId(it)
                                        .build()
                            })
                            .toTypedArray()

                    val coordinates = 0.until(pointerCount)
                            .map({
                                PointerCoordsBuilder
                                        .newBuilder()
                                        .setCoords(10f, 20f)
                                        .build()
                            })
                            .toTypedArray()

                    MotionEvent.obtain(0, 0, action, pointerCount, properties, coordinates, 0, 0, 0f, 0f, 0, 0, 0, 0)
                })
                .map(ScratchPathPoint::create)
                .flatten()

        events.chunked(pointerCount)
                .forEachIndexed({ actionIndex, groupedEvents ->
                    groupedEvents.forEachIndexed({ index, event ->
                        val pointerIndex = index.rem(pointerCount)

                        assertEquals(pointerIndex, event.pointerIndex)
                        assertEquals(10f, event.x)
                        assertEquals(20f, event.y)
                        assertEquals(expectedActions[actionIndex], event.action)
                    })
        })
    }
}