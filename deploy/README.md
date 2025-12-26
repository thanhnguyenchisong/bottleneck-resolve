# Bottleneck Resolve - Production Kubernetes Manifests

This directory contains production-ready Kubernetes manifests for deploying the bottleneck-resolve Java application.

## Prerequisites

- Kubernetes cluster (v1.24+)
- kubectl configured to access your cluster
- (Optional) Prometheus Operator for ServiceMonitor
- (Optional) cert-manager for TLS certificates
- (Optional) NGINX Ingress Controller

## File Structure

```
deploy/
├── 01-namespace.yaml          # Namespace definition
├── 02-configmap.yaml          # Application configuration
├── 03-secret.yaml             # Secrets template (update with actual secrets)
├── 04-deployment.yaml         # Main application deployment
├── 05-service.yaml            # Service definition
├── 06-hpa.yaml                # Horizontal Pod Autoscaler
├── 07-pdb.yaml                # Pod Disruption Budget
├── 08-servicemonitor.yaml     # Prometheus ServiceMonitor (requires Prometheus Operator)
├── 09-ingress.yaml            # Ingress for external access
├── 10-networkpolicy.yaml      # Network policies for security
├── 11-serviceaccount.yaml     # Service account
├── 12-rbac.yaml               # RBAC roles and bindings
├── kustomization.yaml         # Kustomize configuration
└── README.md                  # This file
```

## Quick Start

### 1. Update Configuration

Before deploying, update the following files:

- **03-secret.yaml**: Add your actual secrets (use sealed-secrets or external-secrets in production)
- **09-ingress.yaml**: Update the hostname from `bottleneck-resolve.example.com` to your actual domain
- **04-deployment.yaml**: Update the image name from `bottleneck-resolve:latest` to your actual container image

### 2. Build and Push Docker Image

```bash
# Build the application
mvn clean package

# Build Docker image
docker build -t your-registry/bottleneck-resolve:latest .

# Push to registry
docker push your-registry/bottleneck-resolve:latest

# Update deployment.yaml with your image
```

### 3. Deploy Using kubectl

```bash
# Apply all manifests in order
kubectl apply -f 01-namespace.yaml
kubectl apply -f 02-configmap.yaml
kubectl apply -f 03-secret.yaml
kubectl apply -f 11-serviceaccount.yaml
kubectl apply -f 12-rbac.yaml
kubectl apply -f 04-deployment.yaml
kubectl apply -f 05-service.yaml
kubectl apply -f 06-hpa.yaml
kubectl apply -f 07-pdb.yaml
kubectl apply -f 08-servicemonitor.yaml  # Requires Prometheus Operator
kubectl apply -f 09-ingress.yaml
kubectl apply -f 10-networkpolicy.yaml
```

### 4. Deploy Using Kustomize

```bash
kubectl apply -k .
```

## Production Features

### High Availability
- **Replicas**: 3 pods minimum
- **Pod Disruption Budget**: Ensures at least 2 pods are available during disruptions
- **Rolling Updates**: Zero-downtime deployments with maxUnavailable: 0

### Auto Scaling
- **HPA**: Automatically scales between 3-10 pods based on CPU (70%) and memory (80%) utilization
- **Scale-down**: Conservative (5 minutes stabilization, max 50% reduction)
- **Scale-up**: Aggressive (immediate, up to 100% increase or 2 pods)

### Health Checks
- **Liveness Probe**: Checks `/actuator/health/liveness` every 10s after 60s initial delay
- **Readiness Probe**: Checks `/actuator/health/readiness` every 5s after 30s initial delay
- **Startup Probe**: Allows up to 5 minutes for application startup

### Resource Management
- **Requests**: 500m CPU, 768Mi memory
- **Limits**: 2000m CPU, 1536Mi memory
- **JVM Settings**: G1GC with heap dump on OOM
  - Initial heap: 512MB
  - Max heap: 1024MB
  - GC pause target: 200ms

### Security
- **Security Context**: Non-root user (UID 1000), read-only root filesystem disabled (needed for profiler)
- **Capabilities**: Only SYS_ADMIN and PERFMON (required for async-profiler)
- **Network Policy**: Restricts ingress/egress traffic
- **RBAC**: Minimal permissions for service account

### Observability
- **Prometheus Metrics**: Exposed at `/actuator/prometheus`
- **ServiceMonitor**: For Prometheus Operator integration
- **Annotations**: Prometheus scraping annotations on pods and service

### Networking
- **Service**: ClusterIP with session affinity
- **Ingress**: NGINX ingress with TLS support
- **Network Policy**: Restricts network access

## Configuration

### Environment Variables

