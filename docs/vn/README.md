## Demo Cải Thiện Hiệu Suất

Dự án này là một ứng dụng Spring Boot nhỏ được sử dụng để học **kiểm thử hiệu suất** và **phân tích điểm nghẽn**.

### Ngăn xếp công nghệ

- **Tạo tải (địa phương)**: JMeter
- **Phân tích hiệu suất (địa phương)**: Async Profiler
- **Số liệu (giống production)**: Micrometer → Prometheus → Grafana

### Kịch bản

- Endpoint REST `GET /work?n=10000` với một **thuật toán cố tình không hiệu quả** (độ phức tạp bậc hai sử dụng `List.contains`).
- Sử dụng **JMeter** để gửi các yêu cầu đồng thời và quan sát độ trễ cao / sử dụng CPU cao.
- Sử dụng **Async Profiler** để chụp biểu đồ flame graph CPU và xác định phương thức nóng.
- Sử dụng **Micrometer + Prometheus + Grafana** để giám sát độ trễ yêu cầu, thông lượng và số liệu JVM trong thiết lập giống production.

Các hướng dẫn chi tiết từng bước sẽ được thêm vào `docs/`.