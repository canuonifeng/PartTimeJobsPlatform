SYSTEM_PROMPT = """你是"老登e站"企业端的 AI 招聘助手，帮助企业管理招聘全流程。

## 你的能力
1. 创建和修改岗位、班次
2. 查询岗位列表、详情、班次信息
3. 处理报名审核（通过/拒绝）
4. 管理考勤结算（查看/修改工时、结算、撤回）
5. 管理岗位状态（关闭/重新开放）和班次状态（取消）

## 岗位 JSON Schema
```json
{
  "title": "岗位名称（必填）",
  "description": "岗位职责描述",
  "requirements": "任职要求",
  "headcount": "招聘人数（默认1）",
  "categoryId": "职位分类ID（如 1=保安, 2=保洁, 3=服务员）",
  "deadline": "报名截止日期 yyyy-MM-dd",
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
    {
      "scheduleDate": "2024-01-01",
      "startTime": "14:00",
      "endTime": "18:00",
      "scheduleName": "班次名称（可选）",
      "slotsAvailable": "该班次可报名人数（可选）",
      "contactName": "班次联系人（可选）",
      "contactPhone": "班次联系电话（可选）"
    }
  ]
}
```

## 时间日期处理
- scheduleDate 必须使用真实的当前日期，不要编造或使用占位日期
- 根据用户说的时间词（明天、后天、下周一等）推算真实日期

## 地址处理
- 用户可能用自然语言描述地址，如"兰溪市兰江街道兰荫路1号"
- 如果省市区不全，只需提取你确定的部分填入对应的字段（province/city/district/address）
- 系统会自动通过高德地图补全省市区信息并查询经纬度
- 如果你无法判断某个字段（例如不知道兰溪市属于哪个省），把已知部分填入 address 字段即可

## 同名岗位处理
- 如果用户要创建的岗位名称与已有岗位重复，系统会自动检测并提示用户选择
- 用户选择后，请根据用户的选择调用对应的函数：
  - 用户选择"修改已有岗位" → 调用 update_job 函数，传入原岗位ID和要修改的字段
  - 用户选择"新增班次" → 调用 add_schedule_to_job 函数，传入原岗位ID和新班次
  - 用户选择"重新创建" → 调用 create_job_and_schedules 函数创建新岗位

## 回复规则
- 用自然语言确认用户意图，提取的信息用自然语言逐项列出
- **绝对不要输出任何 JSON 代码块、Markdown 代码块或原始数据结构给用户**
- 用户确认后才执行创建/修改/删除/结算等操作
- 如果信息不完整，追问缺少的必填项
- 保持回复简洁友好

## 搜索岗位
- 当用户提到"某某岗位"（如"档案管理员岗位"）时，如果不知道岗位ID，先调用 search_jobs 搜索岗位名称获取 ID
- search_jobs 返回结果后会包含岗位信息，然后再调用对应函数（add_schedule_to_job / update_job 等）

## 数据查询
- 用户问"看看/查一下/统计/有哪些"等查询性质的问题，调用 query_data 函数
- query_data 会根据 type 自动查询后端数据并返回
- 查询结果会直接返回给你，用自然语言总结给用户

## 执行操作
- 用户要求"通过/拒绝/关闭/开放/结算/撤回"等操作，调用 execute_action 或 batch_action
- execute_action 用于单条操作，batch_action 用于批量操作
- 操作需要用户确认后再执行
- 操作前如果不知道目标ID，先用 search_jobs 或 query_data 查询"""

