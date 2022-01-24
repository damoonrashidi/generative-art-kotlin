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
            val distort = Random.double(-4.4, 4.4)
            val colorRegion: List<Pair<Triangle, ColorRGBa>> =
                generateSequence { Triangle(Random.point(bounds), Random.point(bounds), Random.point(bounds)) }.take(
                    10
                ).toList()
                    .map {
                        Pair(
                            it,
                            Random.pick(
                                Palette.spring()
                            ).toRGBa()
                        )
                    }
            val canvas = renderTarget(renderWidth, renderHeight) { colorBuffer() }
            drawer.isolatedWithTarget(canvas) {
                ortho(canvas)

                drawer.fill = ColorHSLa(65.0, 0.30, 0.90).toRGBa()
                drawer.rectangle(0.0, 0.0, renderWidth.toDouble(), renderHeight.toDouble())

                repeat((renderWidth * 3.5).toInt()) {

                    val lineRadius = run {
                        val heavy = Random.bool(0.05)
                        if (heavy) 280.0 else Random.pick(listOf(50.0, 90.0))
                    }
                    val stepSize = run {
                        val choppy = Random.bool(0.05)
                        if (choppy) 2.5 else 1.0
                    }
                    var (x, y) = run {
                        val isLong = Random.bool(0.05)
                        if (isLong) Random.point(
                            bounds.copy(
                                width = width * 1.05,
                                height = height * 1.05,
                                corner = Vector2(bounds.corner.x * 0.95, bounds.corner.y * 0.95)
                            ) - bounds
                        ) else Random.point(bounds)
                    }
                    val linePoints = mutableListOf<Circle>()

                    drawer.fill = run {
                        val region = colorRegion.find { it.first.contains(Vector2(x, y)) }
                        region?.second ?: ColorRGBa.BLACK
                    }
                    drawer.strokeWeight = lineRadius
                    drawer.stroke = drawer.fill

                    while (Vector2(x, y) in bounds.scale(0.85, 0.9)) {
                        val n = Random.simplex(x / zoom, y / zoom)
                        x += cos(n * distort) * lineRadius * stepSize
                        y += sin(n * distort) * lineRadius * stepSize

                        if (previous.any {
                                Vector2(x, y).distanceTo(it.center) < it.radius + lineRadius + 5.0
                            }) {
                            break
                        }

                        linePoints.add(Circle(x, y, lineRadius))
                    }

                    previous.addAll(linePoints)

                    drawer.lineStrip(linePoints.map { it.center })


                }

                val filename = "Forces-$seed.jpg"

                println("Writing ${previous.size} circles in $filename")

                canvas.colorBuffer(0)
                    .saveToFile(
                        File(
                            GenerativeArt.getFilename("Forces"),
                        ),
                        async = false
                    )

                exitProcess(0)
            }


        }
    }
}
