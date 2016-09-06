#!groovy
build('woody_java', 'docker-host') {
    checkoutRepo()
    loadBuildUtils()

    def javaLibPipeline
    runStage('load JavaLib pipeline') {
        env.JENKINS_LIB = ""//"build_utils/jenkins_lib"
        javaLibPipeline = load("pipeJavaLib.groovy")
    }

    def buildImageTag = "7372dc01bf066b5b26be13d6de0c7bed70648a26"
    javaLibPipeline(buildImageTag) {
        echo 'Java Lib PipeLine FINISHED'
    }
}