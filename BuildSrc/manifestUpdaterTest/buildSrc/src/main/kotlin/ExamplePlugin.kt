import com.android.build.api.artifact.ArtifactType
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import com.android.build.api.dsl.CommonExtension

abstract class ExamplePlugin: Plugin<Project> {

    override fun apply(project: Project) {
        val gitVersionProvider =
            project.tasks.register("gitVersionProvider", GitVersionTask::class.java) {
                it.gitVersionOutputFile.set(
                    File(project.buildDir, "intermediates/gitVersionProvider/output")
                )
                it.outputs.upToDateWhen { false }
            }

        val android = project.extensions.getByType(CommonExtension::class.java)

        android.onVariantProperties {

            val manifestUpdater =
                project.tasks.register(name + "ManifestUpdater", ManifestTransformerTask::class.java) {
                    it.gitInfoFile.set(gitVersionProvider.flatMap(GitVersionTask::gitVersionOutputFile))
                }
            artifacts.use(manifestUpdater)
                .wiredWithFiles(
                    ManifestTransformerTask::mergedManifest,
                    ManifestTransformerTask::updatedManifest)
                .toTransform(ArtifactType.MERGED_MANIFEST)

            project.tasks.register(name + "Verifier", VerifyManifestTask::class.java) {
                it.apkFolder.set(artifacts.get(ArtifactType.APK))
                it.builtArtifactsLoader.set(artifacts.getBuiltArtifactsLoader())
            }
        }
    }
}