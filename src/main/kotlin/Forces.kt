import kotlin.math.*
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noclear.NoClear
import org.openrndr.extra.noise.Random
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.math.*
import org.openrndr.math.Vector2

class Particle(
    var position: Vector2,
    var radius: Double,
) {

  infix fun collides(other: Particle): Boolean {
    val distance = position.distanceTo(other.position)
    return distance < radius + other.radius + 5
  }
}

fun getQuadIndex(
    x: Double,
    y: Double,
    width: Int,
    height: Int,
    subdivisions: Int = 100
): Pair<Int, Int> {
  val xIndex = round(clamp(x, 0.0, width.toDouble()) / width) * subdivisions
  val yIndex = round(clamp(y, 0.0, height.toDouble()) / height) * subdivisions

  return Pair(
      clamp(xIndex.toInt(), 0, subdivisions - 1),
      clamp(yIndex.toInt(), 0, subdivisions - 1)
  )
}

fun main() = application {
  configure {
    width = 800
    height = 800
  }

  oliveProgram {
    extend(NoClear())
    extend(Screenshots())
    extend {
      val quadMap = List(100) { List(100) { mutableSetOf<Particle>() } }
      drawer.strokeWeight = -0.0
      var (x, y) = Random.point(drawer.bounds)

      drawer.fill =
          ColorRGBa(
              Random.gaussian(105.0, 0.0),
              Random.gaussian(60.0, 0.1),
              Random.gaussian(20.0, 0.1),
              100.0
          )
      var length = 0

      while (length < 100 && Vector2(x, y) in drawer.bounds) {
        val n = Random.simplex(x / 1200, y / 1200, 0.0, 2.0)
        val (xI, yI) = getQuadIndex(x, y, width, height)

        val particle = Particle(Vector2(x, y), 2.5)

        if (quadMap[xI][yI].toList().any { particle collides it }) {
          break
        }

        quadMap[xI][yI].add(particle)

        drawer.circle(x, y, 2.5)
        x += cos(n * 2.2) * 5.0
        y += sin(n * 2.2) * 5.0

        length++
      }
    }
  }
}
