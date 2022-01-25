import org.openrndr.color.ColorHSLa
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt


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
    }
}

typealias QuadMap = List<List<MutableList<Circle>>>

class CollisionDetection(private val particles: List<Circle>, private val mapWidth: Int, private val mapHeight: Int) {

    private val map: QuadMap = List(100) { List(100) { mutableListOf<Circle>() } }

    private fun getQuadIndex(particle: Circle): Pair<Int, Int> {
        val x = (particle.center.x / mapWidth * map.size - 1).toInt().coerceIn(0 until map.size - 1)
        val y = (particle.center.y / mapHeight * map[0].size - 1).toInt().coerceIn(0 until map[0].size)

        return Pair(x, y)
    }

    init {
        for (particle in particles) {
            val (x, y) = getQuadIndex(particle)
            map[y][x].add(particle)
        }
    }

    fun addParticles(additionalParticles: List<Circle>) {
        for (particle in additionalParticles) {
            val (x, y) = getQuadIndex(particle)
            map[y][x].add(particle)
        }
    }

    fun getNeighbors(particle: Circle): List<Circle> {
        val (x, y) = getQuadIndex(particle)
        return map[y][x]
    }
}

fun Random.swirl(point: Vector2, bounds: Rectangle): Double {
    val centerX = bounds.width / 2
    val centerY = bounds.height / 2
    val distanceX = abs(centerX - point.x)
    val distanceY = abs(centerY - point.y)
    return sqrt(distanceX * distanceX + distanceY * distanceY)
}

fun Random.Noise.toCenter(point: Vector2, bounds: Rectangle): Double {
    val centerX = bounds.width / 2
    val centerY = bounds.height / 2

    val angle = atan2(abs(point.x - centerX), abs(point.y - centerY))

    return angle * PI
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