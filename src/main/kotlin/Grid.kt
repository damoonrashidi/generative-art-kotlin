import org.openrndr.application
import org.openrndr.color.ColorHSVa
import org.openrndr.extra.noclear.NoClear
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extra.noise.Random
import org.openrndr.shape.*
import java.io.File
import kotlin.system.exitProcess

fun getDotCount(y: Double, area: Double, renderHeight: Int): Double {
    val normalizedArea = area.toString().substring(0, 4).toDouble();
    val count = (renderHeight - y) * Random.double(15.0, 18.0) + normalizedArea;
    return count.coerceAtMost(2_400.0);
}

fun main() = application {
    configure {
        width = 64
        height = 64
        hideWindowDecorations = true
    }

    program {
        extend(NoClear())
        extend {

            Random.randomizeSeed()

            val renderWidth = 4500
            val renderHeight = (4500 * 1.5).toInt()

            val canvas = renderTarget(renderWidth, renderHeight) { colorBuffer() }
            val padding = renderWidth / 10.0

            drawer.isolatedWithTarget(canvas) {
                ortho(canvas)
                var columnCursor = padding
                val columnHeight = Random.double(renderHeight * 0.83, renderHeight * 0.85)
                fill = ColorHSVa(45.0, 0.1, 0.98).toRGBa()
                rectangle(0.0, 0.0, renderWidth.toDouble(), renderHeight.toDouble())

                stroke = null
                while (columnCursor < renderWidth - padding) {

                    val blockWidth = run {

                        val isWide = Random.double(0.0, 1.0) > 0.5

                        if (isWide) {
                            Random.double(renderWidth * 0.009, renderWidth * 0.08)
                        } else {
                            Random.double(renderWidth * 0.003, renderWidth * 0.04)
                        }

                    }
                    var rowCursor = padding

                    while (rowCursor < columnHeight) {

                        val blockHeight = run {
                            val isTall = Random.double(0.0, 1.0) > 0.8;
                            if (isTall) {
                                renderHeight * Random.double(0.02, 0.025);
                            } else {
                                renderHeight * Random.double(0.002, 0.01)
                            }
                        }
                        val block = Rectangle(columnCursor, rowCursor, blockWidth, blockHeight)
                        val blockLuminosity = Random.gaussian(0.2, 0.1)

                        fill = ColorHSVa(0.0, 0.0, blockLuminosity).toRGBa()

                        val dotCount = getDotCount(rowCursor, blockHeight * blockWidth, renderHeight).toInt()
                        points(generateSequence() { Random.point(block) }.take(dotCount).toList())

                        rowCursor += blockHeight
                    }

                    columnCursor += blockWidth
                }
            }

            val filename = "Grid-${Random.int0(1000)}-${Random.int0(1000)}.jpg"

            println("Writing ${renderWidth}x${renderHeight}px @ $filename")

            canvas.colorBuffer(0)
                .saveToFile(File("/Users/damoonrashidi/Desktop/$filename"), async = false)
            Runtime.getRuntime().exec("open /Users/damoonrashidi/Desktop/$filename")
            exitProcess(0)
        }

    }
}
