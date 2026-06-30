# 013 Spring AI 2.0.0 PgVector RAG Demo

对应文章：

`Spring AI 2.0.0 RAG：把 SimpleVectorStore 换成 PgVector`

## 运行环境

- JDK 17
- Spring Boot 4.1.0
- Spring AI 2.0.0
- Chat Model：DeepSeek `deepseek-v4-flash`
- Embedding Model：Ollama `bge-m3`
- Vector Store：PostgreSQL + pgvector

## 启动依赖

启动 PgVector：

```bash
docker run -it --rm --name postgres \
  -p 5432:5432 \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  pgvector/pgvector:pg16
```

准备本地 embedding 模型：

```bash
ollama pull bge-m3
ollama serve
```

如果只测试 `/rag/ingest` 和 `/rag/search`，不需要真实 DeepSeek Key。

如果要测试 `/rag/ask`，先配置：

```bash
export DEEPSEEK_API_KEY=你的 DeepSeek API Key
```

## 启动应用

```bash
./mvnw spring-boot:run
```

如果本机已经有 PostgreSQL，也可以用环境变量覆盖连接信息：

```bash
export POSTGRES_URL=jdbc:postgresql://localhost:5432/postgres
export POSTGRES_USER=postgres
export POSTGRES_PASSWORD=postgres
```

## 读取 txt 并写入向量库

Demo 会读取：

```text
src/main/resources/rag/incident-runbook.txt
```

每段文本用 `---` 分隔，段首是 metadata，空一行后是正文。

```bash
curl -X POST "http://localhost:8080/rag/ingest"
```

## 检索相似资料

```bash
curl --get "http://localhost:8080/rag/search" \
  --data-urlencode "question=支付接口偶发500应该先看什么"
```

预期能命中支付接口 500 的排障资料：

```json
{
  "question": "支付接口偶发500应该先看什么",
  "matches": [
    {
      "id": "9ee4017f-b2aa-35c3-a794-33c7fa6ae6a6",
      "text": "支付接口偶发 500，先拿 traceId 查应用日志，不要一上来就让用户重新支付。",
      "score": 0.7104,
      "metadata": {
        "system": "incident",
        "category": "payment",
        "logicalId": "INCIDENT-001",
        "status": "published",
        "distance": 0.2895
      }
    }
  ]
}
```

这里的结果来自 PgVector 相似度检索，不是 DeepSeek 直接生成。

这里的 `id` 是写入 PgVector 的稳定 UUID，方便数据库存储和更新；人读起来更直观的业务编号放在 `metadata.logicalId` 里。

## 检索后生成回答

```bash
curl --get "http://localhost:8080/rag/ask" \
  --data-urlencode "question=支付接口偶发500应该先看什么"
```

这个接口会先从 PgVector 检索资料，再把资料交给 DeepSeek `deepseek-v4-flash` 生成回答。
