package nacholab.scanera.lib.utils

import kotlin.math.hypot

fun calculateDistance(
    x1: Double,
    y1: Double,
    x2: Double,
    y2: Double
) = hypot(x2 - x1, y2 - y1)

