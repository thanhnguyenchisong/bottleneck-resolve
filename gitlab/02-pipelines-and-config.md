## Part 2: Pipelines and configuration

### Stages and jobs
- Define `stages` order; jobs declare a `stage`.
- Jobs run in parallel within a stage when possible.

### Rules (preferred) vs only/except
- Use `rules:` to control job execution:
```yaml
rules:
  - if: '$CI_COMMIT_BRANCH == "main"'
  - if: '$CI_PIPELINE_SOURCE == "merge_request_event"'
```
- Avoid legacy `only/except` when adding complex conditions.

### Artifacts and dependencies
- Pass build outputs between jobs:
```yaml
artifacts:
  paths: [target/*.jar]
  expire_in: 1 week
```
- Use `needs:` to create DAG and to download artifacts from dependencies:
```yaml
build:
  stage: build
  script: mvn -B package

test:
  stage: test
  needs: [build]
  script: mvn -B test
```

### Cache (speed up)
- Cache Maven repo:
```yaml
cache:
  key: "$CI_PROJECT_NAME-m2"
  paths:
    - .m2/repository
```
- Use separate keys per branch if needed: `key: "$CI_COMMIT_REF_SLUG-m2"`.

### Templates and includes
- Reuse snippets with anchors:
```yaml
.maven-job: &maven-job
  image: maven:3.9-eclipse-temurin-17
  before_script: ['mvn -B -q --version']
```
- Include remote/local templates:
```yaml
include:
  - local: .gitlab/ci/base.yml
  - remote: https://example.com/common.gitlab-ci.yml
```

### Variables and environments
- Define globals:
```yaml
variables:
  MAVEN_OPTS: "-Dmaven.test.skip=false"
```
- Per-job overrides allowed.
- Environments for deploys:
```yaml
deploy:
  stage: deploy
  environment:
    name: staging
    url: https://staging.example.com
```

### Services (for integration tests)
- Spin up dependencies:
```yaml
services:
  - name: postgres:15
    alias: db
variables:
  POSTGRES_HOST: db
  POSTGRES_DB: app
```

Proceed to Part 3 for advanced topics: environments, approvals, security scans, and protected variables/branches.
