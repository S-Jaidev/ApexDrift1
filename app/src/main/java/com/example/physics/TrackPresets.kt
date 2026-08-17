package com.example.physics

import kotlin.math.atan2

object TrackPresets {

    fun getPresetTracks(): List<TrackDef> {
        return listOf(
            createTokyoExpressway(),
            createRedwoodRidge(),
            createSaharaDunes(),
            createCyberGrid()
        )
    }

    fun getTrackById(id: String, customTracks: List<TrackDef> = emptyList()): TrackDef {
        return (getPresetTracks() + customTracks).find { it.id == id }
            ?: createTokyoExpressway()
    }

    private fun createTokyoExpressway(): TrackDef {
        val nodes = listOf(
            Vector2D(0f, 0f),
            Vector2D(600f, 0f),
            Vector2D(1000f, 200f),
            Vector2D(1200f, 600f),
            Vector2D(1100f, 1000f),
            Vector2D(700f, 1100f),
            Vector2D(500f, 800f),
            Vector2D(300f, 900f),
            Vector2D(100f, 1200f),
            Vector2D(-400f, 1100f),
            Vector2D(-700f, 700f),
            Vector2D(-600f, 300f),
            Vector2D(-300f, 100f)
        )
        return buildTrackDef(
            id = "tokyo_expressway",
            name = "Tokyo Expressway",
            description = "High-speed urban highway with sweeping curves & neon night reflections.",
            surfaceType = "Asphalt",
            surfaceFriction = 1.0f,
            nodes = nodes,
            totalLaps = 3
        )
    }

    private fun createRedwoodRidge(): TrackDef {
        val nodes = listOf(
            Vector2D(0f, 0f),
            Vector2D(400f, -100f),
            Vector2D(800f, 100f),
            Vector2D(1000f, 500f),
            Vector2D(700f, 800f),
            Vector2D(800f, 1200f),
            Vector2D(400f, 1400f),
            Vector2D(100f, 1100f),
            Vector2D(-200f, 1300f),
            Vector2D(-600f, 1000f),
            Vector2D(-500f, 500f),
            Vector2D(-300f, 200f)
        )
        return buildTrackDef(
            id = "redwood_ridge",
            name = "Redwood Ridge",
            description = "Technical alpine mountain circuit with elevation changes and tight S-bends.",
            surfaceType = "Asphalt",
            surfaceFriction = 0.95f,
            nodes = nodes,
            totalLaps = 3
        )
    }

    private fun createSaharaDunes(): TrackDef {
        val nodes = listOf(
            Vector2D(0f, 0f),
            Vector2D(500f, 0f),
            Vector2D(900f, 300f),
            Vector2D(800f, 800f),
            Vector2D(400f, 1000f),
            Vector2D(100f, 700f),
            Vector2D(-300f, 900f),
            Vector2D(-700f, 600f),
            Vector2D(-500f, 200f)
        )
        return buildTrackDef(
            id = "sahara_dunes",
            name = "Sahara Dunes Rally",
            description = "Low-traction sand & gravel rally track built for long, thrilling drifts.",
            surfaceType = "Dirt",
            surfaceFriction = 0.65f,
            nodes = nodes,
            totalLaps = 3
        )
    }

    private fun createCyberGrid(): TrackDef {
        val nodes = listOf(
            Vector2D(0f, 0f),
            Vector2D(700f, 0f),
            Vector2D(1100f, 400f),
            Vector2D(1100f, 900f),
            Vector2D(700f, 1300f),
            Vector2D(0f, 1300f),
            Vector2D(-400f, 900f),
            Vector2D(-400f, 400f)
        )
        return buildTrackDef(
            id = "cyber_speedway",
            name = "Neon Cyber Grid",
            description = "Futuristic neon-lit speedway designed for hypercar top-speed racing.",
            surfaceType = "CyberGrid",
            surfaceFriction = 1.1f,
            nodes = nodes,
            totalLaps = 3
        )
    }

    fun buildTrackDef(
        id: String,
        name: String,
        description: String,
        surfaceType: String,
        surfaceFriction: Float,
        nodes: List<Vector2D>,
        trackWidth: Float = 160f,
        totalLaps: Int = 3
    ): TrackDef {
        val checkpoints = mutableListOf<TrackCheckpoint>()
        for (i in nodes.indices) {
            val curr = nodes[i]
            val next = nodes[(i + 1) % nodes.size]
            val dir = (next - curr).normalized()
            checkpoints.add(
                TrackCheckpoint(
                    id = i,
                    center = curr,
                    direction = dir,
                    width = trackWidth
                )
            )
        }
        return TrackDef(
            id = id,
            name = name,
            description = description,
            surfaceType = surfaceType,
            surfaceFriction = surfaceFriction,
            trackWidth = trackWidth,
            totalLaps = totalLaps,
            nodes = nodes,
            checkpoints = checkpoints
        )
    }
}
