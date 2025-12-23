## Part 3: CI/CD practices, agents, and security

### Agents and scalability
- Prefer ephemeral agents (K8s plugin, cloud) to avoid snowflake nodes.
- Label agents and pin heavy stages to matching labels (e.g., `agent { label 'docker' }`).
- Cache responsibly: reuse Maven/Gradle caches via mounts or persistent volumes on dynamic agents.

### Credentials and secrets
- Store in Jenkins credentials store; never hardcode in Jenkinsfile.
- Use `withCredentials` blocks; scope minimally.
- Rotate credentials regularly; restrict who can view them.

### Quality gates
- Add steps for lint/tests/security:
```groovy
stage('Verify') {
  steps {
    sh 'mvn -B test'
    sh 'mvn -B verify'           // integration tests
    // add static analysis tools here (spotbugs, checkstyle, etc.)
  }
}
```
- Fail fast on unit/integration test failures; publish JUnit reports.

### Artifacts and caching
- Archive build outputs: `archiveArtifacts artifacts: 'target/*.jar', fingerprint: true`
- JUnit reports: `junit '**/target/surefire-reports/*.xml'`
- Cache package repos: use repo mirrors (Nexus/Artifactory) to speed pipelines.

### Notifications and insights
- Post to chat/email on failures; include links to build, diff, and test reports.
- Use Blue Ocean or pipeline visualization to spot slow stages.

### Security and hardening
- Lock down who can configure Jenkins and install plugins.
- Keep Jenkins and plugins up to date; remove unused plugins.
- Run controller and agents with least privilege; avoid running as root where possible.
- Enforce CSRF and enable matrix-based security or integrate with SSO/LDAP.
- Disable script approvals for untrusted users; review Groovy sandbox requests.

### Pipeline hygiene
- Keep Jenkinsfiles short; move repetition to shared libraries.
- Parameterize pipelines for branch/env; avoid duplicating jobs.
- Use `when` conditions to skip stages on docs-only changes, etc.

### Delivery patterns (examples)
- Build/test → publish artifact → deploy via separate job with approval.
- Multibranch pipeline for PRs: run tests, static analysis, and publish reports without deploying.

### Backup and recovery
- Backup `JENKINS_HOME` (config, jobs, credentials) regularly.
- Document restore steps; test recovery on a throwaway controller.

You now have a concise Jenkins path from basics to production-ready CI/CD.
