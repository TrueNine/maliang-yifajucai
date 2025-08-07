import crypto from 'node:crypto'
import fs from 'node:fs'
import http from 'node:http'
import https from 'node:https'
import path from 'node:path'
import process from 'node:process'
import AdmZip from 'adm-zip'
import chalk from 'chalk'
import enquirer from 'enquirer'
import tempDir from 'temp-dir'

const { prompt } = enquirer
const PROJECT_ROOT = process.cwd()

// 默认配置
const DEFAULT_SOURCE_URI = 'ts.zip'
const DEFAULT_GENERATE_PATH = 'src/__generated'

/**
 * 状态更新函数 - 在同一行更新状态信息
 */
function updateStatus(status, success = null) {
  const statusText = success === null
    ? chalk.blue(status)
    : success
      ? chalk.green(`✓ ${status}`)
      : chalk.red(`× ${status}`)

  process.stdout.write(`\r${' '.repeat(100)}\r`)
  process.stdout.write(`${statusText}\n`)
}

/**
 * 清除当前行并移动光标到行首
 */
function clearLine() {
  process.stdout.write(`\r${' '.repeat(100)}\r`)
}

/**
 * 处理用户输入的远端路径
 * @param {string} host 用户输入的主机地址
 * @param {string} uri 用户输入的URI
 * @returns {string} 完整的URL
 */
