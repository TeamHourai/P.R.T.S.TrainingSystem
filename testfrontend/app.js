// 固定后端地址（移除自动探测）
const BASE_URL = "http://localhost:8080";

// ping 后端，检查在线状态（直接请求固定 BASE_URL）
function pingServer() {
    fetch(`${BASE_URL}/ping`, { method: "GET", mode: "cors" })
    .then(resp => {
        if (!resp.ok) return resp.text().then(t => { throw new Error(t || resp.statusText); });
        return resp.json();
    })
    .then(data => {
        document.getElementById("result").innerText = `Ping 返回: ${JSON.stringify(data)}`;
    })
    .catch(err => {
        document.getElementById("result").innerText = `Ping 失败: ${err.message}`;
    });
}

// 兼容 index.html 的 ping() 调用
function ping() { pingServer(); }

// 获取所有题目并渲染列表（只读）
function loadQuestions() {
    fetch(`${BASE_URL}/questions`, { method: "GET", mode: "cors" })
    .then(resp => {
        if (!resp.ok) return resp.text().then(t => { throw new Error(t || resp.statusText); });
        return resp.json();
    })
    .then(qs => {
        renderQuestionList(qs);
        document.getElementById("result").innerText = `共加载题目 ${qs.length} 条`;
    })
    .catch(err => {
        document.getElementById("result").innerText = `加载题目失败: ${err.message}`;
    });
}

// 兼容 index.html 的 getQuestions()
function getQuestions() { loadQuestions(); }

// 根据 count 获取试卷并渲染成可答题模式
function getExamPaper() {
    // 兼容两种 id: "paperCount" 或 "paper_count"
    const el = document.getElementById("paperCount") || document.getElementById("paper_count");
    const count = parseInt(el ? el.value : 10) || 10;
    fetch(`${BASE_URL}/exam/paper?count=${count}`, { method: "GET", mode: "cors" })
    .then(resp => {
        if (!resp.ok) return resp.text().then(t => { throw new Error(t || resp.statusText); });
        return resp.json();
    })
    .then(qs => {
        renderExam(qs);
        document.getElementById("result").innerText = `试卷加载完成：${qs.length} 题，请选择答案并提交`;
    })
    .catch(err => {
        document.getElementById("result").innerText = `获取试卷失败: ${err.message}`;
    });
}

// 兼容 index.html 的 getExamPaper()（按钮调用）
function getExamPaperWrapper() { getExamPaper(); }
// index.html 直接调用 getExamPaper 名称，导出同名函数：
window.getExamPaper = getExamPaper;

// 新增：注册用户（POST /register）
// 兼容 index.html 的 reg_username / reg_password id，如果存在则优先使用
function registerUser() {
    const uEl = document.getElementById("regUsername") || document.getElementById("reg_username");
    const pEl = document.getElementById("regPassword") || document.getElementById("reg_password");
    const username = uEl ? uEl.value.trim() : "";
    const password = pEl ? pEl.value.trim() : "";
    if (!username || !password) {
        alert("请输入 username 与 password 用于测试注册（可在页面添加对应输入框）");
        return;
    }
    const body = new URLSearchParams();
    body.append("username", username);
    body.append("password", password);
    fetch(`${BASE_URL}/register`, {
        method: "POST",
        mode: "cors",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: body.toString()
    })
    .then(resp => {
        if (!resp.ok) return resp.text().then(t => { throw new Error(t || resp.statusText); });
        return resp.json();
    })
    .then(data => {
        document.getElementById("result").innerText = `注册成功：${JSON.stringify(data)}`;
    })
    .catch(err => {
        document.getElementById("result").innerText = `注册失败: ${err.message}`;
    });
}
// 兼容 index.html 的按钮 onclick
window.registerUser = registerUser;


// 新增：登录用户（POST /login）
function loginUser() {
    const uEl = document.getElementById("loginUsername") || document.getElementById("login_username");
    const pEl = document.getElementById("loginPassword") || document.getElementById("login_password");
    const username = uEl ? uEl.value.trim() : "";
    const password = pEl ? pEl.value.trim() : "";
    if (!username || !password) {
        alert("请输入 username 与 password 用于登录测试");
        return;
    }
    const body = new URLSearchParams();
    body.append("username", username);
    body.append("password", password);
    fetch(`${BASE_URL}/login`, {
        method: "POST",
        mode: "cors",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: body.toString()
    })
    .then(resp => {
        if (!resp.ok) return resp.text().then(t => { throw new Error(t || resp.statusText); });
        return resp.json();
    })
    .then(data => {
        document.getElementById("result").innerText = `登录成功：${JSON.stringify(data)}`;
    })
    .catch(err => {
        document.getElementById("result").innerText = `登录失败: ${err.message}`;
    });
}
window.loginUser = loginUser;

