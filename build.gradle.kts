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
        // sonar.sources n'est pas defini volontairement : le scanner Gradle (7.3.x)
        // detecte automatiquement les sources Android (via le variant "debug"), et
        // declarer sonar.sources en plus provoque un doublon d'enregistrement avec
        // cette detection automatique ("can't be indexed twice"). Consequence connue
        // et acceptee pour le moment : le rapport de couverture Jacoco n'est pas
        // toujours correctement rattache aux fichiers sources (cf. ticket
        // SCANGRADLE-429, support AGP 9 encore recent). A revoir si un correctif
        // officiel sort cote scanner Sonar.
        property("sonar.coverage.jacoco.xmlReportPaths", "app/build/reports/jacoco/jacocoCombinedReport/jacocoCombinedReport.xml")
        property("sonar.junit.reportPaths", "app/build/test-results/testDebugUnitTest")
    }
}
