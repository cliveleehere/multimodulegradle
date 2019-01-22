// run with configureOnDemand = false
// if 'dot' is not found, make sure you have graphviz installed ('brew install graphviz')

typealias ProjectKey = String

val completedProjects = mutableListOf<String>()

fun Project.createKey() = "$group:$name"
fun DependencyConstraint.createKey() = "$group:$name"

task("dependencyGraph") {
  doLast {
    val map = constructProjectMap(rootProject)
    val fileName = "${project.name}-dependencies.dot"
    val file = File(fileName)
    file.delete()
    file.appendText("strict digraph {\n")
    file.appendText("splines=ortho\n")
    printDependencies(file, project, map.toMutableMap())
    file.appendText("}\n")

    "dot -Tpng -O $fileName".runCommand(File("."))
    "open $fileName.png".runCommand(File("."))
  }
}

typealias ProjectKey = String

fun Project.getKey(): ProjectKey = "$group:$name"
fun Dependency.getKey(): ProjectKey = "$group:$name"

fun constructProjectMap(project: Project): Map<ProjectKey, Project> {
  val map = mutableMapOf(Pair(project.getKey(), project))
  project.childProjects.forEach {
    map.putAll(constructProjectMap(it.value))
  }
  return map
}

fun printDependencies(file:File, project:Project, map: MutableMap<ProjectKey,Project>) {
  map.remove(project.getKey())
  project.configurations
      .flatMap { it -> it.allDependencies}
      .filter { it is ProjectDependency }
      .map { map[it.getKey()] }
      .forEach {
        if (it != null) {
          file.appendText("\"${project.name}\" -> \"${it.name}\"\n")
          printDependencies(file, it, map)
        }
      }
}

fun String.runCommand(workingDir: File) {
  ProcessBuilder(*split(" ").toTypedArray())
      .directory(workingDir)
      .redirectOutput(ProcessBuilder.Redirect.INHERIT)
      .redirectError(ProcessBuilder.Redirect.INHERIT)
      .start()
      .waitFor()
}