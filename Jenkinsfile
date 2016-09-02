#!groovy
build('woody_java', 'docker-host') {
    checkoutRepo()

    runStage('Execute build container') {
        docker.withRegistry('https://dr.rbkmoney.com/v2/', 'dockerhub-rbkmoneycibot') {
            withCredentials([[$class: 'FileBinding', credentialsId: 'java-maven-settings.xml', variable: 'SETTINGS_XML']]) {
                docker.image('dr.rbkmoney.com/rbkmoney/build:7372dc01bf066b5b26be13d6de0c7bed70648a26').inside() {
                    if (env.BRANCH_NAME == 'master') {
                        sh 'mvn deploy --batch-mode --settings  $SETTINGS_XML -P ci'
                    } else {
                        sh 'mvn install --batch-mode --settings  $SETTINGS_XML -P ci'
                    }
                }
            }
        }

    }
}