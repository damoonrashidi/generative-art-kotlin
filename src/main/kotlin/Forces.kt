import org.openrndr.application
import org.openrndr.color.ColorHSLa
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extra.noclear.NoClear
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle
import org.openrndr.shape.Triangle
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
            val previous = mutableListOf<Circle>()
            val zoom = Random.int(900, 4_000)
            val distort = Random.double(-5.0, 5.0)
            val linePadding = 1.0
            val lineWidths = listOf(25.0, 50.0, 100.0)
            val allowEdgeOverflow = Random.bool(1.0)

            println("seed: $seed")
            println("renderWidth: $renderWidth")
            println("renderHeight: $renderHeight")
            println("zoom: $zoom")
            println("distort: $distort")
            println("allowEdgeOverflow: $allowEdgeOverflow")


            val colorRegion: List<Pair<Triangle, ColorRGBa>> =
                generateSequence { Triangle(Random.point(bounds), Random.point(bounds), Random.point(bounds)) }.take(
                    10
                ).toList()
                    .map {
                        Pair(
                            it,
                            Random.pick(Palette.noire()).toRGBa()
                        )
                    }
            val canvas = renderTarget(renderWidth, renderHeight) { colorBuffer() }
            drawer.isolatedWithTarget(canvas) {
                ortho(canvas)

                drawer.fill = ColorHSLa(0.0, 0.0, 0.0).toRGBa()
                drawer.rectangle(0.0, 0.0, renderWidth.toDouble(), renderHeight.toDouble())

                repeat((renderHeight * 3.5).toInt()) {
                    val isLong = Random.bool(0.2)

                    val lineRadius = run {
                        val heavy = Random.bool(0.05)
                        if (heavy) Random.pick(lineWidths) * 10.0 else Random.pick(lineWidths)
                    }
                    val stepSize = run {
                        val choppy = Random.bool(0.05)
                        if (choppy) 5.0 else 1.0
                    }
                    var (x, y) = run {
                        if (allowEdgeOverflow && isLong) {
                            Random.point(
                                bounds.scale(0.9)
                            )
                        } else Random.point(bounds.scale(0.8))
                    }
                    val linePoints = mutableListOf<Circle>()

                    drawer.fill = run {
                        val region = colorRegion.find { it.first.contains(Vector2(x, y)) }
                        region?.second ?: Palette.noire().first().toRGBa()
                    }
                    drawer.strokeWeight = lineRadius
                    drawer.stroke = drawer.fill

                    val innerBounds = if (allowEdgeOverflow)
                        bounds.scale(0.9)
                    else bounds.scale(0.8)

                    while (Vector2(x, y) in innerBounds) {
                        val n = Random.swirl(Vector2(x / zoom, y / zoom), innerBounds)
                        x += sin(n * distort) * lineRadius * stepSize
                        y += cos(n * distort) * lineRadius * stepSize

                        if (previous.any {
                                Vector2(x, y).distanceTo(it.center) < it.radius + lineRadius + linePadding
                            }) {
                            break
                        }

                        linePoints.add(Circle(x, y, lineRadius))
                    }

                    previous.addAll(linePoints)

                    drawer.lineStrip(linePoints.map { it.center })


                }

                canvas.colorBuffer(0)
                    .saveToFile(
                        File(
                            GenerativeArt.getFilename("Forces"),
                        ),
                        async = false
                    )

                println("Wrote to ${GenerativeArt.getFilename("Forces")}")

                exitProcess(status = 0)
            }


        }
    }
}
