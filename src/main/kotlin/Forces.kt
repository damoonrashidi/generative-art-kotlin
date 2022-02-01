import org.openrndr.application
import org.openrndr.color.ColorHSLa
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extra.noclear.NoClear
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2
import org.openrndr.shape.*
import java.io.File
import kotlin.math.cos
import kotlin.math.sin
import kotlin.system.exitProcess

fun main() = application {
    configure {
        width = 64
        height = 64
        hideWindowDecorations = true
    }

    program {
        extend(NoClear())
        extend {
            drawer.stroke = null
            Random.resetState()
            val seed = System.currentTimeMillis().toString()
            Random.seed = seed
            val renderWidth = 4000
            val renderHeight = (4000 * 1.4).toInt()
            val bounds = Rectangle(0.0, 0.0, renderWidth.toDouble(), renderHeight.toDouble())
            val quads = CollisionDetection(listOf<Circle>(), bounds, 10)

            val zoom = Random.int(900, 4_000)
            val distort = Random.double(-4.0, 4.0)
            val linePadding = 25.0
            val lineWidths = listOf<Double>(50.0, 70.0)
            val allowEdgeOverflow = Random.bool(0.25)
            val allowHeavy = Random.bool(0.3)
            val allowChoppy = Random.bool(0.3)
            val backgroundColor =
                if (Random.bool(0.9)) ColorHSLa(30.0, 0.2, 0.9).toRGBa() else ColorHSLa(0.0, 0.0, 0.05).toRGBa()
            val palette =
                Random.pick(listOf(Palette.seaSide(), Palette.noire(), Palette.winterMountain(), Palette.spring()))

            println("seed: $seed")
            println("renderWidth: $renderWidth")
            println("renderHeight: $renderHeight")
            println("zoom: $zoom")
            println("distort: $distort")
            println("palette: $palette")
            println("allowEdgeOverflow: $allowEdgeOverflow")
            println("allowChoppy: $allowChoppy")
            println("allowHeavy: $allowHeavy")
            val colorRegion: List<Pair<Triangle, ColorRGBa>> =
                generateSequence {
                    Triangle(
                        Random.point(bounds),
                        Random.point(bounds),
                        Random.point(bounds)
                    )
                }.take(20).toList().map {
                    Pair(it, Random.pick(palette).toRGBa())
                }
            val canvas = renderTarget(renderWidth, renderHeight) { colorBuffer() }
            drawer.isolatedWithTarget(canvas) {
                ortho(canvas)

                drawer.fill = backgroundColor
                drawer.rectangle(0.0, 0.0, renderWidth.toDouble(), renderHeight.toDouble())

                repeat((renderHeight * 20.0).toInt()) {
                    val isLong = Random.bool(0.6)
                    val lineRadius = run {
                        val heavy = Random.bool(0.05)
                        if (allowHeavy && heavy) Random.pick(lineWidths) * 5.0 else Random.pick(lineWidths)
                    }
                    val stepSize = run {
                        val choppy = Random.bool(0.05)
                        if (allowChoppy && choppy) 5.0 else bounds.width / 10000
                    }

                    var (x, y) = run {
                        if (allowEdgeOverflow && isLong) {
                            Random.point(
                                bounds.scale(0.95)
                            )
                        } else Random.point(bounds.scale(0.8))
                    }
                    val linePoints = mutableListOf<Circle>()

                    drawer.fill = run {
                        val region = colorRegion.find { it.first.contains(Vector2(x, y)) }
                        region?.second ?: Random.pick(palette).toRGBa()
                    }

                    drawer.strokeWeight = lineRadius
                    drawer.stroke = drawer.fill
                    val innerBounds = if (allowEdgeOverflow)
                        bounds.scale(0.9)
                    else bounds.scale(0.8)

                    while (Vector2(x, y) in innerBounds) {
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

                    if (linePoints.size > 30.0) {
                        quads.addParticles(linePoints)
                        drawer.lineStrip(linePoints.map { it.center })
                    }
                }
                val filename = GenerativeArt.getFilename("Forces")

                canvas.colorBuffer(0)
                    .saveToFile(
                        File(filename),
                        async = false
                    )

                GenerativeArt.openOutput(filename)

                exitProcess(status = 0)
            }
        }
    }
}