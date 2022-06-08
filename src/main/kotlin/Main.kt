import com.neitex.DYNAMIC_IMPORTS
import com.neitex.generators.FileDefinition
import com.neitex.generators.KotlinPackage
import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

fun main(args: Array<String>) {
    // Before running, set up your own directories
    val basePath = Path("/home/pavel/temp/package/dist") // Root directory where definitions are stored
    val baseOutputPath = "/home/pavel/temp/kotlin-wrappers/kotlin-ring-ui/src/main/kotlin" // Output directory
    var prevHash = -1
    Files.walkFileTree(basePath, object : FileVisitor<Path> {
        override fun preVisitDirectory(dir: Path?, attrs: BasicFileAttributes?): FileVisitResult {
            println("Visiting: $dir")
            if (dir?.name?.startsWith("analytics") == true)
                return FileVisitResult.SKIP_SUBTREE
            return FileVisitResult.CONTINUE
        }

        override fun visitFile(file: Path, attrs: BasicFileAttributes?): FileVisitResult {
            if (file.toFile().extension != "ts")
                return FileVisitResult.CONTINUE
            if (prevHash != DYNAMIC_IMPORTS.hashCode())
            {
                prevHash = DYNAMIC_IMPORTS.hashCode()
//                println("New in map: $DYNAMIC_IMPORTS")
            }
            val Package = KotlinPackage("ringui." + file.toAbsolutePath().parent.toString().removePrefix("$basePath/"))
            File("$baseOutputPath/${Package.physicalPath}").mkdirs()
            val outputFile = File("$baseOutputPath/${Package.physicalPath}/${file.nameWithoutExtension.removeSuffix(".d")}.kt").apply {
                createNewFile()
            }
            FileDefinition(
                file.toFile(),
                "@jetbrains/ring-ui/components/${file.parent.name}/${file.nameWithoutExtension.removeSuffix(".d")}",
                Package
            ).writeToFile(outputFile)
            return FileVisitResult.CONTINUE
        }

        override fun visitFileFailed(file: Path?, exc: IOException?): FileVisitResult {
            println("Failed: $file")
            return FileVisitResult.CONTINUE
        }

        override fun postVisitDirectory(dir: Path?, exc: IOException?): FileVisitResult {
            println("Done: $dir")
            return FileVisitResult.CONTINUE
        }

    })
    println("Everything done!")
}
