// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    alias(libs.plugins.sonarqube)
}

sonar {
    properties {
        property("sonar.projectKey", "jacqueline-raynaud_p15_eventorias_JR")
        property("sonar.organization", "jacqueline-raynaud")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.coverage.jacoco.xmlReportPaths", "app/build/reports/jacoco/jacocoCombinedReport/jacocoCombinedReport.xml")
        property("sonar.junit.reportPaths", "app/build/test-results/testDebugUnitTest")
    }
}

// Contourne un bug connu du scanner Gradle Sonar (7.3.x) avec AGP 9 : la detection
// automatique des sources Android enregistre certains fichiers de main/java a la fois
// comme sources ET comme tests (via le variant androidTest), ce qui fait echouer
// l'analyse ("can't be indexed twice"). On retire ici, apres resolution, tout fichier
// deja present dans androidSources de la liste androidTests avant que la tache :sonar
// ne parte. A retirer si un correctif officiel sort (cf. ticket SCANGRADLE-429).
subprojects {
    val subproject = this

    val adjustSonarTask = tasks.register("adjustSonar") {
        description = "Retire de androidTests les fichiers deja presents dans androidSources"
        // Reference par nom (resolue paresseusement) plutot que par objet de tache,
        // pour eviter toute interference avec la creation de sonarResolver.
        dependsOn("sonarResolver")

        doLast {
            val resolverTask = subproject.tasks.findByName("sonarResolver")
                as? org.sonarqube.gradle.SonarResolverTask ?: return@doLast
            val resolverFile = resolverTask.outputFile
            if (!resolverFile.exists()) return@doLast

            try {
                val props = org.sonarqube.gradle.ResolutionSerializer.read(resolverFile)
                if (!props.isPresent) return@doLast
                val pp = props.get()

                logger.lifecycle("adjustSonar [${pp.projectName}]: ${pp.androidSources.size} source(s), ${pp.androidTests.size} test(s)")
                logger.lifecycle("adjustSonar [${pp.projectName}]: sources = ${pp.androidSources}")
                logger.lifecycle("adjustSonar [${pp.projectName}]: tests = ${pp.androidTests}")

                val sourcesSet = pp.androidSources.toSet()
                val filteredTests = pp.androidTests.filterNot { it in sourcesSet }
                val removedCount = pp.androidTests.size - filteredTests.size

                if (removedCount == 0) {
                    logger.lifecycle("adjustSonar [${pp.projectName}]: aucun doublon trouve dans le fichier resolver")
                    return@doLast
                }

                val newProps = org.sonarqube.gradle.ProjectProperties.Builder(pp.projectName, pp.isRootProject)
                    .compileClasspath(pp.compileClasspath)
                    .testCompileClasspath(pp.testCompileClasspath)
                    .mainLibraries(pp.mainLibraries)
                    .testLibraries(pp.testLibraries)
                    .androidSources(pp.androidSources)
                    .androidTests(filteredTests)
                    .build()

                org.sonarqube.gradle.ResolutionSerializer.write(resolverFile, newProps)
                logger.lifecycle("adjustSonar [${pp.projectName}]: retire $removedCount fichier(s) en double de androidTests")
            } catch (e: Exception) {
                logger.warn("adjustSonar [${subproject.path}]: echec du traitement du fichier resolver: ${e.message}", e)
            }
        }
    }

    rootProject.tasks.matching { it.name == "sonar" }.configureEach {
        dependsOn(adjustSonarTask)
    }
}