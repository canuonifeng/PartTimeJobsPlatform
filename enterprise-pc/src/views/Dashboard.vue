<script setup>
</script>

<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">0</div>
            <div class="stat-label">进行中的职位</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">0</div>
            <div class="stat-label">待处理应聘</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">0</div>
            <div class="stat-label">本周排班</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">0</div>
            <div class="stat-label">待结算薪资</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="welcome-card">
      <h2>欢迎使用企业管理后台</h2>
      <p>管理您的兼职职位、应聘者、排班和薪资。</p>
    </el-card>
  </div>
</template>

<style scoped>
.dashboard {
  padding: 20px;
}
.stat-item {
  text-align: center;
  padding: 20px 0;
}
.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}
.welcome-card {
  margin-top: 20px;
}
.welcome-card h2 {
  margin: 0 0 8px;
  color: #303133;
}
.welcome-card p {
  margin: 0;
  color: #606266;
}
</style>
