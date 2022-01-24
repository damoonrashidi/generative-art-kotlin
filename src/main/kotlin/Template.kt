import org.openrndr.math.Vector2
import org.openrndr.application
import org.openrndr.color.ColorHSLa
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extra.noise.Random
import org.openrndr.shape.Circle
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries

import kotlin.math.*
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

                canvas.colorBuffer(0).saveToFile(File(getTemplateFilename()), async = false)
                exitProcess(0)
            }

        }
    }
}


fun getTemplateFilename(): String {
    val path = "./outputs/nightfall"
    val entries = Path(path).listDirectoryEntries("*.jpg").size
    return path + "/Nightfall-${entries + 1}.jpg"
}