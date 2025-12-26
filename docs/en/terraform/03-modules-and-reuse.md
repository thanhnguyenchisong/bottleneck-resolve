## Part 3: Modules and reuse

### What is a module?
- A folder with `.tf` files; `main.tf` consuming `module` blocks is the root module.
- Child modules are reusable units with `variables` and `outputs`.

### Creating a local module (example)
```
modules/s3-logs/
  main.tf
  variables.tf
  outputs.tf
```
`variables.tf`:
```hcl
variable "bucket_name" { type = string }
variable "versioning"  { type = bool  default = true }
```
`main.tf`:
```hcl
resource "aws_s3_bucket" "this" {
  bucket = var.bucket_name
  versioning { enabled = var.versioning }
}
```
`outputs.tf`:
```hcl
output "bucket_arn" { value = aws_s3_bucket.this.arn }
```
Use it:
```hcl
module "logs" {
  source      = "./modules/s3-logs"
  bucket_name = "perf-logs"
}
```

### Registry modules
- Use well-maintained registry modules for common infra (VPC, ALB, RDS, GKE, etc.).
- Pin versions: `source = "terraform-aws-modules/vpc/aws"`, `version = "~> 5.0"`.

### Variables best practices
- Type everything; add validation where helpful.
```hcl
variable "env" {
  type = string
  validation {
    condition     = contains(["dev","staging","prod"], var.env)
    error_message = "env must be one of dev|staging|prod."
  }
}
```
- Use maps/objects for structured inputs; avoid many parallel lists.

### Outputs
- Only output what callers need (IDs, ARNs, endpoints).
- Sensitive outputs: `sensitive = true`.

### Composition patterns
- Layered modules: network, data, app. Root module wires them with variables/outputs.
- Per-environment stacks: reuse same modules with different `tfvars`.
- Avoid excessive nesting; prefer flatter composition for clarity.

### Versioning and registries
- For internal modules, version via git tags or a private registry.
- Keep modules backward compatible; document required providers/versions.

### Testing modules (high level)
- `terraform validate` and `terraform plan` in CI.
- Terratest or kitchen-terraform for integration tests (spin resources, assert, destroy).

Proceed to Part 4 for testing and CI practices.
