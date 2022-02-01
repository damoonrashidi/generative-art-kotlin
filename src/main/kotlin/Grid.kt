import org.openrndr.application
import org.openrndr.color.ColorHSLa
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extra.noclear.NoClear
import org.openrndr.extra.noise.Random
import org.openrndr.shape.Rectangle
import java.io.File
import kotlin.system.exitProcess

fun getDotCount(y: Double, area: Double, renderHeight: Int): Int {
    val normalizedArea = area.toString().substring(0, 4).toDouble()
    val count = (renderHeight - y) * Random.double(19.0, 23.0) + normalizedArea
    return count.coerceAtMost(3_200.0).toInt()
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

            Random.resetState()
            Random.seed = System.currentTimeMillis().toString()

            val renderWidth = 4500
            val renderHeight = (4500 * 1.5).toInt()

            val canvas = renderTarget(renderWidth, renderHeight) { colorBuffer() }
            val padding = renderWidth / 10.0

            drawer.isolatedWithTarget(canvas) {
                ortho(canvas)
                var columnCursor = padding
                val columnHeight = Random.double(renderHeight * 0.83, renderHeight * 0.85)
                fill = ColorHSLa(35.0, 0.2, 0.9).toRGBa()
                rectangle(0.0, 0.0, renderWidth.toDouble(), renderHeight.toDouble())

                stroke = null
                while (columnCursor < renderWidth - padding) {

                    val blockWidth = Random.double(renderWidth * 0.003, renderWidth * 0.04)
                    var rowCursor = padding

                    while (rowCursor < columnHeight) {

                        val blockHeight = run {
                            val isTall = Random.double(0.0, 1.0) > 0.8
                            if (isTall) {
                                renderHeight * Random.double(0.03, 0.045)
                            } else {
                                renderHeight * Random.double(0.002, 0.01)
                            }
                        }
                        val block = Rectangle(columnCursor, rowCursor, blockWidth, blockHeight)
                        val blockLuminosity = Random.gaussian(0.2, 0.1)

                        fill = ColorHSLa(0.0, 0.0, blockLuminosity).toRGBa()

                        val dotCount = getDotCount(rowCursor, blockHeight * blockWidth, renderHeight)
                        points(generateSequence { Random.point(block) }.take(dotCount).toList())

                        rowCursor += blockHeight
                    }

                    columnCursor += blockWidth
                }
            }

            val filename = GenerativeArt.getFilename("Grid")

            println("Writing ${renderWidth}x${renderHeight}px @ $filename")

            canvas.colorBuffer(0)
                .saveToFile(File(filename), async = false)
            GenerativeArt.openOutput(filename)
            exitProcess(0)
        }

    }
}
