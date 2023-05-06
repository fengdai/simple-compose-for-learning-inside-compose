package com.github.takahirom.compose

import androidx.compose.runtime.Composition
import androidx.compose.runtime.Recomposer
import kotlinx.coroutines.*

@OptIn(DelicateCoroutinesApi::class)
fun Composition.launchComposeInsideLogger(composer: Recomposer, mainScope: CoroutineScope) {
    mainScope.launch {
        composer.currentState.collect {
            println("composer:$it")
        }
    }
    val slotTable = Class.forName("androidx.compose.runtime.CompositionImpl")
        .getDeclaredField("slotTable")
        .apply {
            isAccessible = true
        }
        .get(this)
    GlobalScope.launch {
        var lastSlotTableString = ""
        while (true) {
            val slotTableString = Class.forName("androidx.compose.runtime.SlotTable")
                .getMethod("asString")
                .apply {
                    isAccessible = true
                }
                .invoke(slotTable) as String

            if (slotTableString != lastSlotTableString) {
                lastSlotTableString = slotTableString
                val groups = Class.forName("androidx.compose.runtime.SlotTable")
                    .getDeclaredField("groups")
                    .apply {
                        isAccessible = true
                    }
                    .get(slotTable) as IntArray
                val slots = Class.forName("androidx.compose.runtime.SlotTable")
                    .getDeclaredField("slots")
                    .apply {
                        isAccessible = true
                    }
                    .get(slotTable) as Array<*>


                println("------")
                println("slotTable:")
                println(slotTableString)
                println("groups:")
                groups.toList().windowed(
                    size = 5,
                    step = 5,
                    partialWindows = false
                )
                    .forEachIndexed { index, group ->
                        val (key, groupInfo, parentAnchor, size, dataAnchor) = group
                        println(
                            "index: $index, " +
                                    "key: $key, " +
                                    "groupInfo: $groupInfo, " +
                                    "parentAnchor: $parentAnchor, " +
                                    "size: $size, " +
                                    "dataAnchor: $dataAnchor"
                        )
                    }
                println("slots:")
                println(slots.mapIndexed { index, slot -> index to slot }.joinToString("\n") { (index, slot) ->
                    "$index: $slot(${slot?.javaClass})"
                })
            }
            yield()
        }
    }
}