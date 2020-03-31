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

          stage("Docker build") {
               steps {
                    sh "docker build -t darknightdocker/covid19-negative:latest ."
               }
          }

          stage("Docker login") {
               steps {
                    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'db296491-d35f-4bbd-9632-f7ff91fd3af1',
                                      usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                         sh "docker login --username $USERNAME --password $PASSWORD"
                    }
               }
          }

          stage("Docker push") {
               steps {
                    sh "docker push darknightdocker/covid19-negative:latest"
               }
          }

     }

}