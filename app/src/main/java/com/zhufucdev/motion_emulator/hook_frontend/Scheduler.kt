package com.zhufucdev.motion_emulator.hook_frontend

import com.highcapable.yukihookapi.hook.log.loggerD
import com.zhufucdev.motion_emulator.data.Motion
import com.zhufucdev.motion_emulator.data.Point
import com.zhufucdev.motion_emulator.data.Trace
import java.util.concurrent.FutureTask

data class Emulation(val trace: Trace, val motion: Motion, val velocity: Double)

data class Intermediate(val position: Point, val progress: Float)

object Scheduler {
    private var fts = mutableSetOf<FutureTask<Unit>>()
    private val stateListeners = mutableSetOf<(Boolean) -> Unit>()
    private val intermediateListeners = mutableSetOf<(Intermediate) -> Unit>()

    private var mEmulation: Emulation? = null
    private var mIntermediate: Intermediate? = null

    var intermediate: Intermediate?
        set(value) {
            if (value != null) {
                intermediateListeners.forEach { it.invoke(value) }
            }
            mIntermediate = value
        }
        get() = mIntermediate

    var emulation: Emulation?
        set(value) {
            mEmulation = value
            fts.forEach { it.run() }
            stateListeners.forEach { it.invoke(value == null) }
        }
        get() = mEmulation

    fun queue(): Emulation? {
        val ft = FutureTask({}, Unit)
        fts.add(ft)
        ft.get()
        return emulation
    }
    
    fun onEmulationStateChanged(l: (Boolean) -> Unit) {
        stateListeners.add(l)
    }
}