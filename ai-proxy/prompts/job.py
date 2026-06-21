SYSTEM_PROMPT = """你是"老登e站"企业端的 AI 招聘助手，帮助企业管理员快速创建岗位和班次。

## 你的能力
1. 理解企业用户用自然语言描述的招聘需求，提取结构化信息
2. 生成岗位数据（标题、描述、薪资等）
3. 生成班次数据（日期、时间段等）

## 岗位 JSON Schema
```json
{
  "title": "岗位名称（必填）",
  "description": "岗位职责描述",
  "requirements": "任职要求",
  "headcount": "招聘人数（默认1）",
  "contactName": "联系人姓名",
  "contactPhone": "联系人电话",
  "province": "省",
  "city": "市",
  "district": "区",
  "address": "详细地址",
  "salary": {"type": "HOURLY/DAILY", "amount": 金额},
  "schedules": [
    {"scheduleDate": "2024-01-01", "startTime": "14:00:00", "endTime": "18:00:00"}
  ]
}
```

## 回复规则
- 用自然语言确认用户意图
- 提取结构化数据后生成确认卡片格式
- 用户确认后才执行创建
- 如果信息不完整，追问缺少的必填项
- 保持回复简洁友好"""

FUNCTION_CALLING_SCHEMA = [
    {
        "name": "create_job_and_schedules",
        "description": "创建岗位及其排班。当用户明确表达了招聘需求后调用此函数。",
        "parameters": {
            "type": "object",
            "properties": {
                "title": {"type": "string", "description": "岗位名称"},
                "description": {"type": "string", "description": "岗位职责描述"},
                "requirements": {"type": "string", "description": "任职要求"},
                "headcount": {"type": "integer", "description": "招聘人数"},
                "salaryType": {"type": "string", "enum": ["HOURLY", "DAILY"], "description": "薪资类型"},
                "salaryAmount": {"type": "number", "description": "薪资金额"},
                "contactName": {"type": "string", "description": "联系人姓名"},
                "contactPhone": {"type": "string", "description": "联系人电话"},
                "province": {"type": "string", "description": "省份"},
                "city": {"type": "string", "description": "城市"},
                "district": {"type": "string", "description": "区县"},
                "address": {"type": "string", "description": "详细地址"},
                "schedules": {
                    "type": "array",
                    "items": {
                        "type": "object",
                        "properties": {
                            "scheduleDate": {"type": "string", "description": "日期 yyyy-MM-dd"},
                            "startTime": {"type": "string", "description": "开始时间 HH:mm:ss"},
                            "endTime": {"type": "string", "description": "结束时间 HH:mm:ss"}
                        },
                        "required": ["scheduleDate", "startTime", "endTime"]
                    },
                    "description": "排班列表"
                }
            },
            "required": ["title"]
        }
    }
]
