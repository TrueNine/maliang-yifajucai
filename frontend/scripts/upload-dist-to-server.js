/**
 * dist文件夹部署脚本
 * 将dist文件夹压缩后上传到服务器指定目录并解压
 *
 * @requires node:child_process exec - 执行系统命令
 * @requires node:fs/promises - 文件系统操作
 * @requires node:path - 路径处理
 * @requires node:process - 进程操作
 * @requires node:url fileURLToPath - 文件URL转换
 * @requires node:util promisify - Promise化回调函数
 *
 * 第三方依赖:
 * @requires chalk - 命令行颜色输出 (pnpm i chalk)
 * @requires cli-progress - 进度条显示 (pnpm i cli-progress)
 * @requires enquirer - 命令行交互 (pnpm i enquirer)
 * @requires node-ssh - SSH连接 (pnpm i node-ssh)
 *
 * 使用方法:
 * 1. 确保已安装所有依赖包
 * 2. 在项目根目录运行: node scripts/upload-dist-to-server.js
 * 3. 按提示输入或选择服务器信息
 *
 * 工作流程:
 * 1. 压缩dist目录为tar.gz文件
 * 2. 通过SSH上传到服务器临时目录
 * 3. 在服务器端解压到目标目录
 * 4. 清理临时文件
 */

import { exec } from 'node:child_process'
import fs from 'node:fs/promises'
import path from 'node:path'
import process from 'node:process'
import { fileURLToPath } from 'node:url'
import { promisify } from 'node:util'
import chalk from 'chalk'
import cliProgress from 'cli-progress'
import enquirer from 'enquirer'
import { NodeSSH } from 'node-ssh'

const execAsync = promisify(exec)
const { prompt } = enquirer

const __dirname = path.dirname(fileURLToPath(import.meta.url))

// 定义常量配置
const SERVER_IP = '114.132.95.38'
const SERVER_USERS = ['ubuntu', 'root']
const SERVER_PASSWORD = '#Pengjigong1997'
const LOCAL_DIST_PATH = path.resolve(__dirname, '../dist')
const REMOTE_PATH = '/opt/dl/nginx/html/dist'
const MAX_UPLOAD_ATTEMPTS = 2

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
  process.stdout.write(statusText)
}

async function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

/**
 * 预清理函数 - 清理可能存在的临时文件
 */
async function preclean() {
  const archiveName = 'dist.tar.gz'
  const archivePath = path.resolve(__dirname, '..', archiveName)

  try {
    const exists = await fs.access(archivePath).then(() => true).catch(() => false)
    if (exists) {
      updateStatus('正在清理旧的临时文件...')
      await fs.unlink(archivePath)
      updateStatus('旧文件清理完成', true)
    }
  } catch (error) {
    updateStatus(`清理旧文件失败: ${error.message}`, false)
  }
}

async function compressDistFolder() {
  const archiveName = 'dist.tar.gz'
  const archivePath = path.resolve(__dirname, '..', archiveName)
  updateStatus('正在压缩dist目录...')
  try {
    const command = `cd "${path.dirname(LOCAL_DIST_PATH)}" && tar -czf "${archiveName}" dist`
    await execAsync(command)
    updateStatus('压缩完成', true)
    return archivePath
  } catch (error) {
    updateStatus(`压缩失败: ${error.message}`, false)
    throw error
  }
}

async function uploadAndExtract(ssh, archivePath) {
  const remoteArchivePath = '/tmp/dist.tar.gz'
  const progressBar = new cliProgress.SingleBar({
    format: '上传进度 |{bar}| {percentage}% \r',
    barCompleteChar: '\u2588',
    barIncompleteChar: '\u2591',
    clearOnComplete: true,
    stream: process.stdout,
  })

  try {
    updateStatus('正在上传压缩文件...')
    progressBar.start(100, 0)

    const progressInterval = setInterval(() => {
      const currentValue = progressBar.value
      if (currentValue < 90) {
        progressBar.update(currentValue + 1)
      }
    }, 100)

    await ssh.putFile(archivePath, remoteArchivePath)
    clearInterval(progressInterval)
    progressBar.update(100)
    progressBar.stop()
    updateStatus('压缩文件上传完成', true)

    updateStatus('正在解压文件...')
    await ssh.execCommand(`rm -rf ${REMOTE_PATH}/*`)
    await ssh.execCommand(`cd ${path.dirname(REMOTE_PATH)} && tar -xzf ${remoteArchivePath} && rm ${remoteArchivePath}`)
    updateStatus('解压完成', true)

    return true
  } catch (error) {
    updateStatus(`操作失败: ${error.message}`, false)
    throw error
  }
}

async function main() {
  try {
    await preclean()

    const credentials = await prompt([
      {
        type: 'input',
        name: 'host',
        message: '请输入服务器地址:',
        initial: SERVER_IP,
        required: true,
      },
      {
        type: 'select',
        name: 'username',
        message: '请选择用户:',
        choices: SERVER_USERS,
        initial: 0,
        required: true,
      },
      {
        type: 'password',
        name: 'password',
        message: '请输入密码:',
        initial: SERVER_PASSWORD,
        required: true,
      },
    ])

    const archivePath = await compressDistFolder()

    for (let attempt = 0; attempt < MAX_UPLOAD_ATTEMPTS; attempt++) {
      try {
        updateStatus('正在连接到服务器...')
        const ssh = new NodeSSH()
        await ssh.connect({
          host: credentials.host,
          username: credentials.username,
          password: credentials.password,
        })

        updateStatus('服务器连接成功', true)

        await uploadAndExtract(ssh, archivePath)

        await fs.unlink(archivePath)
        updateStatus('本地压缩文件已清理', true)
        process.stdout.write('\n')

        ssh.dispose()
        break
      } catch (error) {
        if (attempt < MAX_UPLOAD_ATTEMPTS - 1) {
          updateStatus(`发生错误，准备重新尝试: ${error.message}`, false)
          await sleep(5000)
          continue
        } else {
          updateStatus(`最终错误: ${error.message}`, false)
          try {
            await fs.unlink(archivePath)
          } catch {
            // 忽略清理错误
          }
          process.exit(1)
        }
      }
    }
  } catch (error) {
    updateStatus(`发生错误: ${error.message}`, false)
    process.exit(1)
  }
}

main()
