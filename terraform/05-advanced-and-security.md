## Part 5: Advanced and security

### Locking and concurrency
- Ensure backend supports locks (S3+DynDB, GCS, Azure). Never disable locking in teams.
- Avoid concurrent applies; serialize via CI.

### Secrets handling
- Do not commit secrets. Prefer:
  - Environment variables injected at runtime.
  - External secret managers (AWS Secrets Manager/SSM, GCP Secret Manager, Vault) with data sources.
  - `sops` + `sops` provider or helm-secrets-like patterns for encrypted tfvars.
- Mark outputs `sensitive = true`.

### Policy and guardrails
- checkov/tfsec for static checks.
- OPA/Conftest for custom policies on `terraform show -json plan.tfplan`.
- Sentinel (Terraform Cloud/Enterprise) if available.

### Multi-environment layout
- Option A: separate directories per env (recommended for clarity):
```
envs/
  dev/
    main.tf
    terraform.tfvars
  prod/
    main.tf
    terraform.tfvars
```
- Option B: workspaces with env-specific vars; be disciplined about backend key naming.

### Provider/version pinning
- Pin providers and Terraform version (`required_providers`, `required_version`).
- Commit `.terraform.lock.hcl`; regenerate when upgrading providers intentionally.

### Drift and compliance
- Scheduled `terraform plan` to detect drift.
- Alert on plan deltas in CI; require approval before apply.

### Performance and large states
- Keep states scoped (don’t put everything in one state).
- Use data sources instead of duplicating resources across states.
- For very large plans, split into multiple stacks/modules.

### Imports and refactors
- Use `terraform state mv` to reshape state without recreating resources.
- Plan after moves/imports to ensure no unintended changes.

### Debugging
- `TF_LOG=INFO` (or DEBUG) for provider diagnostics.
- `terraform show -json` for machine-readable outputs.
- `terraform graph | dot -Tsvg > graph.svg` to visualize dependencies.

### Terragrunt (optional mention)
- Terragrunt can help with DRY backends, repeatable module calls, and environment stacks. If adopted, keep rules simple and documented.

You now have a full Terraform path from basics to advanced practices. Keep state safe, plans reviewed, and secrets out of code.
