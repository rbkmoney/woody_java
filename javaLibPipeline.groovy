def call(Closure body) {
    def buildContainer = docker.image('dr.rbkmoney.com/rbkmoney/build:7372dc01bf066b5b26be13d6de0c7bed70648a26')
    buildContainer.pull()

    runStage('Execute build container') {
        docker.withRegistry('https://dr.rbkmoney.com/v2/', 'dockerhub-rbkmoneycibot') {
            withCredentials([[$class: 'FileBinding', credentialsId: 'java-maven-settings.xml', variable: 'SETTINGS_XML']]) {
                buildContainer.inside() {
                    if (env.BRANCH_NAME == 'master') {
                        sh 'mvn deploy --batch-mode --settings  $SETTINGS_XML'
                    } else {
                        sh 'mvn install --batch-mode --settings  $SETTINGS_XML'
                    }
                }
            }
        }
    }

    body.call()
}
return this
