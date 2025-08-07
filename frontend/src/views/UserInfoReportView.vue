<script setup lang="ts">
import type { dynamic, latenil, nil } from '@compose/types'
import type { ECharts, EChartsOption } from 'echarts'
import { init, registerMap } from 'echarts'
import { api } from '@/api'
import { ChinaProvince } from '@/common/map/Gen'

interface ChartValue {
  name: string
  value: number
}

interface ChartContext {
  title: string
  data: [ChartValue, ChartValue]
}

interface ProvinceData {
  name: string
  adCode: string
  value: number
}

// echarts配置
function getChartOptions(ctx: ChartContext, isDark: boolean): EChartsOption {
  return {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
      backgroundColor: isDark ? 'rgba(50,50,50,0.9)' : 'rgba(255,255,255,0.9)',
      borderColor: isDark ? '#555' : '#ddd',
      textStyle: {
        color: isDark ? '#fff' : '#333',
      },
    },
    legend: {
      top: '5%',
      left: 'center',
      textStyle: {
        fontSize: 12,
        color: isDark ? '#eee' : '#333',
      },
    },
    backgroundColor: 'transparent',
    series: [
      {
        name: ctx.title,
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '70%'],
        startAngle: 180,
        endAngle: 360,
        avoidLabelOverlap: false,
        padAngle: 1,
        itemStyle: {
          borderRadius: 6,
          borderColor: isDark ? '#1e1e1e' : '#ffffff',
          borderWidth: 2,
        },
        label: {
          show: true,
          position: 'outside',
          formatter: '{b}\n{d}%',
          fontSize: 12,
          color: isDark ? '#eee' : '#333',
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold',
          },
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)',
          },
        },
        data: ctx.data,
      },
    ],
  }
}

function getMapOptions(data: ProvinceData[], textColor: string, isDark: boolean): EChartsOption {
  const max = data.reduce((p, c) => p + c.value, 0)
  return {
    title: {
      textStyle: {
        color: textColor,
        fontSize: 16,
        fontWeight: 'normal',
      },
      subtextStyle: {
        color: textColor,
        fontSize: 12,
      },
      text: '系统内人员地域分布',
      subtext: '根据统计条件进行筛选',
      left: 'right',
      padding: [10, 20],
    },
    tooltip: {
      trigger: 'item',
      showDelay: 0,
      transitionDuration: 0.2,
      formatter: (params: any) => {
        const { name, value } = params
        const percentage = ((value / max) * 100).toFixed(1)
        return `${name}<br/>人数: ${value}<br/>占比: ${percentage}%`
      },
      backgroundColor: isDark ? 'rgba(50,50,50,0.9)' : 'rgba(255,255,255,0.9)',
      borderColor: isDark ? '#555' : '#ddd',
      textStyle: {
        color: isDark ? '#fff' : '#333',
      },
    },
    visualMap: {
      left: 'right',
      min: 0,
      max,
      inRange: {
        color: isDark
          ? [
              '#e8f4f8',
              '#d5e9f2',
              '#a7d5ed',
              '#7dc0e8',
              '#5ba9e1',
              '#3d93da',
              '#1f79e6',
            ]
          : [
              '#f0f9ff',
              '#bae6fd',
              '#7dd3fc',
              '#38bdf8',
              '#0ea5e9',
              '#0284c7',
              '#0153a5',
            ],
      },
      text: ['高', '低'],
      calculable: true,
      textStyle: {
        color: textColor,
      },
      padding: [10, 20],
    },
    series: [
      {
        name: '人员统计',
        type: 'map',
        map: 'CN',
        zoom: 1.2,
        roam: true,
        itemStyle: {
          areaColor: isDark ? '#e8f4f8' : '#f0f9ff',
          borderColor: isDark ? '#fff' : '#94a3b8',
          borderWidth: 1,
        },
        emphasis: {
          itemStyle: {
            areaColor: isDark ? '#1f79e6' : '#0284c7',
            shadowColor: 'rgba(0, 0, 0, 0.2)',
            shadowBlur: 20,
          },
          label: {
            show: true,
            color: '#fff',
          },
        },
        select: {
          itemStyle: {
            areaColor: isDark ? '#1f79e6' : '#0284c7',
          },
        },
        scaleLimit: {
          min: 1,
          max: 7,
        },
        data,
      },
    ],
  }
}

