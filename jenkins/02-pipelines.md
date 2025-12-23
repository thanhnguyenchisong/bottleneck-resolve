## Part 2: Pipelines (Declarative and Scripted)

### Declarative vs Scripted
- Declarative: opinionated, simpler syntax, validation, recommended for most cases.
- Scripted: full Groovy; more flexible, harder to maintain. Use only when necessary.

### Minimal Declarative Jenkinsfile
```groovy
pipeline {
  agent any
  stages {
    stage('Checkout') {
      steps { checkout scm }
    }
    stage('Build') {
      steps { sh 'mvn -B clean package' }
    }
    stage('Test') {
      steps { sh 'mvn -B test' }
    }
    stage('Archive') {
      steps { archiveArtifacts artifacts: 'target/*.jar', fingerprint: true }
    }
  }
}
```

### Agents
- `agent any` uses any executor.
- Per-stage agent (e.g., Docker):
```groovy
stage('Build') {
  agent { docker { image 'maven:3.9-eclipse-temurin-17' } }
  steps { sh 'mvn -B clean package' }
}
```
- Kubernetes agents with Jenkins K8s plugin (concept):
```groovy
podTemplate(containers: [
  containerTemplate(name: 'maven', image: 'maven:3.9-eclipse-temurin-17', ttyEnabled: true, command: 'cat')
]) {
  node(POD_LABEL) {
    stage('Build') { container('maven') { sh 'mvn -B clean package' } }
  }
}
```

### Environment and credentials
```groovy
environment {
  JAVA_HOME = tool 'jdk17'
  REGISTRY = 'thanhncs'
}
pipeline {
  agent any
  stages {
    stage('Login') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
          sh 'echo "$PASS" | docker login -u "$USER" --password-stdin'
        }
      }
    }
  }
}
```

### Post actions
```groovy
post {
  always { junit '**/target/surefire-reports/*.xml' }
  success { echo 'Build passed' }
  failure { echo 'Build failed' }
}
```

### Shared libraries (when pipelines grow)
- Store common steps in a shared library repo; load with `@Library('my-shared-lib') _`.
- Define vars (e.g., `vars/buildJava.groovy`) to encapsulate repeated logic.

### Scripted pipeline snippet (only if needed)
```groovy
node {
  stage('Build') { sh 'mvn -B clean package' }
}
```

Proceed to Part 3 for CI/CD practices, agents, caching, and security.
