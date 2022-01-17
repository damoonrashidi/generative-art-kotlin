import org.openrndr.application
import org.openrndr.color.ColorHSVa
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noclear.NoClear
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2
import org.openrndr.shape.*
import java.io.File
import kotlin.system.exitProcess

fun getDotCount(y: Double, area: Double, renderHeight: Int): Double {
    val normalizedArea = area.toString().substring(0, 4).toDouble();
    val count = (renderHeight - y) * Random.double(15.0, 18.0) + normalizedArea;
    return count.coerceAtMost(800.0);
}


fun main() = application {
    configure {
        width = 64
        height = 64
        hideWindowDecorations = true
    }

    program {
        extend(NoClear())
        extend(Screenshots())
        extend {

            val renderWidth = 3000
            val renderHeight = (3000 * 1.5).toInt()

            val canvas = renderTarget(renderWidth, renderHeight) { colorBuffer() }
            val padding = renderWidth / 10.0

            drawer.isolatedWithTarget(canvas) {
                ortho(canvas)
                var columnCursor = padding
                val columnHeight = Random.double(renderHeight * 0.83, renderHeight * 0.85)

                drawer.stroke = null
                while (columnCursor < renderWidth - padding) {

                    val blockWidth = Random.double(renderWidth * 0.003, renderWidth * 0.04)
                    var rowCursor = padding

                    while (rowCursor < columnHeight) {
                        var blockHeight = renderHeight * Random.double(0.002, 0.01);
                        val isTall = Random.double(0.0, 1.0) > 0.8;
                        if (isTall) {
                            blockHeight = renderHeight * Random.double(0.02, 0.025);
                        }

                        val block = Rectangle(columnCursor, rowCursor, blockWidth, blockHeight)
                        val blockLuminosity = Random.gaussian(70.0, 30.0)
                        println(blockLuminosity)

                        drawer.fill = ColorHSVa(0.0, 0.0, blockLuminosity).toRGBa()

                        val dotCount = getDotCount(rowCursor, blockHeight * blockWidth, renderHeight).toInt()

                        drawer.points(generateSequence() { Random.point(block) }.take(dotCount).toList())


                        rowCursor += blockHeight
                    }

                    columnCursor += blockWidth
                }
            }
            canvas.colorBuffer(0)
                .saveToFile(File("/Users/damoonrashidi/Desktop/test-${Random.int(0, 1000)}.jpg"), async = false)
            exitProcess(0)
        }

    }
}