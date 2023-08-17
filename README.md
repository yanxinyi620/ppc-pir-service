# ppc-pir-service

* ppc pir service

## 项目结构

ppc-pir-service
  http
  crypto

## 项目接口

```python
PirController
    # 前端/请求方传入任务请求
    http: ClientJobRequest (http: BaseRequest, pir: ClientDataBody)
    # 请求方返回code，message，data响应（若数据方报错，直接返回错误响应）
    http: ClientJobResponse
    # 请求方在ClientJobResponse上包一层result/error结构
    http: ClientPirfailResponse (http: ClientJobResponse)
    http: ClientPirResponse (http: ClientJobResponse)
    # # 请求方返回相应(前面已描述)
    # http: ClientJobResponse
    # http: ClientPirfailResponse (http: ClientJobResponse)
    # http: ClientPirResponse (http: ClientJobResponse)
    # 请求方构造数据方接口请求
    http: ServerJobRequest (http: BaseRequest, pir: ServerDataBody)
    # 请求方解析数据方接口响应
    http: SimpleEntity (http: BaseResponse)

    # 数据方传入请求
    http: ServerJobRequest (http: BaseRequest, pir: ServerDataBody)
    # 数据方返回code，message，data响应
    http: ClientJobResponse
    # 数据方patch请求
    http: JobRequest
    # 数据方patch响应
    http: JobEntity

    # 请求方调用pir，根据searchId计算OT参数，根据披露得到披露id
    pir: ClientOTRequest (pir: ClientDataBody)
    # 请求方返回OT参数结果
    pir: ClientOTResponse (pir: ServerDataBody)
    # 数据方调用pir，计算OT参数请求
    pir: ServerOTRequest (pir: ServerDataBody)
    # 数据方调用pir，计算OT参数响应
    pir: ServerOTResponse (pir: ServerResultlist (pir: ServerResultBody))
    # 请求方调用pir，构造解密OT请求
    pir: ClientDecryptRequest (pir: ClientDataBody, pir: ServerOTResponse (pir: ServerResultlist (pir: ServerResultBody)))
    # 请求方解析pir解密后的OT结果
    pir: ClientDecryptResponse (pir: PirResultBody)
    # 请求方将解密后的结果加上jobId组成新的结果
    http: PirResultResponse (pir: ClientDecryptResponse (pir: PirResultBody))
```
