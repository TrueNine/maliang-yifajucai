/// <reference types="vue/jsx" />
/// <reference types="unplugin-vue-router/client" />

import type { dynamic } from '@compose/types'
import type { AnyObject, Flags, Maybe, Schema } from 'yup'
import type { z } from 'zod'

import type { input, output, RawCreateParams, ZodTypeAny } from 'zod/lib/types'

declare module 'yup' {
  interface StringSchema<TType extends Maybe<string> = string | undefined, TContext = AnyObject, TDefault = undefined, TFlags extends Flags = ''> extends Schema<TType, TContext, TDefault, TFlags> {
    phone: (message?: string) => StringSchema<TType, TContext, TDefault, TFlags>
    idCard2: (message?: string) => StringSchema<TType, TContext, TDefault, TFlags>
    disCard2: (message?: string) => StringSchema<TType, TContext, TDefault, TFlags>
  }
  type IsFileZodType<T> = T extends z.ZodType<File> ? true : false

  interface ArraySchema<TIn extends (File | undefined)[] | null | undefined, TContext, TDefault = undefined, TFlags extends Flags = ''> extends Schema<TIn, TContext, TDefault, TFlags> {
    fileNotRepeat: (message?: string) => ArraySchema<TIn, TContext, TDefault, TFlags>
  }
}

declare module 'core-js/modules/*' {
  const value: dynamic
  export default value
}

declare module 'zod' {
  interface ZodString {
    idCard2Code: (this: z.ZodString, param?: RawCreateParams) => z.ZodEffects<z.ZodString>
    disCardCode3: (this: z.ZodString, param?: RawCreateParams) => z.ZodEffects<z.ZodString>
    phone: (this: z.ZodString, param?: RawCreateParams) => z.ZodString
    id: (this: z.ZodString, param?: RawCreateParams) => z.ZodString
  }
  interface ZodArray<T extends ZodTypeAny, Output = output<T>, Input = input<T>> {
    fileNotRepeat: (this: z.ZodArray<T>) => z.ZodEffects<z.ZodArray<T>, Output[], Input[]>
  }
}

declare module '@compose/types' {
  export type { dynamic, i32, late, nil, Pq, Pr, RefId, timestamp } from '@compose/types'
}

interface ImportMetaEnv {
  readonly [key: string]: string
  readonly VITE_APP_BASE_URL: string
  readonly VITE_APP_BASE_DOMAIN: string
  readonly VITE_APP_PROD: string
  readonly VITE_APP_NAME: string
  readonly VITE_APP_ENV_NAME: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
  readonly hot?: {
    accept: () => void
    dispose: (cb: (data: unknown) => void) => void
    invalidate: () => void
  }
}
