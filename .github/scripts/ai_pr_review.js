// ─────────── .github/scripts/ai_pr_review.js ───────────
// 이 파일은 ESM(ES Modules) 환경을 전제합니다.
// package.json에 "type": "module" 이 반드시 있어야 합니다.

import {Octokit} from '@octokit/rest'
import OpenAI from 'openai'
import * as core from '@actions/core'

const PRICING = {
  "o4-mini": {input: 1.1, cached_input: 0.275, output: 4.4},
  // 필요하면 다른 모델도 추가…
}

const filteredExtension = 'kt'

// ─── 환경 변수 로드 ────────────────────────────────────────────────────────────
const repoFullName = process.env.REPOSITORY   // ex) "user/repo"
const prNumber    = process.env.PR_NUMBER
const token       = process.env.GITHUB_TOKEN
const openaiKey   = process.env.OPENAI_API_KEY

// 누락된 env 변수 확인 및 에러 처리
const missing = []
if (!repoFullName) {
  missing.push('REPOSITORY')
}
if (!prNumber) {
  missing.push('PR_NUMBER')
}
if (!token) {
  missing.push('GITHUB_TOKEN')
}
if (!openaiKey) {
  missing.push('OPENAI_API_KEY')
}
if (missing.length > 0) {
  core.setFailed(`필요한 환경 변수가 누락되었습니다: ${missing.join(', ')}`)
  process.exit(1)
}

const [owner, repo] = repoFullName.split('/')
const octokit = new Octokit({auth: token})
const openai = new OpenAI({apiKey: openaiKey})

// 시스템 메시지 (Kotlin + English)
const systemMessageForDiff = `
You are a senior kotlin spring developer AND global developer using English.
아래는 Kotlin 파일들의 unified diff입니다.
각 파일마다 "파일 경로: <파일 경로>" 섹션을 반드시 포함하고,
수정이 필요한 줄에 대해서만 다음 형식으로 제안을 작성해 주세요:
  LINE_NUMBER | ORIGINAL → SUGGESTED
`;

const systemMessageForOverall = `
You are a senior kotlin spring developer AND global developer using English.
아래는 Kotlin 파일들의 unified diff입니다.
코드 퀄리티, 클린 코드, 객체 지향 코드인지 평가하고, 테스트 나 주석등 영어 사용이 어색하지 않은지 평가해줘.
무엇을 잘했고, 어딜 발전 시킬수 있을지도 같이 포함해줘.
`;

export async function run() {
  try {
    // 1) PR 정보 & 변경된 .kt 파일 조회
    const {data: prInfo} = await octokit.pulls.get(
        {owner, repo, pull_number: Number(prNumber)})
    const headSha = prInfo.head.sha
    const {data: prFiles} = await octokit.pulls.listFiles(
        {owner, repo, pull_number: Number(prNumber)})

    // 2) .kt + patch 있는 파일만 필터링
    const files = prFiles.filter(f =>
        f.filename.endsWith(filteredExtension) &&
        f.status !== 'removed' &&
        f.patch
    )
    if (files.length === 0) {
      core.info(`변경된 ${filteredExtension} 파일이 없습니다. 종료합니다.`)
      return
    }

    // 3) batchSize 단위로 diff 제안 수집
    const commentEntries = []
    const chunkSize = 5
    for (let i = 0; i < files.length; i += chunkSize) {
      const batch = files.slice(i, i + chunkSize)
      const diffPrompt = batch
      .map(f => `파일 경로: ${f.filename}\n\`diff\n${f.patch}\n\``)
      .join('\n\n')

      const resp = await sendChatApi('gpt-4o-mini', systemMessageForDiff,
          diffPrompt)
      const text = resp.choices[0]?.message?.content || ''
      commentEntries.push(...parseBatchedReviewEntries(text))
    }

// 4) Suggestion 코멘트 객체로 변환
    const commentsToAdd = commentEntries.map(
        ({filePath, lineNumber, suggestion}) => ({
          path: filePath,
          line: lineNumber,
          side: 'RIGHT',
          body: ['```suggestion', suggestion, '```'].join('\n'),
        }))
    core.info(`✅ 총 ${commentsToAdd.length}개의 코드 제안 준비 완료`)
    core.debug(`코드 제안 목록 출력`)
    commentsToAdd.forEach((comment) => {
      core.debug(`경로: ${comment.path} 라인: ${comment.line}`)
    })

// 5) 종합 평가 생성
    const overallPrompt = files
    .map(f => `파일 경로: ${f.filename}\n\`diff\n${f.patch}\n\``)
    .join('\n\n')
    const overallResp = await sendChatApi('gpt-4o-mini',
        systemMessageForOverall, overallPrompt)
    const overallEvaluation = overallResp.choices[0]?.message?.content.trim()
        || ''
    if (overallEvaluation) {
      core.info('✅ 종합 평가 준비 완료')
    }

// 6) GitHub 리뷰 업로드
    if (commentsToAdd.length > 0) {
      // 각 suggestion은 개별 코멘트로 등록
      for (const c of commentsToAdd) {
        await octokit.pulls.createReviewComment({
          owner,
          repo,
          pull_number: Number(prNumber),
          commit_id: headSha,
          path: c.path,
          line: c.line,
          side: c.side,
          body: c.body,
        })
      }
      core.info(`🎉 ${commentsToAdd.length}개의 코드 제안 코멘트를 생성했습니다.`)
    }
    if (overallEvaluation) {
      // 종합 평가는 따로 Review로 등록
      await octokit.pulls.createReview({
        owner,
        repo,
        pull_number: Number(prNumber),
        body: overallEvaluation,
        event: 'COMMENT',
      })
      core.info('🎉 종합 평가를 리뷰 본문으로 등록했습니다.')
    }
  } catch (error) {
    core.setFailed(`오류 발생: ${error.message}`)
  }
}

/**
 * OpenAI 응답에서
 * 파일 경로별로 분리하고, 라인번호·suggestion만 추출하여 반환
 */
function parseBatchedReviewEntries(text) {
  printPadding('text', text);
  const entries = []
  const sections = text.split(/\r?\n(?=파일 경로:)/)
  sections.forEach(sec => {
    printPadding('sec', text);
    const m = sec.match(/^파일 경로:\s*([^\r\n]+)\s*$/m)
    if (!m) {
      return
    }
    const filePath = m[1].trim()
    const body = sec.replace(m[0], '').trim()
    printPadding('body', text);
    body.split(/\r?\n/).forEach(line => {
      const mm = line.match(/^\s*(\d+)\s*\|\s*.+?→\s*(.+)$/)
      printPadding('mm', text);
      if (mm) {
        entries.push({
          filePath,
          lineNumber: parseInt(mm[1], 10),
          suggestion: mm[2].trim(),
        })
      }
    })
  })
  return entries
}

function printPadding(title, content) {
  console.log(title);
  console.log('\n');
  console.log(content);
  console.log('\n');
  console.log(title);
}

async function sendChatApi(model, systemMessage, userMessage) {
  return openai.chat.completions.create({
    model,
    messages: [
      {role: 'system', content: systemMessage},
      {role: 'user', content: userMessage},
    ],
    temperature: 0.7,
    max_tokens: 1000,
  })
}

run()