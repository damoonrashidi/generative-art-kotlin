import org.openrndr.application
import org.openrndr.color.ColorHSLa
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extra.noclear.NoClear
import org.openrndr.extra.noise.Random
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle
import org.openrndr.shape.Triangle
import org.openrndr.shape.drawComposition
import kotlin.math.cos
import kotlin.math.sin
import kotlin.system.exitProcess

fun main() = application {
    configure {
        width = 32
        height = 32
        hideWindowDecorations = true
        position = IntVector2(0, 0)
    }

    program {
        extend(NoClear())
        extend {
            drawer.stroke = null
            val seed = System.currentTimeMillis().toString()
            Random.seed = seed
            val renderWidth = 2_000
            val renderHeight = (2_000 * 1.4).toInt()
            val bounds = Rectangle(0.0, 0.0, renderWidth.toDouble(), renderHeight.toDouble())
            val quads = CollisionDetection<Circle>(listOf(), bounds, 15)

            val zoom = Random.int(900, 2_000)
            val distort = Random.double(4.0, 5.2)
            val linePadding = 5.0
            val allowEdgeOverflow = Random.bool(0.25)
            val allowHeavy = Random.bool(0.6)
            val backgroundColor = ColorHSLa(35.0, 0.15, 0.95).toRGBa()
            val palette = Palette.random()
            val minLineLength = 20.0
            val maxLineLength = (renderHeight / 2).toDouble()
            val lineRadius = Random.pick(listOf(5.0))

            println("seed: $seed")
            println("renderWidth: $renderWidth")
            println("renderHeight: $renderHeight")
            println("zoom: $zoom")
            println("distort: $distort")
            println("palette: $palette")
            println("allowEdgeOverflow: $allowEdgeOverflow")
            println("allowHeavy: $allowHeavy")
            val colorRegion: List<Pair<Triangle, ColorRGBa>> = generateSequence {
                Triangle(Random.point(bounds), Random.point(bounds), Random.point(bounds))
            }.take(20).toList().map {
                Pair(it, Random.pick(palette).toRGBa())
            }
            val canvas = renderTarget(renderWidth, renderHeight) { colorBuffer() }
            drawer.isolatedWithTarget(canvas) {
                ortho(canvas)

                drawer.fill = backgroundColor
                drawer.strokeWeight = 1.0
                drawer.rectangle(0.0, 0.0, renderWidth.toDouble(), renderHeight.toDouble())

                val svg = drawComposition {

                    repeat((renderHeight * 1.5).toInt()) {
                        val isLong = Random.bool(0.6)
                        val stepSize = bounds.width / 1000
                        var (x, y) = run {
                            if (allowEdgeOverflow && isLong) {
                                Random.point(bounds.scale(0.95))
                            } else Random.point(bounds.scale(0.8))
                        }
                        val linePoints = mutableListOf<Circle>()

                        drawer.fill = run {
                            val region = colorRegion.find { it.first.contains(Vector2(x, y)) }
                            region?.second ?: palette.first().toRGBa()
                        }

                        drawer.stroke = drawer.fill
                        val innerBounds = if (allowEdgeOverflow) bounds.scale(1.1)
                        else bounds.scale(0.8)

                        while (Vector2(x, y) in innerBounds && !exceedsMaxLineLength(linePoints, maxLineLength)) {
                            val n = Random.simplex(x / zoom, y / zoom)
                            x += sin(n * distort) * lineRadius * stepSize
                            y += cos(n * distort) * lineRadius * stepSize
                            val neighbors = quads.getNeighbors(Circle(x, y, lineRadius))

                            if (neighbors.any {
                                    Vector2(x, y).distanceTo(it.center) < it.radius / 2 + lineRadius / 2 + linePadding
                                }) {
                                break
                            }

                            linePoints.add(Circle(x, y, lineRadius))
                        }

                        if (getLineLength(linePoints) > minLineLength) {
                            quads.addParticles(linePoints)
                            linePoints.forEach {
                                drawer.circle(it)
                                this.circle(it)
                            }
                        }
                    }
                }

                GenerativeArt.saveOutput("ForcesDots",
                    canvas,
                    svg,
                    Vector2(renderWidth.toDouble(), renderHeight.toDouble())
                )

                exitProcess(status = 0)
            }
        }

    }
}
