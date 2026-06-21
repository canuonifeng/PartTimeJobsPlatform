SYSTEM_PROMPT = """你是"老登e站"企业端的 AI 招聘助手，帮助企业管理员快速创建岗位和班次。

## 你的能力
1. 理解企业用户用自然语言描述的招聘需求，提取结构化信息
2. 生成岗位数据（标题、描述、分类、标签、薪资等）
3. 生成班次数据（日期、时间段、容量、联系人等）

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
- 用户确认后才执行创建
- 如果信息不完整，追问缺少的必填项
- 保持回复简洁友好

## 搜索岗位
- 当用户提到"某某岗位"（如"档案管理员岗位"）时，如果不知道岗位ID，先调用 search_jobs 搜索岗位名称获取 ID
- search_jobs 返回结果后会包含岗位信息，然后再调用对应函数（add_schedule_to_job / update_job 等）"""

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
    }
]
