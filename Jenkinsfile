#!groovy
build('woody_java', 'docker-host') {
    checkoutRepo()
    loadBuildUtils()

    def javaLibPipeline
    runStage('load JavaLib pipeline') {
        javaLibPipeline = load("build_utils/jenkins_lib/pipeJavaLib.groovy")
    }

    def buildImageTag = "c66dc597fdc30abcb7a6368ba7cc13c02151f8de"
    javaLibPipeline(buildImageTag)
}