// 装饰图表配置
function getDecorativeChartOptions(isDark: boolean): EChartsOption {
  return {
    grid: {
      left: 0,
      right: 0,
      top: 0,
      bottom: 0,
    },
    xAxis: {
      type: 'value',
      show: false,
    },
    yAxis: {
      type: 'value',
      show: false,
    },
    series: [
      {
        type: 'graph',
        layout: 'force',
        animation: true,
        data: Array.from({ length: 20 }, () => ({
          value: Math.random() * 20,
          symbolSize: Math.random() * 10 + 5,
          x: Math.random() * 300,
          y: Math.random() * 100,
        })),
        itemStyle: {
          color: isDark ? 'rgba(64, 158, 255, 0.2)' : 'rgba(24, 144, 255, 0.2)',
          borderColor: isDark ? 'rgba(64, 158, 255, 0.6)' : 'rgba(24, 144, 255, 0.6)',
          borderWidth: 1,
        },
        force: {
          repulsion: 100,
          edgeLength: 50,
        },
        links: [],
        silent: true,
      },
    ],
  }
}

function getWaveChartOptions(isDark: boolean): EChartsOption {
  return {
    grid: {
      left: 0,
      right: 0,
      top: 0,
      bottom: 0,
    },
    xAxis: {
      type: 'value',
      show: false,
    },
    yAxis: {
      type: 'value',
      show: false,
    },
    series: [
      {
        type: 'line',
        smooth: true,
        symbol: 'none',
        data: Array.from({ length: 50 }, (_, i) => [
          i,
          Math.sin(i / 5) * 3 + Math.cos(i / 10) * 2,
        ]),
        lineStyle: {
          color: isDark ? 'rgba(64, 158, 255, 0.2)' : 'rgba(24, 144, 255, 0.2)',
          width: 1,
        },
        areaStyle: {
          color: isDark ? 'rgba(64, 158, 255, 0.1)' : 'rgba(24, 144, 255, 0.1)',
        },
        animation: false,
        silent: true,
      },
    ],
  }
}

// 状态管理
const map = shallowRef<ECharts>()
const d = useDark()
const textColor = computed(() => d.value ? 'white' : 'black')

// DOM引用
const mapRef = ref<nil<HTMLElement>>(null)
const accountRef = ref<nil<HTMLElement>>(null)
const idcardsRef = ref<nil<HTMLElement>>(null)
const disRef = ref<nil<HTMLElement>>(null)
const decorative1Ref = ref<nil<HTMLElement>>(null)
const decorative2Ref = ref<nil<HTMLElement>>(null)
const decorative3Ref = ref<nil<HTMLElement>>(null)
const decorative4Ref = ref<nil<HTMLElement>>(null)

// 图表初始化
function initChart(ctx: ChartContext, ele?: latenil<HTMLElement>) {
  if (!ele) {
    return
  }
  const chart = init(ele, d.value ? 'dark' : 'light')
  chart.setOption(getChartOptions(ctx, d.value))
  return chart
}

// 地图数据处理
function processProvinceData(countData: Record<string, number>): ProvinceData[] {
  return ChinaProvince.features
    .map((f) => ({
      name: f.properties.name,
      adCode: f.properties.adcode.toString().slice(0, 2),
      value: 0,
    }))
    .map((e) => ({ ...e, value: countData[e.adCode] || 0 }))
    .filter((e) => e.name && e.value)
}

// 渲染地图
function renderMapData(data: ProvinceData[]) {
  if (!map.value) {
    return
  }
  map.value.clear()
  map.value.setOption(getMapOptions(data, textColor.value, d.value))
}

// 数据获取
async function fetchAndRenderData() {
  try {
    const countData = await api.userInfoV2Api.getUserCountByProvinceAsAdmin({ spec: {} })
    const data = processProvinceData(countData)
    renderMapData(data)
  } catch (error) {
    console.error('获取地域分布数据失败:', error)
  }
}