function normalizeSourceUrl(host, uri) {
  // 移除开头的斜杠
  const cleanUri = uri.replace(/^\//, '')
  // 移除结尾的斜杠
  const cleanHost = host.replace(/\/$/, '')
  // 判断是否包含协议
  const hasProtocol = /^https?:\/\//.test(cleanHost)
  const baseUrl = hasProtocol ? cleanHost : `http://${cleanHost}`
  return `${baseUrl}/${cleanUri}`
}

/**
 * 处理用户输入的路径
 * @param {string} inputPath 用户输入的路径
 * @returns {string} 处理后的绝对路径
 */
function normalizeGeneratePath(inputPath) {
  // 移除开头的 ./ 或 /
  const cleanPath = inputPath.replace(/^\.\/|^\//, '')
  return path.resolve(PROJECT_ROOT, cleanPath)
}

/**
 * 下载文件
 */
async function downloadFile(url, tmpFilePath) {
  return new Promise((resolve, reject) => {
    const protocol = url.startsWith('https') ? https : http
    const tmpFile = fs.createWriteStream(tmpFilePath)

    const request = protocol.get(url, {
      // 允许自动重定向
      followRedirects: true,
      // HTTPS选项
      ...(url.startsWith('https') && {
        rejectUnauthorized: false,
        timeout: 10000,
      }),
    }, (response) => {
      // 处理重定向
      if (response.statusCode === 301 || response.statusCode === 302) {
        const redirectUrl = response.headers.location
        if (!redirectUrl) {
          reject(new Error('重定向地址无效'))
          return
        }
        // 关闭当前请求和文件
        request.destroy()
        tmpFile.close()

        // 重新下载重定向后的地址
        downloadFile(redirectUrl, tmpFilePath)
          .then(resolve)
          .catch(reject)
        return
      }

      if (response.statusCode !== 200) {
        reject(new Error(`服务器返回状态码: ${response.statusCode}`))
        return
      }

      response.pipe(tmpFile)
      tmpFile.on('finish', () => {
        tmpFile.close()
        resolve()
      })
    })

    request.on('error', (err) => {
      fs.unlink(tmpFilePath, () => {
        if (err.code === 'ECONNREFUSED') {
          reject(new Error('连接被拒绝，请检查服务器地址是否正确'))
        } else if (err.code === 'ETIMEDOUT') {
          reject(new Error('连接超时，请检查网络状态'))
        } else {
          reject(new Error(`下载失败: ${err.message}`))
        }
      })
    })

    request.on('timeout', () => {
      request.destroy()
      fs.unlink(tmpFilePath, () => {
        reject(new Error('请求超时'))
      })
    })
  })
}

/**
 * 测试URL连接是否可用
 * @param {string} url 要测试的URL
 * @returns {Promise<boolean>} 连接是否成功
 */
async function testConnection(url) {
  return new Promise((resolve) => {
    const protocol = url.startsWith('https') ? https : http
    const request = protocol.get(url, {
      timeout: 5000,
      ...(url.startsWith('https') && {
        rejectUnauthorized: false,
      }),
    }, () => {
      // 只要有响应就认为是成功的
      request.destroy()
      resolve(true)
    })

    request.on('error', (err) => {
      // 只有在网络错误（比如无法连接）时才返回false
      if (err.code === 'ECONNREFUSED' || err.code === 'ENOTFOUND' || err.code === 'ETIMEDOUT') {
        resolve(false)
      } else {
        resolve(true)
      }
    })

    request.on('timeout', () => {
      request.destroy()
      resolve(false)
    })
  })
}

async function main() {
  try {
    // 获取用户输入
    const answers = await prompt([
      {
        type: 'input',
        name: 'sourceHost',
        message: '请输入API服务器地址(例如: localhost:8080 或 https://api.example.com):',
        initial: 'localhost:8080',
      },
    ])

    const { sourceHost } = answers

    // 测试主机连接
    const baseUrl = normalizeSourceUrl(sourceHost, '')
    process.stdout.write(`正在测试服务器连接 ${baseUrl} `)
    const isHostConnected = await testConnection(baseUrl)
    clearLine()
    if (!isHostConnected) {
      throw new Error(`无法连接到服务器，请检查地址是否正确或服务器是否在线`)
    }

    // 获取URI输入
    const { sourceUri } = await prompt([
      {
        type: 'input',
        name: 'sourceUri',
        message: '请输入API定义文件的URI:',
        initial: DEFAULT_SOURCE_URI,
      },
    ])

    // 测试完整URL连接
    const sourceUrl = normalizeSourceUrl(sourceHost, sourceUri)
    process.stdout.write(`正在测试API文件地址 ${sourceUrl} `)
    const isFileAccessible = await testConnection(sourceUrl)
    clearLine()
    if (!isFileAccessible) {
      throw new Error(`无法连接到地址，请检查网络连接是否正常`)
    }

    // 获取生成路径
    const { generatePath } = await prompt([
      {
        type: 'input',
        name: 'generatePath',
        message: '请输入生成文件的目标路径(相对于项目根目录):',
        initial: DEFAULT_GENERATE_PATH,
      },
    ])

    const tmpFilePath = `${tempDir}/${crypto.randomBytes(16).toString('hex')}.zip`
    const normalizedGeneratePath = normalizeGeneratePath(generatePath)

    // 确保目标目录存在
    await fs.promises.mkdir(normalizedGeneratePath, { recursive: true })

    // 下载文件
    process.stdout.write(`正在从 ${sourceUrl} 下载文件...`)
    await downloadFile(sourceUrl, tmpFilePath)
    clearLine()
    updateStatus('文件下载完成', true)

    // 清理已存在的目录内容
    if (fs.existsSync(normalizedGeneratePath)) {
      process.stdout.write('正在清理已存在的目标目录内容...')
      const files = await fs.promises.readdir(normalizedGeneratePath)
      await Promise.all(
        files.map((file) =>
          fs.promises.rm(path.join(normalizedGeneratePath, file), { recursive: true, force: true }),
        ),
      )
      clearLine()
      updateStatus('目标目录内容清理完成', true)
    }

    // 解压文件
    process.stdout.write('正在解压文件...')
    const zip = new AdmZip(tmpFilePath)
    zip.extractAllTo(normalizedGeneratePath, true)
    clearLine()
    updateStatus('文件解压完成', true)

    // 清理临时文件
    process.stdout.write('正在清理临时文件...')
    await fs.promises.unlink(tmpFilePath)
    clearLine()
    updateStatus('临时文件清理完成', true)

    updateStatus('所有操作已完成！', true)
  } catch (error) {
    updateStatus(`操作失败: ${error.message}`, false)
    process.exit(1)
  }
}

main()
