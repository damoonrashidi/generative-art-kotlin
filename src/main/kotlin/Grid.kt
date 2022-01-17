import org.openrndr.application
import org.openrndr.color.ColorHSVa
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noclear.NoClear
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.random
import org.openrndr.shape.*
import java.io.File
import kotlin.system.exitProcess

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

            val renderWidth = 900
            val renderHeight = 1200

            val canvas = renderTarget(renderWidth, renderHeight) { colorBuffer() }
            val padding = renderWidth / 10.0
            val columnWidth = renderWidth / 15.0


            drawer.isolatedWithTarget(canvas) {
                ortho(canvas)
                var columnCursor = padding

                while (columnCursor < renderWidth - padding) {

                    drawer.fill = ColorHSVa(columnCursor, 50.0, 50.0).toRGBa()

                    drawer.rectangle(columnCursor % 255.0, padding, columnWidth, renderHeight - padding)

                    columnCursor += columnWidth
                }
            }
            canvas.colorBuffer(0)
                .saveToFile(File("/Users/damoonrashidi/Desktop/test-${Random.double(0.0, 100.0)}.jpg"), async = false)
            exitProcess(0)
        }

    }
}