// 生命周期
onMounted(async () => {
  // 注册地图
  registerMap('CN', ChinaProvince as dynamic)

  // 初始化饼图
  initChart(
    {
      title: '账号',
      data: [
        { name: '有账号', value: 133 },
        { name: '无账号', value: 133 },
      ],
    },
    accountRef.value,
  )

  initChart(
    {
      title: '身份证情况',
      data: [
        { name: '有身份证', value: 43 },
        { name: '无身份证', value: 43 },
      ],
    },
    idcardsRef.value,
  )

  initChart(
    {
      title: '残疾证号',
      data: [
        { name: '有', value: 43 },
        { name: '无', value: 43 },
      ],
    },
    disRef.value,
  )

  // 初始化地图
  if (mapRef.value) {
    map.value = init(mapRef.value)
    renderMapData([])
    await fetchAndRenderData()
  }

  // 初始化装饰图表
  const decorativeRefs = [decorative1Ref, decorative2Ref, decorative3Ref, decorative4Ref]
  decorativeRefs.forEach((ref, index) => {
    if (ref.value) {
      const chart = init(ref.value, d.value ? 'dark' : 'light')
      chart.setOption(index % 2 === 0
        ? getDecorativeChartOptions(d.value)
        : getWaveChartOptions(d.value),
      )
    }
  })
})

// 监听主题变化
watch(d, () => {
  // 重新渲染所有图表
  const charts = [accountRef, idcardsRef, disRef].map((ref) => ref.value).filter(Boolean)
  charts.forEach((ele) => {
    const chart = init(ele, d.value ? 'dark' : 'light')
    chart.setOption(chart.getOption())
  })

  // 重新渲染地图
  if (map.value) {
    map.value.setOption(map.value.getOption())
  }

  // 重新渲染装饰图表
  const decorativeRefs = [decorative1Ref, decorative2Ref, decorative3Ref, decorative4Ref]
  decorativeRefs.forEach((ref, index) => {
    if (ref.value) {
      const chart = init(ref.value, d.value ? 'dark' : 'light')
      chart.setOption(index % 2 === 0
        ? getDecorativeChartOptions(d.value)
        : getWaveChartOptions(d.value),
      )
    }
  })
})

// 自动调整大小
useEventListener(window, 'resize', () => {
  const charts = [map.value, ...([accountRef, idcardsRef, disRef]
    .map((ref) => ref.value)
    .filter(Boolean)
    .map((ele) => init(ele)))]

  charts.forEach((chart) => chart?.resize())
})
</script>

<template>
<VContainer fluid class="pa-4">
  <VCard elevation="2" class="mb-4 overflow-hidden rounded-lg">
    <div class="relative">
      <div ref="decorative1Ref" class="absolute left-0 top-0 h-32 w-32 opacity-50" />
      <div ref="decorative2Ref" class="absolute right-0 top-0 h-32 w-32 opacity-50" />
      <div ref="mapRef" class="min-h-[500px] w-full" />
      <div ref="decorative3Ref" class="absolute bottom-0 left-0 h-32 w-32 opacity-50" />
      <div ref="decorative4Ref" class="absolute bottom-0 right-0 h-32 w-32 opacity-50" />
    </div>
  </VCard>

  <VRow :dense="true">
    <VCol cols="12" md="4">
      <VCard elevation="2" class="overflow-hidden rounded-lg transition-shadow duration-300 hover:shadow-lg">
        <div class="relative">
          <VCardTitle class="text-h6 pa-4">
            账号情况统计
          </VCardTitle>
          <div ref="accountRef" class="min-h-[300px]" />
          <div class="absolute right-0 top-0 h-24 w-24 opacity-30">
            <VIcon icon="mdi-account-check" size="x-large" class="text-primary" />
          </div>
        </div>
      </VCard>
    </VCol>

    <VCol cols="12" md="4">
      <VCard elevation="2" class="overflow-hidden rounded-lg transition-shadow duration-300 hover:shadow-lg">
        <div class="relative">
          <VCardTitle class="text-h6 pa-4">
            残疾证持有统计
          </VCardTitle>
          <div ref="disRef" class="min-h-[300px]" />
          <div class="absolute right-0 top-0 h-24 w-24 opacity-30">
            <VIcon icon="mdi-card-account-details" size="x-large" class="text-primary" />
          </div>
        </div>
      </VCard>
    </VCol>

    <VCol cols="12" md="4">
      <VCard elevation="2" class="overflow-hidden rounded-lg transition-shadow duration-300 hover:shadow-lg">
        <div class="relative">
          <VCardTitle class="text-h6 pa-4">
            身份证持有统计
          </VCardTitle>
          <div ref="idcardsRef" class="min-h-[300px]" />
          <div class="absolute right-0 top-0 h-24 w-24 opacity-30">
            <VIcon icon="mdi-card-bulleted" size="x-large" class="text-primary" />
          </div>
        </div>
      </VCard>
    </VCol>
  </VRow>
</VContainer>
</template>

<style lang="scss" scoped>
.relative {
  position: relative;
}

.absolute {
  position: absolute;
}
</style>
