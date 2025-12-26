# Khái niệm cốt lõi về Kubernetes

## Node
Node là máy worker chạy container. Nếu chỉ có một node và node đó sập, ứng dụng sẽ sập — cần nhiều node để chia tải và đảm bảo tính khả dụng cao.

## Cluster
Cluster là một tập hợp các node cùng phối hợp để chạy workloads.

## Control Plane (Master)
Control Plane là node (hoặc nhiều node) chạy các thành phần quản lý của Kubernetes, theo dõi cluster và thực hiện orchestration. Nên triển khai multi-master HA để tránh điểm lỗi đơn.

## API Server
API Server cung cấp API của Kubernetes. Các client như `kubectl`, dashboard hoặc công cụ bên ngoài tương tác với cluster thông qua API Server.

## etcd
etcd là cơ sở dữ liệu key-value phân tán và tin cậy lưu toàn bộ trạng thái của cluster. etcd lưu thông tin về nodes, pods, cấu hình, v.v. và cung cấp cơ chế bầu leader/khóa để tránh xung đột giữa các master.

## Scheduler
Scheduler chịu trách nhiệm phân phối Pods lên các node phù hợp dựa trên yêu cầu tài nguyên, ràng buộc và chính sách.

## Controller Manager
Controller Manager chạy các controller chịu trách nhiệm đối chiếu trạng thái mong muốn và thực tế (ví dụ ReplicaSet controller, Node controller, Endpoint controller). Nó phát hiện lỗi (node, Pod, endpoint) và thực hiện hành động khôi phục.

## Container Runtime
Container Runtime là phần mềm nền tảng để chạy container, ví dụ containerd, Docker, CRI-O.

## Kubelet
Kubelet là agent chạy trên mỗi node, đảm bảo các container trong Pod được khởi động và duy trì trạng thái sức khỏe, đồng thời báo cáo trạng thái về control plane.