## Part 3: Advanced, environments, security

### Environments and deployments
- Use `environment` to track deploys and links:
```yaml
deploy_staging:
  stage: deploy
  script: ./scripts/deploy.sh staging
  environment:
    name: staging
    url: https://staging.example.com
```
- `only: [main]` or `rules:` to guard deploys.
- Use `when: manual` for manual approvals; `allow_failure: false` to block pipeline until run.

### Protected branches/tags and variables
- Protect `main`/`prod` branches; limit who can push/merge.
- Protect environment-scoped variables (e.g., `PROD_DB_PASSWORD`) so only protected branches/tags can access them.

### Approval patterns
- Use manual jobs in deploy stage.
- For merge requests, enable approval rules in project settings; require green pipeline before merge.

### Includes and reusability
- Centralize templates (e.g., `.gitlab/ci/*.yml`) and include them in projects:
```yaml
include:
  - project: mygroup/ci-templates
    ref: main
    file: /maven.yml
```

### Caching and performance
- Separate caches per branch or per major dependency set to avoid pollution.
- Use `cache:policy: pull-push` to reuse prior caches when available.
- Prefer artifacts for passing build outputs; use caches for dependency dirs.

### Security scans (if licensed)
- SAST/DAST/Dependency scans via GitLab templates:
```yaml
include:
  - template: Security/SAST.gitlab-ci.yml
  - template: Security/Dependency-Scanning.gitlab-ci.yml
```
- Ensure language-specific analyzers fit your stack; tune `variables:` for paths/modules.

### Container registry and image build
- Use built-in registry: `$CI_REGISTRY_IMAGE`.
```yaml
build_image:
  stage: build
  image: docker:24
  services: [docker:24-dind]
  variables:
    DOCKER_DRIVER: overlay2
  script:
    - docker login -u "$CI_REGISTRY_USER" -p "$CI_REGISTRY_PASSWORD" "$CI_REGISTRY"
    - docker build -t "$CI_REGISTRY_IMAGE:$CI_COMMIT_SHORT_SHA" .
    - docker push "$CI_REGISTRY_IMAGE:$CI_COMMIT_SHORT_SHA"
  artifacts:
    reports:
      dotenv: build.env   # e.g., write IMAGE_TAG for later stages
```

### K8s deploy (example sketch)
```yaml
deploy_prod:
  stage: deploy
  image: bitnami/kubectl
  script:
    - kubectl config set-cluster prod --server="$KUBE_SERVER" --certificate-authority=$KUBE_CA_PEM
    - kubectl config set-credentials ci --token="$KUBE_TOKEN"
    - kubectl config set-context prod --cluster=prod --user=ci --namespace=perf
    - kubectl config use-context prod
    - kubectl set image deploy/perf-app perf-app=$CI_REGISTRY_IMAGE:$CI_COMMIT_SHORT_SHA
  environment:
    name: production
    url: https://perf.example.com
  when: manual
  only:
    refs: [main]
```

### Observability for pipelines
- Use job-level `script` timings and artifacts to trace slow steps.
- Enable pipeline metrics (if available) to monitor queue time vs execution time.

You now have advanced GitLab CI patterns: guarded deploys, protected secrets, reusable includes, caches, security scans, and container/K8s deploy examples.
