# UI & Styling

Để build giao diện Angular hoàn chỉnh cần chọn cách styling (SCSS, component styles), thư viện UI (Angular Material, PrimeNG, …) và cách làm responsive, theme.

## Mục lục
1. [Component styles và SCSS](#component-styles-và-scss)
2. [Angular Material](#angular-material)
3. [Theming và responsive](#theming-và-responsive)
4. [Best practices](#best-practices)
5. [Câu hỏi thường gặp](#câu-hỏi-thường-gặp)

---

## Component styles và SCSS

- **styleUrls** / **styleUrl**: File SCSS/CSS riêng cho component; mặc định **encapsulation** (Emulated) nên style chỉ áp cho view của component.
- **styles**: Inline CSS/SCSS trong `@Component({ styles: ['...'] })`.
- **Global**: `src/styles.scss` (được import trong `angular.json`), ảnh hưởng toàn app.

SCSS thường dùng: biến, nested, mixin, import.

```scss
// styles.scss (global)
$primary: #1976d2;
@import 'theme/mixins';

// component
.container {
  padding: 1rem;
  .title {
    color: $primary;
  }
}
```

`:host` và `:host-context` để style host element hoặc theo context (ví dụ theme).

```scss
:host {
  display: block;
}
:host-context(.dark) {
  .card { background: #333; }
}
```

---

## Angular Material

Component library chính thức (Material Design). Cài và cấu hình:

```bash
ng add @angular/material
```

Chọn theme (Indigo/Pink, …), typography, animations. Sẽ thêm vào `angular.json` và `app.config.ts`.

Dùng component trong template (import standalone):

```typescript
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';

@Component({
  imports: [MatButtonModule, MatTableModule],
  ...
})
```

```html
<button mat-raised-button color="primary">Lưu</button>
<table mat-table [dataSource]="dataSource">...</table>
```

- **Cdk (Component Dev Kit)**: Một số primitive (overlay, a11y) dùng mà không bắt buộc Material theme.
- **Icons**: `MatIconModule` + font hoặc SVG; **Material Icons** font.

---

## Theming và responsive

- **Material Theming**: Dùng Sass variables và mixins (`@include angular-material-theme($theme)`); tạo theme sáng/tối bằng cách đổi palette.
- **CSS variables**: Có thể dùng biến CSS (custom properties) cho màu, spacing; đổi giá trị theo class (ví dụ `.dark`) để switch theme.
- **Responsive**: Media queries trong SCSS hoặc dùng **BreakpointObserver** (CDK) trong class để hiển thị/ẩn, đổi layout.

```typescript
import { BreakpointObserver } from '@angular/cdk/layout';

constructor(private bp: BreakpointObserver) {}
isSmall$ = this.bp.observe('(max-width: 600px)').pipe(
  map(s => s.matches),
);
```

---

## Best practices

- Ưu tiên **component-scoped** style; global chỉ cho reset, typography, theme biến.
- Dùng **Angular Material** (hoặc một lib nhất quán) để UI đồng bộ và a11y.
- **Responsive**: Mobile-first, test nhiều kích thước.
- **Accessibility**: Semantic HTML, label, focus, contrast; Material component đã hỗ trợ nhiều.

---

## Câu hỏi thường gặp

**Angular Material vs PrimeNG / other?**  
Material tích hợp tốt với Angular, design chuẩn. PrimeNG có nhiều component (table, chart), theme riêng. Chọn theo yêu cầu design và component cần dùng.

**Style component ảnh hưởng child?**  
Mặc định encapsulation Emulated: style component chỉ ảnh hưởng view của nó (Angular thêm attribute). Style không “xuyên” vào child component. Để style child có thể dùng `::ng-deep` (deprecated) hoặc đưa style lên global có scope class cha.

**Làm theme tối/sáng?**  
Material: tạo 2 theme (light/dark), đổi class trên body hoặc dùng `@media (prefers-color-scheme)`. Hoặc dùng CSS variables và đổi giá trị theo class.

**Bảng dữ liệu lớn (sort, filter, virtual scroll)?**  
Dùng **AG-Grid**: [16 - AG-Grid](../16-ag-grid.md). Material Table đủ cho list đơn giản; AG-Grid cho màn admin/report.

---

→ Tiếp theo: [12 - Testing](12-testing.md)
