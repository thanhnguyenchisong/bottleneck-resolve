# Build & Deploy

Bài này tóm tắt môi trường (environments), lệnh build, SSR (Server-Side Rendering) và cách deploy ứng dụng Angular (static host, Docker).

## Mục lục
1. [Environments](#environments)
2. [Build](#build)
3. [SSR và SSG](#ssr-và-ssg)
4. [Deploy](#deploy)
5. [Câu hỏi thường gặp](#câu-hỏi-thường-gặp)

---

## Environments

Cấu hình theo môi trường (dev, staging, prod): API URL, feature flags, key (không commit secret thật).

**Cách 1 – file replacement (truyền thống):**

- `src/environments/environment.ts` (default, thường dev)
- `src/environments/environment.prod.ts`

Trong `angular.json`, mỗi configuration có `fileReplacements`:

```json
"configurations": {
  "production": {
    "fileReplacements": [
      { "replace": "src/environments/environment.ts", "with": "src/environments/environment.prod.ts" }
    ]
  }
}
```

Import trong code: `import { environment } from '../environments/environment';`

**Cách 2 – inject tại runtime:** Build một lần, inject config khi deploy (ví dụ file `config.json` load lúc khởi động, hoặc biến môi trường qua script). Phù hợp khi cùng một build chạy nhiều môi trường.

---

## Build

- **Development**: `ng serve` (mặc định), build trong memory, hot reload.
- **Production**: `ng build` hoặc `ng build --configuration=production`.

Output mặc định: `dist/<project-name>/` — static files (index.html, JS, CSS, assets).

- **Optimization**: Production build minify, tree-shake, AOT (Ahead-of-Time) compile.
- **Chunk**: Lazy-loaded route tạo chunk riêng; có thể xem bằng `ng build --stats-json` và phân tích bundle.

---

## SSR và SSG

- **SSR (Server-Side Rendering)**: Render HTML trên server mỗi request → tốt cho SEO và first paint. Angular có **Angular Universal** (integrated với `ng add @angular/ssr`).
- **SSG (Static Site Generation)**: Pre-render một số route thành HTML tĩnh tại build time.

Thêm SSR (Angular 17+):

```bash
ng add @angular/ssr
```

Sẽ cấu hình server entry, `server.ts`, và script build. Deploy cần chạy Node server (hoặc platform hỗ trợ Node).

---

## Deploy

**Static (SPA):**  
Build `ng build`, upload nội dung `dist/<project>/` lên host tĩnh (Nginx, S3, Firebase Hosting, Netlify, Vercel). Cấu hình fallback `index.html` cho client-side routing (mọi path trả về index.html).

**Docker:**  
Dùng image Nginx với static files từ `dist/`, hoặc Node image nếu dùng SSR.

Ví dụ Dockerfile (static):

```dockerfile
FROM node:20 AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY .. .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist/my-app/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

`nginx.conf`: root `/usr/share/nginx/html`, `try_files $uri $uri/ /index.html;` cho SPA.

**CI/CD:** Chạy `npm ci`, `ng test`, `ng build` trong pipeline; deploy artifact lên host hoặc push image Docker.

---

## Câu hỏi thường gặp

**AOT vs JIT?**  
Production mặc định dùng AOT: compile template tại build time, bundle nhỏ hơn, bảo mật hơn. JIT compile trên browser (dev có thể dùng), ít dùng trong production.

**Làm sao giảm bundle size?**  
Lazy load route, dùng tree-shake (import đúng path, tránh barrel export dư), kiểm tra dependency nặng; `ng build --stats-json` + công cụ phân tích (webpack-bundle-analyzer).

**Deploy SSR khác gì SPA?**  
SPA chỉ cần host static file. SSR cần chạy Node server (hoặc serverless function) để render HTML mỗi request; deploy lên Node host (Railway, Render, Vercel Node, …).

---

→ **Senior/Master**: Performance build, bundle analysis, SSR trade-offs — [15 - Master Angular](./15-master-angular.md). Quay lại [README](./README.md).
