import org.openrndr.color.ColorHSLa
import org.openrndr.draw.RenderTarget
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.Composition
import org.openrndr.shape.Rectangle
import org.openrndr.svg.saveToFile
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension

class GenerativeArt {
    companion object {
        fun getFilename(projectName: String): String {
            val path = "./outputs/$projectName"
            val entries = Path("$path/jpg").listDirectoryEntries("*.jpg")
            if (entries.isEmpty()) {
                return "/${projectName.replaceFirstChar { it.uppercaseChar() }}-1"
            }
            val last = entries.map { it.nameWithoutExtension.split("-").last().toInt() }.maxOf { it }
            return "${projectName.replaceFirstChar { it.uppercaseChar() }}-${last + 1}"
        }

        fun openOutput(filename: String) {
            Runtime.getRuntime().exec("open $filename")
        }

        fun saveOutput(
            projectName: String,
            renderTarget: RenderTarget,
            svg: Composition,
            size: Vector2,
        ) {
            val path = "./outputs/$projectName"
            val filename = getFilename(projectName)
            renderTarget.colorBuffer(0).saveToFile(File("$path/jpg/$filename.jpg"), async = false)

            svg.root.attributes["width"] = size.x.toString()
            svg.root.attributes["height"] = size.y.toString()

            val svgName = "$path/svg/$filename.svg"
            val file = File(svgName)
            svg.saveToFile(file)
        }
    }
}

typealias QuadMap<T> = List<List<MutableList<T>>>

class CollisionDetection<T>(
    particles: List<T>,
    private val bounds: Rectangle,
    private val cellCount: Int = 10,
) {
    private val map: QuadMap<T> = List(cellCount) { List(cellCount) { mutableListOf() } }

    private fun getQuadIndex(particle: T): Pair<Int, Int> {
        when (particle) {
            is Circle -> {
                val x = (particle.center.x / bounds.width * cellCount).toInt().coerceIn(map.indices)
                val y = (particle.center.y / bounds.height * cellCount).toInt().coerceIn(map.indices)
                return Pair(x, y)
            }
            is Rectangle -> {
                val x = (particle.center.x / bounds.width * cellCount).toInt().coerceIn(map.indices)
                val y = (particle.center.y / bounds.height * cellCount).toInt().coerceIn(map.indices)
                return Pair(x, y)
            }
            is Vector2 -> {
                val x = (particle.x / bounds.width * cellCount).toInt().coerceIn(map.indices)
                val y = (particle.y / bounds.height * cellCount).toInt().coerceIn(map.indices)
                return Pair(x, y)
            }
            else -> throw Error("doesn't work")
        }
    }

    init {
        this.addParticles(particles)
    }

    fun addParticles(additionalParticles: List<T>) {
        for (particle in additionalParticles) {
            val (x, y) = this.getQuadIndex(particle)
            map[y][x].add(particle)
        }
    }

    fun getNeighbors(particle: T): List<T> {
        val (x, y) = this.getQuadIndex(particle)
        return map[y][x]
    }
}

class Palette {
    companion object {
        @JvmStatic
        fun spring(): List<ColorHSLa> {
            return listOf(
                ColorHSLa(
                    190.0, 0.6, 0.98
                ),
                ColorHSLa(192.0, 0.63, 0.94),
                ColorHSLa(169.0, 0.46, 0.86),
                ColorHSLa(166.0, 0.45, 0.72),
                ColorHSLa(160.0, 0.43, 0.58)
            )
        }

        @JvmStatic
        fun noire(): List<ColorHSLa> {
            return listOf(ColorHSLa(0.0, 0.0, 0.1))
        }

        @JvmStatic
        fun nesso(): List<ColorHSLa> {
            return listOf(
                ColorHSLa(233.0, 0.72, 0.75),
                ColorHSLa(0.0, 0.8, 0.65),
                ColorHSLa(0.0, 0.0, 0.31),
                ColorHSLa(24.0, 0.2, 0.8),
                ColorHSLa(61.0, 0.74, 0.75)
            )
        }

        @JvmStatic
        fun winterMountain(): List<ColorHSLa> {
            return listOf(
                ColorHSLa(0.0, 0.1, 0.1),
                ColorHSLa(44.0, 0.1, 0.94),
                ColorHSLa(192.0, 0.1, 0.94),
                ColorHSLa(193.0, 0.27, 0.76),
                ColorHSLa(0.0, 0.8, 0.21),
                ColorHSLa(0.0, 0.44, 0.44),
            )
        }

        @JvmStatic
        fun seaSide(): List<ColorHSLa> {
            return listOf(
                ColorHSLa(40.0, 0.52, 0.88),
                ColorHSLa(48.0, 0.24, 0.46),
                ColorHSLa(0.0, 0.2, 0.38),
                ColorHSLa(214.0, 0.29, 0.41),
            )
        }

        @JvmStatic
        fun random(): List<ColorHSLa> {
            return Random.pick(listOf(seaSide(), winterMountain(), nesso(), noire(), spring()))
        }
    }
}