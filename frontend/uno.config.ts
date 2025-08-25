import type { StaticRule } from 'unocss'
import { presetVarlet } from '@varlet/preset-unocss'
import { defineConfig, presetIcons, presetMini, presetWind3, presetWind4, transformerCompileClass, transformerDirectives } from 'unocss'
import { StyleConfig } from './src/config'

const breakpoints = {
  xs: '600px',
  sm: '960px',
  md: '1280px',
  lg: '1920px',
  xl: '2560px',
  xxl: '2560px',
}

interface Theme {
  primaryColor?: string
  secondaryColor?: string
  warningColor?: string
  errorColor?: string
}

function unoRules(cfg: Theme): StaticRule[] {
  return [
    ['c-p', { color: cfg.primaryColor ?? `var(--primary-color)` }],
    ['bg-p', { 'background-color': cfg.primaryColor ?? `var(--primary-color)` }],
    ['c-s', { color: cfg.secondaryColor ?? `var(--secondary-color)` }],
    ['bg-s', { 'background-color': cfg.secondaryColor ?? `var(--secondary-color)` }],
    ['c-w', { color: cfg.warningColor ?? `var(--warning-color)` }],
    ['bg-w', { 'background-color': cfg.warningColor ?? `var(--warning-color)` }],
    ['w-fit', { width: 'fit-content' }],
    ['h-fit', { height: 'fit-content' }],
    ['c-e', { color: cfg.errorColor ?? `var(--error-color)` }],
    ['bg-e', { 'background-color': cfg.errorColor ?? `var(--error-color)` }],
  ]
}

export default defineConfig({
  rules: unoRules(StyleConfig as Theme),
  presets: [
    presetVarlet(),
    presetIcons(),
    presetMini(),
    presetWind3(),
    presetWind4(),
  ],
  theme: {
    breakpoints,
  },
  transformers: [
    transformerDirectives(),
    transformerCompileClass(),
  ],
  shortcuts: [
    ['wh-full', 'w-full h-full'],
    ['wh-0', 'w-0 h-0'],
    ['f-c', 'flex justify-center items-center'],
    ['f-x-c', 'flex justify-center'],
    ['f-y-c', 'flex items-center'],
    ['flex-col', 'flex flex-col'],
    ['text-ellipsis', 'truncate'],
    ['text-ell', 'truncate'],
    ['bg-m', 'bg-gray-500 bg-opacity-5'],
    ['bg-m-h', 'hover:bg-gray-500 hover:bg-opacity-5 duration-100'],
    ['bg-m-h-s', 'hover:shadow-md bg-m-h'],
  ],
})
