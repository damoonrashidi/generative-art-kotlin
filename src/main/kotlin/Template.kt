import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extra.noise.Random
import org.openrndr.shape.Circle
import java.io.File

import kotlin.system.exitProcess


fun main() = application {
    configure {
        width = 42
        height = 42
        hideWindowDecorations = true
    }


    program {
        extend {

            val renderWidth = 4_000
            val renderHeight = (4_000 * 1.4).toInt()

            Random.seed = System.currentTimeMillis().toString()

            val canvas = renderTarget(renderWidth, renderHeight) { colorBuffer() }

            drawer.isolatedWithTarget(canvas) {

                ortho(canvas)
                drawer.fill = ColorRGBa.WHITE

                drawer.circle(Circle(50.0, 50.0, 50.0))

                val filename = GenerativeArt.getFilename("Template")
                canvas.colorBuffer(0)
                    .saveToFile(
                        File(filename),
                        async = false
                    )
                GenerativeArt.openOutput(filename)

                exitProcess(0)
            }

        }
    }
}
