// ─────────── .github/scripts/ai_pr_review.js ───────────
// 이 파일은 ESM(ES Modules) 환경을 전제합니다.
// package.json에 "type": "module" 이 반드시 있어야 합니다.

import { Octokit } from '@octokit/rest'
import OpenAI from 'openai'
import * as core from '@actions/core'

const PRICING = {
  "o4-mini": { input: 1.1, cached_input: 0.275, output: 4.4 },
  // 필요하면 다른 모델도 추가…
}

const filteredExtension = 'kt'

const repoFullName = process.env.REPOSITORY   // ex) "user/repo"
const prNumber     = process.env.PR_NUMBER
const token        = process.env.GITHUB_TOKEN
const openaiKey    = process.env.OPENAI_API_KEY

if (!repoFullName || !prNumber || !token || !openaiKey) {
  core.setFailed('필요한 환경 변수가 누락되었습니다.')
  process.exit(1)
}

const [ owner, repo ] = repoFullName.split('/')
const octokit = new Octokit({ auth: token })
const openai  = new OpenAI({ apiKey: openaiKey })

async function run() {
  try {
    // 1) PR 정보 & 변경 파일 가져오기
    const { data: prInfo }   = await octokit.pulls.get({ owner, repo, pull_number: Number(prNumber) })
    const headSha            = prInfo.head.sha
    const { data: prFiles }  = await octokit.pulls.listFiles({ owner, repo, pull_number: Number(prNumber) })

    // 2) .kt + patch 있는 파일만
    const files = prFiles.filter(f =>
        f.filename.endsWith(filteredExtension) &&
        f.status !== 'removed' &&
        f.patch
    )
    if (files.length === 0) {
      core.info(`변경된 ${filteredExtension} 파일이 없습니다. 종료합니다.`)
      return
    }

    // 3) diff 제안 모아둘 배열
    const commentEntries = []

    // 4) batchSize 단위로 patch만 보내 diff 제안 받기
    const chunkSize = 5
    for (let i = 0; i < files.length; i += chunkSize) {
      const batch = files.slice(i, i + chunkSize)
      const diffPrompt = batch
      .map(f => `파일 경로: ${f.filename}\n\`\`\`diff\n${f.patch}\n\`\`\``)
      .join('\n\n')

      const resp = await sendChatApi(
          'gpt-4o-mini',
          // 시스템 메시지는 미리 정의해도 됩니다
          `
You are a senior kotlin spring developer AND global developer using english
아래 코드 diff를 보고, 클린 코드·코틀린 문법·객체지향 설계 관점에서
“라인 번호 | 기존 → 제안” 형태로만 응답하세요.
`,
          diffPrompt
      )

      const text = resp.choices[0]?.message?.content || ''
      commentEntries.push(...parseBatchedReviewEntries(text))
    }

    // 5) Suggestion 코멘트 객체로 변환
    const commentsToAdd = commentEntries.map(({ filePath, lineNumber, suggestion }) => ({
      path: filePath,
      line: lineNumber,
      side: 'RIGHT',
      body: ['```suggestion', suggestion, '```'].join('\n'),
    }))

    core.info(`✅ 총 ${commentsToAdd.length}개의 코드 제안 준비 완료`)

    // 6) **종합 평가 생성**
    //    모든 diffPrompt를 다시 모아서 한 번에 “전체 평가”를 요청
    const overallPrompt = files
    .map(f => `파일: ${f.filename}\n\`\`\`diff\n${f.patch}\n\`\`\``)
    .join('\n\n')

    const overallResp = await sendChatApi(
        'gpt-4o-mini',
        `
You are a senior kotlin spring developer AND global developer using english
아래 PR에 포함된 전체 diff를 보고, 코드 품질·객체지향 설계·클린 코드 관점에서
종합적으로 간결하게 평가해주세요. (“무엇이 잘 되어 있고, 무엇을 개선하면 좋을지”)
      `,
        overallPrompt
    )
    const overallEvaluation = overallResp.choices[0]?.message?.content.trim() || ''
    if (overallEvaluation) {
      core.info('✅ 종합 평가 준비 완료')
    }

    // 7) **GitHub에 리뷰 업로드**
    if (commentsToAdd.length > 0 || overallEvaluation) {
      await octokit.pulls.createReview({
        owner,
        repo,
        pull_number: Number(prNumber),
        commit_id: headSha,
        // body에 종합 평가 넣고, comments에 개별 제안 삽입
        body: overallEvaluation || '자동 생성된 리뷰입니다.',
        event: 'COMMENT',
        comments: commentsToAdd,
      })
      core.info(`🎉 리뷰 생성 완료: 제안 ${commentsToAdd.length}개${overallEvaluation ? ' + 종합 평가' : ''}`)
    } else {
      core.info('남은 리뷰 코멘트가 없어 리뷰를 생성하지 않습니다.')
    }

  } catch (error) {
    core.setFailed(`오류 발생: ${error.message}`)
  }
}

/**
 * OpenAI 응답에서
 * “파일 경로: X” 섹션별로 분리한 뒤,
 * 라인 번호·suggestion 을 [{filePath, lineNumber, suggestion}, …] 형태로 반환
 */
function parseBatchedReviewEntries(text) {
  const entries = []
  const sections = text.split(/\r?\n(?=파일 경로:)/)
  sections.forEach(sec => {
    const header = sec.match(/^파일 경로:\s*(.+?)\s*/)
    if (!header) return
    const filePath = header[1].trim()
    const body = sec.replace(header[0], '').trim()
    body.split(/\r?\n/).forEach(line => {
      const m = line.match(/^\s*(\d+)\s*\|\s*.+?→\s*(.+)$/)
      if (m) {
        entries.push({
          filePath,
          lineNumber: parseInt(m[1], 10),
          suggestion: m[2].trim(),
        })
      }
    })
  })
  return entries
}

async function sendChatApi(model, systemMessage, userMessage) {
  return await openai.chat.completions.create({
    model,
    messages: [
      { role: 'system', content: systemMessage },
      { role: 'user',   content: userMessage   },
    ],
    temperature: 0.6,
    max_tokens: 1000,
  })
}

run()
