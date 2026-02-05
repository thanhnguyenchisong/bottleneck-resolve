# Tài liệu Frontend — Angular và ứng dụng web hoàn chỉnh

Bộ tài liệu này bao gồm **Angular** và các thành phần xung quanh cần thiết để xây dựng một **ứng dụng web Angular hoàn chỉnh**: TypeScript, components, routing, forms, HTTP, RxJS, UI, testing và build/deploy.

## 📚 Mục lục

Đọc theo thứ tự số để đi từ nền tảng đến ứng dụng hoàn chỉnh.

| # | File | Nội dung |
|---|------|----------|
| 01 | [TypeScript cơ bản](./01-typescript-basics.md) | TypeScript cho Angular: types, class, interface, decorators, module |
| 02 | [Angular căn bản](./02-angular-fundamentals.md) | Angular là gì, CLI, cấu trúc project, lifecycle |
| 03 | [Components & Templates](./03-components-templates.md) | Component, template, data binding, input/output, ViewChild |
| 04 | [Directives & Pipes](./04-directives-pipes.md) | Structural/attribute directives, built-in & custom pipes |
| 05 | [Services & Dependency Injection](./05-services-di.md) | Service, DI, inject(), providedIn, singleton |
| 06 | [Routing & Navigation](./06-routing-navigation.md) | Router, lazy loading, guards, resolvers |
| 07 | [Forms](./07-forms.md) | Template-driven forms, Reactive Forms, validation |
| 08 | [HTTP Client](./08-http-client.md) | HttpClient, interceptors, error handling |
| 09 | [RxJS trong Angular](./09-rxjs-angular.md) | Observable, operators, async pipe, Subject |
| 10 | [State & Kiến trúc](./10-state-architecture.md) | Quản lý state, service-based, khi nào dùng NgRx |
| 11 | [UI & Styling](./11-ui-styling.md) | Angular Material, SCSS, theming, responsive |
| 12 | [Testing](./12-testing.md) | Unit test (Jasmine/Karma), e2e (Cypress/Playwright) |
| 13 | [Build & Deploy](./13-build-deploy.md) | Environments, build, SSR, deploy (static, Docker) |
| 14 | [**NgRx**](./14-ngrx.md) | Store, Actions, Reducers, Effects, Selectors, feature state |
| 15 | [**Master Angular**](./15-master-angular.md) | Change Detection, Signals, Performance, Security, Kiến trúc, **Checklist phỏng vấn Senior** |
| 16 | [**AG-Grid**](angular/16-ag-grid.md) | Data grid: columnDefs, sort/filter, virtual scroll, cell editor/renderer, tích hợp Angular |

## 🎯 Lộ trình học

### Bắt đầu (ứng dụng đơn giản)
1. **01** TypeScript → **02** Angular căn bản → **03** Components & Templates → **05** Services & DI → **06** Routing

### Ứng dụng đầy đủ (CRUD, form, API)
2. **07** Forms → **08** HTTP Client → **09** RxJS trong Angular

### Nâng cao (state, UI, chất lượng)
3. **10** State & Kiến trúc → **14** NgRx (chi tiết) → **11** UI & Styling → **16** AG-Grid (data grid) → **12** Testing → **13** Build & Deploy

### Senior / Master (phỏng vấn, kiến trúc, performance)
4. **15** Master Angular — Change Detection, Signals, Performance, Security, **checklist câu hỏi phỏng vấn Senior**

## 📝 Cấu trúc mỗi bài

- **Khái niệm**: Giải thích ngắn gọn
- **Ví dụ code**: Angular/TypeScript minh họa
- **Best practices**: Gợi ý khi dùng trong dự án thật
- **Câu hỏi thường gặp**: FAQ và gợi ý trả lời phỏng vấn

## 🔗 Công cụ & tài liệu chính thức

- [Angular Documentation](https://angular.dev)
- [Angular CLI](https://angular.dev/tools/cli)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)
- [RxJS](https://rxjs.dev)
- [Angular Material](https://material.angular.io)
- [AG-Grid Angular](https://www.ag-grid.com/angular-data-grid/)

---

**Mục tiêu**: Sau khi học xong bộ tài liệu (01–16), bạn có thể thiết kế và build một **ứng dụng web Angular hoàn chỉnh** (UI, form, gọi API, routing, state, **data grid AG-Grid**, test, build và deploy).

**Mục tiêu Senior / Master — Đọc xong = Master Angular & Lập trình web bằng Angular:**

- Học đủ **01 → 16** (gồm **16 - AG-Grid** cho bảng dữ liệu enterprise) và **15 - Master Angular** (Change Detection, Signals, Performance, Security, kiến trúc).
- Trả lời được **checklist phỏng vấn Senior** trong bài 15 (và thực hành AG-Grid trong project).
- Kết quả: Bạn đủ nền để **làm master Angular** và **lập trình web bằng Angular** ở mức senior — thiết kế kiến trúc, chọn công nghệ (Material, AG-Grid, NgRx), tối ưu performance, bảo mật, test và deploy.
