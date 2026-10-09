// Declarative pipeline for the Sauce Demo parallel E2E suite.
//
// Needs on the Jenkins agent: JDK 21+, Maven (on PATH), Chrome and Firefox.
// Needs these Jenkins plugins: Pipeline, Git, JUnit, HTML Publisher.
// The Extent report is self-contained (offline mode), but Jenkins' default
// Content-Security-Policy blocks its inline scripts. To see it styled in the
// browser, relax the policy once in Script Console:
//   System.setProperty("hudson.model.DirectoryBrowserSupport.CSP", "")
pipeline {
    agent any

    options {
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    parameters {
        string(name: 'SUITE_XML', defaultValue: 'testng-sauce.xml', description: 'TestNG suite file to run')
        booleanParam(name: 'HEADLESS', defaultValue: true, description: 'Run browsers without a window')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                script {
                    def cmd = "mvn -B clean test -DsuiteXmlFile=${params.SUITE_XML} -Dheadless=${params.HEADLESS}"
                    if (isUnix()) {
                        sh cmd
                    } else {
                        bat cmd
                    }
                }
            }
        }
    }

    post {
        always {
            junit testResults: 'target/surefire-reports/TEST-*.xml', allowEmptyResults: true
            // Archive first, so the reports are kept even if the HTML Publisher plugin is missing
            archiveArtifacts artifacts: 'target/reports/**, target/logs/**, target/surefire-reports/**',
                    allowEmptyArchive: true
            publishHTML(target: [
                reportName           : 'Extent Spark Report',
                reportDir            : 'target/reports',
                reportFiles          : 'spark.html',
                keepAll              : true,
                alwaysLinkToLastBuild: true,
                allowMissing         : true
            ])
        }
    }
}
