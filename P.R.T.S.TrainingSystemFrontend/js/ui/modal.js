(function () {
  'use strict';

  // Simple center modal + toast replacement for alert().
  // Usage:
  //   uiModal.info('...')
  //   uiModal.success('...')
  //   uiModal.error('...')
  //   uiModal.confirm('...', { okText, cancelText }).then(ok => ...)

  function ensureStyles() {
    if (document.getElementById('ui-modal-style')) return;
    const style = document.createElement('style');
    style.id = 'ui-modal-style';
    style.textContent = `
      .ui-modal-mask{
        position:fixed;inset:0;z-index:20000;
        background:rgba(0,0,0,.55);
        display:flex;align-items:center;justify-content:center;
        padding:16px;
      }
      .ui-modal{
        width:min(480px, 92vw);
        background: var(--bg-card, #1E1E2E);
        color: var(--text-primary, #fff);
        border: 1px solid var(--border-color, rgba(255,255,255,.1));
        border-radius: var(--border-radius-lg, 20px);
        box-shadow: var(--shadow-lg, 0 16px 48px rgba(0,0,0,.5));
        overflow:hidden;
        animation: uiModalIn .18s ease-out;
      }
      @keyframes uiModalIn{from{opacity:0;transform:scale(.96) translateY(8px)}to{opacity:1;transform:scale(1) translateY(0)}}
      .ui-modal-header{
        padding:14px 18px;
        font-weight:700;
        background: linear-gradient(90deg, var(--primary-color,#5E35B1), var(--accent-color,#FF7043));
      }
      .ui-modal-body{padding:18px;white-space:pre-wrap;word-break:break-word;}
      .ui-modal-footer{padding:14px 18px;display:flex;justify-content:flex-end;gap:10px;background:rgba(255,255,255,.03)}
      .ui-btn{border:none;border-radius:var(--border-radius,12px);padding:10px 16px;font-weight:700;cursor:pointer;transition:var(--transition, all .2s)}
      .ui-btn:active{transform:translateY(1px)}
      .ui-btn-primary{background:var(--primary-color,#5E35B1);color:#fff;box-shadow:0 4px 12px rgba(94,53,177,.3)}
      .ui-btn-primary:hover{background:var(--primary-dark,#4527A0)}
      .ui-btn-ghost{background:transparent;color:var(--text-primary,#fff);border:1px solid var(--border-color, rgba(255,255,255,.2))}
      .ui-btn-danger{background:var(--wrong-color,#F44336);color:#fff;box-shadow:0 4px 12px rgba(244,67,54,.25)}
      .ui-btn-danger:hover{filter:brightness(.95)}
      .ui-modal-icon{display:inline-flex;align-items:center;justify-content:center;width:26px;height:26px;border-radius:50%;margin-right:10px;font-weight:900}
      .ui-icon-info{background:rgba(33,150,243,.18);color:#90CAF9}
      .ui-icon-success{background:rgba(67,160,71,.18);color:#A5D6A7}
      .ui-icon-error{background:rgba(244,67,54,.18);color:#FFCDD2}
    `;
    document.head.appendChild(style);
  }

  function showModal(type, title, message, options) {
    ensureStyles();
    options = options || {};

    const mask = document.createElement('div');
    mask.className = 'ui-modal-mask';

    const modal = document.createElement('div');
    modal.className = 'ui-modal';

    const header = document.createElement('div');
    header.className = 'ui-modal-header';
    header.textContent = title || '提示';

    const body = document.createElement('div');
    body.className = 'ui-modal-body';

    const icon = document.createElement('span');
    icon.className = 'ui-modal-icon ' + (type === 'success' ? 'ui-icon-success' : type === 'error' ? 'ui-icon-error' : 'ui-icon-info');
    icon.textContent = type === 'success' ? '✓' : type === 'error' ? '!' : 'i';

    const text = document.createElement('span');
    text.textContent = message == null ? '' : String(message);

    body.appendChild(icon);
    body.appendChild(text);

    const footer = document.createElement('div');
    footer.className = 'ui-modal-footer';

    modal.appendChild(header);
    modal.appendChild(body);
    modal.appendChild(footer);
    mask.appendChild(modal);

    function close(result) {
      try { document.removeEventListener('keydown', onKeyDown); } catch (e) {}
      if (mask && mask.parentNode) mask.parentNode.removeChild(mask);
      if (typeof options.onClose === 'function') options.onClose(result);
    }

    function onKeyDown(e) {
      if (e.key === 'Escape') {
        if (options.escapeToClose !== false) close(false);
      }
      if (e.key === 'Enter') {
        // Enter triggers ok by default
        if (options.enterToOk !== false && okBtn) okBtn.click();
      }
    }

    let okBtn = null;
    if (options.showCancel) {
      const cancelBtn = document.createElement('button');
      cancelBtn.className = 'ui-btn ui-btn-ghost';
      cancelBtn.textContent = options.cancelText || '取消';
      cancelBtn.onclick = () => close(false);
      footer.appendChild(cancelBtn);
    }

    okBtn = document.createElement('button');
    okBtn.className = 'ui-btn ' + (options.okStyle === 'danger' ? 'ui-btn-danger' : 'ui-btn-primary');
    okBtn.textContent = options.okText || '确定';
    okBtn.onclick = () => close(true);
    footer.appendChild(okBtn);

    if (options.maskClickToClose) {
      mask.addEventListener('click', (e) => {
        if (e.target === mask) close(false);
      });
    }

    document.addEventListener('keydown', onKeyDown);
    document.body.appendChild(mask);

    // focus
    setTimeout(() => okBtn && okBtn.focus && okBtn.focus(), 0);
  }

  function confirmModal(message, options) {
    options = options || {};
    return new Promise((resolve) => {
      showModal(options.type || 'info', options.title || '确认', message, {
        showCancel: true,
        okText: options.okText || '确定',
        cancelText: options.cancelText || '取消',
        okStyle: options.okStyle,
        maskClickToClose: false,
        escapeToClose: true,
        onClose: resolve
      });
    });
  }

  window.uiModal = {
    info: (msg, title) => showModal('info', title || '提示', msg, {}),
    success: (msg, title) => showModal('success', title || '成功', msg, {}),
    error: (msg, title) => showModal('error', title || '错误', msg, {}),
    confirm: confirmModal
  };
})();

