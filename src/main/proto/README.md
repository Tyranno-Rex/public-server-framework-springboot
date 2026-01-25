# Protocol Buffers & gRPC Services

This directory contains `.proto` files that define the gRPC service contracts.

## Proto Files

- `common.proto` - Common types (Empty, ApiResponse, PageRequest, etc.)
- `health.proto` - Health check service
- `user.proto` - User management service

## Build & Generate Java Code

Proto files are automatically compiled during Gradle build:

```bash
./gradlew clean build
```

Generated Java code will be in:
- `build/generated/source/proto/main/java/` - Message types
- `build/generated/source/proto/main/grpc/` - Service stubs

## gRPC Server Configuration

Configure in `application.yml`:

```yaml
grpc:
  server:
    port: 9090
    max-inbound-message-size: 10MB
```

## Implemented Services

### HealthService (Port: 9090)
- ✅ `check()` - Health check
- ✅ `version()` - Get version info

### UserService (Port: 9090)
- ⚠️ `getUser()` - Get user by ID (TODO: implement)
- ⚠️ `getProfile()` - Get current user profile (TODO: implement)
- ⚠️ `updateProfile()` - Update profile (TODO: implement)
- ⚠️ `listUsers()` - List users with pagination (TODO: implement)
- ⚠️ `deleteUser()` - Delete user (TODO: implement)

## Testing gRPC Services

### Using grpcurl

```bash
# Install grpcurl
brew install grpcurl

# Health check
grpcurl -plaintext localhost:9090 health.HealthService/Check

# Get version
grpcurl -plaintext localhost:9090 health.HealthService/Version
```

### Using BloomRPC

Download from: https://github.com/bloomrpc/bloomrpc

1. Import proto files
2. Connect to `localhost:9090`
3. Send requests

## Adding New Services

1. Create new `.proto` file in this directory
2. Define service and messages
3. Build project to generate Java code
4. Implement service in `src/main/java/com/common/server/grpc/service/`
5. Add `@GrpcService` annotation

## Client Integration

Proto files should be copied to client projects:
- Web: `public-client-framework-react/proto/`
- React Native: `public-client-framework-app-react/proto/`
