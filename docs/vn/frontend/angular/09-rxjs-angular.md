# RxJS trong Angular

Angular dùng RxJS (Observable) cho HTTP, Router, Forms, EventEmitter. Hiểu cơ bản Observable và operators giúp xử lý async và data flow đúng cách.

## Mục lục
1. [Observable cơ bản](#observable-cơ-bản)
2. [Operators thường dùng](#operators-thường-dùng)
3. [Subject và multicast](#subject-và-multicast)
4. [async pipe và unsubscribe](#async-pipe-và-unsubscribe)
5. [Câu hỏi thường gặp](#câu-hỏi-thường-gặp)

---

## Observable cơ bản

Observable là stream giá trị theo thời gian: emit 0, 1 hoặc nhiều giá trị, có thể complete hoặc error. Subscribe để nhận.

```typescript
import { Observable, of, from } from 'rxjs';

const obs$ = new Observable<number>(subscriber => {
  subscriber.next(1);
  subscriber.next(2);
  subscriber.complete();
});
obs$.subscribe({
  next: v => console.log(v),
  complete: () => console.log('done'),
});

of(1, 2, 3).subscribe(v => console.log(v));
from([1, 2, 3]).subscribe(v => console.log(v));
```

- **of**: Emit lần lượt các tham số rồi complete.
- **from**: Từ array, Promise, hoặc iterable → Observable.

---

## Operators thường dùng

Dùng trong `.pipe(...)`.

| Operator | Mục đích |
|----------|----------|
| `map` | Biến đổi từng giá trị |
| `filter` | Chỉ emit khi điều kiện true |
| `tap` | Side effect (log, không đổi value) |
| `catchError` | Bắt lỗi, trả về Observable mới |
| `switchMap` | Chuyển sang Observable khác, hủy Observable cũ (dùng cho search, route params) |
| `mergeMap` / `concatMap` | Map sang Observable, merge/concat kết quả |
| `debounceTime` | Chờ một khoảng không emit mới → dùng cho search input |
| `distinctUntilChanged` | Chỉ emit khi giá trị thay đổi |
| `takeUntilDestroyed` | Unsubscribe khi component destroy (Angular 16+) |

```typescript
this.route.params.pipe(
  switchMap(p => this.productService.getById(+p['id'])),
  catchError(() => of(null)),
).subscribe(product => this.product = product);
```

```typescript
this.searchControl.valueChanges.pipe(
  debounceTime(300),
  distinctUntilChanged(),
  switchMap(q => this.api.search(q)),
).subscribe(results => this.results = results);
```

---

## Subject và multicast

- **Subject**: Vừa Observable vừa Observer; nhiều subscriber dùng chung, emit qua `.next(value)`.
- **BehaviorSubject**: Có giá trị khởi tạo, subscriber mới nhận ngay giá trị hiện tại.
- **ReplaySubject**: Replay n giá trị gần nhất cho subscriber mới.

```typescript
private search$ = new BehaviorSubject<string>('');

setSearch(v: string) {
  this.search$.next(v);
}

getSearch(): Observable<string> {
  return this.search$.asObservable();
}
```

Dùng BehaviorSubject khi cần “giá trị hiện tại” (ví dụ user đã login, theme).

---

## async pipe và unsubscribe

**async pipe**: Subscribe Observable trong template và tự unsubscribe khi component destroy.

```html
@if (products$ | async; as products) {
  <ul>
    @for (p of products; track p.id) {
      <li>{{ p.name }}</li>
    }
  </ul>
}
```

Trong component chỉ cần:

```typescript
products$ = this.productService.getAll();
```

Tránh quên unsubscribe khi subscribe trong class:

```typescript
private destroyRef = inject(DestroyRef);
ngOnInit() {
  this.productService.getAll()
    .pipe(takeUntilDestroyed(this.destroyRef))
    .subscribe(list => this.products = list);
}
```

---

## Câu hỏi thường gặp

**switchMap vs mergeMap?**  
switchMap hủy Observable nội khi có emit mới (ví dụ search: gõ tiếp thì hủy request cũ). mergeMap giữ nhiều Observable chạy song song. Dùng switchMap cho “chỉ cần kết quả mới nhất”.

**Khi nào dùng Subject?**  
Khi cần phát sự kiện/state từ service cho nhiều component (event bus, shared state đơn giản) hoặc khi chuyển từ event/callback sang Observable.

**Memory leak do không unsubscribe?**  
Có. Subscribe trong component mà không unsubscribe khi destroy sẽ giữ reference. Cách an toàn: async pipe hoặc takeUntilDestroyed.

---

→ Tiếp theo: [10 - State & Kiến trúc](10-state-architecture.md)
