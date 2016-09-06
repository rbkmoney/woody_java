//Default pipeline for Java library
def call(String buildImageTag, Closure body) {

    def buildContainer = docker.image("rbkmoney/build:${buildImageTag}")
    runStage('Pull build container') {
        docker.withRegistry('https://dr.rbkmoney.com/v2/', 'dockerhub-rbkmoneycibot') {
            buildContainer.pull()
        }
        buildContainer = docker.image('dr.rbkmoney.com/rbkmoney/build:$BUILD_IMAGE_TAG')
    }

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
