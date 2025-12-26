## Part 5: Advanced Helm patterns

### Subcharts vs library charts
- **Subcharts**: packaged dependencies (e.g., redis). Values live under the subchart key. Disable with `enabled: false`.
- **Library charts**: share templates/helpers without creating resources. Set `type: library` in Chart.yaml; consumer chart `dependencies` imports helpers.

### Global values
- Use sparingly; accessible as `.Values.global.*` across subcharts. Helps share domain, image registry, or labels.

### JSON schema for values (strong validation)
- Add `values.schema.json` to validate inputs; CI should run `helm lint` to enforce.

### Security and supply chain
- Sign charts: `helm package --sign ...` and distribute provenance files.
- Verify before install: `helm verify <chart.tgz>`
- Pin dependencies to exact versions; avoid `latest`.

### Common production patterns
- **Immutable tags**: deploy with image tags tied to commits.
- **Config layering**: base `values.yaml` + env overrides (`values-prod.yaml`) + per-release tweaks.
- **Secrets management**: use external secret stores (e.g., External Secrets Operator) or SOPS/helm-secrets plugin to encrypt values.
- **CRDs**: install CRDs separately or mark them with `crd-install` hook; avoid removing in uninstall unless safe.

### Templating tips
- Use `tpl` to render templated strings from values:
```gotmpl
{{ tpl .Values.extraConfig . | nindent 2 }}
```
- Quote defensively: `{{ .Values.someString | quote }}`.
- Avoid logic bombs: keep templates simple; move complex logic to values and helpers.

### Helm + GitOps
- Flux/ArgoCD can sync Helm charts declaratively. Key flags:
  - `--values`/`--set` in the Application/HelmRelease spec.
  - Enable drift detection and health checks.
- Store rendered manifests for auditing when possible.

### Testing and QA
- Unit test templates: [helm-unittest](https://github.com/helm-unittest/helm-unittest) plugin.
- Smoke tests: `helm test` hook Jobs or external scripts.
- Render diffs in CI with `helm diff` against the live release.

### Performance and size
- Keep chart small; prune vendored charts from git if using dependency update.
- Use `.helmignore` to drop non-essential files (docs, large assets) from the package.

### Troubleshooting quickies
- What got rendered? `helm get manifest <release> | less`
- What values were used? `helm get values <release> -o yaml`
- Why failed? `helm status <release>` + Pod events/logs.

You now have the full path from basics to advanced Helm usage tailored for this project.
