import org.openrndr.color.ColorHSLa
import org.openrndr.color.ColorHSVa
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension


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

        fun winterMountain(): List<ColorHSVa> {
            return listOf(
                ColorHSVa(0.0, 0.1, 0.1),
                ColorHSVa(44.0, 0.1, 0.94),
                ColorHSVa(192.0, 0.1, 0.94),
                ColorHSVa(193.0, 0.27, 0.76),
                ColorHSVa(0.0, 0.8, 0.21),
                ColorHSVa(0.0, 0.44, 0.44),
            )
        }

        fun seaSide(): List<ColorHSVa> {
            return listOf(
                ColorHSVa(40.0, 0.52, 0.88),
                ColorHSVa(48.0, 0.24, 0.46),
                ColorHSVa(0.0, 0.2, 0.38),
                ColorHSVa(214.0, 0.29, 0.41),
            )
        }
    }
}