FUNCTION_CALLING_SCHEMA = [
    {
        "name": "search_jobs",
        "description": "根据岗位名称搜索已有的岗位，获取岗位ID等信息。当用户提到已有岗位名称时调用此函数。",
        "parameters": {
            "type": "object",
            "properties": {
                "keyword": {"type": "string", "description": "岗位名称关键词"}
            },
            "required": ["keyword"]
        }
    },
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
                "categoryId": {"type": "integer", "description": "职位分类ID（如 1=保安, 2=保洁, 3=服务员）"},
                "deadline": {"type": "string", "description": "报名截止日期 yyyy-MM-dd"},
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
                            "startTime": {"type": "string", "description": "开始时间 HH:mm"},
                            "endTime": {"type": "string", "description": "结束时间 HH:mm"},
                            "scheduleName": {"type": "string", "description": "班次名称（可选）"},
                            "slotsAvailable": {"type": "integer", "description": "该班次可报名人数（可选）"},
                            "contactName": {"type": "string", "description": "班次联系人（可选）"},
                            "contactPhone": {"type": "string", "description": "班次联系电话（可选）"}
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
                "categoryId": {"type": "integer", "description": "职位分类ID"},
                "deadline": {"type": "string", "description": "报名截止日期 yyyy-MM-dd"},
                "salaryType": {"type": "string", "enum": ["HOURLY", "DAILY"], "description": "薪资类型"},
                "salaryAmount": {"type": "number", "description": "薪资金额"},
                "contactName": {"type": "string", "description": "联系人姓名"},
                "contactPhone": {"type": "string", "description": "联系人电话"},
                "province": {"type": "string", "description": "省份"},
                "city": {"type": "string", "description": "城市"},
                "district": {"type": "string", "description": "区县"},
                "address": {"type": "string", "description": "详细地址"},
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
                "startTime": {"type": "string", "description": "开始时间 HH:mm"},
                "endTime": {"type": "string", "description": "结束时间 HH:mm"},
                "scheduleName": {"type": "string", "description": "班次名称（可选）"},
                "slotsAvailable": {"type": "integer", "description": "可报名人数（可选）"},
                "contactName": {"type": "string", "description": "联系人姓名（可选）"},
                "contactPhone": {"type": "string", "description": "联系人电话（可选）"}
            },
            "required": ["jobId", "scheduleDate", "startTime", "endTime"]
        }
    },
    {
        "name": "update_schedule",
        "description": "修改已有班次的信息。当用户要求修改某个班次时调用此函数。",
        "parameters": {
            "type": "object",
            "properties": {
                "scheduleId": {"type": "integer", "description": "要修改的班次ID"},
                "scheduleName": {"type": "string", "description": "班次名称"},
                "scheduleDate": {"type": "string", "description": "日期 yyyy-MM-dd"},
                "startTime": {"type": "string", "description": "开始时间 HH:mm"},
                "endTime": {"type": "string", "description": "结束时间 HH:mm"},
                "slotsAvailable": {"type": "integer", "description": "可报名人数"},
                "contactName": {"type": "string", "description": "联系人姓名"},
                "contactPhone": {"type": "string", "description": "联系人电话"},
                "status": {"type": "string", "enum": ["ACTIVE", "CANCELLED"], "description": "班次状态"}
            },
            "required": ["scheduleId"]
        }
    },
    {
        "name": "copy_schedule",
        "description": "复制已有班次到新日期或新时段。当用户要求复制班次时调用此函数。",
        "parameters": {
            "type": "object",
            "properties": {
                "sourceScheduleId": {"type": "integer", "description": "源班次ID"},
                "scheduleDate": {"type": "string", "description": "新日期 yyyy-MM-dd（可选）"},
                "startTime": {"type": "string", "description": "新开始时间 HH:mm（可选）"},
                "endTime": {"type": "string", "description": "新结束时间 HH:mm（可选）"}
            },
            "required": ["sourceScheduleId"]
        }
    },
    {
        "name": "batch_create_schedules",
        "description": "为已有岗位按日期范围和星期几批量创建班次。当用户要求批量创建班次时调用。",
        "parameters": {
            "type": "object",
            "properties": {
                "jobId": {"type": "integer", "description": "岗位ID"},
                "startDate": {"type": "string", "description": "开始日期 yyyy-MM-dd"},
                "endDate": {"type": "string", "description": "结束日期 yyyy-MM-dd"},
                "weekdays": {
                    "type": "array",
                    "items": {"type": "integer"},
                    "description": "周几，1=周一至7=周日，如[1,2,3,4,5]表示工作日"
                },
                "startTime": {"type": "string", "description": "开始时间 HH:mm"},
                "endTime": {"type": "string", "description": "结束时间 HH:mm"}
            },
            "required": ["jobId", "startDate", "endDate", "startTime", "endTime"]
        }
    },
    {
        "name": "query_data",
        "description": "查询岗位、班次、报名、考勤等数据。当用户问'看看/查一下/统计/有哪些'时调用。查询结果直接返回给你总结。",
        "parameters": {
            "type": "object",
            "properties": {
                "type": {
                    "type": "string",
                    "enum": ["jobs", "job_detail", "schedules", "applications", "attendance"],
                    "description": "查询类型：jobs=岗位列表, job_detail=岗位详情(需jobId), schedules=班次列表(需jobId), applications=报名列表(需jobTitle或jobId), attendance=考勤记录"
                },
                "jobId": {"type": "integer", "description": "岗位ID（查询岗位详情/班次/报名时使用）"},
                "jobTitle": {"type": "string", "description": "岗位名称关键词（查询报名/考勤时使用）"},
                "scheduleId": {"type": "integer", "description": "班次ID"},
                "status": {"type": "string", "description": "筛选状态：PENDING/ACCEPTED/REJECTED/ACTIVE/CANCELLED"},
                "settlementStatus": {"type": "string", "description": "结算状态：UNPAID/PAYING/PAID"},
                "workerName": {"type": "string", "description": "工人姓名"},
                "dateFrom": {"type": "string", "description": "开始日期 yyyy-MM-dd"},
                "dateTo": {"type": "string", "description": "结束日期 yyyy-MM-dd"}
            },
            "required": ["type"]
        }
    },
    {
        "name": "execute_action",
        "description": "执行单条操作：通过/拒绝报名、关闭/重新开放岗位、取消班次、修改工时、结算/撤回结算。操作需要用户确认。",
        "parameters": {
            "type": "object",
            "properties": {
                "action": {
                    "type": "string",
                    "enum": [
                        "accept_application", "reject_application",
                        "close_job", "reopen_job",
                        "cancel_schedule",
                        "update_attendance_hours",
                        "pay_attendance", "unsettle_attendance"
                    ],
                    "description": "操作类型"
                },
                "targetId": {"type": "integer", "description": "操作目标ID（applicationId/jobId/scheduleId/attendanceId）"},
                "reason": {"type": "string", "description": "拒绝原因等补充说明"},
                "updates": {
                    "type": "object",
                    "description": "修改字段，如 {\"totalHours\": 8}",
                    "properties": {
                        "totalHours": {"type": "number"},
                        "scheduledPay": {"type": "number"},
                        "payablePay": {"type": "number"},
                        "salaryType": {"type": "string"},
                        "salaryAmount": {"type": "number"}
                    }
                }
            },
            "required": ["action", "targetId"]
        }
    },
    {
        "name": "batch_action",
        "description": "批量操作：批量通过报名、批量结算工资。操作需要用户确认。",
        "parameters": {
            "type": "object",
            "properties": {
                "action": {
                    "type": "string",
                    "enum": ["batch_accept", "batch_pay"],
                    "description": "批量操作类型"
                },
                "targetIds": {
                    "type": "array",
                    "items": {"type": "integer"},
                    "description": "操作目标ID列表（如不传则按filters筛选）"
                },
                "filters": {
                    "type": "object",
                    "description": "筛选条件：如 {\"jobId\": 1, \"settlementStatus\": \"UNPAID\"}",
                    "properties": {
                        "jobId": {"type": "integer"},
                        "jobTitle": {"type": "string"},
                        "settlementStatus": {"type": "string"}
                    }
                }
            },
            "required": ["action"]
        }
    }
]
