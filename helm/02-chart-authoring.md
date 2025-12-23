## Part 2: Chart authoring

### Chart metadata (Chart.yaml)
- Required: `apiVersion: v2`, `name`, `version` (chart), `description`.
- App version: `appVersion` (string) → reflects the container app version.
- Dependencies: list subcharts with `name`, `version`, `repository`.

### Files to know
- `values.yaml`: default values (keep minimal, sane defaults).
- `templates/`: Kubernetes manifests with Go template syntax.
- `.helmignore`: exclude junk from packaged chart.
- `charts/`: packaged subcharts (after `helm dependency build`).

### Create a chart
```bash
helm create perf-app
rm -rf perf-app/templates/tests   # optional: remove test job
```

### Common templates
- `deployment.yaml`, `service.yaml`, `_helpers.tpl` (defines template helpers), `ingress.yaml`, `hpa.yaml` (if enabled).
- Use helper templates for names/labels:
```gotmpl
{{- define "perf-app.fullname" -}}
{{ include "perf-app.name" . }}-{{ .Release.Name }}
{{- end }}
```

### Naming/labels
- Follow Kubernetes recommended labels:
```yaml
metadata:
  labels:
    app.kubernetes.io/name: {{ include "perf-app.name" . }}
    app.kubernetes.io/instance: {{ .Release.Name }}
    app.kubernetes.io/version: {{ .Chart.AppVersion }}
    helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version }}
```

### Values conventions
- Keep defaults simple; put env-specific overrides in `values-<env>.yaml`.
- Group logically:
```yaml
image:
  repository: thanhncs/thanhdev
  tag: "latest"
  pullPolicy: IfNotPresent
resources:
  requests: {cpu: 500m, memory: 512Mi}
  limits:   {cpu: "1",   memory: 1Gi}
service:
  type: ClusterIP
  port: 80
```

### Dependencies (subcharts)
- Declare in Chart.yaml, then:
```bash
helm dependency update
```
- Disable/enable via values: subchart `enabled: false`.

### Hooks (use sparingly)
- For migrations or one-off jobs:
```yaml
annotations:
  "helm.sh/hook": pre-install,pre-upgrade
  "helm.sh/hook-delete-policy": before-hook-creation,hook-succeeded
```
- Keep hooks idempotent; they run outside normal rollout.

### Schema for values (validation)
- Add `values.schema.json` to validate user values.
- Example snippet:
```json
{
  "type": "object",
  "properties": {
    "replicaCount": {"type": "integer", "minimum": 1},
    "image": {
      "type": "object",
      "properties": {
        "repository": {"type": "string"},
        "tag": {"type": "string"}
      },
      "required": ["repository"]
    }
  }
}
```

### Lint early
```bash
helm lint ./perf-app
helm template ./perf-app -f values.yaml | kubeconform -strict   # optional schema check
```

Next: Part 3 dives into templating and values patterns.