// 提交试卷答案到 /exam/submit
function submitExam() {
    // 兼容不同 userId 输入 id
    const uidEl = document.getElementById("userId") || document.getElementById("submit_userId");
    const userId = uidEl ? uidEl.value.trim() : "";
    if (!userId) {
        alert("请输入 userId（可通过 /login 或查看 data/users.csv 得到示例 id）");
        return;
    }

    // 先尝试从文本框直接读取 answers（index.html 提供 submit_answers）
    const answersTextEl = document.getElementById("submit_answers");
    let answers = "";
    if (answersTextEl && answersTextEl.value.trim()) {
        answers = answersTextEl.value.trim();
    } else {
        // 从页面收集所有题目的选择（radio 模式）
        const qBlocks = document.querySelectorAll(".question-block");
        const pairs = [];
        qBlocks.forEach(block => {
            const inputs = block.querySelectorAll('input[type="radio"]:checked');
            if (inputs.length > 0) {
                const name = inputs[0].name;
                const qid = name.split('_')[1];
                const val = inputs[0].value;
                pairs.push(`${qid}:${val}`);
            }
        });
        if (pairs.length === 0) {
            alert("至少答一道题或在 answers 文本框中输入 answers 再提交");
            return;
        }
        answers = pairs.join(',');
    }

    const body = new URLSearchParams();
    body.append("userId", userId);
    body.append("answers", answers);
    fetch(`${BASE_URL}/exam/submit`, {
        method: "POST",
        mode: "cors",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: body.toString()
    })
    .then(resp => {
        if (!resp.ok) return resp.text().then(t => { throw new Error(t || resp.statusText); });
        return resp.json ? resp.json() : resp.text();
    })
    .then(data => {
        document.getElementById("result").innerText = `提交成功：${JSON.stringify(data)}`;
    })
    .catch(err => {
        document.getElementById("result").innerText = `提交失败: ${err.message}`;
    });
}
// 兼容 index.html 的 submitExam() 调用（index already calls submitExam）
window.submitExam = submitExam;

// 新增：获取用户错题 GET /user/{id}/wrong
function getUserWrongs() {
    const idEl = document.getElementById("wrong_userId") || document.getElementById("wrongUserId");
    const id = idEl ? idEl.value.trim() : "";
    if (!id) {
        alert("请输入 userId");
        return;
    }
    fetch(`${BASE_URL}/user/${encodeURIComponent(id)}/wrong`, { method: "GET", mode: "cors" })
    .then(resp => {
        if (!resp.ok) return resp.text().then(t => { throw new Error(t || resp.statusText); });
        return resp.json ? resp.json() : resp.text();
    })
    .then(data => {
        document.getElementById("result").innerText = `错题：${JSON.stringify(data)}`;
    })
    .catch(err => {
        document.getElementById("result").innerText = `获取错题失败: ${err.message}`;
    });
}
window.getUserWrongs = getUserWrongs;

// 渲染只读题目列表
function renderQuestionList(qs) {
    const container = document.getElementById("questions");
    container.innerHTML = "";
    qs.forEach(q => {
        const div = document.createElement("div");
        div.className = "question";
        div.innerHTML = `<strong>#${q.id}</strong> ${escapeHtml(q.question)}<br>
                         <em>选项：</em> ${ (q.options || []).map((o,i)=>`[${i+1}] ${escapeHtml(o)}`).join(' , ') }`;
        container.appendChild(div);
    });
}

// 渲染可答题的试卷（radio）
function renderExam(qs) {
    const container = document.getElementById("questions");
    container.innerHTML = "";
    qs.forEach(q => {
        const div = document.createElement("div");
        div.className = "question-block";
        let html = `<div class="q-title"><strong>#${q.id}</strong> ${escapeHtml(q.question)}</div>`;
        const opts = q.options || [];
        html += '<div class="q-options">';
        opts.forEach((opt, idx) => {
            // 使用 1-based 值上传（后端示例为 1-based）
            const val = idx + 1;
            html += `<label><input type="radio" name="q_${q.id}" value="${val}"> ${escapeHtml(opt)}</label><br>`;
        });
        html += '</div>';
        div.innerHTML = html;
        container.appendChild(div);
    });
}

// 简单的 HTML 转义，防止注入显示问题
function escapeHtml(s) {
    if (!s && s !== 0) return "";
    return String(s)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// 旧的示例功能保留但已移除或替换
