## Part 4: Testing and CI

### Local quality gates
```bash
terraform fmt -recursive
terraform validate
```

### Lint and security
- tflint (syntax + best practices per provider):
```bash
tflint --init
tflint
```
- checkov (IaC security/policy):
```bash
checkov -d .
```
- tfsec (alternative security scanner):
```bash
tfsec .
```

### Plan checks
- Always run `terraform plan` in CI; fail on drift/unexpected changes.
- Optional: `terraform plan -out=plan.tfplan` then `terraform show -json plan.tfplan` for policy checks.

### Testing modules
- Unit-ish: `terraform validate` + `tflint` + `terraform plan` with sample `tfvars`.
- Integration: Terratest (Go) or kitchen-terraform to stand up resources, assert, and destroy.
- For cost/risk, restrict integration tests to a sandbox account/project.

### Sample CI flow (conceptual)
1) `terraform fmt -check`
2) `terraform validate`
3) `tflint`
4) `checkov` (or tfsec)
5) `terraform plan` (against remote state backend)
6) Optional: store plan artifact; manual approval gates before apply

### Apply gates
- Require plan review/approval before apply.
- Use service accounts/roles with least privilege; avoid personal creds in CI.
- Separate apply step (manual or on protected branches).

### State/backends in CI
- Backend config should be consistent; avoid overriding to local state in CI.
- Ensure lock table/bucket exists before running CI plans.

Proceed to Part 5 for advanced topics and security patterns.
