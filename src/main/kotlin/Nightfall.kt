import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2
import org.openrndr.shape.*
import java.io.File
import kotlin.math.abs
import kotlin.system.exitProcess

const val GRID_SIZE = 20
const val RENDER_WIDTH = 4_000
const val RENDER_HEIGHT = (4_000 * 1.4).toInt()

fun main() = application {
    configure {
        width = 42
        height = 42
        hideWindowDecorations = true
    }


    program {
        extend {

            Random.seed = System.currentTimeMillis().toString()

            val quadMap = List(GRID_SIZE) { List(GRID_SIZE) { mutableListOf<Vector2>() } }
            val canvas = renderTarget(RENDER_WIDTH, RENDER_HEIGHT) { colorBuffer() }
            val padding = RENDER_WIDTH * 0.1
            val bounds = Rectangle(padding, padding, RENDER_WIDTH - padding * 2, RENDER_HEIGHT.toDouble())

            drawer.isolatedWithTarget(canvas) {
                ortho(canvas)

                drawer.stroke = ColorRGBa.WHITE
                val points = generateSequence {
                    val x = Random.double(bounds.corner.x, bounds.corner.x + bounds.width)
                    val y = weightedRandom(padding, bounds.height)

                    Vector2(x, y)
                }.take(7_000).toMutableList()

                val forceField = Circle(RENDER_WIDTH / 2.0, RENDER_HEIGHT / 2.0, 600.0)

                points.withIndex().forEach { (index, point) ->
                    if (forceField.contains(point)) {
                        val distance = point.distanceTo(forceField.center)
                        val moved = pointAlong(point, forceField.center, distance / -forceField.radius)
                        points[index] = moved
                    }

                }

                points.forEach {
                    val (x, y) = it
                    val xI = (x / RENDER_WIDTH * GRID_SIZE).toInt() - 1
                    val yI = (y / RENDER_HEIGHT * GRID_SIZE).toInt() - 1
                    quadMap[yI][xI].add(it)
                }

                points.forEach { point ->

                    val neighbors = getCloseNeighbors(quadMap, point)

                    neighbors.filter { point.distanceTo(it) < Random.gaussian(180.0, 2.0) }
                        .forEach {
                            drawer.lineStrip(listOf(it, point))
                        }
                }

                canvas.colorBuffer(0).saveToFile(File(GenerativeArt.getFilename("Nightfall")), async = false)
                exitProcess(0)
            }

        }
    }
}

fun pointAlong(p1: Vector2, p2: Vector2, percentage: Double = 0.5): Vector2 {
    return Vector2(p1.x + (p2.x - p1.x) * percentage, p1.y + (p2.y - p1.y) * percentage);
}

fun getNeighbors(quadMap: List<List<List<Vector2>>>, point: Vector2): List<Vector2> {
    val xI = (point.x / RENDER_WIDTH * GRID_SIZE).toInt() - 1
    val yI = (point.y / RENDER_HEIGHT * GRID_SIZE).toInt() - 1

    val nodes = mutableListOf<Vector2>()

    for (y in yI - 1..yI + 1) {
        for (x in xI - 1..xI + 1) {
            if (y in 0..GRID_SIZE && x in 0..GRID_SIZE) {
                nodes.addAll(quadMap[y][x])
            }
        }
    }
    return nodes
}

fun getCloseNeighbors(quadMap: List<List<List<Vector2>>>, point: Vector2): List<Vector2> {
    val xI = (point.x / RENDER_WIDTH * GRID_SIZE).toInt() - 1
    val yI = (point.y / RENDER_HEIGHT * GRID_SIZE).toInt() - 1

    return quadMap[yI][xI]
}

fun weightedRandom(min: Double, max: Double): Double {
    val a = Random.double(min, max);
    val b = Random.double(min, max);

    return min.coerceAtLeast(abs(b - a))
}

