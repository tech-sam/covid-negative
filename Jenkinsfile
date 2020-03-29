pipeline {
     agent any
     stages {
          stage("Compile") {
               steps {
                    sh "./gradlew compileJava"
               }
          }


          stage("Package") {
               steps {
                    sh "./gradlew build"
               }
          }


          stage("Run") {
               steps {
                    sh "./gradlew run"
               }
          }

     }

}