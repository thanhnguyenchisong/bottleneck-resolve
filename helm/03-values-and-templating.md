## Part 3: Values and templating

### Values flow
- Precedence: built-in defaults < chart `values.yaml` < `-f custom.yaml` < `--set key=val` < `--set-file`.
- Inspect merged values of a release: `helm get values <release> -o yaml`.

### Templating essentials (Go template + Sprig)
- References:
  - `.Values` (user config), `.Chart`, `.Release`, `.Capabilities`.
  - Include helper templates from `_helpers.tpl` with `{{ include "name" . }}`.
- Conditions:
```gotmpl
{{- if .Values.ingress.enabled }}
...ingress manifest...
{{- end }}
```
- Loops:
```gotmpl
{{- range .Values.extraEnv }}
- name: {{ .name }}
  value: {{ .value | quote }}
{{- end }}
```
- Defaults and required:
```gotmpl
imagePullPolicy: {{ .Values.image.pullPolicy | default "IfNotPresent" }}
{{ required "image.repository is required" .Values.image.repository }}
```

### Common patterns
- Image block:
```gotmpl
image: {{ .Values.image.repository }}:{{ .Values.image.tag | default .Chart.AppVersion }}
imagePullPolicy: {{ .Values.image.pullPolicy | default "IfNotPresent" }}
```
- Probes:
```gotmpl
livenessProbe:
  httpGet: {path: /actuator/health, port: http}
  initialDelaySeconds: {{ .Values.probes.livenessDelay | default 10 }}
```
- Resources with defaults:
```gotmpl
resources:
{{- toYaml .Values.resources | nindent 2 }}
```
- Conditional Service/Ingress:
```gotmpl
{{- if eq .Values.service.type "LoadBalancer" }}
  type: LoadBalancer
{{- end }}
```

### Helpers and reuse
- Define once in `_helpers.tpl`:
```gotmpl
{{- define "perf-app.labels" -}}
app.kubernetes.io/name: {{ include "perf-app.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion }}
{{- end }}
```
- Then reuse:
```gotmpl
metadata:
  labels:
{{ include "perf-app.labels" . | nindent 4 }}
```

### Values organization (example)
```yaml
replicaCount: 3
image:
  repository: thanhncs/thanhdev
  tag: "latest"
  pullPolicy: IfNotPresent
service:
  type: ClusterIP
  port: 80
ingress:
  enabled: false
  className: nginx
  host: perf.example.com
resources:
  requests: {cpu: 500m, memory: 512Mi}
  limits:   {cpu: "1",   memory: 1Gi}
```

### Template debugging
- Render locally: `helm template ./chart -f values.yaml`
- With specific values: `helm template ./chart --set image.tag=test`
- Inspect a single template: use `tpl` function or extract a snippet to test.

Next: Part 4 covers release operations (install/upgrade/diff/rollback/testing).
