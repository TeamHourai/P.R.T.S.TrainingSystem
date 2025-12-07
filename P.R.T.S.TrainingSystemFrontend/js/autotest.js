// 自动化测试脚本：测试前端数据能否直接写入数据库
// 运行方式：在 Node.js 环境或浏览器控制台执行

async function testApiUser() {
  const res = await fetch('http://localhost:8888/api/user', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      username: 'autotest_user',
      password: 'autotest_pass',
      isAdmin: false,
      registerTime: '2025-12-07 21:00:00'
    })
  });
  const text = await res.text();
  console.log('user接口返回:', text);
  let userId = null;
  try {
    const obj = JSON.parse(text);
    userId = obj.userId || obj.id || null;
  } catch (e) {}
  console.log('自动化测试 userId:', userId);
  return userId;
}

async function testApiQuestion() {
  const res = await fetch('http://localhost:8888/api/question', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      type: 1,
      difficulty: 2,
      resource: '',
      question: '自动化测试题目',
      hasPicture: false,
      options: 'A|B|C|D',
      answer: 2,
      analysis: '自动化测试解析'
    })
  });
  const text = await res.text();
  console.log('question接口返回:', text);
  let questionId = null;
  try {
    const obj = JSON.parse(text);
    questionId = obj.questionId || obj.id || null;
  } catch (e) {}
  console.log('自动化测试 questionId:', questionId);
  return questionId;
}

async function testApiExamRecord(userId) {
  console.log('testApiExamRecord 使用 userId:', userId);
  const res = await fetch('http://localhost:8888/api/exam_record', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      userId: userId,
      score: 100,
      submitTime: '2025-12-07 21:05:00'
    })
  });
  const text = await res.text();
  console.log('exam_record接口返回:', text);
}

async function testApiUserAnswer(userId, questionId) {
  console.log('testApiUserAnswer 使用 userId:', userId, 'questionId:', questionId);
  const res = await fetch('http://localhost:8888/api/user_answer', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      userId: userId,
      questionId: questionId,
      questionType: '单选',
      isCorrect: true,
      selected: 2,
      answeredAt: '2025-12-07 21:10:00'
    })
  });
  const text = await res.text();
  console.log('user_answer接口返回:', text);
}

async function runAllTests() {
  const userId = await testApiUser();
  const questionId = await testApiQuestion();
  await testApiExamRecord(userId);
  await testApiUserAnswer(userId, questionId);
  console.log('全部自动化测试完成，请在数据库中查询结果。');
}

runAllTests();
