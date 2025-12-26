## Part 4: Release operations (install, upgrade, diff, rollback)

### Install / upgrade / rollback
```bash
# Install with values
helm install perf ./chart -f values.yaml

# Upgrade with overrides
helm upgrade perf ./chart -f values-prod.yaml --set image.tag=1.2.3

# Rollback to previous revision
helm rollback perf 1
```
- Check history: `helm history perf`
- Status: `helm status perf`

### Diff before applying (recommended)
- Plugin: `helm plugin install https://github.com/databus23/helm-diff`
```bash
helm diff upgrade perf ./chart -f values-prod.yaml
```

### Dry runs and render checks
```bash
helm upgrade --install perf ./chart -f values.yaml --dry-run
helm template ./chart -f values.yaml | kubeconform -strict   # schema validation
```

### Testing charts
- Helm test hooks (Job created by chart):
```yaml
annotations:
  "helm.sh/hook": test
```
Run: `helm test perf`
- For app-level tests, prefer external smoke tests (curl, integration scripts) after install.

### Managing values per environment
- Keep `values-dev.yaml`, `values-staging.yaml`, `values-prod.yaml`.
- Use `--set` only for small one-offs; prefer files for repeatability.
- Render to inspect: `helm template ./chart -f values-prod.yaml | less`

### CI hints
- Steps: lint → template → kubeconform → diff (to cluster) → deploy on merge.
- Use immutable image tags; avoid `latest` in CI/CD.
- Fail the pipeline on `helm lint` or diff changes that are unsafe (policy via OPA/Gatekeeper/Kyverno if available).

### Cleanup
```bash
helm uninstall perf
# Remove CRDs manually if the chart installed them and did not clean up
```

Next: Part 5 covers advanced patterns (subcharts, library charts, schema, security).
