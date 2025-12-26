## Part 1: Jenkins basics

### What Jenkins is
- CI server that runs jobs/pipelines; highly extensible via plugins.
- Core concepts: **controller** (master), **agents** (workers), **jobs/pipelines**, **credentials**, **artifacts**.

### Install options
- Docker quickstart:
```bash
docker run -u 0 --rm -p 8080:8080 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home jenkins/jenkins:lts
```
- Native install: packages for Debian/Ubuntu/RHEL, or war file.
- Kubernetes: use the official Helm chart (includes controller and dynamic agents via K8s plugin).

### First login
- Unlock with the admin password printed in logs (`initialAdminPassword`).
- Install recommended plugins when prompted.
- Create an admin user; configure URL in **Manage Jenkins → Configure System**.

### Key UI areas
- Manage Jenkins: plugins, nodes, credentials, global tool config (JDK, Maven, Git, Node).
- Nodes: add static agents or configure K8s/cloud for dynamic agents.
- Credentials: store secrets (SSH keys, tokens, passwords) scoped globally or per folder.

### Quick freestyle job (baseline)
1) New Item → Freestyle.
2) Source Code Management: Git repo URL.
3) Build: e.g., `mvn test` or `./gradlew test`.
4) Post-build: archive artifacts (`target/*.jar`) or JUnit reports (`**/surefire-reports/*.xml`).

### Why pipelines (next part)
- Pipelines (Jenkinsfile) are code, versioned with the app, reproducible, and support stages/parallelism/agents.

Proceed to Part 2 for Declarative/Scripted pipeline patterns.
