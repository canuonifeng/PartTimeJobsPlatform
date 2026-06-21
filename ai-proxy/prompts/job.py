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
  "latitude": "纬度（可选，如 30.275）",
  "longitude": "经度（可选，如 119.992）",
  "salary": {"type": "HOURLY/DAILY", "amount": 金额},
  "schedules": [
    {"scheduleDate": "2024-01-01", "startTime": "14:00:00", "endTime": "18:00:00"}
  ]
}
```

## 时间日期处理
- scheduleDate 必须使用真实的当前日期，不要编造或使用占位日期
- 根据用户说的时间词（明天、后天、下周一等）推算真实日期

## 定位信息
- 如果用户提供了地图上的定位或具体地址，提取 latitude/longitude
- 如果用户只说了大致地址而没有具体坐标，可以不填经纬度
- 定位可以让工人更准确地找到工作地点

## 同名岗位处理
- 如果用户要创建的岗位名称与已有岗位重复，系统会自动检测并提示用户选择
- 用户选择后，请根据用户的选择调用对应的函数：
  - 用户选择"修改已有岗位" → 调用 update_job 函数，传入原岗位ID和要修改的字段
  - 用户选择"新增班次" → 调用 add_schedule_to_job 函数，传入原岗位ID和新班次
  - 用户选择"重新创建" → 调用 create_job_and_schedules 函数创建新岗位

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
                "latitude": {"type": "number", "description": "纬度（可选，如 30.275）"},
                "longitude": {"type": "number", "description": "经度（可选，如 119.992）"},
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
    },
    {
        "name": "update_job",
        "description": "修改已有岗位的信息。当用户要求修改已有岗位时调用此函数。",
        "parameters": {
            "type": "object",
            "properties": {
                "jobId": {"type": "integer", "description": "要修改的岗位ID"},
                "title": {"type": "string", "description": "岗位名称"},
                "description": {"type": "string", "description": "岗位职责描述"},
                "requirements": {"type": "string", "description": "任职要求"},
                "headcount": {"type": "integer", "description": "招聘人数"},
                "salaryType": {"type": "string", "enum": ["HOURLY", "DAILY"], "description": "薪资类型"},
                "salaryAmount": {"type": "number", "description": "薪资金额"},
                "latitude": {"type": "number", "description": "纬度（可选，如 30.275）"},
                "longitude": {"type": "number", "description": "经度（可选，如 119.992）"}
            },
            "required": ["jobId"]
        }
    },
    {
        "name": "add_schedule_to_job",
        "description": "为已有岗位新增班次。当用户要求新增班次到已有岗位时调用此函数。",
        "parameters": {
            "type": "object",
            "properties": {
                "jobId": {"type": "integer", "description": "岗位ID"},
                "scheduleDate": {"type": "string", "description": "日期 yyyy-MM-dd"},
                "startTime": {"type": "string", "description": "开始时间 HH:mm:ss"},
                "endTime": {"type": "string", "description": "结束时间 HH:mm:ss"}
            },
            "required": ["jobId", "scheduleDate", "startTime", "endTime"]
        }
    }
]
