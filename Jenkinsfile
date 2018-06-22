pipeline {
  agent any
  environment{
      def files = findFiles(glob: '**/terramaster.jar')[0].getPath()
  }
  stages {
    stage('version') {
        steps{
          script{
            if (env.BRANCH_NAME == 'master') {
                script {
                  def props = readProperties file: 'target/maven-archiver/pom.properties'
                  def message = props['version'] 
                }              
             }
          }
        }
    }

    stage( 'build' ) {
      steps{
         script {
            if (env.BRANCH_NAME != 'master') {
                withEnv(["JAVA_HOME=${ tool 'jdk1.8.0_121' }"]) {
                  withMaven(maven: 'Maven 3.5.3') {
                    bat "mvn clean install"
                  }                   
                }  
                archiveArtifacts '*.jar'    
             }              
           }
           junit 'target/surefire-reports/*.xml'             
       }
    }
    
    stage( 'deploy' ) {
      steps{
        script{
            echo env.BRANCH_NAME
            if (env.BRANCH_NAME == 'master') {
                def props = readProperties file: 'target/maven-archiver/pom.properties'
                def releaseProps = readProperties file: 'release.properties'
                def version = props['version'] 
                def tag = releaseProps[ 'scm.tag' ]
                echo "Releasing ${version} Tag : ${tag}"

                withEnv(["JAVA_HOME=${ tool 'jdk1.8.0_121' }"]) {
                  withMaven(maven: 'Maven 3.5.3') {
//                    bat "mvn release:perform"
                  }                   
                }  
            }
            archiveArtifacts '*.jar'
          }
        }              
     }
  }
}
