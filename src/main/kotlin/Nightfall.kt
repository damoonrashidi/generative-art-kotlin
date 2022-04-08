import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle
import org.openrndr.shape.drawComposition
import kotlin.math.*
import kotlin.system.exitProcess

const val GRID_SIZE = 20
const val RENDER_WIDTH = 5000
const val RENDER_HEIGHT = (5000 * 1.4).toInt()
const val POINT_COUNT = 15_000

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
            val bounds = Rectangle(padding, padding, RENDER_WIDTH - (padding * 2), RENDER_HEIGHT.toDouble())

            val forceFields = generateSequence {
                Circle(RENDER_WIDTH * Random.double(0.01, 1.0),
                    RENDER_HEIGHT * Random.double(0.01, 1.0),
                    Random.double(400.0, 900.0))
            }.take(Random.int0(5)).toList()

            drawer.isolatedWithTarget(canvas) {
                ortho(canvas)

                val svg = drawComposition {
                    this.strokeWeight = 1.0
                    this.stroke = ColorRGBa.BLACK
                    drawer.stroke = ColorRGBa.WHITE
                    val points = generateSequence {
                        val x = Random.double(bounds.corner.x, bounds.corner.x + bounds.width)
                        val y = weightedRandom(padding, bounds.height)
                        Vector2(x, y)
                    }.take(POINT_COUNT).filter { it.y > padding + 0.01 }.toMutableList()

                    points.withIndex().forEach { (index, point) ->
                        forceFields.forEach {
                            val distance = point.distanceTo(it.center)
                            if (distance < it.radius) {
                                val angle = atan2(point.y - it.center.y, point.x - it.center.x) * 180 / PI
                                val force = -distance / it.radius
                                val x = point.x + map(0.0, 0.0, 0.0, it.radius, cos(angle) * force)
                                val y = point.y + map(0.0, 0.0, 0.0, it.radius, sin(angle) * force)
                                val moved = Vector2(x, y)
                                points[index] = moved
                            }
                        }
                    }

                    points.forEach {
                        val (x, y) = it
                        val xI = ((x / RENDER_WIDTH * GRID_SIZE).toInt()).coerceIn(0 until GRID_SIZE)
                        val yI = ((y / RENDER_HEIGHT * GRID_SIZE).toInt()).coerceIn(0 until GRID_SIZE)
                        quadMap[yI][xI].add(it)
                    }


                    quadMap.forEach { cell ->
                        cell.forEach { points ->
                            points.forEach { point ->
                                val close = getCloseNeighbors(quadMap, point)
                                val neighbors = close.filter {
                                    it.distanceTo(point) < 90.0
                                }.take(8)
                                neighbors.forEach {
                                    drawer.lineStrip(listOf(it, point))
                                    this.lineStrip(listOf(it, point))
                                }
                            }
                        }
                    }
                }

                GenerativeArt.saveOutput("Nightfall",
                    canvas,
                    svg,
                    Vector2(RENDER_WIDTH.toDouble(), RENDER_HEIGHT.toDouble()))
                exitProcess(0)
            }
        }
    }
}

fun getNeighbors(quadMap: QuadMap<Vector2>, point: Vector2): List<Vector2> {
    val xI = (point.x / RENDER_WIDTH * GRID_SIZE).toInt() - 1
    val yI = (point.y / RENDER_HEIGHT * GRID_SIZE).toInt() - 1

    val nodes = mutableListOf<Vector2>()

    for (y in yI - 1..yI + 1) {
        for (x in xI - 1..xI + 1) {
            if (y in 0..GRID_SIZE && x in 0..GRID_SIZE) {
                nodes.addAll(quadMap[y][x].map { it })
            }
        }
    }
    return nodes
}

fun getCloseNeighbors(quadMap: QuadMap<Vector2>, point: Vector2): List<Vector2> {
    val xI = ((point.x / RENDER_WIDTH * GRID_SIZE).toInt()).coerceIn(quadMap.indices)
    val yI = ((point.y / RENDER_HEIGHT * GRID_SIZE).toInt()).coerceIn(quadMap.indices)

    return quadMap[yI][xI]
}

fun weightedRandom(min: Double, max: Double): Double {
    val a = Random.double(min, max)
    val b = Random.double(min, max)

    return min.coerceAtLeast(abs(b - a))
}