The deployment uses the following environment variables:
- `SPRING_CONFIG_LOCATION`: Points to ConfigMap-mounted configuration
- `JAVA_TOOL_OPTIONS`: JVM tuning options
- `SPRING_PROFILES_ACTIVE`: Set to "production"

### Secrets Management

⚠️ **Important**: The `03-secret.yaml` file is a template. In production:
- Use [Sealed Secrets](https://github.com/bitnami-labs/sealed-secrets)
- Use [External Secrets Operator](https://external-secrets.io/)
- Use [HashiCorp Vault](https://www.vaultproject.io/)
- Never commit actual secrets to version control

### Customization

#### Update Image
```yaml
# In 04-deployment.yaml
image: your-registry/bottleneck-resolve:tag
```

#### Adjust Resources
```yaml
# In 04-deployment.yaml
resources:
  requests:
    cpu: "500m"
    memory: "768Mi"
  limits:
    cpu: "2000m"
    memory: "1536Mi"
```

#### Modify HPA
```yaml
# In 06-hpa.yaml
minReplicas: 3
maxReplicas: 10
```

#### Update Ingress Host
```yaml
# In 09-ingress.yaml
spec:
  rules:
  - host: your-domain.com
```

## Monitoring

### Check Pod Status
```bash
kubectl get pods -n bottleneck-resolve
```

### Check Service
```bash
kubectl get svc -n bottleneck-resolve
```

### Check HPA
```bash
kubectl get hpa -n bottleneck-resolve
```

### View Logs
```bash
kubectl logs -f deployment/bottleneck-resolve -n bottleneck-resolve
```

### Access Metrics
```bash
# Port forward to service
kubectl port-forward svc/bottleneck-resolve 8080:80 -n bottleneck-resolve

# Access metrics
curl http://localhost:8080/actuator/prometheus
```

### Test Application
```bash
# Port forward
kubectl port-forward svc/bottleneck-resolve 8080:80 -n bottleneck-resolve

# Test endpoint
curl http://localhost:8080/work?n=1000
```

## Troubleshooting

### Pods Not Starting
1. Check pod events: `kubectl describe pod <pod-name> -n bottleneck-resolve`
2. Check logs: `kubectl logs <pod-name> -n bottleneck-resolve`
3. Verify image exists and is accessible
4. Check resource quotas: `kubectl describe quota -n bottleneck-resolve`

### Health Checks Failing
1. Verify the application is responding: `kubectl exec -it <pod-name> -n bottleneck-resolve -- curl localhost:8080/actuator/health`
2. Check probe timing (initialDelaySeconds might be too short)
3. Verify the health endpoint is enabled in ConfigMap

### HPA Not Scaling
1. Check HPA status: `kubectl describe hpa bottleneck-resolve-hpa -n bottleneck-resolve`
2. Verify metrics server is installed: `kubectl top nodes`
3. Check if metrics are available: `kubectl get --raw /apis/metrics.k8s.io/v1beta1/namespaces/bottleneck-resolve/pods`

### Network Policy Issues
1. Check if pods can communicate: `kubectl exec -it <pod-name> -n bottleneck-resolve -- curl <target-service>`
2. Verify namespace labels match NetworkPolicy selectors
3. Temporarily remove NetworkPolicy to test: `kubectl delete networkpolicy bottleneck-resolve-network-policy -n bottleneck-resolve`

### Image Pull Errors
1. Verify image exists in registry: `docker pull your-registry/bottleneck-resolve:latest`
2. Check imagePullSecrets if using private registry
3. Verify network connectivity to registry

## Cleanup

To remove all resources:

```bash
kubectl delete namespace bottleneck-resolve
```

Or using Kustomize:

```bash
kubectl delete -k .
```

## Notes

- The deployment requires `SYS_ADMIN` and `PERFMON` capabilities for async-profiler functionality
- Seccomp profile is set to `Unconfined` for profiler compatibility
- Adjust security settings based on your organization's security policies
- The Ingress assumes NGINX Ingress Controller - adjust annotations for other controllers
- ServiceMonitor requires Prometheus Operator - remove if not using it
- NetworkPolicy assumes specific namespace labels - adjust based on your cluster setup

## Best Practices Applied

✅ **Resource Limits**: CPU and memory limits set to prevent resource exhaustion  
✅ **Health Probes**: Liveness, readiness, and startup probes configured  
✅ **Security**: Non-root user, minimal capabilities, network policies  
✅ **High Availability**: Multiple replicas with PDB  
✅ **Auto-scaling**: HPA configured for dynamic scaling  
✅ **Observability**: Prometheus metrics and ServiceMonitor  
✅ **Rolling Updates**: Zero-downtime deployment strategy  
✅ **Configuration Management**: ConfigMap for app config, Secret template for sensitive data  
✅ **RBAC**: Least privilege access with Role and RoleBinding  
