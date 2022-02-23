import org.openrndr.color.ColorHSLa
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle
import kotlin.io.path.*

class GenerativeArt {
    companion object {
        fun getFilename(project: String): String {
            val path = "./outputs/$project"
            val entries = Path(path).listDirectoryEntries("*.jpg")
            if (entries.isEmpty()) {
                return "/${project.replaceFirstChar { it.uppercaseChar() }}-1.jpg"
            }
            val last = entries.map { it.nameWithoutExtension.split("-").last().toInt() }.maxOf { it }
            return path + "/${project.replaceFirstChar { it.uppercaseChar() }}-${last + 1}.jpg"
        }

        fun openOutput(filename: String) {
            Runtime.getRuntime().exec("open $filename")
        }
    }
}

typealias QuadMap = List<List<MutableList<Circle>>>

class CollisionDetection(
    particles: List<Circle>,
    private val bounds: Rectangle,
    private val cellCount: Int = 10
) {
    private val map: QuadMap = List(cellCount) { List(cellCount) { mutableListOf<Circle>() } }

    private fun getQuadIndex(particle: Circle): Pair<Int, Int> {
        val x = (particle.center.x / bounds.width * cellCount).toInt().coerceIn(map.indices)
        val y = (particle.center.y / bounds.height * cellCount).toInt().coerceIn(map.indices)

        return Pair(x, y)
    }

    init {
        this.addParticles(particles)
    }

    fun addParticles(additionalParticles: List<Circle>) {
        for (particle in additionalParticles) {
            val (x, y) = this.getQuadIndex(particle)
            map[y][x].add(particle)
        }
    }

    fun getNeighbors(particle: Circle): List<Circle> {
        val (x, y) = this.getQuadIndex(particle)
        return map[y][x]
    }
}

class Palette {
    companion object {
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

        fun noire(): List<ColorHSLa> {
            return listOf(ColorHSLa(0.0, 0.0, 0.1))
        }

        fun nesso(): List<ColorHSLa> {
            return listOf(
                ColorHSLa(233.0, 0.72, 0.75),
                ColorHSLa(0.0, 0.8, 0.65),
                ColorHSLa(0.0, 0.0, 0.31),
                ColorHSLa(24.0, 0.2, 0.8),
                ColorHSLa(61.0, 0.74, 0.75)
            )
        }

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

        fun seaSide(): List<ColorHSLa> {
            return listOf(
                ColorHSLa(40.0, 0.52, 0.88),
                ColorHSLa(48.0, 0.24, 0.46),
                ColorHSLa(0.0, 0.2, 0.38),
                ColorHSLa(214.0, 0.29, 0.41),
            )
        }
    }
}