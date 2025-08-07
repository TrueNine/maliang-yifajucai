import type { i32, nil } from '@compose/types'
import type { CertPointTyping } from '@/__generated/model/enums'

export interface MultipleData {
  name?: string
  doc?: string
  type?: i32
  contentType?: i32
  pointType?: i32
  file?: nil<File>
}

export const defaultData: MultipleData = {
  name: '',
  doc: '',
  file: null,
}

export const certs = [
  { k: '身份证', v: 'ID_CARD2' },
  { k: '残疾证2', v: 'DISABILITY_CARD2' },
  { k: '残疾证3', v: 'DISABILITY_CARD3' },
  { k: '银行卡', v: 'BANK_CARD' },
  { k: '户口', v: 'HOUSEHOLD_CARD' },
  { k: '寸照', v: 'TITLE_IMAGE' },
]

export const certPointMaps: Record<string, { k: string, v: CertPointTyping }[]> = {
  ID_CARD2: [
    { k: '双面', v: 'DOUBLE' },
    { k: '正面', v: 'HEADS' },
    { k: '反面', v: 'TAILS' },
  ],
  DISABILITY_CARD2: [
    { k: '双面', v: 'DOUBLE' },
    { k: '前页', v: 'HEADS' },
    { k: '后页', v: 'TAILS' },
  ],
  DISABILITY_CARD3: [
    { k: '双面', v: 'DOUBLE' },
    { k: '正面', v: 'HEADS' },
    { k: '反面', v: 'TAILS' },
  ],
  BANK_CARD: [
    { k: '双面', v: 'DOUBLE' },
    { k: '正面', v: 'HEADS' },
    { k: '反面', v: 'TAILS' },
  ],
  HOUSEHOLD_CARD: [
    { k: '双页', v: 'DOUBLE' },
    { k: '自身', v: 'HEADS' },
    { k: '户主', v: 'TAILS' },
  ],
  TITLE_IMAGE: [{ k: '正面', v: 'HEADS' }],
}

export const certContents = [
  { k: '扫描', v: 'SCANNED_IMAGE' },
  { k: '拍照', v: 'REMAKE_IMAGE' },
]
