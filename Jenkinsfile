#!groovy
build('woody_java', 'docker-host') {
    checkoutRepo()
    loadBuildUtils()

    def javaLibPipeline
    runStage('load JavaLib pipeline') {
        env.JENKINS_LIB = ""//"build_utils/jenkins_lib"
        javaLibPipeline = load("javaLibPipeline.groovy")
    }

    javaLibPipeline() {
        echo 'Java Lib PipeLine FINISHED'
    }
}