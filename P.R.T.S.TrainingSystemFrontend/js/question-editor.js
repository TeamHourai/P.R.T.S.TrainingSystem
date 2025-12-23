(function () {
    'use strict';

    const MODE = (window.EDITOR_MODE || 'questions').toLowerCase();
    const isTraining = MODE === 'training';

    const el = {
        keyword: document.getElementById('qe-keyword'),
        type: document.getElementById('qe-type'),
        difficulty: document.getElementById('qe-difficulty'),
        size: document.getElementById('qe-size'),
        searchBtn: document.getElementById('qe-search-btn'),
        resetBtn: document.getElementById('qe-reset-btn'),
        newBtn: document.getElementById('qe-new-btn'),
        batchDelBtn: document.getElementById('qe-batch-del-btn'),
        checkAll: document.getElementById('qe-check-all'),
        tbody: document.getElementById('qe-tbody'),
        prev: document.getElementById('qe-prev'),
        next: document.getElementById('qe-next'),
        pageinfo: document.getElementById('qe-pageinfo'),
        hint: document.getElementById('qe-hint'),

        form: document.getElementById('qe-form'),
        formTitle: document.getElementById('qe-form-title'),
        formSub: document.getElementById('qe-form-sub'),
        formHint: document.getElementById('qe-form-hint'),
        id: document.getElementById('qe-id'),
        formType: document.getElementById('qe-form-type'),
        formDifficulty: document.getElementById('qe-form-difficulty'),
        formQuestion: document.getElementById('qe-form-question'),
        optA: document.getElementById('qe-opt-a'),
        optB: document.getElementById('qe-opt-b'),
        optC: document.getElementById('qe-opt-c'),
        optD: document.getElementById('qe-opt-d'),
        formAnswer: document.getElementById('qe-form-answer'),
        formAnalysis: document.getElementById('qe-form-analysis'),
        formResource: document.getElementById('qe-form-resource'),
        formKeywords: document.getElementById('qe-form-keywords'),
        formPicture: document.getElementById('qe-form-picture'),
        clearBtn: document.getElementById('qe-clear-btn')
    };

    function setHint(target, msg, type) {
        if (!target) return;
        target.textContent = msg || '';
        target.classList.remove('qe-error', 'qe-success');
        if (type === 'error') target.classList.add('qe-error');
        if (type === 'success') target.classList.add('qe-success');
    }

    function getStored(keyA, keyB) {
        return localStorage.getItem(keyA) || sessionStorage.getItem(keyA) || localStorage.getItem(keyB) || sessionStorage.getItem(keyB);
    }

    function getUserInfo() {
        const str = getStored('userInfo', 'user_info');
        if (!str) return null;
        try { return JSON.parse(str); } catch { return null; }
    }

    async function ensureAdmin() {
        const token = getStored('token', 'auth_token');
        if (!token) {
            if (window.uiModal && window.uiModal.error) window.uiModal.error('请先登录管理员账号');
            else alert('请先登录管理员账号');
            return false;
        }

        let user = getUserInfo();
        if (!user && window.userApi && typeof userApi.getCurrentUser === 'function') {
            try { user = await userApi.getCurrentUser(); } catch { /* ignore */ }
        }

        const isAdmin = user && (user.isAdmin === true || user.role === 'admin');
        if (!isAdmin) {
            if (window.uiModal && window.uiModal.error) window.uiModal.error('需要管理员权限');
            else alert('需要管理员权限');
            return false;
        }

        return true;
    }

    // 映射
    const typeText = {
        1: '干员调配',
        2: '空间部署',
        3: '效能审计',
        4: '横向分析',
        5: '作战环境'
    };
    const diffText = { 1: '常识', 2: '基操', 3: '娴熟', 4: '明智', 5: '深邃' };

    function normalizeListResponse(resp) {
        // 兼容：
        // 1) {questions,total,page,size,pages}
        // 2) {data:{questions...}} 已由 request.js 拦截器解包，通常不会出现
        // 3) 直接数组
        if (Array.isArray(resp)) {
            return { questions: resp, total: resp.length, page: 1, size: resp.length, pages: 1 };
        }

        if (resp && Array.isArray(resp.questions)) {
            return {
                questions: resp.questions,
                total: Number(resp.total || resp.questions.length || 0),
                page: Number(resp.page || 1),
                size: Number(resp.size || resp.questions.length || 50),
                pages: Number(resp.pages || 1)
            };
        }

        // training/questions 可能直接返回数组，或与 questions 同结构
        if (resp && resp.data && Array.isArray(resp.data.questions)) {
            return {
                questions: resp.data.questions,
                total: Number(resp.data.total || resp.data.questions.length || 0),
                page: Number(resp.data.page || 1),
                size: Number(resp.data.size || resp.data.questions.length || 20),
                pages: Number(resp.data.pages || 1)
            };
        }

        return { questions: [], total: 0, page: 1, size: 50, pages: 1 };
    }

    function parseKeywords(input) {
        if (!input) return [];
        return String(input)
            .split(/[,，]/)
            .map(s => s.trim())
            .filter(Boolean);
    }

    function toSnippet(text, maxLen = 60) {
        const t = (text || '').replace(/\s+/g, ' ').trim();
        if (t.length <= maxLen) return t;
        return t.slice(0, maxLen) + '…';
    }

    const state = {
        page: 1,
        size: isTraining ? 20 : 50,
        pages: 1,
        total: 0,
        list: [],
        selectedIds: new Set()
    };

    async function apiList(params) {
        if (!isTraining) {
            if (window.questionApi && typeof questionApi.getQuestions === 'function') {
                return await questionApi.getQuestions(params);
            }
            if (window.api && api.questions && typeof api.questions.getList === 'function') {
                return await api.questions.getList(params);
            }
            throw new Error('题库API未加载（questionApi/api.questions）');
        }

        // training
        if (window.trainingQuestionApi && typeof trainingQuestionApi.getTrainingQuestions === 'function') {
            return await trainingQuestionApi.getTrainingQuestions(params);
        }
        if (window.api && api.trainingQuestions && typeof api.trainingQuestions.getList === 'function') {
            return await api.trainingQuestions.getList(params);
        }
        throw new Error('培训题目API未加载（trainingQuestionApi/api.trainingQuestions）');
    }

    async function apiGetDetail(id) {
        if (!isTraining) {
            if (window.questionApi && typeof questionApi.getQuestionById === 'function') {
                return await questionApi.getQuestionById(id, true);
            }
            if (window.api && api.questions && typeof api.questions.getDetail === 'function') {
                return await api.questions.getDetail(id, true);
            }
            throw new Error('题库详情API未加载');
        }

        if (window.trainingQuestionApi && typeof trainingQuestionApi.getTrainingQuestionById === 'function') {
            return await trainingQuestionApi.getTrainingQuestionById(id);
        }
        if (window.api && api.trainingQuestions && typeof api.trainingQuestions.getDetail === 'function') {
            return await api.trainingQuestions.getDetail(id);
        }
        throw new Error('培训题目详情API未加载');
    }

    async function apiCreate(payload) {
        if (!isTraining) {
            if (window.questionApi && typeof questionApi.createQuestion === 'function') {
                return await questionApi.createQuestion(payload);
            }
            if (window.api && api.questions && typeof api.questions.create === 'function') {
                return await api.questions.create(payload);
            }
            throw new Error('题库创建API未加载');
        }

        if (window.trainingQuestionApi && typeof trainingQuestionApi.createTrainingQuestion === 'function') {
            return await trainingQuestionApi.createTrainingQuestion(payload);
        }
        if (window.api && api.trainingQuestions && typeof api.trainingQuestions.create === 'function') {
            return await api.trainingQuestions.create(payload);
        }
        throw new Error('培训题目创建API未加载');
    }

    async function apiUpdate(id, payload) {
        if (!isTraining) {
            if (window.questionApi && typeof questionApi.updateQuestion === 'function') {
                return await questionApi.updateQuestion(id, payload);
            }
            if (window.api && api.questions && typeof api.questions.update === 'function') {
                return await api.questions.update(id, payload);
            }
            throw new Error('题库更新API未加载');
        }

        if (window.trainingQuestionApi && typeof trainingQuestionApi.updateTrainingQuestion === 'function') {
            return await trainingQuestionApi.updateTrainingQuestion(id, payload);
        }
        if (window.api && api.trainingQuestions && typeof api.trainingQuestions.update === 'function') {
            return await api.trainingQuestions.update(id, payload);
        }
        throw new Error('培训题目更新API未加载');
    }

    async function apiDelete(id) {
        if (!isTraining) {
            if (window.questionApi && typeof questionApi.deleteQuestion === 'function') {
                return await questionApi.deleteQuestion(id);
            }
            if (window.api && api.questions && typeof api.questions.delete === 'function') {
                return await api.questions.delete(id);
            }
            throw new Error('题库删除API未加载');
        }

        if (window.trainingQuestionApi && typeof trainingQuestionApi.deleteTrainingQuestion === 'function') {
            return await trainingQuestionApi.deleteTrainingQuestion(id);
        }
        if (window.api && api.trainingQuestions && typeof api.trainingQuestions.delete === 'function') {
            return await api.trainingQuestions.delete(id);
        }
        throw new Error('培训题目删除API未加载');
    }

    function readFilters() {
        const keyword = el.keyword ? el.keyword.value.trim() : '';
        const size = el.size ? Number(el.size.value || state.size) : state.size;
        const type = el.type ? el.type.value : '';
        const difficulty = el.difficulty ? el.difficulty.value : '';

        const params = {
            page: state.page,
            size,
            keyword
        };

        if (!isTraining) {
            if (type) params.type = Number(type);
            if (difficulty) params.difficulty = Number(difficulty);
        }

        return params;
    }

    function clearForm() {
        if (el.id) el.id.value = '';
        if (el.formType) el.formType.value = '';
        if (el.formDifficulty) el.formDifficulty.value = '';
        if (el.formQuestion) el.formQuestion.value = '';
        if (el.optA) el.optA.value = '';
        if (el.optB) el.optB.value = '';
        if (el.optC) el.optC.value = '';
        if (el.optD) el.optD.value = '';
        if (el.formAnswer) el.formAnswer.value = '';
        if (el.formAnalysis) el.formAnalysis.value = '';
        if (el.formResource) el.formResource.value = '';
        if (el.formKeywords) el.formKeywords.value = '';
        if (el.formPicture) el.formPicture.checked = false;

        if (el.formTitle) el.formTitle.textContent = '新建题目';
        if (el.formSub) el.formSub.textContent = '填写题目内容并保存';
        setHint(el.formHint, '', null);
    }

    function fillForm(q) {
        if (!q) return;

        // 将文本中的转义换行(\n / \r\n)转换为真实换行，便于 textarea 显示
        const toTextareaText = (str) => (str || '')
            .replace(/\\r\\n/g, '\n')
            .replace(/\\n/g, '\n')
            .replace(/\r\n/g, '\n');

        if (el.id) el.id.value = q.id != null ? String(q.id) : '';

        if (el.formType) el.formType.value = q.type != null ? String(q.type) : '';
        if (el.formDifficulty) el.formDifficulty.value = q.difficulty != null ? String(q.difficulty) : '';

        if (el.formQuestion) el.formQuestion.value = toTextareaText(q.question);

        const opts = Array.isArray(q.options) ? q.options : ['', '', '', ''];
        if (el.optA) el.optA.value = toTextareaText(opts[0] || '');
        if (el.optB) el.optB.value = toTextareaText(opts[1] || '');
        if (el.optC) el.optC.value = toTextareaText(opts[2] || '');
        if (el.optD) el.optD.value = toTextareaText(opts[3] || '');

        if (el.formAnswer) el.formAnswer.value = q.answer != null ? String(q.answer) : '';
        if (el.formAnalysis) el.formAnalysis.value = toTextareaText(q.analysis);
        if (el.formResource) el.formResource.value = q.resource || '';

        const keywords = Array.isArray(q.keywords) ? q.keywords : parseKeywords(q.keywords);
        if (el.formKeywords) el.formKeywords.value = keywords.join(',');

        if (el.formPicture) el.formPicture.checked = !!q.picture;

        if (el.formTitle) el.formTitle.textContent = `编辑题目 #${q.id}`;
        if (el.formSub) el.formSub.textContent = '修改后点击保存更新';
        setHint(el.formHint, '', null);
    }

    function buildPayload() {
        // 将 textarea 中的转义换行(\n / \r\n)统一成真实换行再提交
        const normalizeSubmitText = (str) => (str || '')
            .replace(/\\r\\n/g, '\n')
            .replace(/\\n/g, '\n')
            .replace(/\r\n/g, '\n');
        const payload = {
            question: normalizeSubmitText((el.formQuestion ? el.formQuestion.value : '')).trim(),
            options: [
                normalizeSubmitText(el.optA ? el.optA.value : ''),
                normalizeSubmitText(el.optB ? el.optB.value : ''),
                normalizeSubmitText(el.optC ? el.optC.value : ''),
                normalizeSubmitText(el.optD ? el.optD.value : '')
            ],
            answer: el.formAnswer ? Number(el.formAnswer.value) : 0,
            analysis: normalizeSubmitText(el.formAnalysis ? el.formAnalysis.value : ''),
            resource: el.formResource ? el.formResource.value : '',
            keywords: parseKeywords(el.formKeywords ? el.formKeywords.value : ''),
            picture: !!(el.formPicture && el.formPicture.checked)
        };

        if (!isTraining) {
            payload.type = el.formType ? Number(el.formType.value) : 0;
            payload.difficulty = el.formDifficulty ? Number(el.formDifficulty.value) : 0;
        }

        // 后端兼容：允许 keywords 传数组或字符串
        return payload;
    }

    function renderTable() {
        if (!el.tbody) return;

        if (state.list.length === 0) {
            el.tbody.innerHTML = `<tr><td colspan="${isTraining ? 3 : 6}" style="padding:18px;color:var(--text-secondary);">暂无数据</td></tr>`;
            return;
        }

        const rows = state.list.map(q => {
            const qid = q.id;
            const qText = toSnippet(q.question, 80);

            if (isTraining) {
                return `
          <tr data-id="${qid}">
            <td>${qid}</td>
            <td>
              <div class="qe-qtext">${escapeHtml(qText)}</div>
            </td>
            <td>
              <div class="qe-row-actions">
                <button class="qe-mini" data-action="edit" data-id="${qid}"><i class="fas fa-pen"></i></button>
                <button class="qe-mini" data-action="delete" data-id="${qid}"><i class="fas fa-trash"></i></button>
              </div>
            </td>
          </tr>`;
            }

            const checked = state.selectedIds.has(String(qid)) ? 'checked' : '';
            const t = q.type ? `${q.type} ${typeText[q.type] || ''}` : '';
            const d = q.difficulty ? `${q.difficulty} ${diffText[q.difficulty] || ''}` : '';
            return `
        <tr data-id="${qid}">
          <td><input type="checkbox" class="qe-check" data-id="${qid}" ${checked} /></td>
          <td>${qid}</td>
          <td>${escapeHtml(t)}</td>
          <td>${escapeHtml(d)}</td>
          <td>
            <div class="qe-qtext">${escapeHtml(qText)}</div>
            <div class="qe-qmeta">${escapeHtml((q.resource || ''))}</div>
          </td>
          <td>
            <div class="qe-row-actions">
              <button class="qe-mini" data-action="edit" data-id="${qid}"><i class="fas fa-pen"></i></button>
              <button class="qe-mini" data-action="delete" data-id="${qid}"><i class="fas fa-trash"></i></button>
            </div>
          </td>
        </tr>`;
        }).join('');

        el.tbody.innerHTML = rows;

        updateBatchDeleteState();
    }

    function updatePagination() {
        if (el.pageinfo) {
            el.pageinfo.textContent = `第 ${state.page} 页 / 共 ${state.pages} 页（共 ${state.total} 题）`;
        }
        if (el.prev) el.prev.disabled = state.page <= 1;
        if (el.next) el.next.disabled = state.page >= state.pages;
    }

    function updateBatchDeleteState() {
        if (!el.batchDelBtn) return;
        const enabled = state.selectedIds.size > 0;
        el.batchDelBtn.disabled = !enabled;
    }

    function escapeHtml(str) {
        return String(str || '')
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    async function loadList() {
        setHint(el.hint, '加载中...', null);
        try {
            const params = readFilters();
            state.size = Number(params.size || state.size);

            const resp = await apiList(params);
            const norm = normalizeListResponse(resp);

            state.list = (norm.questions || []).slice();
            state.total = norm.total || 0;
            state.page = norm.page || state.page;
            state.pages = Math.max(1, Number(norm.pages || 1));

            renderTable();
            updatePagination();

            setHint(el.hint, `已加载 ${state.list.length} 题`, 'success');
        } catch (e) {
            console.error(e);
            renderTable();
            updatePagination();
            setHint(el.hint, `加载失败：${e.message || e}`, 'error');
        }
    }

    async function handleEdit(id) {
        try {
            setHint(el.formHint, '加载题目详情中...', null);
            const q = await apiGetDetail(id);
            fillForm(q);
            setHint(el.formHint, '已载入，可修改后保存', 'success');
        } catch (e) {
            console.error(e);
            setHint(el.formHint, `加载详情失败：${e.message || e}`, 'error');
        }
    }

    async function handleDelete(id) {
        if (!confirm(`确定删除题目 #${id} 吗？`)) return;
        try {
            await apiDelete(id);
            // 清理选择
            state.selectedIds.delete(String(id));
            if (el.checkAll) el.checkAll.checked = false;

            // 若正在编辑该题，清空表单
            if (el.id && el.id.value && String(el.id.value) === String(id)) {
                clearForm();
            }

            await loadList();
            setHint(el.hint, `已删除 #${id}`, 'success');
        } catch (e) {
            console.error(e);
            setHint(el.hint, `删除失败：${e.message || e}`, 'error');
        }
    }

    async function handleBatchDelete() {
        if (isTraining) return;

        const ids = Array.from(state.selectedIds).map(x => Number(x)).filter(n => !isNaN(n));
        if (ids.length === 0) return;

        if (!confirm(`确定批量删除 ${ids.length} 题吗？`)) return;

        try {
            // 优先使用“准备好但未使用”的批量删除接口
            if (window.adminApi && typeof adminApi.batchDeleteQuestions === 'function') {
                await adminApi.batchDeleteQuestions(ids);
            } else {
                // 降级：逐条删除
                for (const id of ids) {
                    // eslint-disable-next-line no-await-in-loop
                    await apiDelete(id);
                }
            }

            state.selectedIds.clear();
            if (el.checkAll) el.checkAll.checked = false;

            await loadList();
            setHint(el.hint, `已批量删除 ${ids.length} 题`, 'success');
        } catch (e) {
            console.error(e);
            setHint(el.hint, `批量删除失败：${e.message || e}`, 'error');
        }
    }

    async function handleSave(evt) {
        evt.preventDefault();
        setHint(el.formHint, '', null);

        const payload = buildPayload();

        // 基本校验
        if (!payload.question) {
            setHint(el.formHint, '题干不能为空', 'error');
            return;
        }
        if (!payload.options || payload.options.some(o => !String(o || '').trim())) {
            setHint(el.formHint, '选项 A-D 均不能为空', 'error');
            return;
        }
        if (!payload.answer || payload.answer < 1 || payload.answer > 4) {
            setHint(el.formHint, '请选择正确答案（A-D）', 'error');
            return;
        }
        if (!isTraining) {
            if (!payload.type || payload.type < 1 || payload.type > 5) {
                setHint(el.formHint, '请选择题目类型（1-5）', 'error');
                return;
            }
            if (!payload.difficulty || payload.difficulty < 1 || payload.difficulty > 5) {
                setHint(el.formHint, '请选择题目难度（1-5）', 'error');
                return;
            }
        }

        try {
            const idStr = el.id ? el.id.value : '';
            if (idStr) {
                await apiUpdate(Number(idStr), payload);
                setHint(el.formHint, `已更新题目 #${idStr}`, 'success');
            } else {
                const created = await apiCreate(payload);
                const newId = (created && (created.id || created.questionId)) ? (created.id || created.questionId) : null;
                setHint(el.formHint, newId ? `创建成功（#${newId}）` : '创建成功', 'success');
                if (newId && el.id) {
                    el.id.value = String(newId);
                    if (el.formTitle) el.formTitle.textContent = `编辑题目 #${newId}`;
                }
            }

            await loadList();
        } catch (e) {
            console.error(e);
            setHint(el.formHint, `保存失败：${e.message || e}`, 'error');
        }
    }

    function bindEvents() {
        if (el.searchBtn) {
            el.searchBtn.addEventListener('click', () => {
                state.page = 1;
                loadList();
            });
        }

        if (el.resetBtn) {
            el.resetBtn.addEventListener('click', () => {
                if (el.keyword) el.keyword.value = '';
                if (el.type) el.type.value = '';
                if (el.difficulty) el.difficulty.value = '';
                if (el.size) el.size.value = String(state.size);
                state.page = 1;
                loadList();
            });
        }

        if (el.newBtn) {
            el.newBtn.addEventListener('click', () => clearForm());
        }

        if (el.clearBtn) {
            el.clearBtn.addEventListener('click', () => clearForm());
        }

        if (el.prev) {
            el.prev.addEventListener('click', () => {
                if (state.page <= 1) return;
                state.page -= 1;
                loadList();
            });
        }

        if (el.next) {
            el.next.addEventListener('click', () => {
                if (state.page >= state.pages) return;
                state.page += 1;
                loadList();
            });
        }

        if (el.form) {
            el.form.addEventListener('submit', handleSave);
        }

        if (el.tbody) {
            el.tbody.addEventListener('click', (evt) => {
                const btn = evt.target.closest('button');
                if (!btn) return;
                const action = btn.getAttribute('data-action');
                const id = btn.getAttribute('data-id');
                if (!id) return;

                if (action === 'edit') {
                    handleEdit(Number(id));
                } else if (action === 'delete') {
                    handleDelete(Number(id));
                }
            });

            if (!isTraining) {
                el.tbody.addEventListener('change', (evt) => {
                    const cb = evt.target;
                    if (!(cb instanceof HTMLInputElement)) return;
                    if (!cb.classList.contains('qe-check')) return;
                    const id = cb.getAttribute('data-id');
                    if (!id) return;

                    if (cb.checked) state.selectedIds.add(String(id));
                    else state.selectedIds.delete(String(id));

                    // 更新全选状态
                    const checks = Array.from(el.tbody.querySelectorAll('input.qe-check'));
                    const allChecked = checks.length > 0 && checks.every(c => c.checked);
                    if (el.checkAll) el.checkAll.checked = allChecked;

                    updateBatchDeleteState();
                });
            }
        }

        if (!isTraining && el.checkAll) {
            el.checkAll.addEventListener('change', () => {
                const checks = Array.from(el.tbody.querySelectorAll('input.qe-check'));
                checks.forEach(c => { c.checked = el.checkAll.checked; });

                state.selectedIds.clear();
                if (el.checkAll.checked) {
                    checks.forEach(c => {
                        const id = c.getAttribute('data-id');
                        if (id) state.selectedIds.add(String(id));
                    });
                }
                updateBatchDeleteState();
            });
        }

        if (!isTraining && el.batchDelBtn) {
            el.batchDelBtn.addEventListener('click', () => handleBatchDelete());
        }

        // Enter 快速查询
        if (el.keyword) {
            el.keyword.addEventListener('keydown', (e) => {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    state.page = 1;
                    loadList();
                }
            });
        }
    }

    async function loadKeywords() {
        try {
            if (window.keywordApi && typeof keywordApi.getAll === 'function') {
                const data = await keywordApi.getAll({ mode: isTraining ? 'onboarding' : undefined });
                const list = Array.isArray(data) ? data : (data && Array.isArray(data.keywords) ? data.keywords : []);
                const dedup = Array.from(new Set(list.map(k => String(k).trim()).filter(Boolean)));
                // populate any datalist with id 'qe-keywords-list'
                const dl = document.getElementById('qe-keywords-list');
                if (dl) {
                    dl.innerHTML = dedup.map(k => `<option value="${escapeHtml(k)}"></option>`).join('');
                }
            }
        } catch (e) {
            console.warn('加载关键词失败', e);
        }
    }

    async function init() {
        // training-editor.html 中不存在 type/difficulty 字段，隐藏无关校验
        if (isTraining) {
            if (el.type) el.type.closest('.qe-field')?.remove?.();
            if (el.difficulty) el.difficulty.closest('.qe-field')?.remove?.();
            if (el.batchDelBtn) el.batchDelBtn.remove();
            if (el.checkAll) el.checkAll.closest('th')?.remove?.();
            if (el.formType) el.formType.closest('.qe-field')?.remove?.();
            if (el.formDifficulty) el.formDifficulty.closest('.qe-field')?.remove?.();
        } else {
            // 默认 size
            if (el.size) el.size.value = '50';
        }

        const ok = await ensureAdmin();
        if (!ok) return;

        // 加载关键词用于建议
        await loadKeywords();

        bindEvents();
        clearForm();
        await loadList();
    }

    init();
})();
