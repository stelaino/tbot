# 统一消息转发中心 MVP

基于 JDK 21、Spring Boot、Spring Cloud Gateway 的配置化消息转发服务。配置在启动时校验，变更配置后以重启容器生效；不依赖数据库、Redis 或 MQ。

## 工作流

```text
Gateway 路由 → API Key 鉴权 → 单 Client 限流 → 路由授权 → 幂等防重
→ Endpoint / Route 目标解析 → 策略投递 → 汇总结果
```

Gateway 仅匹配两个固定入口：`POST /api/v1/messages` 与 `POST /hook/**`。`/hook/**` 最终仍须精确命中 `notify.endpoints` 中的路径，不能代理或转发至调用方给出的任意 URL。

## 接口

所有调用都必须带 `X-Api-Key`。

```http
POST /api/v1/messages
X-Api-Key: <client-key>
Content-Type: application/json

{
  "routeKey": "chatgpt-notify",
  "requestId": "chatgpt-20260820-001",
  "message": { "type": "TEXT", "content": "库存异常已处理" }
}
```

固定 Endpoint 不需要 `routeKey`：

```http
POST /hook/finance
X-Api-Key: <finance-key>
Content-Type: application/json

{
  "requestId": "FIN-001",
  "message": { "type": "MARKDOWN", "content": "# 财务日报" }
}
```

响应统一返回 `SUCCESS`、`PARTIAL_SUCCESS` 或 `FAILED`。相同 `Client + requestId` 在 `notify.duplicate-ttl` 内会返回 `DUPLICATE_REQUEST`，且不再执行投递。

## ChatGPT 网页版

使用自定义 GPT 的 **Actions**，不是 MCP。将 [OpenAPI 文件](docs/chatgpt-action-openapi.yaml) 粘贴或导入 Actions 配置；认证选择 **API Key → Custom header**，Header 名填写 `X-Api-Key`，值使用 `chatgpt-action` Client 的 key。该 Action 可使用 `chatgpt-notify`、`gossip-notify` 与 `dingtalk-notify` 三个 Route。

`gossip-notify` 会将消息发送到只包含“八卦机器人”的 `gossip-group`。也可使用固定入口 `POST /hook/gossip`；它必须使用被授权的 Client Key。

`dingtalk-notify` 会直接投递到“钉钉机器人”。在 `.env` 中填入自定义机器人的 Webhook（含 `access_token`）和开启“加签”后获得的 `DINGTALK_ROBOT_SECRET`；服务会自动计算 `timestamp` 与 HMAC-SHA256 签名，不会记录 Secret 或带签名的 Webhook。

`chatgpt-notify` 默认组除企业微信机器人外，也会投递到飞书机器人。在 `.env` 中填写 `FEISHU_CHATGPT_WEBHOOK`；TEXT 消息会以飞书文本消息发送，MARKDOWN 消息会以飞书交互卡片发送。

每个 Bot 都可选配 `header`。配置后会原样前置到该 Bot 的 TEXT 或 MARKDOWN 内容，例如 `header: "【统一消息转发中心】"`；不配置或留空时，消息内容不作任何改变。

## 本地运行

使用指定 GraalVM JDK：

```bash
export JAVA_HOME=/Users/jincheng/Library/Java/JavaVirtualMachines/graalvm-jdk-21.0.5/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
mvn test
```

## Docker Compose

```bash
cp deploy/.env.example deploy/.env
mkdir -p deploy/config
cp deploy/config/application.yml.example deploy/config/application.yml
mvn package
docker compose up --build
```

编辑 `deploy/.env` 中的密钥和 Webhook，再编辑 `deploy/config/application.yml` 中的 Client、机器人、群组、路由和 Endpoint。两者都在部署时挂载，更新后重启容器即可；应用镜像无需重新构建。该模式只提供 `http://localhost:8080`，适合本地和受信任内网联调。

默认 Compose 使用 [Dockerfile.local](Dockerfile.local)，将本机已打包的 jar 放入 JDK 21 运行镜像，避免本地部署时在容器内重复下载 Maven 依赖。需要在容器内从源码独立构建时，可使用原始 [Dockerfile](Dockerfile)。

默认模板已包含“八卦机器人”：只需在 `.env` 填写 `GOSSIP_WECOM_WEBHOOK`，随后使用 `gossip-notify` 路由即可将内容发送到八卦群。

默认模板也包含“钉钉机器人”：填写 `DINGTALK_ROBOT_WEBHOOK` 与 `DINGTALK_ROBOT_SECRET` 后，ChatGPT Action 使用 `dingtalk-notify` 即可发送消息。

需要让 ChatGPT Actions 调用时，必须使用真实公网域名和 HTTPS。复制 Caddy 配置并运行：

```bash
cp deploy/Caddyfile.example deploy/Caddyfile
docker compose -f docker-compose.yml -f docker-compose.public.yml up --build -d
```

在 `deploy/.env` 填写可解析至部署机器的 `DOMAIN` 与可接收证书通知的 `EMAIL`。Caddy 负责 TLS，应用容器继续使用内部 HTTP。

不配置 HTTPS 时，应用功能仍可在本地或内网工作；但不能把 API Key 明文暴露在公网 HTTP 上，也无法供 ChatGPT 云端安全、可靠地调用。

## 文档需求覆盖

- Endpoint → Bot / Group / Route：`TargetResolver`
- Route → Bot / Group：`TargetResolver`
- ALL / FIRST_SUCCESS / PRIMARY_BACKUP：`DeliveryOrchestrator`
- TEXT / MARKDOWN 与 WECOM 协议适配：`WeComBotSender`
- Client Key、授权、限流、幂等、固定 Endpoint 防 SSRF：接入工作流
- ChatGPT 直接 POST：`docs/chatgpt-action-openapi.yaml`

当前 V1 依需求刻意不包含数据库配置后台、管理 UI、MQ、RBAC、长久投递记录和多实例共享限流/幂等状态。
