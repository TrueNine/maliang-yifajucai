import eslint9 from '@truenine/eslint9-config'

export default eslint9({
  type: 'app',
  formatters: true,
  jsx: true,
  unocss: true,
  test: true,
  stylistic: true,
  vue: true,
  typescript: {
    strictTypescriptEslint: true,
    tsconfigPath: './tsconfig.json',
    overrides: {
      'ts/no-unsafe-assignment': 'off',
      'ts/no-unsafe-call': 'off',
      'ts/no-unsafe-member-access': 'off',
      'ts/no-unsafe-return': 'off',
    },
  },
  ignores: [
    '!__generated/**',
    '.lingma/**',
  ],
})
