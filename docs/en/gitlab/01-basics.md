## Part 1: GitLab CI/CD basics

### What GitLab CI is
- Built-in CI/CD tied to your repo; pipelines defined in `.gitlab-ci.yml`.
- Runs jobs on **runners** (shared, project, group, or self-hosted).

### Quickstart `.gitlab-ci.yml`
```yaml
stages: [test, build]

test:
  stage: test
  image: maven:3.9-eclipse-temurin-17
  script:
    - mvn -B test
  artifacts:
    when: always
    reports:
      junit: **/target/surefire-reports/*.xml

build:
  stage: build
  image: maven:3.9-eclipse-temurin-17
  script:
    - mvn -B package
  artifacts:
    paths: [target/*.jar]
```

### Runners
- Shared runners: provided by GitLab (depending on plan).
- Specific runners: registered to a project/group.
- Choose executor: shell, docker, docker+machine, kubernetes.

### Key syntax
- `stages`: pipeline order.
- `script`: commands per job.
- `image`: container image for job; overrides default.
- `artifacts`: keep outputs/reports between jobs.
- `cache`: speed builds (e.g., `.m2/repository`).
- `only`/`except` or `rules`: control when jobs run.

### Variables
- Define in UI (masked/protected) or in `.gitlab-ci.yml` under `variables:`.
- Access in jobs as env vars, e.g., `$CI_PROJECT_NAME`.

### Viewing pipelines
- Pipelines → select run → view logs per job; artifacts downloadable from the UI.

Proceed to Part 2 for deeper pipeline configuration, rules, caching, and templates.
