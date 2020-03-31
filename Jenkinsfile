pipeline {
     agent any
     stages {
          stage('Checkout') {
               scm checkout
          }


          stage("Compile") {
               steps {
                    sh "./gradlew compileJava"
               }
          }


     }

}